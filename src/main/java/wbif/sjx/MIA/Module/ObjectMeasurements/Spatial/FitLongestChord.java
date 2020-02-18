package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Deprecated.AddObjectsOverlay;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.common.Analysis.LongestChordCalculator;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Process.IntensityMinMax;

import java.awt.*;

/**
 * Created by sc13967 on 20/06/2018.
 */
public class FitLongestChord extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String RENDERING_SEPARATOR = "Longest chord rendering";
    public static final String ADD_ENDPOINTS_AS_OVERLAY = "Add endpoints as overlay";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";

    public FitLongestChord(ModuleCollection modules) {
        super("Fit longest chord",modules);
    }


    public interface Measurements {
        String LENGTH_PX = "LONGEST_CHORD // LENGTH (PX)";
        String LENGTH_CAL = "LONGEST_CHORD // LENGTH (${CAL})";
        String X1_PX = "LONGEST_CHORD // X1 (PX)";
        String Y1_PX = "LONGEST_CHORD // Y1 (PX)";
        String Z1_SLICE = "LONGEST_CHORD // Z1 (SLICE)";
        String X2_PX = "LONGEST_CHORD // X2 (PX)";
        String Y2_PX = "LONGEST_CHORD // Y2 (PX)";
        String Z2_SLICE = "LONGEST_CHORD // Z2 (SLICE)";
        String MEAN_SURF_DIST_PX = "LONGEST_CHORD // MEAN_SURF_DIST (PX)";
        String MEAN_SURF_DIST_CAL = "LONGEST_CHORD // MEAN_SURF_DIST (${CAL})";
        String STD_SURF_DIST_PX = "LONGEST_CHORD // STD_SURF_DIST (PX)";
        String STD_SURF_DIST_CAL = "LONGEST_CHORD // STD_SURF_DIST (${CAL})";
        String MAX_SURF_DIST_PX = "LONGEST_CHORD // MAX_SURF_DIST (PX)";
        String MAX_SURF_DIST_CAL = "LONGEST_CHORD // MAX_SURF_DIST (${CAL})";
        String ORIENTATION_XY_DEGS = "LONGEST_CHORD // ORIENTATION_XY_(DEGS)";

    }


    public void processObject(Obj object) {
        double dppXY = object.getDppXY();

        LongestChordCalculator calculator = new LongestChordCalculator(object);

        double longestChordLength = calculator.getLCLength();
        object.addMeasurement(new Measurement(Measurements.LENGTH_PX,longestChordLength));
        object.addMeasurement(new Measurement(Measurements.LENGTH_CAL,longestChordLength*dppXY));

        double[][] LC = calculator.getLC();
        object.addMeasurement(new Measurement(Measurements.X1_PX,LC[0][0]));
        object.addMeasurement(new Measurement(Measurements.Y1_PX,LC[0][1]));
        object.addMeasurement(new Measurement(Measurements.Z1_SLICE,LC[0][2]));
        object.addMeasurement(new Measurement(Measurements.X2_PX,LC[1][0]));
        object.addMeasurement(new Measurement(Measurements.Y2_PX,LC[1][1]));
        object.addMeasurement(new Measurement(Measurements.Z2_SLICE,LC[1][2]));

        CumStat cumStat = calculator.calculateAverageDistanceFromLC();
        if (cumStat == null) {
            object.addMeasurement(new Measurement(Measurements.MEAN_SURF_DIST_PX,Double.NaN));
            object.addMeasurement(new Measurement(Measurements.MEAN_SURF_DIST_CAL,Double.NaN));
            object.addMeasurement(new Measurement(Measurements.STD_SURF_DIST_PX,Double.NaN));
            object.addMeasurement(new Measurement(Measurements.STD_SURF_DIST_CAL,Double.NaN));
            object.addMeasurement(new Measurement(Measurements.MAX_SURF_DIST_PX,Double.NaN));
            object.addMeasurement(new Measurement(Measurements.MAX_SURF_DIST_CAL,Double.NaN));
        } else {
            object.addMeasurement(new Measurement(Measurements.MEAN_SURF_DIST_PX,cumStat.getMean()));
            object.addMeasurement(new Measurement(Measurements.MEAN_SURF_DIST_CAL,cumStat.getMean()*dppXY));
            object.addMeasurement(new Measurement(Measurements.STD_SURF_DIST_PX,cumStat.getStd()));
            object.addMeasurement(new Measurement(Measurements.STD_SURF_DIST_CAL,cumStat.getStd()*dppXY));
            object.addMeasurement(new Measurement(Measurements.MAX_SURF_DIST_PX,cumStat.getMax()));
            object.addMeasurement(new Measurement(Measurements.MAX_SURF_DIST_CAL,cumStat.getMax()*dppXY));
        }

        double orientationDegs = Math.toDegrees(calculator.getXYOrientationRads());
        orientationDegs = Math.abs((orientationDegs + 90) % 180 - 90);
        if (orientationDegs >= 90) orientationDegs = orientationDegs - 180;
        object.addMeasurement(new Measurement(Measurements.ORIENTATION_XY_DEGS,orientationDegs));

    }

    public void addEndpointsOverlay(Obj object, ImagePlus imagePlus) {
        int x1 = (int) object.getMeasurement(Measurements.X1_PX).getValue();
        int y1 = (int) object.getMeasurement(Measurements.Y1_PX).getValue();
        int z1 = (int) object.getMeasurement(Measurements.Z1_SLICE).getValue();
        int x2 = (int) object.getMeasurement(Measurements.X2_PX).getValue();
        int y2 = (int) object.getMeasurement(Measurements.Y2_PX).getValue();
        int z2 = (int) object.getMeasurement(Measurements.Z2_SLICE).getValue();

        String[] pos1 = new String[]{Measurements.X1_PX,Measurements.Y1_PX,Measurements.Z1_SLICE,""};
        String[] pos2 = new String[]{Measurements.X2_PX,Measurements.Y2_PX,Measurements.Z2_SLICE,""};

        AddObjectsOverlay.addPositionMeasurementsOverlay(object,imagePlus, Color.ORANGE,1,pos1,false);
        AddObjectsOverlay.addPositionMeasurementsOverlay(object,imagePlus, Color.CYAN,1,pos2,false);

    }
    

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        boolean addOverlay = parameters.getValue(ADD_ENDPOINTS_AS_OVERLAY);
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        boolean addToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // If necessary, getting image for overlay
        ImagePlus inputImagePlus = null;
        if (addOverlay) {
            inputImagePlus = workspace.getImage(inputImageName).getImagePlus();

            // If the overlay shouldn't be added to the input image, a duplicate is created
            if (!applyToInput) inputImagePlus = new Duplicator().run(inputImagePlus);
        }

        // Running through each object, taking measurements and adding new object to the workspace where necessary
        int count = 0;
        int nTotal = inputObjects.size();
        for (Obj inputObject:inputObjects.values()) {
            processObject(inputObject);
            if (addOverlay) addEndpointsOverlay(inputObject,inputImagePlus);

            writeMessage("Processed object "+(++count)+" of "+nTotal);
        }

        if (addOverlay) {
            if (showOutput) {
                ImagePlus dispIpl = inputImagePlus.duplicate();
                dispIpl.setTitle(inputImageName);
                IntensityMinMax.run(dispIpl, true);
                dispIpl.setPosition(1, 1, 1);
                dispIpl.updateChannelAndDraw();
                dispIpl.show();
            }

            // If the user requested, the output image can be added to the workspace
            if (!applyToInput && addToWorkspace) {
                Image outputImage = new Image(outputImageName, inputImagePlus);
            }
        }

        if (showOutput) inputObjects.showMeasurements(this,modules);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ParamSeparatorP(RENDERING_SEPARATOR,this));
        parameters.add(new BooleanP(ADD_ENDPOINTS_AS_OVERLAY, this,false));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this,false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_ENDPOINTS_AS_OVERLAY));
        if ((boolean) parameters.getValue(ADD_ENDPOINTS_AS_OVERLAY)) {
            returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

            returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
            if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
                returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));
                if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE)) {
                    returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
                }
            }
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.LENGTH_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Length of the longest chord (the vector passing between the two points on the " +
                "\""+inputObjectsName+"\" object surface with the greatest spacing).  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.LENGTH_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Length of the longest chord (the vector passing between the two points on the " +
                "\""+inputObjectsName+"\" object surface with the greatest spacing).  Measured in calibrated ("
                +Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.X1_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("X-coordinate for one end of the longest chord fit to the object \""
                +inputObjectsName+"\" .  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Y1_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Y-coordinate for one end of the longest chord fit to the object \""
                +inputObjectsName+"\" .  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Z1_SLICE);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Z-coordinate for one end of the longest chord fit to the object \""
                +inputObjectsName+"\" .  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.X2_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("X-coordinate for one end of the longest chord fit to the object \""
                +inputObjectsName+"\" .  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Y2_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Y-coordinate for one end of the longest chord fit to the object \""
                +inputObjectsName+"\" .  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Z2_SLICE);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Z-coordinate for one end of the longest chord fit to the object \""
                +inputObjectsName+"\" .  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_SURF_DIST_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Mean distance of all points on the \""+inputObjectsName+"\" object surface to the " +
                "respective closest point on the longest chord.  Measured in pixel units (i.e. Z-coordinates are " +
                "converted to pixel units prior to calculation).");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_SURF_DIST_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Mean distance of all points on the \""+inputObjectsName+"\" object surface to the " +
                "respective closest point on the longest chord.  Measured in calibrated ("
                +Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.STD_SURF_DIST_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Standard deviation distance of all points on the\""+inputObjectsName+"\"object " +
                "surface to the respective closest point on the longest chord.  Measured in pixel units (i.e. " +
                "Z-coordinates are converted to pixel units prior to calculation).");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.STD_SURF_DIST_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Standard deviation distance of all points on the \""+inputObjectsName+"\" object " +
                "surface to the respective closest point on the longest chord.  Measured in calibrated ("
                +Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_SURF_DIST_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum distance of all points on the \""+inputObjectsName+"\" object surface to the " +
                "respective closest point on the longest chord.  Measured in pixel units (i.e. Z-coordinates are " +
                "converted to pixel units prior to calculation).");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_SURF_DIST_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum distance of all points on the \""+inputObjectsName+"\" object surface to the " +
                "respective closest point on the longest chord.  Measured in calibrated ("
                +Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.ORIENTATION_XY_DEGS);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Orientation in XY of longest chord fit to the object, \""+ inputObjectsName+"\".  " +
                "Measured in degrees, relative to positive x-axis (positive above x-axis, negative below x-axis).");
        returnedRefs.add(reference);

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
