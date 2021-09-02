package io.github.mianalysis.MIA.Module.ObjectProcessing.Identification;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling3D;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import io.github.mianalysis.MIA.Module.ImageProcessing.Stack.ImageTypeConverter;
import io.github.mianalysis.MIA.Object.Image;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.Objs;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.VolumeTypesInterface;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.BooleanP;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceP;
import io.github.mianalysis.MIA.Object.Parameters.InputImageP;
import io.github.mianalysis.MIA.Object.Parameters.Parameters;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceInterfaces.BinaryLogicInterface;
import io.github.mianalysis.MIA.Object.Parameters.Objects.OutputObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.Text.IntegerP;
import io.github.mianalysis.MIA.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.PartnerRefs;
import io.github.mianalysis.MIA.Object.Units.TemporalUnit;
import io.github.sjcross.common.Exceptions.IntegerOverflowException;
import io.github.sjcross.common.Object.Volume.SpatCal;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class IdentifyObjects extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String IDENTIFICATION_SEPARATOR = "Object identification";
    public static final String BINARY_LOGIC = "Binary logic";
    public static final String SINGLE_OBJECT = "Identify as single object";
    public static final String CONNECTIVITY = "Connectivity";
    public static final String VOLUME_TYPE = "Volume type";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";
    public static final String MIN_STRIP_WIDTH = "Minimum strip width (px)";

    public IdentifyObjects(Modules modules) {
        super("Identify objects", modules);
    }

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public interface Connectivity {
        String SIX = "6";
        String TWENTYSIX = "26";

        String[] ALL = new String[] { SIX, TWENTYSIX };

    }

    public interface VolumeTypes extends VolumeTypesInterface {
    }

    public static ImageStack connectedComponentsLabellingMT(ImageStack ist, int connectivity, int minStripWidth) {
        // Calculating strip width
        int imW = ist.getWidth();
        int imH = ist.getHeight();
        int imNSlices = ist.size();
        int bitDepth = ist.getBitDepth();
        int nThreads = Prefs.getThreads();
        int tempSW = Math.floorDiv(imW, nThreads);
        if (tempSW < minStripWidth) {
            nThreads = (int) Math.ceil((double) imW / (double) minStripWidth);
            tempSW = Math.floorDiv(imW, nThreads);
        }
        int sW = tempSW;

        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Iterating over each strip of the input image, creating the connectivity map
        Map<Integer, ImageStack> strips = Collections.synchronizedMap(new HashMap<>());
        Map<Integer, HashMap<Double, Fragment>> fragments = Collections.synchronizedMap(new HashMap<>());

        for (int stripIdx = 0; stripIdx < nThreads; stripIdx++) {
            final int finalStripIdx = stripIdx;

            int x0 = finalStripIdx == 0 ? 0 : (sW * finalStripIdx) - 1;
            int w;
            if (finalStripIdx == 0) {
                w = sW;
            } else if (finalStripIdx == nThreads - 1) {
                w = imW - (sW * (nThreads - 1)) + 1;
            } else {
                w = sW + 1;
            }

            ImageStack cropIst = ist.crop(x0, 0, 0, w, imH, imNSlices);
            Runnable task = () -> {
                // Running connected components labelling
                FloodFillComponentsLabeling3D ffcl3D;
                try {
                    ffcl3D = new FloodFillComponentsLabeling3D(connectivity, 16);
                } catch (RuntimeException e2) {
                    ffcl3D = new FloodFillComponentsLabeling3D(connectivity, 32);
                }
                ImageStack strip = ffcl3D.computeLabels(cropIst);
                strips.put(finalStripIdx, strip);

                // Identifying separate regions
                HashMap<Double, Fragment> currentFragments = new HashMap<>();
                fragments.put(finalStripIdx, currentFragments);

                for (int z = 0; z < strip.size(); z++) {
                    for (int x = 0; x < strip.getWidth(); x++) {
                        for (int y = 0; y < strip.getHeight(); y++) {
                            Double val = (Double) strip.getVoxel(x, y, z);

                            if (val == 0)
                                continue;

                            // Check if this object exists
                            currentFragments.putIfAbsent(val, new Fragment(finalStripIdx, val));

                        }
                    }
                }
            };
            pool.submit(task);
        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }

        // Processing overlapping regions, adding links
        for (int stripIdx = 1; stripIdx < nThreads; stripIdx++) {
            ImageStack stripL = strips.get(stripIdx - 1);
            ImageStack stripR = strips.get(stripIdx);

            int currW = stripL.getWidth();

            // Iterating over the right-most column of stripL and the left-most column of
            // stripR. Any rows which contain regions in both columns will be linked.
            for (int z = 0; z < stripL.size(); z++) {
                for (int y = 0; y < stripL.getHeight(); y++) {
                    Double valL = (Double) stripL.getVoxel(currW - 1, y, z);
                    Double valR = (Double) stripR.getVoxel(0, y, z);

                    // If both are labelled regions, create a link
                    if (valL > 0 & valR > 0) {
                        Fragment fragmentL = fragments.get(stripIdx - 1).get(valL);
                        Fragment fragmentR = fragments.get(stripIdx).get(valR);

                        fragmentL.addLink(fragmentR);
                        fragmentR.addLink(fragmentL);

                    }
                }
            }
        }

        // Iterating over all RegionObjs, assigning groups
        int maxGroup = 0;
        for (HashMap<Double, Fragment> currFragments : fragments.values()) {
            for (Fragment fragment : currFragments.values()) {
                // If this region hasn't been assigned before, create a new group
                if (fragment.getRegion() == 0)
                    fragment.propagateRegion(++maxGroup);
            }
        }

        // If number of groups is greater than 65535, switching stack to 32-bit
        if (bitDepth < 32 && maxGroup > 65535) {
            ImagePlus tempIpl = new ImagePlus("Temp", ist);
            ImageTypeConverter.process(tempIpl, 32, ImageTypeConverter.ScalingModes.CLIP);
            ist = tempIpl.getImageStack();
        } else if (bitDepth < 16 && maxGroup > 255) {
            ImagePlus tempIpl = new ImagePlus("Temp", ist);
            ImageTypeConverter.process(tempIpl, 16, ImageTypeConverter.ScalingModes.CLIP);
            ist = tempIpl.getImageStack();
        }

        // Restarting the pool
        pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        for (int z = 0; z < ist.size(); z++) {
            final int finalZ = z;
            ImageProcessor ipr = ist.getProcessor(finalZ + 1);
            Runnable task = () -> {
                for (int stripIdx = 0; stripIdx < strips.size(); stripIdx++) {
                    ImageStack strip = strips.get(stripIdx);
                    int x0 = stripIdx == 0 ? 0 : (sW * stripIdx) - 1;
                    HashMap<Double, Fragment> currFragments = fragments.get(stripIdx);

                    ImageProcessor stripIpr = strip.getProcessor(finalZ + 1);

                    for (int x = 0; x < strip.getWidth(); x++) {
                        for (int y = 0; y < strip.getHeight(); y++) {

                            // Getting value from strip
                            Double val = (Double) ((double) stripIpr.getf(x, y));

                            if (val == 0)
                                continue;

                            // Converting to grouped label
                            int regionID = currFragments.get(val).regionID;

                            // Assigning to original stack
                            ipr.setf(x0 + x, y, regionID);

                        }
                    }
                }
            };
            pool.submit(task);
        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }

        return ist;

    }

    public static Objs process(Image inputImage, String outputObjectsName, boolean blackBackground,
            boolean singleObject, int connectivity, String type, boolean multithread, int minStripWidth,
            boolean verbose) throws IntegerOverflowException, RuntimeException {
        String name = new IdentifyObjects(null).getName();

        ImagePlus inputImagePlus = inputImage.getImagePlus();

        SpatCal cal = SpatCal.getFromImage(inputImagePlus);
        int nFrames = inputImagePlus.getNFrames();
        double frameInterval = inputImagePlus.getCalibration().frameInterval;
        Objs outputObjects = new Objs(outputObjectsName, cal, nFrames, frameInterval,
                TemporalUnit.getOMEUnit());

        for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
            // Creating a copy of the input image
            ImagePlus currStack;
            if (inputImagePlus.getNFrames() == 1) {
                currStack = inputImagePlus.duplicate();
            } else {
                currStack = SubHyperstackMaker.makeSubhyperstack(inputImagePlus, "1-" + inputImagePlus.getNChannels(),
                        "1-" + inputImagePlus.getNSlices(), t + "-" + t).duplicate();
                currStack.setCalibration(inputImagePlus.getCalibration());
            }
            currStack.updateChannelAndDraw();

            if (!blackBackground)
                InvertIntensity.process(currStack);

            // Applying connected components labelling
            if (!singleObject) {
                int nThreads = multithread ? Prefs.getThreads() : 1;
                if (nThreads > 1 && minStripWidth < currStack.getWidth()) {
                    currStack.setStack(
                            connectedComponentsLabellingMT(currStack.getStack(), connectivity, minStripWidth));
                } else {
                    try {
                        FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(connectivity, 16);
                        currStack.setStack(ffcl3D.computeLabels(currStack.getStack()));
                    } catch (RuntimeException e2) {
                        FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(connectivity, 32);
                        currStack.setStack(ffcl3D.computeLabels(currStack.getStack()));
                    }
                }
            }

            // Converting image to objects
            Image tempImage = new Image("Temp image", currStack);
            Objs currOutputObjects = tempImage.convertImageToObjects(type, outputObjectsName, singleObject);

            // Updating the current objects (setting the real frame number and offsetting
            // the ID)
            int maxID = 0;
            for (Obj object : outputObjects.values()) {
                maxID = Math.max(object.getID(), maxID);
            }

            for (Obj object : currOutputObjects.values()) {
                object.setID(object.getID() + maxID + 1);
                object.setT(t - 1);
                outputObjects.put(object.getID(), object);
            }

            writeProgressStatus(t, inputImagePlus.getNFrames(), "images", name);

        }

        return outputObjects;

    }

    private static int getConnectivity(String connectivityName) {
        switch (connectivityName) {
            case Connectivity.SIX:
            default:
                return 6;
            case Connectivity.TWENTYSIX:
                return 26;
        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getDescription() {
        return "Creates objects from an input binary image.  Each object is identified in 3D as a contiguous region of "
                + "foreground labelled pixels.  All coordinates corresponding to that object are stored for use later.<br>"
                + "<br>Note: Input binary images must be 8-bit and only contain values 0 and 255.<br>"
                + "<br>Note: Uses MorphoLibJ to perform connected components labelling in 3D.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String binaryLogic = parameters.getValue(BINARY_LOGIC);
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);
        boolean singleObject = parameters.getValue(SINGLE_OBJECT);
        String connectivityName = parameters.getValue(CONNECTIVITY);
        String type = parameters.getValue(VOLUME_TYPE);

        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);
        int minStripWidth = parameters.getValue(MIN_STRIP_WIDTH);

        // Getting options
        int connectivity = getConnectivity(connectivityName);

        Objs outputObjects = process(inputImage, outputObjectsName, blackBackground, singleObject,
                connectivity, type, multithread, minStripWidth, true);

        // Adding objects to workspace
        writeStatus("Adding objects (" + outputObjectsName + ") to workspace");
        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput)
            outputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(IDENTIFICATION_SEPARATOR, this));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));
        parameters.add(new BooleanP(SINGLE_OBJECT, this, false));
        parameters.add(new ChoiceP(CONNECTIVITY, this, Connectivity.TWENTYSIX, Connectivity.ALL));
        parameters.add(new ChoiceP(VOLUME_TYPE, this, VolumeTypes.POINTLIST, VolumeTypes.ALL));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));
        parameters.add(new IntegerP(MIN_STRIP_WIDTH, this, 60));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(INPUT_IMAGE));
        returnedParameters.add(parameters.get(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.get(IDENTIFICATION_SEPARATOR));
        returnedParameters.add(parameters.get(BINARY_LOGIC));
        returnedParameters.add(parameters.get(SINGLE_OBJECT));
        returnedParameters.add(parameters.get(CONNECTIVITY));
        returnedParameters.add(parameters.get(VOLUME_TYPE));

        returnedParameters.add(parameters.get(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.get(ENABLE_MULTITHREADING));
        if ((boolean) parameters.getValue(ENABLE_MULTITHREADING)) {
            returnedParameters.add(parameters.get(MIN_STRIP_WIDTH));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription(
                "Input binary image from which objects will be identified.  This image must be 8-bit and only contain values 0 and 255.");

        parameters.get(OUTPUT_OBJECTS).setDescription("Name of output objects to be stored in workspace.");

        parameters.get(SINGLE_OBJECT).setDescription(
                "Add all pixels to a single output object.  Enabling this skips the connected-components step.");

        parameters.get(CONNECTIVITY).setDescription(
                "When performing connected components labelling, the connectivity determines which neighbouring pixels are considered to be in contact.<br><ul>"
                        + "<li>\"" + Connectivity.SIX
                        + "\" considers immediate neighbours to lie in the cardinal directions (i.e. left, right, in-front, behind, above and below).  In 2D this is actually 4-way connectivity.</li>"
                        + "<li> - \"" + Connectivity.TWENTYSIX
                        + "\" (default) considers neighbours to include the cardinal directions as well as diagonal to the pixel in question.  In 2D this is actually 8-way connectivity.</li></ul>");

        parameters.get(VOLUME_TYPE).setDescription(
                "The method used to store pixel coordinates.  This only affects performance and memory usage, there is no difference in results obtained using difference storage methods.<br><ul>"
                        + "<li>\"" + VolumeTypes.POINTLIST
                        + "\" (default) stores object coordinates as a list of XYZ coordinates.  This is most efficient for small objects, very thin objects or objects with lots of holes.</li>"
                        + "<li>\"" + VolumeTypes.OCTREE
                        + "\" stores objects in an octree format.  Here, the coordinate space is broken down into cubes of different sizes, each of which is marked as foreground (i.e. an object) or background.  Octrees are most efficient when there are lots of large cubic regions of the same label, as the space can be represented by larger (and thus fewer) cubes.  This is best used when there are large, completely solid objects.  If z-axis sampling is much larger than xy-axis sampling, it's typically best to opt for the quadtree method.</li>"
                        + "<li>\"" + VolumeTypes.QUADTREE
                        + "\" stores objects in a quadtree format.  Here, each Z-plane of the object is broken down into squares of different sizes, each of which is marked as foreground (i.e. an object) or background.  Quadtrees are most efficient when there are lots of large square regions of the same label, as the space can be represented by larger (and thus fewer) squares.  This is best used when there are large, completely solid objects.</li></ul>");

        parameters.get(BINARY_LOGIC).setDescription(BinaryLogicInterface.getDescription());
                        
        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Break the image down into strips, each one processed on a separate CPU thread.  The overhead required to do this means it's best for large multi-core CPUs, but should be left disabled for small images or on CPUs with few cores.");

        parameters.get(MIN_STRIP_WIDTH).setDescription(
                "Minimum width of each strip to be processed on a separate CPU thread.  Measured in pixel units.");

    }
}

class Fragment {
    int stripIdx;
    Double stripID;
    int regionID = 0;
    HashSet<Fragment> links = new HashSet<>();

    public Fragment(int stripIdx, Double stripID) {
        this.stripIdx = stripIdx;
        this.stripID = stripID;
    }

    public void addLink(Fragment linkObj) {
        links.add(linkObj);
    }

    public void propagateRegion(int regionID) {
        // If this has already been linked, skip it
        if (this.regionID != 0)
            return;

        // Assigning an ID to this region
        this.regionID = regionID;

        // Assigning the same ID to all linked regions, which in turn will propagate to
        // their links
        for (Fragment link : links) {
            link.propagateRegion(regionID);
        }
    }

    public int getRegion() {
        return regionID;
    }
}
