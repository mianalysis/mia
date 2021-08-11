package wbif.sjx.MIA.Module.ObjectMeasurements.Intensity;

import java.util.HashMap;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import sc.fiji.coloc.gadgets.DataContainer;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageMeasurements.MeasureImageColocalisation;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

public class MeasureObjectColocalisation<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Input separator";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE_1 = "Input image 1";
    public static final String INPUT_IMAGE_2 = "Input image 2";

    public static final String THRESHOLD_SEPARATOR = "Threshold controls";
    public static final String THRESHOLDING_MODE = "Thresholding mode";
    public static final String FIXED_THRESHOLD_1 = "Threshold (C1)";
    public static final String FIXED_THRESHOLD_2 = "Threshold (C2)";
    
    public static final String MEASUREMENT_SEPARATOR = "Measurement controls";
    public static final String PCC_IMPLEMENTATION = "PCC implementation";
    public static final String MEASURE_KENDALLS_RANK = "Measure Kendall's Rank Correlation";
    public static final String MEASURE_LI_ICQ = "Measure Li's ICQ";
    public static final String MEASURE_MANDERS = "Measure Manders' Correlation";
    public static final String MEASURE_PCC = "Measure PCC";
    public static final String MEASURE_SPEARMANS_RANK = "Measure Spearman's Rank Correlation";

    public MeasureObjectColocalisation(ModuleCollection modules) {
        super("Measure object colocalisation", modules);
    }

    public interface PCCImplementations extends MeasureImageColocalisation.PCCImplementations {
    }

    public interface ThresholdingModes extends MeasureImageColocalisation.ThresholdingModes {
    }

    public interface Measurements extends MeasureImageColocalisation.Measurements {
    }

    public static String getFullName(String imageName1, String imageName2, String measurement) {
        return "COLOCALISATION // " + imageName1 + "-" + imageName2 + "_" + measurement;
    }

    public void setObjectMeasurements(Obj obj, HashMap<String, Double> measurements, String imageName1,
            String imageName2) {
        for (String measurementName : measurements.keySet()) {
            String fullName = getFullName(imageName1, imageName2, measurementName);
            obj.addMeasurement(new Measurement(fullName, measurements.get(measurementName)));
        }
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getDescription() {
        return "Calculates colocalisation of two input images individually for each object.  Measurements for each object only consider pixels within that object.  All measurements are associated with the relevant object."
        
        +"<br><br>All calculations are performed using the <a href=\"https://imagej.net/plugins/coloc-2\">Coloc2 plugin</a>.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input images
        String imageName1 = parameters.getValue(INPUT_IMAGE_1);
        Image<T> image1 = (Image<T>) workspace.getImage(imageName1);

        String imageName2 = parameters.getValue(INPUT_IMAGE_2);
        Image<T> image2 = (Image<T>) workspace.getImages().get(imageName2);

        // Getting parameters
        String objectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection objects = workspace.getObjects().get(objectName);
        String thresholdingMode = parameters.getValue(THRESHOLDING_MODE);
        double fixedThreshold1 = parameters.getValue(FIXED_THRESHOLD_1);
        double fixedThreshold2 = parameters.getValue(FIXED_THRESHOLD_2);
        String pccImplementationName = parameters.getValue(PCC_IMPLEMENTATION);
        boolean measureKendalls = parameters.getValue(MEASURE_KENDALLS_RANK);
        boolean measureLiICQ = parameters.getValue(MEASURE_LI_ICQ);
        boolean measureManders = parameters.getValue(MEASURE_MANDERS);
        boolean measurePCC = parameters.getValue(MEASURE_PCC);
        boolean measureSpearman = parameters.getValue(MEASURE_SPEARMANS_RANK);

        // If objects are to be used as a mask a binary image is created. Otherwise,
        // null is returned

        for (Obj inputObject : objects.values()) {
            Image maskImage = inputObject.getAsImage("Mask", false);
            // Image maskImage = MeasureImageColocalisation.getObjectMask(objects,
            // MeasureImageColocalisation.ObjectMaskLogic.MEASURE_INSIDE_OBJECTS);

            // Creating data container against which all algorithms will be run
            DataContainer<T> data = MeasureImageColocalisation.prepareDataContainer(image1, image2, maskImage);

            switch (thresholdingMode) {
                case ThresholdingModes.BISECTION:
                case ThresholdingModes.COSTES:
                    HashMap<String, Double> measurements = MeasureImageColocalisation.setAutoThresholds(data,
                            thresholdingMode, pccImplementationName);
                    setObjectMeasurements(inputObject, measurements, imageName1, imageName2);
                    break;
                case ThresholdingModes.MANUAL:
                    MeasureImageColocalisation.setManualThresholds(data, image1, fixedThreshold1, fixedThreshold2);
                    break;
            }

            // Making colocalisation measurements
            if (measureKendalls) {
                HashMap<String, Double> measurements = MeasureImageColocalisation.measureKendalls(data);
                setObjectMeasurements(inputObject, measurements, imageName1, imageName2);
            }

            if (measureLiICQ) {
                HashMap<String, Double> measurements = MeasureImageColocalisation.measureLiICQ(data);
                setObjectMeasurements(inputObject, measurements, imageName1, imageName2);
            }

            if (measureManders) {
                HashMap<String, Double> measurements = MeasureImageColocalisation.measureManders(data);
                setObjectMeasurements(inputObject, measurements, imageName1, imageName2);
            }

            if (measurePCC) {
                HashMap<String, Double> measurements = MeasureImageColocalisation.measurePCC(data,
                        pccImplementationName);
                setObjectMeasurements(inputObject, measurements, imageName1, imageName2);
            }

            if (measureSpearman) {
                HashMap<String, Double> measurements = MeasureImageColocalisation.measureSpearman(data);
                setObjectMeasurements(inputObject, measurements, imageName1, imageName2);
            }
        }

        if (showOutput)
            objects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new InputImageP(INPUT_IMAGE_1, this));
        parameters.add(new InputImageP(INPUT_IMAGE_2, this));

        parameters.add(new SeparatorP(THRESHOLD_SEPARATOR, this));
        parameters.add(new ChoiceP(THRESHOLDING_MODE, this, ThresholdingModes.BISECTION, ThresholdingModes.ALL));
        parameters.add(new DoubleP(FIXED_THRESHOLD_1, this, 1.0));
        parameters.add(new DoubleP(FIXED_THRESHOLD_2, this, 1.0));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new ChoiceP(PCC_IMPLEMENTATION, this, PCCImplementations.FAST, PCCImplementations.ALL));                
        parameters.add(new BooleanP(MEASURE_KENDALLS_RANK, this, true));
        parameters.add(new BooleanP(MEASURE_LI_ICQ, this, true));
        parameters.add(new BooleanP(MEASURE_MANDERS, this, true));
        parameters.add(new BooleanP(MEASURE_PCC, this, true));
        parameters.add(new BooleanP(MEASURE_SPEARMANS_RANK, this, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE_1));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE_2));

        returnedParameters.add(parameters.getParameter(THRESHOLD_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THRESHOLDING_MODE));
        switch ((String) parameters.getValue(THRESHOLDING_MODE)) {
            case ThresholdingModes.MANUAL:
                returnedParameters.add(parameters.getParameter(FIXED_THRESHOLD_1));
                returnedParameters.add(parameters.getParameter(FIXED_THRESHOLD_2));
                break;
        }

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        if ((boolean) parameters.getValue(MEASURE_PCC)
                || ((String) parameters.getValue(THRESHOLDING_MODE)).equals(ThresholdingModes.BISECTION)
                || ((String) parameters.getValue(THRESHOLDING_MODE)).equals(ThresholdingModes.COSTES)) {
            returnedParameters.add(parameters.getParameter(PCC_IMPLEMENTATION));
        }
        returnedParameters.add(parameters.getParameter(MEASURE_KENDALLS_RANK));
        returnedParameters.add(parameters.getParameter(MEASURE_LI_ICQ));
        returnedParameters.add(parameters.getParameter(MEASURE_MANDERS));
        returnedParameters.add(parameters.getParameter(MEASURE_PCC));
        returnedParameters.add(parameters.getParameter(MEASURE_SPEARMANS_RANK));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String inputImage1Name = parameters.getValue(INPUT_IMAGE_1);
        String inputImage2Name = parameters.getValue(INPUT_IMAGE_2);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        switch ((String) parameters.getValue(THRESHOLDING_MODE)) {
            case ThresholdingModes.BISECTION:
            case ThresholdingModes.COSTES:
                String name = getFullName(inputImage1Name, inputImage2Name, Measurements.THRESHOLD_1);
                ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
                reference.setObjectsName(inputObjectsName);
                returnedRefs.add(reference);

                name = getFullName(inputImage1Name, inputImage2Name, Measurements.THRESHOLD_2);
                reference = objectMeasurementRefs.getOrPut(name);
                reference.setObjectsName(inputObjectsName);
                returnedRefs.add(reference);

                name = getFullName(inputImage1Name, inputImage2Name, Measurements.THRESHOLD_SLOPE);
                reference = objectMeasurementRefs.getOrPut(name);
                reference.setObjectsName(inputObjectsName);
                returnedRefs.add(reference);

                name = getFullName(inputImage1Name, inputImage2Name, Measurements.THRESHOLD_Y_INTERCEPT);
                reference = objectMeasurementRefs.getOrPut(name);
                reference.setObjectsName(inputObjectsName);
                returnedRefs.add(reference);

                break;
        }

        if ((boolean) parameters.getValue(MEASURE_KENDALLS_RANK)) {
            String name = getFullName(inputImage1Name, inputImage2Name, Measurements.KENDALLS_TAU);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(MEASURE_LI_ICQ)) {
            String name = getFullName(inputImage1Name, inputImage2Name, Measurements.LI_ICQ);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(MEASURE_MANDERS)) {
            String name = getFullName(inputImage1Name, inputImage2Name, Measurements.M1_ABOVE_ZERO);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);

            name = getFullName(inputImage1Name, inputImage2Name, Measurements.M2_ABOVE_ZERO);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);

            if (!((String) parameters.getValue(THRESHOLDING_MODE)).equals(ThresholdingModes.NONE)) {
                name = getFullName(inputImage1Name, inputImage2Name, Measurements.M1_ABOVE_THRESHOLD);
                reference = objectMeasurementRefs.getOrPut(name);
                reference.setObjectsName(inputObjectsName);
                returnedRefs.add(reference);

                name = getFullName(inputImage1Name, inputImage2Name, Measurements.M2_ABOVE_THRESHOLD);
                reference = objectMeasurementRefs.getOrPut(name);
                reference.setObjectsName(inputObjectsName);
                returnedRefs.add(reference);
            }
        }

        if ((boolean) parameters.getValue(MEASURE_PCC)) {
            String name = getFullName(inputImage1Name, inputImage2Name, Measurements.PCC);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);

            if (!((String) parameters.getValue(THRESHOLDING_MODE)).equals(ThresholdingModes.NONE)) {
                name = getFullName(inputImage1Name, inputImage2Name, Measurements.PCC_BELOW_THRESHOLD);
                reference = objectMeasurementRefs.getOrPut(name);
                reference.setObjectsName(inputObjectsName);
                returnedRefs.add(reference);

                name = getFullName(inputImage1Name, inputImage2Name, Measurements.PCC_ABOVE_THRESHOLD);
                reference = objectMeasurementRefs.getOrPut(name);
                reference.setObjectsName(inputObjectsName);
                returnedRefs.add(reference);
            }
        }

        if ((boolean) parameters.getValue(MEASURE_SPEARMANS_RANK)) {
            String name = getFullName(inputImage1Name, inputImage2Name, Measurements.SPEARMAN_RHO);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);

            name = getFullName(inputImage1Name, inputImage2Name, Measurements.SPEARMAN_DF);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);

            name = getFullName(inputImage1Name, inputImage2Name, Measurements.SPEARMAN_T_STATISTIC);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);
        }

        return returnedRefs;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}