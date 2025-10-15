package io.github.mianalysis.mia.module.objects.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Scaler;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.ScaleStack;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import ome.units.UNITS;

/**
* 
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ScaleObjectSet extends Module {

    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String X_AXIS_SEPARATOR = "X-axis scaling controls";
    public static final String X_SCALE_MODE = "Scale mode (x-axis)";
    public static final String X_RESOLUTION = "Resolution (x-axis)";
    public static final String X_IMAGE = "Image (x-axis)";
    public static final String X_OBJECTS = "Object set (x-axis)";
    public static final String X_ADOPT_CALIBRATION = "Adopt calibration (xy-axes)";
    public static final String X_SCALE_FACTOR = "Scale factor (x-axis)";

    public static final String Y_AXIS_SEPARATOR = "Y-axis scaling controls";
    public static final String XY_ANISOTROPY_WARNING = "Anisotropy warning";
    public static final String Y_SCALE_MODE = "Scale mode (y-axis)";
    public static final String Y_RESOLUTION = "Resolution (y-axis)";
    public static final String Y_IMAGE = "Image (y-axis)";
    public static final String Y_OBJECTS = "Object set (y-axis)";
    public static final String Y_SCALE_FACTOR = "Scale factor (y-axis)";

    public static final String Z_AXIS_SEPARATOR = "Z-axis scaling controls";
    public static final String Z_SCALE_MODE = "Scale mode (z-axis)";
    public static final String Z_RESOLUTION = "Resolution (z-axis)";
    public static final String Z_IMAGE = "Image (z-axis)";
    public static final String Z_OBJECTS = "Object set (z-axis)";
    public static final String Z_ADOPT_CALIBRATION = "Adopt calibration (z-axis)";
    public static final String Z_SCALE_FACTOR = "Scale factor (z-axis)";

    public interface ScaleModes extends ScaleStack.ScaleModes {
        String MATCH_OBJECTS = "Match object set";
        String[] ALL = new String[] { FIXED_RESOLUTION, MATCH_IMAGE, MATCH_OBJECTS, SCALE_FACTOR };

    }

    public ScaleObjectSet(Modules modules) {
        super("Scale object set", modules);
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Scales the entire object space and all objects within.  "
                + "This has the effect of uniformly scaling all objects.  "
                + "Scaled objects are related as children of their respective input objects.";
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_TRANSFORM;
    }

    @Override
    public Status process(WorkspaceI workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        String xScaleMode = parameters.getValue(X_SCALE_MODE, workspace);
        int xResolution = parameters.getValue(X_RESOLUTION, workspace);
        String xImageName = parameters.getValue(X_IMAGE, workspace);
        String xObjectsName = parameters.getValue(X_OBJECTS, workspace);
        boolean xAdoptCalibration = parameters.getValue(X_ADOPT_CALIBRATION, workspace);
        double xScaleFactor = parameters.getValue(X_SCALE_FACTOR, workspace);
        String yScaleMode = parameters.getValue(Y_SCALE_MODE, workspace);
        int yResolution = parameters.getValue(Y_RESOLUTION, workspace);
        String yImageName = parameters.getValue(Y_IMAGE, workspace);
        String yObjectsName = parameters.getValue(Y_OBJECTS, workspace);
        double yScaleFactor = parameters.getValue(Y_SCALE_FACTOR, workspace);
        String zScaleMode = parameters.getValue(Z_SCALE_MODE, workspace);
        int zResolution = parameters.getValue(Z_RESOLUTION, workspace);
        String zImageName = parameters.getValue(Z_IMAGE, workspace);
        String zObjectsName = parameters.getValue(Z_OBJECTS, workspace);
        boolean zAdoptCalibration = parameters.getValue(Z_ADOPT_CALIBRATION, workspace);
        double zScaleFactor = parameters.getValue(Z_SCALE_FACTOR, workspace);

        ObjsI inputObjects = workspace.getObjects(inputObjectsName);

        // Updating resolution values if fixed resolution not already provided
        switch (xScaleMode) {
            case ScaleModes.MATCH_IMAGE:
                xResolution = workspace.getImage(xImageName).getImagePlus().getWidth();
                break;
            case ScaleModes.MATCH_OBJECTS:
                xResolution = workspace.getObjects(xObjectsName).getWidth();
                break;
            case ScaleModes.SCALE_FACTOR:
                xResolution = (int) Math.round(inputObjects.getWidth() * xScaleFactor);
                break;
        }

        switch (yScaleMode) {
            case ScaleModes.MATCH_IMAGE:
                yResolution = workspace.getImage(yImageName).getImagePlus().getHeight();
                break;
            case ScaleModes.MATCH_OBJECTS:
                yResolution = workspace.getObjects(yObjectsName).getHeight();
                break;
            case ScaleModes.SCALE_FACTOR:
                yResolution = (int) Math.round(inputObjects.getHeight() * yScaleFactor);
                break;
        }

        switch (zScaleMode) {
            case ScaleModes.MATCH_IMAGE:
                zResolution = workspace.getImage(zImageName).getImagePlus().getNSlices();
                break;
            case ScaleModes.MATCH_OBJECTS:
                zResolution = workspace.getObjects(zObjectsName).getNSlices();
                break;
            case ScaleModes.SCALE_FACTOR:
                zResolution = (int) Math.round(inputObjects.getNSlices() * zScaleFactor);
                break;
        }

        double dppXYOut = inputObjects.getDppXY() * inputObjects.getWidth() / xResolution;
        double dppZOut = inputObjects.getDppZ() * inputObjects.getNSlices() / zResolution;
        String unitsOut = inputObjects.getSpatialUnits();

        // If setting a dimension to an image, there is an option to use that image's
        // calibration
        if (xScaleMode.equals(ScaleModes.MATCH_IMAGE) && xAdoptCalibration) {
            dppXYOut = workspace.getImage(xImageName).getImagePlus().getCalibration().pixelWidth;
            unitsOut = workspace.getImage(xImageName).getImagePlus().getCalibration().getXUnit();
        } else if (xScaleMode.equals(ScaleModes.MATCH_OBJECTS) && xAdoptCalibration) {
            dppXYOut = workspace.getObjects(xObjectsName).getDppXY();
            unitsOut = workspace.getObjects(xObjectsName).getSpatialUnits();
        }

        if (zScaleMode.equals(ScaleModes.MATCH_IMAGE) && zAdoptCalibration)
            dppZOut = workspace.getImage(zImageName).getImagePlus().getCalibration().pixelDepth;
        else if (zScaleMode.equals(ScaleModes.MATCH_OBJECTS) && zAdoptCalibration)
            dppZOut = workspace.getObjects(zObjectsName).getDppZ();

        ObjsI outputObjects = ObjsFactories.getDefaultFactory().createObjs(outputObjectsName, dppXYOut, dppZOut, unitsOut, xResolution, yResolution,
                zResolution, inputObjects.getNFrames(), inputObjects.getFrameInterval(),
                inputObjects.getTemporalUnit());

        for (ObjI inputObject : inputObjects.values()) {
            ImagePlus inputIpl = inputObject.getAsImage("Object image", true).getImagePlus();
            ImagePlus outputIpl = Scaler.resize(inputIpl, xResolution, yResolution, zResolution, "None");

            // Applying new calibration
            Calibration outputCal = outputIpl.getCalibration();
            outputCal.pixelWidth = dppXYOut;
            outputCal.pixelHeight = dppXYOut;
            outputCal.pixelDepth = dppZOut;
            outputCal.setUnit(unitsOut);
            outputCal.frameInterval = inputObjects.getFrameInterval();
            outputCal.fps = 1 / TemporalUnit.getOMEUnit().convertValue(inputObjects.getFrameInterval(), UNITS.SECOND);

            ImageI objectImage = ImageFactory.createImage("Object image", outputIpl);
            ObjsI tempObjects = objectImage.convertImageToObjects(inputObject.getCoordinateSetFactory(), outputObjectsName, true);

            if (tempObjects.size() == 0)
                continue;

            ObjI scaledObject = tempObjects.getFirst();
            scaledObject.setID(inputObject.getID());
            scaledObject.setT(inputObject.getT());
            scaledObject.addParent(inputObject);
            inputObject.addChild(scaledObject);

            outputObjects.add(scaledObject);

        }

        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput)
            outputObjects.convertToImageIDColours().showWithNormalisation(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(X_AXIS_SEPARATOR, this));
        parameters.add(new ChoiceP(X_SCALE_MODE, this, ScaleModes.SCALE_FACTOR, ScaleModes.ALL));
        parameters.add(new IntegerP(X_RESOLUTION, this, 1));
        parameters.add(new InputImageP(X_IMAGE, this));
        parameters.add(new InputObjectsP(X_OBJECTS, this));
        parameters.add(new BooleanP(X_ADOPT_CALIBRATION, this, false));
        parameters.add(new DoubleP(X_SCALE_FACTOR, this, 1.0));

        parameters.add(new SeparatorP(Y_AXIS_SEPARATOR, this));
        parameters.add(new MessageP(XY_ANISOTROPY_WARNING, this,
                "Output resolution potentially different in X and Y.  "
                        + "MIA uses a common spatial calibration in X and Y (taken from X-axis).  "
                        + "Calibrated results may be incorrect if Y axis is scaled differently from X.",
                ParameterState.WARNING));
        parameters.add(new ChoiceP(Y_SCALE_MODE, this, ScaleModes.SCALE_FACTOR, ScaleModes.ALL));
        parameters.add(new IntegerP(Y_RESOLUTION, this, 1));
        parameters.add(new InputImageP(Y_IMAGE, this));
        parameters.add(new InputObjectsP(Y_OBJECTS, this));
        parameters.add(new DoubleP(Y_SCALE_FACTOR, this, 1.0));

        parameters.add(new SeparatorP(Z_AXIS_SEPARATOR, this));
        parameters.add(new ChoiceP(Z_SCALE_MODE, this, ScaleModes.SCALE_FACTOR, ScaleModes.ALL));
        parameters.add(new IntegerP(Z_RESOLUTION, this, 1));
        parameters.add(new InputImageP(Z_IMAGE, this));
        parameters.add(new InputObjectsP(Z_OBJECTS, this));
        parameters.add(new BooleanP(Z_ADOPT_CALIBRATION, this, false));
        parameters.add(new DoubleP(Z_SCALE_FACTOR, this, 1.0));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(X_AXIS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(X_SCALE_MODE));
        switch ((String) parameters.getValue(X_SCALE_MODE, workspace)) {
            case ScaleModes.FIXED_RESOLUTION:
                returnedParameters.add(parameters.getParameter(X_RESOLUTION));
                break;
            case ScaleModes.MATCH_IMAGE:
                returnedParameters.add(parameters.getParameter(X_IMAGE));
                returnedParameters.add(parameters.getParameter(X_ADOPT_CALIBRATION));
                break;
            case ScaleModes.MATCH_OBJECTS:
                returnedParameters.add(parameters.getParameter(X_OBJECTS));
                returnedParameters.add(parameters.getParameter(X_ADOPT_CALIBRATION));
                break;
            case ScaleModes.SCALE_FACTOR:
                returnedParameters.add(parameters.getParameter(X_SCALE_FACTOR));
                break;
        }

        String xScaleMode = parameters.getValue(X_SCALE_MODE, workspace);
        int xResolution = parameters.getValue(X_RESOLUTION, workspace);
        String xImageName = parameters.getValue(X_IMAGE, workspace);
        String xObjectsName = parameters.getValue(X_OBJECTS, workspace);
        double xScaleFactor = parameters.getValue(X_SCALE_FACTOR, workspace);
        String yScaleMode = parameters.getValue(Y_SCALE_MODE, workspace);
        int yResolution = parameters.getValue(Y_RESOLUTION, workspace);
        String yImageName = parameters.getValue(Y_IMAGE, workspace);
        String yObjectsName = parameters.getValue(Y_OBJECTS, workspace);
        double yScaleFactor = parameters.getValue(Y_SCALE_FACTOR, workspace);

        boolean showAnisotropyWarning = false;
        switch (xScaleMode) {
            case ScaleModes.FIXED_RESOLUTION:
                switch (yScaleMode) {
                    case ScaleModes.FIXED_RESOLUTION:
                        showAnisotropyWarning = xResolution != yResolution;
                        break;
                    case ScaleModes.MATCH_IMAGE:
                        showAnisotropyWarning = true;
                        break;
                    case ScaleModes.MATCH_OBJECTS:
                        showAnisotropyWarning = true;
                        break;
                    case ScaleModes.SCALE_FACTOR:
                        showAnisotropyWarning = true;
                        break;
                }
                break;
            case ScaleModes.MATCH_IMAGE:
                switch (yScaleMode) {
                    case ScaleModes.FIXED_RESOLUTION:
                        showAnisotropyWarning = true;
                        break;
                    case ScaleModes.MATCH_IMAGE:
                        showAnisotropyWarning = !xImageName.equals(yImageName);
                        break;
                    case ScaleModes.MATCH_OBJECTS:
                        showAnisotropyWarning = true;
                        break;
                    case ScaleModes.SCALE_FACTOR:
                        showAnisotropyWarning = true;
                        break;
                }
                break;
            case ScaleModes.MATCH_OBJECTS:
                switch (yScaleMode) {
                    case ScaleModes.FIXED_RESOLUTION:
                        showAnisotropyWarning = true;
                        break;
                    case ScaleModes.MATCH_IMAGE:
                        showAnisotropyWarning = true;
                        break;
                    case ScaleModes.MATCH_OBJECTS:
                        showAnisotropyWarning = !xObjectsName.equals(yObjectsName);
                        break;
                    case ScaleModes.SCALE_FACTOR:
                        showAnisotropyWarning = true;
                        break;
                }
                break;
            case ScaleModes.SCALE_FACTOR:
                switch (yScaleMode) {
                    case ScaleModes.FIXED_RESOLUTION:
                        showAnisotropyWarning = true;
                        break;
                    case ScaleModes.MATCH_IMAGE:
                        showAnisotropyWarning = true;
                        break;
                    case ScaleModes.MATCH_OBJECTS:
                        showAnisotropyWarning = true;
                        break;
                    case ScaleModes.SCALE_FACTOR:
                        showAnisotropyWarning = xScaleFactor != yScaleFactor;
                        break;
                }
                break;
        }

        returnedParameters.add(parameters.getParameter(Y_AXIS_SEPARATOR));
        if (showAnisotropyWarning)
            returnedParameters.add(parameters.getParameter(XY_ANISOTROPY_WARNING));
        returnedParameters.add(parameters.getParameter(Y_SCALE_MODE));
        switch ((String) parameters.getValue(Y_SCALE_MODE, workspace)) {
            case ScaleModes.FIXED_RESOLUTION:
                returnedParameters.add(parameters.getParameter(Y_RESOLUTION));
                break;
            case ScaleModes.MATCH_IMAGE:
                returnedParameters.add(parameters.getParameter(Y_IMAGE));
                break;
            case ScaleModes.MATCH_OBJECTS:
                returnedParameters.add(parameters.getParameter(Y_OBJECTS));
                break;
            case ScaleModes.SCALE_FACTOR:
                returnedParameters.add(parameters.getParameter(Y_SCALE_FACTOR));
                break;
        }

        returnedParameters.add(parameters.getParameter(Z_AXIS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(Z_SCALE_MODE));
        switch ((String) parameters.getValue(Z_SCALE_MODE, workspace)) {
            case ScaleModes.FIXED_RESOLUTION:
                returnedParameters.add(parameters.getParameter(Z_RESOLUTION));
                break;
            case ScaleModes.MATCH_IMAGE:
                returnedParameters.add(parameters.getParameter(Z_IMAGE));
                returnedParameters.add(parameters.getParameter(Z_ADOPT_CALIBRATION));
                break;
            case ScaleModes.MATCH_OBJECTS:
                returnedParameters.add(parameters.getParameter(Z_OBJECTS));
                returnedParameters.add(parameters.getParameter(Z_ADOPT_CALIBRATION));
                break;
            case ScaleModes.SCALE_FACTOR:
                returnedParameters.add(parameters.getParameter(Z_SCALE_FACTOR));
                break;
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
        Workspace workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        returnedRelationships.add(parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS, workspace),
                parameters.getValue(OUTPUT_OBJECTS, workspace)));

        return returnedRelationships;

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
        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects to be scaled.  These are related as a parent of their respective scaled object.");

        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Output scaled objects to be stored in the workspace.  These are related as children of the respective input object.");

        parameters.get(X_SCALE_MODE)
                .setDescription("Controls how the output x-axis resolution is calculated:<br><ul>"

                        + "<li>\"" + ScaleModes.FIXED_RESOLUTION
                        + "\" The output object set will have the specific resolution defined by the \"" + X_RESOLUTION
                        + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.MATCH_IMAGE
                        + "\" The output object set will have the same resolution as the image specified by the \""
                        + X_IMAGE + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.MATCH_OBJECTS
                        + "\" The output object set will have the same resolution as the object set specified by the \""
                        + X_OBJECTS + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.SCALE_FACTOR
                        + "\" The output object set will have an equal resolution to the input size multiplied by the scaling factor, \""
                        + X_SCALE_FACTOR
                        + "\".  The output resolution will be rounded to the closest whole number.</li></ul>");

        parameters.get(X_RESOLUTION).setDescription("If \"" + X_SCALE_MODE + "\" is set to \""
                + ScaleModes.FIXED_RESOLUTION + "\", this is the size of the output object set.");

        parameters.get(X_IMAGE).setDescription("If \"" + X_SCALE_MODE + "\" is set to \"" + ScaleModes.MATCH_IMAGE
                + "\", the output object set will have the same size as this image.");

        parameters.get(X_ADOPT_CALIBRATION).setDescription("If \"" + X_SCALE_MODE + "\" is set to \""
                + ScaleModes.MATCH_IMAGE + "\" or \"" + ScaleModes.MATCH_OBJECTS + "\", and this is "
                + "selected, the output object set's x and y axis calibration will be copied from the input image's x-axis (MIA uses the same value for both axes).");

        parameters.get(X_SCALE_FACTOR).setDescription("If \"" + X_SCALE_MODE + "\" is set to \""
                + ScaleModes.SCALE_FACTOR
                + "\", the output object set will have a size equal to the input resolution multiplied by this scale factor.  "
                + "The applied size will be rounded to the closest whole number");

        parameters.get(Y_SCALE_MODE)
                .setDescription("Controls how the output y-axis resolution is calculated:<br><ul>"

                        + "<li>\"" + ScaleModes.FIXED_RESOLUTION
                        + "\" The output object set will have the specific resolution defined by the \"" + Y_RESOLUTION
                        + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.MATCH_IMAGE
                        + "\" The output object set will have the same resolution as the image specified by the \""
                        + Y_IMAGE + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.MATCH_OBJECTS
                        + "\" The output object set will have the same resolution as the object set specified by the \""
                        + Y_OBJECTS + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.SCALE_FACTOR
                        + "\" The output object set will have an equal resolution to the input size multiplied by the scaling factor, \""
                        + Y_SCALE_FACTOR
                        + "\".  The output resolution will be rounded to the closest whole number.</li></ul>");

        parameters.get(Y_RESOLUTION).setDescription("If \"" + Y_SCALE_MODE + "\" is set to \""
                + ScaleModes.FIXED_RESOLUTION + "\", this is the size in the output object set.");

        parameters.get(Y_IMAGE).setDescription("If \"" + Y_SCALE_MODE + "\" is set to \"" + ScaleModes.MATCH_IMAGE
                + "\", the output object set will have the same size as this image.");

        parameters.get(Y_SCALE_FACTOR).setDescription("If \"" + Y_SCALE_MODE + "\" is set to \""
                + ScaleModes.SCALE_FACTOR
                + "\", the output object set will have a size equal to the input resolution multiplied by this scale factor.  The applied size will be rounded to the closest whole number");

        parameters.get(Z_SCALE_MODE)
                .setDescription("Controls how the output z-axis resolution (number of slices) is calculated:<br><ul>"

                        + "<li>\"" + ScaleModes.FIXED_RESOLUTION
                        + "\" The output object set will have the specific number of slices defined by the \""
                        + Z_RESOLUTION
                        + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.MATCH_IMAGE
                        + "\" The output object set will have the same number of slices as the image specified by the \""
                        + Z_IMAGE + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.MATCH_OBJECTS
                        + "\" The output object set will have the same resolution as the object set specified by the \""
                        + Z_OBJECTS + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.SCALE_FACTOR
                        + "\" The output object set will have an equal number of slices to the input number of slices multiplied by the scaling factor, \""
                        + Z_SCALE_FACTOR
                        + "\".  The output number of slices will be rounded to the closest whole number.</li></ul>");

        parameters.get(Z_RESOLUTION).setDescription("If \"" + Z_SCALE_MODE + "\" is set to \""
                + ScaleModes.FIXED_RESOLUTION + "\", this is the number of slices in the output object set.");

        parameters.get(Z_IMAGE).setDescription("If \"" + Z_SCALE_MODE + "\" is set to \"" + ScaleModes.MATCH_IMAGE
                + "\", the output object set will have the same number of slices as there are in this image.");

        parameters.get(Z_ADOPT_CALIBRATION).setDescription("If \"" + Z_SCALE_MODE + "\" is set to \""
                + ScaleModes.MATCH_IMAGE + "\" or \"" + ScaleModes.MATCH_OBJECTS + "\", and this is selected, "
                + "the output object set's z-axis calibration will be copied from the input image.");

        parameters.get(Z_SCALE_FACTOR).setDescription("If \"" + Z_SCALE_MODE + "\" is set to \""
                + ScaleModes.SCALE_FACTOR
                + "\", the output object set will have a number of slices equal to the input number of slices multiplied by this scale factor.  "
                + "The applied number of slices will be rounded to the closest whole number");

    }
}
