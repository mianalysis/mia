// TODO: Add object linking for 4D - linking should be done on spatial overlap (similar to how its done in 3D)

package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling3D;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume.SpatCal;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class IdentifyObjects extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String IDENTIFICATION_SEPARATOR = "Object identification";
    public static final String WHITE_BACKGROUND = "Black objects/white background";
    public static final String SINGLE_OBJECT = "Identify as single object";
    public static final String CONNECTIVITY = "Connectivity";
    public static final String VOLUME_TYPE = "Volume type";

    public IdentifyObjects(ModuleCollection modules) {
        super("Identify objects",modules);
    }

    public interface Connectivity {
        String SIX = "6";
        String TWENTYSIX = "26";

        String[] ALL = new String[]{SIX,TWENTYSIX};

    }

    public interface VolumeTypes extends Image.VolumeTypes {}


    private ObjCollection importFromImage(Image inputImage, String outputObjectsName, boolean whiteBackground,
                                          boolean singleObject, int connectivity, String type)
            throws IntegerOverflowException, RuntimeException {

        ImagePlus inputImagePlus = inputImage.getImagePlus();
        inputImagePlus = inputImagePlus.duplicate();

        SpatCal cal = SpatCal.getFromImage(inputImagePlus);
        int nFrames = inputImagePlus.getNFrames();

        ObjCollection outputObjects = new ObjCollection(outputObjectsName,cal,nFrames);

        for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
            writeMessage("Processing image "+t+" of "+inputImagePlus.getNFrames());

            // Creating a copy of the input image
            ImagePlus currStack;
            if (inputImagePlus.getNFrames()==1) {
                currStack = new Duplicator().run(inputImagePlus);

            } else {
                currStack = SubHyperstackMaker.makeSubhyperstack(inputImagePlus, "1-" +
                        inputImagePlus.getNChannels(), "1-" + inputImagePlus.getNSlices(), t + "-" + t);
                currStack.setCalibration(inputImagePlus.getCalibration());
            }
            currStack.updateChannelAndDraw();

            if (whiteBackground) InvertIntensity.process(currStack);

            // Applying connected components labelling
            try {
                FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(connectivity, 16);
                currStack.setStack(ffcl3D.computeLabels(currStack.getStack()));
            } catch (RuntimeException e2) {
                FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(connectivity, 32);
                currStack.setStack(ffcl3D.computeLabels(currStack.getStack()));
            }

            // Converting image to objects
            Image tempImage = new Image("Temp image", currStack);
            ObjCollection currOutputObjects = tempImage.convertImageToObjects(type,outputObjectsName,singleObject);

            // Updating the current objects (setting the real frame number and offsetting the ID)
            int maxID = 0;
            for (Obj object:outputObjects.values()) {
                maxID = Math.max(object.getID(),maxID);
            }

            for (Obj object:currOutputObjects.values()) {
                object.setID(object.getID() + maxID + 1);
                object.setT(t-1);
                outputObjects.put(object.getID(),object);
            }
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
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getDescription() {
        return "Creates objects from an input binary image.  Each object is identified in 3D as a contiguous region of " +
                "foreground labelled pixels.  All coordinates corresponding to that object are stored for use later.<br>" +
                "<br>Note: Input binary images must be 8-bit and only contain values 0 and 255.<br>" +
                "<br>Note: Uses MorphoLibJ to perform connected components labelling in 3D.";
    }


    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        boolean whiteBackground = parameters.getValue(WHITE_BACKGROUND);
        boolean singleObject = parameters.getValue(SINGLE_OBJECT);
        String connectivityName = parameters.getValue(CONNECTIVITY);
        String type = parameters.getValue(VOLUME_TYPE);

        // Getting options
        int connectivity = getConnectivity(connectivityName);

        ObjCollection outputObjects = importFromImage(inputImage, outputObjectsName, whiteBackground, singleObject, connectivity, type);

        // Adding objects to workspace
        writeMessage("Adding objects ("+outputObjectsName+") to workspace");
        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput) outputObjects.convertToImageRandomColours().showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE,this,"","Input binary image from which objects will be identified.  This image must be 8-bit and only contain values 0 and 255."));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this,"","Name of output objects to be stored in workspace."));

        parameters.add(new ParamSeparatorP(IDENTIFICATION_SEPARATOR,this));
        parameters.add(new BooleanP(WHITE_BACKGROUND,this,true,"When selected, \"foreground\" pixels are considered to have intensities of 0 and background 255 (i.e. black objects on a white background).  When not selected, the inverse is true."));
        parameters.add(new BooleanP(SINGLE_OBJECT,this,false,"Add all pixels to a single output object.  Enabling this skips the connected-components step."));
        parameters.add(new ChoiceP(CONNECTIVITY, this, Connectivity.TWENTYSIX, Connectivity.ALL,"When performing connected components labelling, the connectivity determines which neighbouring pixels are considered to be in contact.<br>" +
                "<br> - \""+Connectivity.SIX+"\" considers immediate neighbours to lie in the cardinal directions (i.e. left, right, in-front, behind, above and below).  In 2D this is actually 4-way connectivity.<br>" +
                "<br> - \""+Connectivity.TWENTYSIX+"\" (default) considers neighbours to include the cardinal directions as well as diagonal to the pixel in question.  In 2D this is actually 8-way connectivity,"));
        parameters.add(new ChoiceP(VOLUME_TYPE, this, VolumeTypes.POINTLIST, VolumeTypes.ALL,"The method used to store pixel coordinates.  This only affects performance and memory usage, there is no difference in results obtained using difference storage methods.<br>" +
                "<br> - \""+VolumeTypes.POINTLIST+"\" (default) stores object coordinates as a list of XYZ coordinates.  This is most efficient for small objects, very thin objects or objects with lots of holes.<br>" +
                "<br> - \""+VolumeTypes.OCTREE+"\" stores objects in an octree format.  Here, the coordinate space is broken down into cubes of different sizes, each of which is marked as foreground (i.e. an object) or background.  Octrees are most efficient when there are lots of large cubic regions of the same label, as the space can be represented by larger (and thus fewer) cubes.  This is best used when there are large, completely solid objects.  If z-axis sampling is much larger than xy-axis sampling, it's typically best to opt for the quadtree method.<br>" +
                "<br> - \""+VolumeTypes.QUADTREE+"\" stores objects in a quadtree format.  Here, each Z-plane of the object is broken down into squares of different sizes, each of which is marked as foreground (i.e. an object) or background.  Quadtrees are most efficient when there are lots of large square regions of the same label, as the space can be represented by larger (and thus fewer) squares.  This is best used when there are large, completely solid objects."));
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
