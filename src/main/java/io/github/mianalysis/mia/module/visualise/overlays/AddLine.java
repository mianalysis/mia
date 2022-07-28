package io.github.mianalysis.mia.module.visualise.overlays;

import java.awt.Color;
import java.util.HashMap;

import ij.ImagePlus;
import ij.gui.Line;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ImageMeasurementP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.sjcross.sjcommon.object.Point;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class AddLine extends AbstractOverlay {
    public static final String INPUT_SEPARATOR = "Image and object input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String OUTPUT_SEPARATOR = "Image output";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String REFERENCE_SEPARATOR = "Reference selection";
    public static final String REFERENCE_MODE_1 = "Reference mode 1";
    public static final String REFERENCE_IMAGE_1 = "Reference image 1";
    public static final String X_POSITION_MEASUREMENT_IM_1 = "X-pos. image meas. 1 (px)";
    public static final String Y_POSITION_MEASUREMENT_IM_1 = "Y-pos. image meas. 1 (px)";
    // public static final String Z_POSITION_MEASUREMENT_IM_1 = "Z-pos. image meas.
    // 1 (slice)";
    public static final String X_POSITION_MEASUREMENT_OBJ_1 = "X-pos. object meas. 1 (px)";
    public static final String Y_POSITION_MEASUREMENT_OBJ_1 = "Y-pos. object meas. 1 (px)";
    // public static final String Z_POSITION_MEASUREMENT_OBJ_1 = "Z-pos. object
    // meas. 1 (slice)";
    public static final String REFERENCE_MODE_2 = "Reference mode 2";
    public static final String REFERENCE_IMAGE_2 = "Reference image 2";
    public static final String X_POSITION_MEASUREMENT_IM_2 = "X-pos. image meas. 2 (px)";
    public static final String Y_POSITION_MEASUREMENT_IM_2 = "Y-pos. image meas. 2 (px)";
    // public static final String Z_POSITION_MEASUREMENT_IM_2 = "Z-pos. image meas.
    // 2 (slice)";
    public static final String X_POSITION_MEASUREMENT_OBJ_2 = "X-pos. object meas. 2 (px)";
    public static final String Y_POSITION_MEASUREMENT_OBJ_2 = "Y-pos. object meas. 2 (px)";
    // public static final String Z_POSITION_MEASUREMENT_OBJ_2 = "Z-pos. object
    // meas. 2 (slice)";

    public static final String RENDERING_SEPARATOR = "Overlay rendering";
    public static final String LINE_WIDTH = "Line width";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public interface ReferenceModes {
        String CENTROID = "Object centroid";
        String IMAGE_MEASUREMENT = "Image measurement";
        String OBJECT_MEASUREMENT = "Object measurement";

        String[] ALL = new String[] { CENTROID, IMAGE_MEASUREMENT, OBJECT_MEASUREMENT };

    }

    public AddLine(final Modules modules) {
        super("Add line", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.VISUALISATION_OVERLAYS;
    }

    public static Point<Double> getCentroid(final Obj obj) {
        final double xMeas = obj.getXMean(true);
        final double yMeas = obj.getYMean(true);
        final double zMeas = obj.getZMean(true, true);

        return new Point<Double>(xMeas, yMeas, zMeas);

    }

    public static Point<Double> getImageReference(final Image image, final String xMeasName, final String yMeasName) {
        final double xMeas = image.getMeasurement(xMeasName).getValue();
        final double yMeas = image.getMeasurement(yMeasName).getValue();

        return new Point<Double>(xMeas, yMeas, 0d);

    }

    public static Point<Double> getObjectReference(final Obj obj, final String xMeasName, final String yMeasName) {
        final double xMeas = obj.getMeasurement(xMeasName).getValue();
        final double yMeas = obj.getMeasurement(yMeasName).getValue();

        return new Point<Double>(xMeas, yMeas, 0d);

    }

    public static void addOverlay(ImagePlus ipl, Color colour, double lineWidth, Point<Double> pos1,
            Point<Double> pos2, int t) {
                if (ipl.getOverlay() == null)
                ipl.setOverlay(new ij.gui.Overlay());
            ij.gui.Overlay ovl = ipl.getOverlay();
    
            // Creating the line
            Line line = new Line(pos1.x, pos1.y, pos2.x, pos2.y);
    
            if (ipl.isHyperStack()) {
                ipl.setPosition(1, 1, t);
                line.setPosition(1, 1, t);
            } else {
                int pos = Math.max(1, t);
                ipl.setPosition(pos);
                line.setPosition(pos);
            }
    
            line.setStrokeWidth(lineWidth);
            line.setStrokeColor(colour);
            ovl.addElement(line);
    }

    public static void addOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth, Point<Double> pos1,
            Point<Double> pos2) {
        addOverlay(ipl, colour, lineWidth, pos1, pos2, object.getT()+1);
    }

    @Override
    protected Status process(final Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        final String refMode1 = parameters.getValue(REFERENCE_MODE_1,workspace);
        final String referenceImageName1 = parameters.getValue(REFERENCE_IMAGE_1,workspace);
        final String xPosMeasIm1 = parameters.getValue(X_POSITION_MEASUREMENT_IM_1,workspace);
        final String yPosMeasIm1 = parameters.getValue(Y_POSITION_MEASUREMENT_IM_1,workspace);
        // final String zPosMeasIm1 = parameters.getValue(Z_POSITION_MEASUREMENT_IM_1,workspace);
        final String xPosMeasObj1 = parameters.getValue(X_POSITION_MEASUREMENT_OBJ_1,workspace);
        final String yPosMeasObj1 = parameters.getValue(Y_POSITION_MEASUREMENT_OBJ_1,workspace);
        // final String zPosMeasObj1 =
        // parameters.getValue(Z_POSITION_MEASUREMENT_OBJ_1,workspace);

        final String refMode2 = parameters.getValue(REFERENCE_MODE_2,workspace);
        final String referenceImageName2 = parameters.getValue(REFERENCE_IMAGE_2,workspace);
        final String xPosMeasIm2 = parameters.getValue(X_POSITION_MEASUREMENT_IM_2,workspace);
        final String yPosMeasIm2 = parameters.getValue(Y_POSITION_MEASUREMENT_IM_2,workspace);
        // final String zPosMeasIm2 = parameters.getValue(Z_POSITION_MEASUREMENT_IM_2,workspace);
        final String xPosMeasObj2 = parameters.getValue(X_POSITION_MEASUREMENT_OBJ_2,workspace);
        final String yPosMeasObj2 = parameters.getValue(Y_POSITION_MEASUREMENT_OBJ_2,workspace);
        // final String zPosMeasObj2 =
        // parameters.getValue(Z_POSITION_MEASUREMENT_OBJ_2,workspace);

        double lineWidth = parameters.getValue(LINE_WIDTH,workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING,workspace);

        Image referenceImage1 = null;
        if (refMode1.equals(ReferenceModes.IMAGE_MEASUREMENT)) {
            referenceImage1 = workspace.getImage(referenceImageName1);
        }

        Image referenceImage2 = null;
        if (refMode2.equals(ReferenceModes.IMAGE_MEASUREMENT)) {
            referenceImage2 = workspace.getImage(referenceImageName2);
        }

        // Only add output to workspace if not applying to input
        if (applyToInput)
            addOutputToWorkspace = false;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
            ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer, Color> colours = getColours(inputObjects, workspace);

        for (final Obj inputObject : inputObjects.values()) {
            final Point<Double> pos1;
            final Point<Double> pos2;

            // Getting reference points
            switch (refMode1) {
                case ReferenceModes.CENTROID:
                default:
                    pos1 = getCentroid(inputObject);
                    break;
                case ReferenceModes.IMAGE_MEASUREMENT:
                    pos1 = getImageReference(referenceImage1, xPosMeasIm1, yPosMeasIm1);
                    break;
                case ReferenceModes.OBJECT_MEASUREMENT:
                    pos1 = getObjectReference(inputObject, xPosMeasObj1, yPosMeasObj1);
                    break;
            }

            switch (refMode2) {
                case ReferenceModes.CENTROID:
                default:
                    pos2 = getCentroid(inputObject);
                    break;
                case ReferenceModes.IMAGE_MEASUREMENT:
                    pos2 = getImageReference(referenceImage2, xPosMeasIm2, yPosMeasIm2);
                    break;
                case ReferenceModes.OBJECT_MEASUREMENT:
                    pos2 = getObjectReference(inputObject, xPosMeasObj2, yPosMeasObj2);
                    break;
            }

            // Adding the overlay
            Color colour = colours.get(inputObject.getID());
            addOverlay(inputObject, ipl, colour, lineWidth, pos1, pos2);

        }

        Image outputImage = ImageFactory.createImage(outputImageName, ipl);

        // If necessary, adding output image to workspace. This also allows us to show
        // it.
        if (addOutputToWorkspace)
            workspace.addImage(outputImage);
        if (showOutput)
            outputImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(REFERENCE_SEPARATOR, this));
        parameters.add(new ChoiceP(REFERENCE_MODE_1, this, ReferenceModes.CENTROID, ReferenceModes.ALL));
        parameters.add(new InputImageP(REFERENCE_IMAGE_1, this));
        parameters.add(new ImageMeasurementP(X_POSITION_MEASUREMENT_IM_1, this));
        parameters.add(new ImageMeasurementP(Y_POSITION_MEASUREMENT_IM_1, this));
        // parameters.add(new ImageMeasurementP(Z_POSITION_MEASUREMENT_IM_1,this));
        parameters.add(new ObjectMeasurementP(X_POSITION_MEASUREMENT_OBJ_1, this));
        parameters.add(new ObjectMeasurementP(Y_POSITION_MEASUREMENT_OBJ_1, this));
        // parameters.add(new ObjectMeasurementP(Z_POSITION_MEASUREMENT_OBJ_1,this));
        parameters.add(new ChoiceP(REFERENCE_MODE_2, this, ReferenceModes.CENTROID, ReferenceModes.ALL));
        parameters.add(new InputImageP(REFERENCE_IMAGE_2, this));
        parameters.add(new ImageMeasurementP(X_POSITION_MEASUREMENT_IM_2, this));
        parameters.add(new ImageMeasurementP(Y_POSITION_MEASUREMENT_IM_2, this));
        // parameters.add(new ImageMeasurementP(Z_POSITION_MEASUREMENT_IM_2,this));
        parameters.add(new ObjectMeasurementP(X_POSITION_MEASUREMENT_OBJ_2, this));
        parameters.add(new ObjectMeasurementP(Y_POSITION_MEASUREMENT_OBJ_2, this));
        // parameters.add(new ObjectMeasurementP(Z_POSITION_MEASUREMENT_OBJ_2,this));

        parameters.add(new SeparatorP(RENDERING_SEPARATOR, this));
        parameters.add(new DoubleP(LINE_WIDTH, this, 1));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE,workspace)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
            }
        }

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        String referenceImageName1 = parameters.getValue(REFERENCE_IMAGE_1,workspace);
        String referenceImageName2 = parameters.getValue(REFERENCE_IMAGE_2,workspace);

        returnedParameters.add(parameters.getParameter(REFERENCE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE_1));
        switch ((String) parameters.getValue(REFERENCE_MODE_1,workspace)) {
            case ReferenceModes.IMAGE_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE_1));
                returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT_IM_1));
                returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT_IM_1));

                ((ImageMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT_IM_1))
                        .setImageName(referenceImageName1);
                ((ImageMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT_IM_1))
                        .setImageName(referenceImageName1);

                break;
            case ReferenceModes.OBJECT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT_OBJ_1));
                returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT_OBJ_1));

                ((ObjectMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT_OBJ_1))
                        .setObjectName(inputObjectsName);
                ((ObjectMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT_OBJ_1))
                        .setObjectName(inputObjectsName);

                break;
        }

        returnedParameters.add(parameters.getParameter(REFERENCE_MODE_2));
        switch ((String) parameters.getValue(REFERENCE_MODE_2,workspace)) {
            case ReferenceModes.IMAGE_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE_2));
                returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT_IM_2));
                returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT_IM_2));

                ((ImageMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT_IM_2))
                        .setImageName(referenceImageName2);
                ((ImageMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT_IM_2))
                        .setImageName(referenceImageName2);

                break;
            case ReferenceModes.OBJECT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT_OBJ_2));
                returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT_OBJ_2));

                ((ObjectMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT_OBJ_2))
                        .setObjectName(inputObjectsName);
                ((ObjectMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT_OBJ_2))
                        .setObjectName(inputObjectsName);

                break;
        }

        // ((ImageMeasurementP)
        // parameters.getParameter(Z_POSITION_MEASUREMENT_IM_1)).setImageName(referenceImageName1);

        // ((ImageMeasurementP)
        // parameters.getParameter(Z_POSITION_MEASUREMENT_IM_2)).setImageName(referenceImageName2);

        returnedParameters.addAll(super.updateAndGetParameters(inputObjectsName));
        
        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LINE_WIDTH));

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
Workspace workspace = null;
        return null;

    }

    @Override
