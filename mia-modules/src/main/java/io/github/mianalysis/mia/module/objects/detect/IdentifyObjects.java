package io.github.mianalysis.mia.module.objects.detect;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling3D;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageTypeConverter;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactories;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.OctreeFactory;
import io.github.mianalysis.mia.object.coordinates.volume.PointListFactory;
import io.github.mianalysis.mia.object.coordinates.volume.QuadtreeFactory;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.ConnectivityInterface;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;

/**
 * Created by sc13967 on 06/06/2017.
 */

/**
 * Creates objects from an input binary image. Each object is identified in 3D
 * as a contiguous region of foreground labelled pixels. All coordinates
 * corresponding to that object are stored for use later.<br>
 * <br>
 * Note: Input binary images must be 8-bit and only contain values 0 and
 * 255.<br>
 * <br>
 * Note: Uses MorphoLibJ to perform connected components labelling in 3D.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class IdentifyObjects extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input, object output";

    /**
     * Input binary image from which objects will be identified. This image must be
     * 8-bit and only contain values 0 and 255.
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * Name of output objects to be stored in workspace.
     */
    public static final String OUTPUT_OBJECTS = "Output objects";

    /**
    * 
    */
    public static final String IDENTIFICATION_SEPARATOR = "Object identification";

    /**
     * Controls whether objects are considered to be white (255 intensity) on a
     * black (0 intensity) background, or black on a white background.
     */
    public static final String BINARY_LOGIC = "Binary logic";

    /**
    * 
    */
    public static final String DETECTION_MODE = "Detection mode";

    /**
     * Add all pixels to a single output object. Enabling this skips the
     * connected-components step.
     */
    public static final String SINGLE_OBJECT = "Identify as single object";

    /**
     * When performing connected components labelling, the connectivity determines
     * which neighbouring pixels are considered to be in contact.<br>
     * <ul>
     * <li>"6" considers immediate neighbours to lie in the cardinal directions
     * (i.e. left, right, in-front, behind, above and below). In 2D this is actually
     * 4-way connectivity.</li>
     * <li>- "26" (default) considers neighbours to include the cardinal directions
     * as well as diagonal to the pixel in question. In 2D this is actually 8-way
     * connectivity.</li>
     * </ul>
     */
    public static final String CONNECTIVITY = "Connectivity";

    /**
     * The method used to store pixel coordinates. This only affects performance and
     * memory usage, there is no difference in results obtained using difference
     * storage methods.<br>
     * <ul>
     * <li>"Pointlist" (default) stores object coordinates as a list of XYZ
     * coordinates. This is most efficient for small objects, very thin objects or
     * objects with lots of holes.</li>
     * <li>"Octree" stores objects in an octree format. Here, the coordinate space
     * is broken down into cubes of different sizes, each of which is marked as
     * foreground (i.e. an object) or background. Octrees are most efficient when
     * there are lots of large cubic regions of the same label, as the space can be
     * represented by larger (and thus fewer) cubes. This is best used when there
     * are large, completely solid objects. If z-axis sampling is much larger than
     * xy-axis sampling, it's typically best to opt for the quadtree method.</li>
     * <li>"Quadtree" stores objects in a quadtree format. Here, each Z-plane of the
     * object is broken down into squares of different sizes, each of which is
     * marked as foreground (i.e. an object) or background. Quadtrees are most
     * efficient when there are lots of large square regions of the same label, as
     * the space can be represented by larger (and thus fewer) squares. This is best
     * used when there are large, completely solid objects.</li>
     * </ul>
     */
    public static final String VOLUME_TYPE = "Volume type";

    /**
    * 
    */
    public static final String EXECUTION_SEPARATOR = "Execution controls";

    /**
     * Break the image down into strips, each one processed on a separate CPU
     * thread. The overhead required to do this means it's best for large multi-core
     * CPUs, but should be left disabled for small images or on CPUs with few cores.
     */
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";
    public static final String MIN_STRIP_WIDTH = "Minimum strip width (px)";

    public IdentifyObjects(Modules modules) {
        super("Identify objects", modules);
    }

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public interface DetectionModes {
        String SLICE_BY_SLICE = "2D (slice-by-slice)";
        String THREE_D = "3D";

        String[] ALL = new String[] { SLICE_BY_SLICE, THREE_D };

    }

    public interface Connectivity extends ConnectivityInterface {
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
            if (finalStripIdx == 0)
                w = sW;
            else if (finalStripIdx == nThreads - 1)
                w = imW - (sW * (nThreads - 1)) + 1;
            else
                w = sW + 1;

            ImageStack cropIst = ist.crop(x0, 0, 0, w, imH, imNSlices);
            Runnable task = () -> {
                ImageStack strip = cropIst.duplicate();

                // Running connected components labelling
                try {
                    FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(connectivity, 16);
                    strip = ffcl3D.computeLabels(strip);
                } catch (RuntimeException e2) {
                    FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(connectivity, 32);
                    strip = ffcl3D.computeLabels(strip);
                }

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
            // Do nothing as the user has selected this
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
            // Do nothing as the user has selected this
        }

        return ist;

    }

    public static ObjsI process(ImageI inputImage, String outputObjectsName, boolean blackBackground,
            boolean singleObject, String detectionMode, int connectivity, CoordinateSetFactoryI factory, boolean multithread,
            int minStripWidth, boolean verbose) throws IntegerOverflowException, RuntimeException {
        String name = new IdentifyObjects(null).getName();

        ImagePlus inputImagePlus = inputImage.getImagePlus();
        int nChannels = inputImagePlus.getNChannels();
        int nSlices = inputImagePlus.getNSlices();
        int nFrames = inputImagePlus.getNFrames();

        SpatCal cal = SpatCal.getFromImage(inputImagePlus);
        double frameInterval = inputImagePlus.getCalibration().frameInterval;
        ObjsI outputObjects = ObjsFactories.getDefaultFactory().createFromSpatCal(outputObjectsName, cal, nFrames, frameInterval,
                TemporalUnit.getOMEUnit());

        if (detectionMode.equals(DetectionModes.THREE_D))
            nSlices = 1;

        int count = 0;
        int total = nSlices * nFrames;
        for (int z = 1; z <= nSlices; z++) {
            for (int t = 1; t <= nFrames; t++) {
                // Creating a copy of the input image
                ImagePlus currStack;
                switch (detectionMode) {
                    case DetectionModes.SLICE_BY_SLICE:
                        currStack = SubHyperstackMaker
                                .makeSubhyperstack(inputImagePlus, "1-" + nChannels, z + "-" + z,
                                        t + "-" + t)
                                .duplicate();
                        break;
                    case DetectionModes.THREE_D:
                    default:
                        currStack = SubHyperstackMaker
                                .makeSubhyperstack(inputImagePlus, "1-" + nChannels, "1-" + inputImagePlus.getNSlices(),
                                        t + "-" + t)
                                .duplicate();
                        break;
                }
                currStack.setCalibration(inputImagePlus.getCalibration());
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
                            FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(
                                    connectivity, 16);
                            currStack.setStack(ffcl3D.computeLabels(currStack.getStack()));
                        } catch (RuntimeException e2) {
                            FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(
                                    connectivity, 32);
                            currStack.setStack(ffcl3D.computeLabels(currStack.getStack()));
                        }
                    }
                }

                // Converting image to objects
                ImageI tempImage = ImageFactory.createImage("Temp image", currStack);
                ObjsI currOutputObjects = tempImage.convertImageToObjects(factory, outputObjectsName, singleObject);

                // If processing each slice separately, offsetting it to the correct Z-position
                if (detectionMode.equals(DetectionModes.SLICE_BY_SLICE)) {
                    currOutputObjects.setSpatialCalibration(cal, true);
                    for (ObjI currOutputObj : currOutputObjects.values())
                        currOutputObj.translateCoords(0, 0, z - 1);
                }

                // Updating the current objects (setting the real frame number and offsetting
                // the ID)
                int maxID = 0;
                for (ObjI object : outputObjects.values())
                    maxID = Math.max(object.getID(), maxID);

                for (ObjI object : currOutputObjects.values()) {
                    object.setID(object.getID() + maxID + 1);
                    object.setT(t - 1);
                    outputObjects.put(object.getID(), object);
                }

                writeProgressStatus(++count, total, "images", name);

            }
        }

        return outputObjects;

    }

    public static int getConnectivity(String connectivityName) {
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
        return Categories.OBJECTS_DETECT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Creates objects from an input binary image.  Each object is identified in 3D as a contiguous region of "
                + "foreground labelled pixels.  All coordinates corresponding to that object are stored for use later.<br>"
                + "<br>Note: Input binary images must be 8-bit and only contain values 0 and 255.<br>"
                + "<br>Note: Uses MorphoLibJ to perform connected components labelling in 3D.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        ImageI inputImage = workspace.getImage(inputImageName);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        String binaryLogic = parameters.getValue(BINARY_LOGIC, workspace);
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);
        String detectionMode = parameters.getValue(DETECTION_MODE, workspace);
        boolean singleObject = parameters.getValue(SINGLE_OBJECT, workspace);
        String connectivityName = parameters.getValue(CONNECTIVITY, workspace);
        String type = parameters.getValue(VOLUME_TYPE, workspace);

        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);
        int minStripWidth = parameters.getValue(MIN_STRIP_WIDTH, workspace);

        // Getting options
        int connectivity = getConnectivity(connectivityName);
        CoordinateSetFactoryI factory = CoordinateSetFactories.getFactory(type);

        ObjsI outputObjects = process(inputImage, outputObjectsName, blackBackground, singleObject, detectionMode,
                connectivity, factory, multithread, minStripWidth, true);

        // Adding objects to workspace
        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput)
            outputObjects.convertToImageIDColours().showWithNormalisation(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(IDENTIFICATION_SEPARATOR, this));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));
        parameters.add(new ChoiceP(DETECTION_MODE, this, DetectionModes.THREE_D, DetectionModes.ALL));
        parameters.add(new BooleanP(SINGLE_OBJECT, this, false));
        parameters.add(new ChoiceP(CONNECTIVITY, this, Connectivity.TWENTYSIX, Connectivity.ALL));
        parameters.add(new ChoiceP(VOLUME_TYPE, this, CoordinateSetFactories.getDefaultFactoryName(), CoordinateSetFactories.listFactoryNames()));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));
        parameters.add(new IntegerP(MIN_STRIP_WIDTH, this, 60));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(INPUT_IMAGE));
        returnedParameters.add(parameters.get(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.get(IDENTIFICATION_SEPARATOR));
        returnedParameters.add(parameters.get(BINARY_LOGIC));
        returnedParameters.add(parameters.get(DETECTION_MODE));
        returnedParameters.add(parameters.get(SINGLE_OBJECT));
        returnedParameters.add(parameters.get(CONNECTIVITY));
        returnedParameters.add(parameters.get(VOLUME_TYPE));

        returnedParameters.add(parameters.get(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.get(ENABLE_MULTITHREADING));
        if ((boolean) parameters.getValue(ENABLE_MULTITHREADING, workspace)) {
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
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
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
                        + "<li>\"" + new PointListFactory().getName()
                        + "\" (default) stores object coordinates as a list of XYZ coordinates.  This is most efficient for small objects, very thin objects or objects with lots of holes.</li>"
                        + "<li>\"" + new OctreeFactory().getName()
                        + "\" stores objects in an octree format.  Here, the coordinate space is broken down into cubes of different sizes, each of which is marked as foreground (i.e. an object) or background.  Octrees are most efficient when there are lots of large cubic regions of the same label, as the space can be represented by larger (and thus fewer) cubes.  This is best used when there are large, completely solid objects.  If z-axis sampling is much larger than xy-axis sampling, it's typically best to opt for the quadtree method.</li>"
                        + "<li>\"" + new QuadtreeFactory().getName()
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
