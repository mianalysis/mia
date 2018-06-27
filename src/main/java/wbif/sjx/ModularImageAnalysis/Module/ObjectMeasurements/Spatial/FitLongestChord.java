package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.Visualisation.AddObjectsOverlay;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.Analysis.LongestChordCalculator;
import wbif.sjx.common.MathFunc.CumStat;

import java.awt.*;

/**
 * Created by sc13967 on 20/06/2018.
 */
public class FitLongestChord extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String ADD_ENDPOINTS_AS_OVERLAY = "Add endpoints as overlay";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SHOW_IMAGE = "Show image";


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

    }


    public void processObject(Obj object) {
        double dppXY = object.getDistPerPxXY();

        LongestChordCalculator calculator = new LongestChordCalculator(object);

        double longestChordLength = calculator.getLCLength();
        object.addMeasurement(new Measurement(Measurements.LENGTH_PX,longestChordLength));
        object.addMeasurement(new Measurement(Units.replace(Measurements.LENGTH_CAL),longestChordLength*dppXY));

        double[][] LC = calculator.getLC();
        object.addMeasurement(new Measurement(Measurements.X1_PX,LC[0][0]));
        object.addMeasurement(new Measurement(Measurements.Y1_PX,LC[0][1]));
        object.addMeasurement(new Measurement(Measurements.Z1_SLICE,LC[0][2]));
        object.addMeasurement(new Measurement(Measurements.X2_PX,LC[1][0]));
        object.addMeasurement(new Measurement(Measurements.Y2_PX,LC[1][1]));
        object.addMeasurement(new Measurement(Measurements.Z2_SLICE,LC[1][2]));

        CumStat cumStat = calculator.calculateAverageDistanceFromLC();
        object.addMeasurement(new Measurement(Measurements.MEAN_SURF_DIST_PX,cumStat.getMean()));
        object.addMeasurement(new Measurement(Units.replace(Measurements.MEAN_SURF_DIST_CAL),cumStat.getMean()*dppXY));
        object.addMeasurement(new Measurement(Measurements.STD_SURF_DIST_PX,cumStat.getStd()));
        object.addMeasurement(new Measurement(Units.replace(Measurements.STD_SURF_DIST_CAL),cumStat.getStd()*dppXY));
        object.addMeasurement(new Measurement(Measurements.MAX_SURF_DIST_PX,cumStat.getMax()));
        object.addMeasurement(new Measurement(Units.replace(Measurements.MAX_SURF_DIST_CAL),cumStat.getMax()*dppXY));

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

        AddObjectsOverlay.addPositionMeasurementsOverlay(object,imagePlus, Color.ORANGE,1,pos1);
        AddObjectsOverlay.addPositionMeasurementsOverlay(object,imagePlus, Color.CYAN,1,pos2);

    }


    @Override
    public String getTitle() {
        return "Fit longest chord";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        boolean addOverlay = parameters.getValue(ADD_ENDPOINTS_AS_OVERLAY);
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        boolean addToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean showImage = parameters.getValue(SHOW_IMAGE);

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
            if (showImage) {
                Image inputImage = workspace.getImage(inputImageName);
                ImagePlus showIpl = new Duplicator().run(inputImage.getImagePlus());
                showIpl.setTitle(inputImageName);
                showIpl.show();
            }

            // If the user requested, the output image can be added to the workspace
            if (!applyToInput && addToWorkspace) {
                Image outputImage = new Image(outputImageName, inputImagePlus);
            }
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(ADD_ENDPOINTS_AS_OVERLAY, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(ADD_OUTPUT_TO_WORKSPACE, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(ADD_ENDPOINTS_AS_OVERLAY));
        if (parameters.getValue(ADD_ENDPOINTS_AS_OVERLAY)) {
            returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

            returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
            if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
                returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));
                if (parameters.getValue(ADD_OUTPUT_TO_WORKSPACE)) {
                    returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
                }
            }

            returnedParameters.add(parameters.getParameter(SHOW_IMAGE));

        }

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        objectMeasurementReferences.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        MeasurementReference reference = objectMeasurementReferences.getOrPut(Measurements.LENGTH_PX);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.LENGTH_CAL));
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementReferences.getOrPut(Measurements.X1_PX);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementReferences.getOrPut(Measurements.Y1_PX);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementReferences.getOrPut(Measurements.Z1_SLICE);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementReferences.getOrPut(Measurements.X2_PX);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementReferences.getOrPut(Measurements.Y2_PX);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementReferences.getOrPut(Measurements.Z2_SLICE);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementReferences.getOrPut(Measurements.MEAN_SURF_DIST_PX);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEAN_SURF_DIST_CAL));
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementReferences.getOrPut(Measurements.STD_SURF_DIST_PX);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.STD_SURF_DIST_CAL));
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementReferences.getOrPut(Measurements.MAX_SURF_DIST_PX);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MAX_SURF_DIST_CAL));
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}