package wbif.sjx.ModularImageAnalysis.Module;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class ObjectImageConverter extends HCModule {
    public static final String CONVERSION_MODE = "Conversion mode";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String TEMPLATE_IMAGE = "Template image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String USE_GROUP_ID = "Use group ID";

    public static final int IMAGE_TO_OBJECTS = 0;
    public static final int OBJECTS_TO_IMAGE = 1;


    public HCImage convertObjectsToImage(HCObjectSet objects, HCName outputImageName, HCImage templateImage, boolean useGroupID) {
        ImagePlus ipl;

        if (templateImage == null) {
            // Getting range of object pixels
            int[][] coordinateRange = new int[5][2];

            for (HCObject object : objects.values()) {
                // Getting range of XYZ
                int[][] currCoordinateRange = object.getCoordinateRange();
                for (int dim = 0; dim < coordinateRange.length; dim++) {
                    if (currCoordinateRange[dim][0] < coordinateRange[dim][0]) {
                        coordinateRange[dim][0] = currCoordinateRange[dim][0];
                    }

                    if (currCoordinateRange[dim][1] > coordinateRange[dim][1]) {
                        coordinateRange[dim][1] = currCoordinateRange[dim][1];
                    }
                }

                // Getting range of additional dimensions
                for (int dim:object.getPositions().keySet()) {
                    int currValue = object.getPosition(dim);

                    if (currValue < coordinateRange[dim][0]) {
                        coordinateRange[dim][0] = currValue;
                    }

                    if (currValue > coordinateRange[dim][1]) {
                        coordinateRange[dim][1] = currValue;
                    }
                }
            }

            // Creating a new image
            ipl = IJ.createHyperStack("Objects", coordinateRange[HCObject.X][1] + 1,
                    coordinateRange[HCObject.Y][1] + 1, coordinateRange[HCObject.C][1] + 1,
                    coordinateRange[HCObject.Z][1] + 1, coordinateRange[HCObject.T][1] + 1, 16);

        } else {
            ImagePlus templateIpl = templateImage.getImagePlus();
            ipl = IJ.createHyperStack("Objects",templateIpl.getWidth(),templateIpl.getHeight(),
                    templateIpl.getNChannels(),templateIpl.getNSlices(),templateIpl.getNFrames(),16);
        }

        // Labelling pixels in image
        for (HCObject object:objects.values()) {
            ArrayList<Integer> x = object.getCoordinates(HCObject.X);
            ArrayList<Integer> y = object.getCoordinates(HCObject.Y);
            ArrayList<Integer> z = object.getCoordinates(HCObject.Z);
            Integer c = object.getCoordinates(HCObject.C);
            Integer t = object.getCoordinates(HCObject.T);

            for (int i=0;i<x.size();i++) {
                int zPos = z==null ? 0 : z.get(i);
                int cPos = c==null ? -1 : c;
                int tPos = t==null ? -1 : t;

                ipl.setPosition(cPos+1,zPos+1,tPos+1);
                if (useGroupID) {
                    ipl.getProcessor().set(x.get(i),y.get(i),object.getGroupID());
                } else {
                    ipl.getProcessor().set(x.get(i), y.get(i), object.getID());
                }

            }
        }

        return new HCImage(outputImageName,ipl);

    }

    public HCObjectSet convertImageToObjects(HCImage image, HCName outputObjectsName) {
        // Converting to ImagePlus for this operation
        ImagePlus ipl = image.getImagePlus();

        // Need to get coordinates and convert to a HCObject
        HCObjectSet objects = new HCObjectSet(outputObjectsName); //Local ArrayList of objects

        ImageProcessor ipr = ipl.getProcessor();

        int h = ipl.getHeight();
        int w = ipl.getWidth();
        int d = ipl.getNSlices();

        for (int z=0;z<d;z++) {
            ipl.setSlice(z+1);
            for (int x=0;x<w;x++) {
                for (int y=0;y<h;y++) {
                    int ID = (int) ipr.getPixelValue(x,y); //Pixel value

                    if (ID != 0) {
                        objects.computeIfAbsent(ID, k -> new HCObject(ID));

                        objects.get(ID).addCoordinate(HCObject.X, x);
                        objects.get(ID).addCoordinate(HCObject.Y, y);
                        objects.get(ID).addCoordinate(HCObject.Z, z);

                    }
                }
            }
        }

        // Adding distance calibration to each object
        Calibration calibration = ipl.getCalibration();
        for (HCObject object:objects.values()) {
            object.addCalibration(HCObject.X,calibration.getX(1));
            object.addCalibration(HCObject.Y,calibration.getY(1));
            object.addCalibration(HCObject.Z,calibration.getZ(1));
            object.setCalibratedUnits(calibration.getUnits());

        }

        return objects;

    }

    @Override
    public String getTitle() {
        return "Image-object converter";

    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        int conversionMode = parameters.getValue(CONVERSION_MODE);

        if (conversionMode == IMAGE_TO_OBJECTS) {
            HCName inputImageName = parameters.getValue(INPUT_IMAGE);
            HCName outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

            HCImage inputImage = workspace.getImages().get(inputImageName);

            HCObjectSet objects = convertImageToObjects(inputImage, outputObjectsName);

            workspace.addObjects(objects);

        } else if (conversionMode == OBJECTS_TO_IMAGE) {
            HCName objectName = parameters.getValue(INPUT_OBJECTS);
            HCName templateImageName = parameters.getValue(TEMPLATE_IMAGE);
            HCName outputImageName = parameters.getValue(OUTPUT_IMAGE);
            boolean useGroupID = parameters.getValue(USE_GROUP_ID);

            HCObjectSet inputObjects = workspace.getObjects().get(objectName);
            HCImage templateImage = workspace.getImages().get(templateImageName);

            HCImage outputImage = convertObjectsToImage(inputObjects,outputImageName,templateImage,useGroupID);

            workspace.addImage(outputImage);

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(CONVERSION_MODE, HCParameter.INTEGER,0));
        parameters.addParameter(new HCParameter(INPUT_IMAGE, HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(OUTPUT_OBJECTS, HCParameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(TEMPLATE_IMAGE, HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(INPUT_OBJECTS, HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(OUTPUT_IMAGE, HCParameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(USE_GROUP_ID, HCParameter.BOOLEAN,true));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(CONVERSION_MODE));

        if (parameters.getValue(CONVERSION_MODE).equals(IMAGE_TO_OBJECTS)) {
            returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_OBJECTS));

        } else if(parameters.getValue(CONVERSION_MODE).equals(OBJECTS_TO_IMAGE)) {
            returnedParameters.addParameter(parameters.getParameter(TEMPLATE_IMAGE));
            returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
            returnedParameters.addParameter(parameters.getParameter(USE_GROUP_ID));

        }

        return returnedParameters;
    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }

}