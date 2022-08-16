package io.github.mianalysis.mia.module.objects.detect;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.visualise.overlays.AddLabels;
import io.github.mianalysis.mia.module.visualise.overlays.AddObjectOutline;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.mianalysis.mia.process.LabelFactory;
import io.github.sjcross.sjcommon.process.IntensityMinMax;

public abstract class AbstractHoughDetection extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String OUTPUT_TRANSFORM_IMAGE = "Output transform image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String DETECTION_SEPARATOR = "Detection controls";
    public static final String DOWNSAMPLE_FACTOR = "Downsample factor";
    public static final String NORMALISE_SCORES = "Normalise scores";
    public static final String DETECTION_MODE = "Detection mode";
    public static final String DETECTION_THRESHOLD = "Detection threshold";
    public static final String NUMBER_OF_OBJECTS = "Number of objects";
    public static final String EXCLUSION_RADIUS = "Exclusion radius (px)";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public static final String VISUALISATION_SEPARATOR = "Visualisation controls";
    public static final String SHOW_TRANSFORM_IMAGE = "Show transform image";
    public static final String SHOW_DETECTION_IMAGE = "Show detection image";
    public static final String SHOW_HOUGH_SCORE = "Show detection score";
    public static final String LABEL_SIZE = "Label size";

    public AbstractHoughDetection(String name, Modules modules) {
        super(name, modules);
    }

    protected interface DetectionModes {
        String ALL_ABOVE_SCORE = "All above score";
        String N_HIGHEST_SCORES = "N highest scores";

        String[] ALL = new String[] { ALL_ABOVE_SCORE, N_HIGHEST_SCORES };

    }

    protected interface Measurements {
        String SCORE = "HOUGH_DETECTION//SCORE";

    }

    public static String resampleRange(String range, double samplingRate) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(range);

        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            int oldValue = Integer.parseInt(matcher.group());

            // Preventing divide-by-zero errors
            int newValue = oldValue;
            if (oldValue != 0)
                newValue = (int) Math.round(oldValue / samplingRate);

            newValue = Math.max(newValue, 1);

            matcher.appendReplacement(out, String.valueOf(newValue));
        }

        matcher.appendTail(out);

        return out.toString();

    }

    public static void showDetectionImage(Image image, Objs outputObjects, boolean showHoughScore, int labelSize) {
        ImagePlus dispIpl = new Duplicator().run(image.getImagePlus());
        IntensityMinMax.run(dispIpl, true);

        HashMap<Integer, Float> hues = ColourFactory.getRandomHues(outputObjects);
        HashMap<Integer, Color> colours = ColourFactory.getColours(hues);

        HashMap<Integer, String> IDs = null;
        if (showHoughScore) {
            DecimalFormat df = LabelFactory.getDecimalFormat(0, true);
            IDs = LabelFactory.getMeasurementLabels(outputObjects, Measurements.SCORE, df);
            AddLabels.addOverlay(dispIpl, outputObjects, AddLabels.LabelPositions.CENTRE, IDs, labelSize, 0, 0, colours,
                    false, false, true);
        }

        AddObjectOutline.addOverlay(dispIpl, outputObjects, 1, 1, colours, false, true);

        dispIpl.setPosition(1, 1, 1);
        dispIpl.updateChannelAndDraw();
        dispIpl.show();

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new BooleanP(OUTPUT_TRANSFORM_IMAGE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(DETECTION_SEPARATOR, this));
        parameters.add(new IntegerP(DOWNSAMPLE_FACTOR, this, 1));
        parameters.add(new BooleanP(NORMALISE_SCORES, this, true));
        parameters.add(new ChoiceP(DETECTION_MODE, this, DetectionModes.ALL_ABOVE_SCORE, DetectionModes.ALL));
        parameters.add(new DoubleP(DETECTION_THRESHOLD, this, 1.0));
        parameters.add(new IntegerP(NUMBER_OF_OBJECTS, this, 1));
        parameters.add(new IntegerP(EXCLUSION_RADIUS, this, 10));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        parameters.add(new SeparatorP(VISUALISATION_SEPARATOR, this));
        parameters.add(new BooleanP(SHOW_TRANSFORM_IMAGE, this, true));
        parameters.add(new BooleanP(SHOW_DETECTION_IMAGE, this, true));
        parameters.add(new BooleanP(SHOW_HOUGH_SCORE, this, false));
        parameters.add(new IntegerP(LABEL_SIZE, this, 12));

    }

    public Parameters updateAndGetInputParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_TRANSFORM_IMAGE));
        if ((boolean) parameters.getValue(OUTPUT_TRANSFORM_IMAGE, null))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        return returnedParameters;

    }

    public Parameters updateAndGetDetectionParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(DETECTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(DOWNSAMPLE_FACTOR));
        returnedParameters.add(parameters.getParameter(NORMALISE_SCORES));

        returnedParameters.add(parameters.getParameter(DETECTION_MODE));
        switch ((String) parameters.getValue(DETECTION_MODE, null)) {
            case DetectionModes.ALL_ABOVE_SCORE:
                returnedParameters.add(parameters.getParameter(DETECTION_THRESHOLD));
                break;
            case DetectionModes.N_HIGHEST_SCORES:
                returnedParameters.add(parameters.getParameter(NUMBER_OF_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(EXCLUSION_RADIUS));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    public Parameters updateAndGetVisualisationParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(VISUALISATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SHOW_TRANSFORM_IMAGE));
        returnedParameters.add(parameters.getParameter(SHOW_DETECTION_IMAGE));
        if ((boolean) parameters.getValue(SHOW_DETECTION_IMAGE, null)) {
            returnedParameters.add(parameters.getParameter(SHOW_HOUGH_SCORE));
            if ((boolean) parameters.getValue(SHOW_HOUGH_SCORE, null))
                returnedParameters.add(parameters.getParameter(LABEL_SIZE));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        ObjMeasurementRef score = objectMeasurementRefs.getOrPut(Measurements.SCORE);
        score.setObjectsName(parameters.getValue(OUTPUT_OBJECTS, workspace));
        returnedRefs.add(score);

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
        parameters.get(INPUT_IMAGE).setDescription("Input image from which objects will be detected.");

        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Output objects to be added to the workspace.  Irrespective of the form of the input features, output objects are always solid.");

        parameters.get(OUTPUT_TRANSFORM_IMAGE).setDescription(
                "When selected, the Hough-transform image will be output to the workspace with the name specified by \""
                        + OUTPUT_IMAGE + "\".");

        parameters.get(DOWNSAMPLE_FACTOR).setDescription(
                "To speed up the detection process, the image can be downsampled.  For example, a downsample factor of 2 will downsize the image in X and Y by a factor of 2 prior to detection of circles.");

        parameters.get(NORMALISE_SCORES).setDescription(
                "When selected, scores for each parameter combination will be normalised based on the number of samples used to produce them.  Normalisation will prevent reduction of scores towards the image edge; however, it can also lead to some overestimation in these regions.");

        parameters.get(DETECTION_MODE)
                .setDescription("Controls which objects will be added to the output collection:<br><ul>"

                        + "<li>\"" + DetectionModes.ALL_ABOVE_SCORE
                        + "\" All objects with scores above the threshold specified by \"" + DETECTION_THRESHOLD
                        + "\" will be exported.  Objects will be detected starting with that with the highest score.  Following export of each object all potential objects within \""
                        + EXCLUSION_RADIUS
                        + "\" will be suppressed to prevent the same object being detected multiple times.</li>"

                        + "<li>\"" + DetectionModes.N_HIGHEST_SCORES + "\" A specific number (controlled by \""
                        + NUMBER_OF_OBJECTS
                        + "\") of detected objects with the highest scores will be exported.  Following export of each object all potential objects within \""
                        + EXCLUSION_RADIUS
                        + "\" will be suppressed to prevent the same object being detected multiple times.</li></ul>");

        parameters.get(DETECTION_THRESHOLD).setDescription("If \"" + DETECTION_MODE + "\" is set to \""
                + DetectionModes.ALL_ABOVE_SCORE
                + "\", this is the minimum score a detected circle must have to be stored.  Scores are the sum of all pixel intensities lying on the perimeter of the circle.  As such, higher scores correspond to brighter circles, circles with high circularity (where all points lie on the perimeter of the detected circle) and circles with continuous intensity along their perimeter (no gaps).");

        parameters.get(NUMBER_OF_OBJECTS).setDescription("If \"" + DETECTION_MODE + "\" is set to \""
                + DetectionModes.N_HIGHEST_SCORES
                + "\", this is the number of objects with the highest scores that will be added to the output object collection.");

        parameters.get(EXCLUSION_RADIUS).setDescription(
                "The minimum distance between adjacent circles.  For multiple candidate points within this range, the circle with the highest score will be retained.  Specified in pixel units.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple radii simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

        parameters.get(SHOW_TRANSFORM_IMAGE).setDescription(
                "When selected, the transform image will be displayed (as long as the module is currently set to show its output).");

        parameters.get(SHOW_DETECTION_IMAGE).setDescription(
                "When selected, the detection image will be displayed (as long as the module is currently set to show its output).");

        parameters.get(SHOW_HOUGH_SCORE).setDescription(
                "When selected, the detection image will also show the score associated with each detected object.");

        parameters.get(LABEL_SIZE).setDescription("Font size of the detection score text label.");

    }
}
