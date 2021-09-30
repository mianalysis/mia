package io.github.mianalysis.mia.module.objectprocessing.identification;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.visualisation.overlays.AddLabels;
import io.github.mianalysis.mia.module.visualisation.overlays.AddObjectOutline;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.mianalysis.mia.process.LabelFactory;
import io.github.sjcross.common.process.IntensityMinMax;

public abstract class AbstractHoughDetection extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String OUTPUT_TRANSFORM_IMAGE = "Output transform image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String VISUALISATION_SEPARATOR = "Visualisation controls";
    public static final String SHOW_TRANSFORM_IMAGE = "Show transform image";
    public static final String SHOW_DETECTION_IMAGE = "Show detection image";
    public static final String SHOW_HOUGH_SCORE = "Show detection score";
    public static final String LABEL_SIZE = "Label size";

    public AbstractHoughDetection(String name, Modules modules) {
        super(name, modules);
    }

    protected interface Measurements {
        String SCORE = "HOUGH_DETECTION//SCORE";

    }

    public static String resampleRange(String range, int samplingRate) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(range);
            
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            int oldValue = Integer.parseInt(matcher.group());
            int newValue = Math.floorDiv(oldValue, samplingRate);
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

        HashMap<Integer, String> IDs = null;
        if (showHoughScore) {
            DecimalFormat df = LabelFactory.getDecimalFormat(0, true);
            IDs = LabelFactory.getMeasurementLabels(outputObjects, Measurements.SCORE, df);
            AddLabels.addOverlay(dispIpl, outputObjects, AddLabels.LabelPositions.CENTRE, IDs, labelSize, 0, 0, hues,
                    100, false, false, true);
        }

        AddObjectOutline.addOverlay(dispIpl, outputObjects, 1, 1, hues, 100, false, true);

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
        if ((boolean) parameters.getValue(OUTPUT_TRANSFORM_IMAGE))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        return returnedParameters;

    }

    public Parameters updateAndGetVisualisationParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(VISUALISATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SHOW_TRANSFORM_IMAGE));
        returnedParameters.add(parameters.getParameter(SHOW_DETECTION_IMAGE));
        if ((boolean) parameters.getValue(SHOW_DETECTION_IMAGE)) {
            returnedParameters.add(parameters.getParameter(SHOW_HOUGH_SCORE));
            if ((boolean) parameters.getValue(SHOW_HOUGH_SCORE))
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
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        ObjMeasurementRef score = objectMeasurementRefs.getOrPut(Measurements.SCORE);
        score.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
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

        parameters.get(SHOW_TRANSFORM_IMAGE).setDescription(
                "When selected, the transform image will be displayed (as long as the module is currently set to show its output).");

        parameters.get(SHOW_DETECTION_IMAGE).setDescription(
                "When selected, the detection image will be displayed (as long as the module is currently set to show its output).");

        parameters.get(SHOW_HOUGH_SCORE).setDescription(
                "When selected, the detection image will also show the score associated with each detected object.");

        parameters.get(LABEL_SIZE).setDescription("Font size of the detection score text label.");

    }
}