public MetadataRefs updateAndGetMetadataReferences() {
Workspace workspace = null;
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
    public boolean verify() {
        return true;

    }

    @Override
    public String getDescription() {
        return "Draws an overlay line between two specified points.<br><br>" + "Note: This currently only works in 2D.";
    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(INPUT_IMAGE)
                .setDescription("Image onto which overlay will be rendered.  Input image will only be updated if \""
                        + APPLY_TO_INPUT
                        + "\" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by \""
                        + OUTPUT_IMAGE + "\".");

        parameters.get(INPUT_OBJECTS).setDescription("Objects to represent as overlays.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "Determines if the modifications made to the input image (added overlay elements) will be applied to that image or directed to a new image.  When selected, the input image will be updated.");

        parameters.get(ADD_OUTPUT_TO_WORKSPACE).setDescription(
                "If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).");

        parameters.get(REFERENCE_MODE_1)
                .setDescription("Controls for the location of the first end of the line to be drawn:<br>"

                        + "<br>- \"" + ReferenceModes.CENTROID
                        + "\" This end of the line is coincident with the centre of the input object.<br>"

                        + "<br>- \"" + ReferenceModes.IMAGE_MEASUREMENT
                        + "\" This end of the line is coincident with a pair of XY measurements taken from an image.  The image to be used is selected with the \""
                        + REFERENCE_IMAGE_1 + "\" parameter and the measurements by the \""
                        + X_POSITION_MEASUREMENT_IM_1 + "\" and \"" + Y_POSITION_MEASUREMENT_IM_1 + "\" parameters.<br>"

                        + "<br>- \"" + ReferenceModes.OBJECT_MEASUREMENT
                        + "\" This end of the line is coincident with a pair of XY measurements taken from the current object.  The measurements are selected with the \""
                        + X_POSITION_MEASUREMENT_OBJ_1 + "\" and \"" + Y_POSITION_MEASUREMENT_OBJ_1
                        + "\" parameters.<br>");

        parameters.get(REFERENCE_IMAGE_1).setDescription(
                "Image providing the XY coordinate measurements to be used as the location of the first end of the line.");

        parameters.get(X_POSITION_MEASUREMENT_IM_1).setDescription(
                "Measurement used as X-coordinate for the first end of the line.  This measurement will be taken from the image specified by \""
                        + REFERENCE_IMAGE_1 + "\" when \"" + REFERENCE_MODE_1 + "\" is set to \""
                        + ReferenceModes.IMAGE_MEASUREMENT + "\".  The coordinate value is specified in pixel units.");

        parameters.get(Y_POSITION_MEASUREMENT_IM_1).setDescription(
                "Measurement used as Y-coordinate for the first end of the line.  This measurement will be taken from the image specified by \""
                        + REFERENCE_IMAGE_1 + "\" when \"" + REFERENCE_MODE_1 + "\" is set to \""
                        + ReferenceModes.IMAGE_MEASUREMENT + "\".  The coordinate value is specified in pixel units.");

        parameters.get(X_POSITION_MEASUREMENT_OBJ_1).setDescription(
                "Measurement used as X-coordinate for the first end of the line.  This measurement will be taken from the current object.  The coordinate value is specified in pixel units.");

        parameters.get(Y_POSITION_MEASUREMENT_OBJ_1).setDescription(
                "Measurement used as Y-coordinate for the first end of the line.  This measurement will be taken from the current object.  The coordinate value is specified in pixel units.");

        parameters.get(REFERENCE_MODE_2)
                .setDescription("Controls for the location of the second end of the line to be drawn:<br>"

                        + "<br>- \"" + ReferenceModes.CENTROID
                        + "\" This end of the line is coincident with the centre of the input object.<br>"

                        + "<br>- \"" + ReferenceModes.IMAGE_MEASUREMENT
                        + "\" This end of the line is coincident with a pair of XY measurements taken from an image.  The image to be used is selected with the \""
                        + REFERENCE_IMAGE_2 + "\" parameter and the measurements by the \""
                        + X_POSITION_MEASUREMENT_IM_2 + "\" and \"" + Y_POSITION_MEASUREMENT_IM_2 + "\" parameters.<br>"

                        + "<br>- \"" + ReferenceModes.OBJECT_MEASUREMENT
                        + "\" This end of the line is coincident with a pair of XY measurements taken from the current object.  The measurements are selected with the \""
                        + X_POSITION_MEASUREMENT_OBJ_2 + "\" and \"" + Y_POSITION_MEASUREMENT_OBJ_2
                        + "\" parameters.<br>");

        parameters.get(REFERENCE_IMAGE_2).setDescription(
                "Image providing the XY coordinate measurements to be used as the location of the second end of the line.");

        parameters.get(X_POSITION_MEASUREMENT_IM_2).setDescription(
                "Measurement used as X-coordinate for the second end of the line.  This measurement will be taken from the image specified by \""
                        + REFERENCE_IMAGE_2 + "\" when \"" + REFERENCE_MODE_2 + "\" is set to \""
                        + ReferenceModes.IMAGE_MEASUREMENT + "\".  The coordinate value is specified in pixel units.");

        parameters.get(Y_POSITION_MEASUREMENT_IM_2).setDescription(
                "Measurement used as Y-coordinate for the second end of the line.  This measurement will be taken from the image specified by \""
                        + REFERENCE_IMAGE_2 + "\" when \"" + REFERENCE_MODE_2 + "\" is set to \""
                        + ReferenceModes.IMAGE_MEASUREMENT + "\".  The coordinate value is specified in pixel units.");

        parameters.get(X_POSITION_MEASUREMENT_OBJ_2).setDescription(
                "Measurement used as X-coordinate for the second end of the line.  This measurement will be taken from the current object.  The coordinate value is specified in pixel units.");

        parameters.get(Y_POSITION_MEASUREMENT_OBJ_2).setDescription(
                "Measurement used as Y-coordinate for the second end of the line.  This measurement will be taken from the current object.  The coordinate value is specified in pixel units.");

        parameters.get(LINE_WIDTH).setDescription("Width of the rendered lines.  Specified in pixel units.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple overlay elements simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}