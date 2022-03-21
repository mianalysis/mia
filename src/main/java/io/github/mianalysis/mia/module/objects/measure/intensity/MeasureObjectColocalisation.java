package io.github.mianalysis.mia.module.objects.measure.intensity;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.measure.MeasureImageColocalisation;
import io.github.mianalysis.mia.module.images.transform.CropImage;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ImageMeasurementP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import sc.fiji.coloc.algorithms.MissingPreconditionException;
import sc.fiji.coloc.gadgets.DataContainer;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureObjectColocalisation<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Input separator";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE_1 = "Input image 1";
    public static final String INPUT_IMAGE_2 = "Input image 2";

    public static final String THRESHOLD_SEPARATOR = "Threshold controls";
    public static final String THRESHOLDING_MODE = "Thresholding mode";
    public static final String IMAGE_MEASUREMENT_1 = "Image measurement (C1)";
    public static final String IMAGE_MEASUREMENT_2 = "Image measurement (C2)";
    public static final String FIXED_THRESHOLD_1 = "Threshold (C1)";
    public static final String FIXED_THRESHOLD_2 = "Threshold (C2)";

    public static final String MEASUREMENT_SEPARATOR = "Measurement controls";
    public static final String PCC_IMPLEMENTATION = "PCC implementation";
    public static final String MEASURE_KENDALLS_RANK = "Measure Kendall's Rank Correlation";
    public static final String MEASURE_LI_ICQ = "Measure Li's ICQ";
    public static final String MEASURE_MANDERS = "Measure Manders' Correlation";
    public static final String MEASURE_PCC = "Measure PCC";
    public static final String MEASURE_SPEARMANS_RANK = "Measure Spearman's Rank Correlation";

    public MeasureObjectColocalisation(Modules modules) {
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
        return Categories.OBJECTS_MEASURE_INTENSITY;
    }

    @Override
    public String getDescription() {
        return "Calculates colocalisation of two input images individually for each object.  Measurements for each object only consider pixels within that object.  All measurements are associated with the relevant object.  Colocalisation analysis has many potential pitfalls, so users are advised to read the Fiji <a href=\"https://imagej.net/imaging/colocalization-analysis\">Colocalization analysis</a> page and/or the <a href=\"https://journals.physiology.org/doi/full/10.1152/ajpcell.00462.2010\">Dunn et al 2011 review</a>."

                + "<br><br>All calculations are performed using the <a href=\"https://imagej.net/plugins/coloc-2\">Coloc2 plugin</a>.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input images
        String imageName1 = parameters.getValue(INPUT_IMAGE_1);
        Image image1 = (Image) workspace.getImage(imageName1);

        String imageName2 = parameters.getValue(INPUT_IMAGE_2);
        Image image2 = (Image) workspace.getImages().get(imageName2);

        // Getting parameters
        String objectName = parameters.getValue(INPUT_OBJECTS);
        Objs objects = workspace.getObjects().get(objectName);
        String thresholdingMode = parameters.getValue(THRESHOLDING_MODE);
        String imageMeasurementName1 = parameters.getValue(IMAGE_MEASUREMENT_1);
        String imageMeasurementName2 = parameters.getValue(IMAGE_MEASUREMENT_2);
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
        int count = 0;
        int total = objects.size();
        for (Obj inputObject : objects.values()) {
            Image maskImage = null;

            if (inputObject.size() > 0)
                maskImage = inputObject.getAsTightImage("Mask");

            // Cropping image to object
            double[][] extents = inputObject.getExtents(true, false);
            int top = (int) Math.round(extents[1][0]);
            int left = (int) Math.round(extents[0][0]);
            int width = (int) Math.round(extents[0][1] - extents[0][0]+1);
            int height = (int) Math.round(extents[1][1] - extents[1][0]+1);
            Image crop1 = CropImage.cropImage(image1, "Crop1", top, left, width, height);
            Image crop2 = CropImage.cropImage(image2, "Crop2", top, left, width, height);

            Image timepoint1 = ExtractSubstack.extractSubstack(crop1, "Timepoint1", "1-end", "1-end",
                    String.valueOf(inputObject.getT() + 1));
            Image timepoint2 = ExtractSubstack.extractSubstack(crop2, "Timepoint2", "1-end", "1-end",
                    String.valueOf(inputObject.getT() + 1));

            // timepoint1.showImage();
            // timepoint2.showImage();
            // maskImage.showImage();

            // Creating data container against which all algorithms will be run
            DataContainer<T> data = MeasureImageColocalisation.prepareDataContainer(timepoint1, timepoint2, maskImage);

            switch (thresholdingMode) {
                case ThresholdingModes.BISECTION:
                case ThresholdingModes.COSTES:
                    HashMap<String, Double> measurements = MeasureImageColocalisation.setAutoThresholds(data,
                            thresholdingMode, pccImplementationName);
                    setObjectMeasurements(inputObject, measurements, imageName1, imageName2);
                    break;
                case ThresholdingModes.IMAGE_MEASUREMENTS:
                    double threshold1 = image1.getMeasurement(imageMeasurementName1).getValue();
                    double threshold2 = image1.getMeasurement(imageMeasurementName2).getValue();
                    MeasureImageColocalisation.setManualThresholds(data, timepoint1, threshold1, threshold2);
                    break;
                case ThresholdingModes.MANUAL:
                    MeasureImageColocalisation.setManualThresholds(data, timepoint1, fixedThreshold1, fixedThreshold2);
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
                try {
                    HashMap<String, Double> measurements = MeasureImageColocalisation.measurePCC(data,
                            pccImplementationName);
                    setObjectMeasurements(inputObject, measurements, imageName1, imageName2);
                } catch (MissingPreconditionException e) {
                    MIA.log.writeWarning("PCC can't be calculated for object with ID " + inputObject.getID());
                }
            }

            if (measureSpearman) {
                HashMap<String, Double> measurements = MeasureImageColocalisation.measureSpearman(data);
                setObjectMeasurements(inputObject, measurements, imageName1, imageName2);
            }

            writeProgressStatus(++count, total, "objects");

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
        parameters.add(new ImageMeasurementP(IMAGE_MEASUREMENT_1, this));
        parameters.add(new ImageMeasurementP(IMAGE_MEASUREMENT_2, this));
        parameters.add(new DoubleP(FIXED_THRESHOLD_1, this, 1.0));
        parameters.add(new DoubleP(FIXED_THRESHOLD_2, this, 1.0));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new ChoiceP(PCC_IMPLEMENTATION, this, PCCImplementations.FAST, PCCImplementations.ALL));
        parameters.add(new BooleanP(MEASURE_KENDALLS_RANK, this, true));
        parameters.add(new BooleanP(MEASURE_LI_ICQ, this, true));
        parameters.add(new BooleanP(MEASURE_MANDERS, this, true));
        parameters.add(new BooleanP(MEASURE_PCC, this, true));
        parameters.add(new BooleanP(MEASURE_SPEARMANS_RANK, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE_1));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE_2));

        returnedParameters.add(parameters.getParameter(THRESHOLD_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THRESHOLDING_MODE));
        switch ((String) parameters.getValue(THRESHOLDING_MODE)) {
            case ThresholdingModes.IMAGE_MEASUREMENTS:
                returnedParameters.add(parameters.getParameter(IMAGE_MEASUREMENT_1));
                String imageName1 = parameters.getValue(INPUT_IMAGE_1);
                ((ImageMeasurementP) parameters.getParameter(IMAGE_MEASUREMENT_1)).setImageName(imageName1);
                returnedParameters.add(parameters.getParameter(IMAGE_MEASUREMENT_2));
                String imageName2 = parameters.getValue(INPUT_IMAGE_2);
                ((ImageMeasurementP) parameters.getParameter(IMAGE_MEASUREMENT_2)).setImageName(imageName2);
                break;
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
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

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
        String siteRef = "Description taken from <a href=\"https://imagej.net/imaging/colocalization-analysis\">https://imagej.net/imaging/colocalization-analysis</a>";

        parameters.get(INPUT_IMAGE_1).setDescription("First image for which colocalisation will be calculated.");

        parameters.get(INPUT_IMAGE_2).setDescription("Second image for which colocalisation will be calculated.");

        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects for which colocalisation will be measured.  For each object, colocalisation will be independently measured for the pixels coincident with the object's coordinates.  Measurements will be associated with the corresponding object.");

        parameters.get(THRESHOLDING_MODE)
                .setDescription("Controls how the thresholds for measurements such as Manders' are set:<br><ul>"

                        + "<li>\"" + ThresholdingModes.BISECTION
                        + "\" A faster method to calculate thresholds than the Costes approach.</li>"

                        + "<li>\"" + ThresholdingModes.COSTES
                        + "\" The \"standard\" method to calculate thresholds for Manders' colocalisation measures.  This approach sets the thresholds for the two input images such that the pixels with intensities lower than their respective thresholds don't have any statistical correlation (i.e. have PCC values less than or equal to 0).  This is based on Costes' 2004 paper (Costes et al., <i>Biophys. J.</i> <b>86</b> (2004) 3993–4003.</li>"

                        + "<li>\"" + ThresholdingModes.IMAGE_MEASUREMENTS
                        + "\" Thresholds for each image will be set equal to measurements associated with each object.</li>"

                        + "<li>\"" + ThresholdingModes.MANUAL
                        + "\" Threshold values are manually set from user-defined values (\"" + FIXED_THRESHOLD_1
                        + "\" and \"" + FIXED_THRESHOLD_2 + "\" parameters).</li>"

                        + "<li>\"" + ThresholdingModes.NONE
                        + "\" No threshold is set.  In this instance, Manders' metrics will only be calculated above zero intensity rather than both above zero and above the thresholds.  Similarly, Pearson's correlation coefficients will only be calculated for the entire region (after masking) rather than also for above and below the thresholds.</li></ul>");

        parameters.get(IMAGE_MEASUREMENT_1)
                .setDescription("If \"" + THRESHOLDING_MODE + "\" is set to \"" + ThresholdingModes.IMAGE_MEASUREMENTS
                        + "\", this is the measurement associated with \"" + INPUT_IMAGE_1
                        + "\" that will be applied to the first image.");

        parameters.get(IMAGE_MEASUREMENT_2)
                .setDescription("If \"" + THRESHOLDING_MODE + "\" is set to \"" + ThresholdingModes.IMAGE_MEASUREMENTS
                        + "\", this is the measurement associated with \"" + INPUT_IMAGE_2
                        + "\" that will be applied to the second image.");

        parameters.get(FIXED_THRESHOLD_1).setDescription("If \"" + THRESHOLDING_MODE + "\" is set to \""
                + ThresholdingModes.MANUAL + "\", this is the threshold that will be applied to the first image.");

        parameters.get(FIXED_THRESHOLD_2).setDescription("If \"" + THRESHOLDING_MODE + "\" is set to \""
                + ThresholdingModes.MANUAL + "\", this is the threshold that will be applied to the second image.");

        parameters.get(PCC_IMPLEMENTATION).setDescription(
                "Controls whether PCC should be calculated using the classic algorithm or using the Coloc2-default \"fast\" method.");

        parameters.get(MEASURE_KENDALLS_RANK).setDescription(
                "When selected, Kendall's rank correlation will be calculated.  This works in a similar manner to Pearson's PCC, except it's calculated on ranked data rather than raw pixel intensities.");

        parameters.get(MEASURE_LI_ICQ).setDescription(
                "When selected, Li's ICQ (intensity correlation quotient) will be calculated.  This measure reports the frequency with which both corresponding pixels for both channels are either both above or both below their respective means.  Values are scaled into the range -0.5 to +0.5, with values below 0 corresponding to anti-correlation and values above 0 indicating correlation.");

        parameters.get(MEASURE_MANDERS).setDescription(
                "When selected, Manders' M1 and M2 coefficients will be calculated.  \"Proportional to the amount of fluorescence of the colocalizing pixels or voxels in each colour channel. You can get more details in Manders et al. Values range from 0 to 1, expressing the fraction of intensity in a channel that is located in pixels where there is above zero (or threshold) intensity in the other colour channel.\" "
                        + siteRef);

        parameters.get(MEASURE_PCC).setDescription(
                "When selected, Pearson's Correlation Coefficient (PCC) will be calculated.  \"It is not sensitive to differences in mean signal intensities or range, or a zero offset between the two components. The result is +1 for perfect correlation, 0 for no correlation, and -1 for perfect anti-correlation. Noise makes the value closer to 0 than it should be.\" "
                        + siteRef);

        parameters.get(MEASURE_SPEARMANS_RANK).setDescription(
                "When selected, Spearman's rank correlation will be calculated.  Spearman's rho is calculated in a similar manner to Pearson's PCC, except the image intensities are replaced by their respective rank.  Spearman's correlation works with monotonic relationships.  As with PCC, values are in the range -1 to +1.");

    }
}