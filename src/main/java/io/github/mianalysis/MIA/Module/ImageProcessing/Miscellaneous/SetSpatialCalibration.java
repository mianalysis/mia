package io.github.mianalysis.MIA.Module.ImageProcessing.Miscellaneous;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ij.ImagePlus;
import ij.measure.Calibration;
import io.github.mianalysis.MIA.MIA;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Object.Image;
import io.github.mianalysis.MIA.Object.Measurement;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.BooleanP;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceP;
import io.github.mianalysis.MIA.Object.Parameters.ImageMeasurementP;
import io.github.mianalysis.MIA.Object.Parameters.InputImageP;
import io.github.mianalysis.MIA.Object.Parameters.InputObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.ObjectMeasurementP;
import io.github.mianalysis.MIA.Object.Parameters.ParameterCollection;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.Text.DoubleP;
import io.github.mianalysis.MIA.Object.References.ImageMeasurementRef;
import io.github.mianalysis.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.MetadataRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ParentChildRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.PartnerRefCollection;
import io.github.mianalysis.MIA.Object.Units.SpatialUnit;

public class SetSpatialCalibration extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";

    public static final String CALIBRATION_SEPARATOR = "Calibration controls";
    public static final String AXIS_MODE = "Axis mode";

    public static final String PD_SEPARATOR = "Physical distance controls";
    public static final String PD_SOURCE = "Physical distance (PD) source";
    public static final String OBJECTS_FOR_PD = "Objects for measurement (PD)";
    public static final String OBJECTS_MEASURUREMENT_FOR_PD = "Objects measurement (PD)";
    public static final String FIXED_VALUE_FOR_PD = "Fixed value (PD)";
    public static final String DISPLAY_IMAGE_PD = "Display image (PD)";
    public static final String STORE_DISTANCE_AS_MEASUREMENT_PD = "Store distance as measurement (PD)";
    public static final String IMAGE_FOR_PD = "Image for measurement (PD)";
    public static final String IMAGE_MEASUREMENT_FOR_PD = "Image measurement (PD)";

    public static final String ID_SEPARATOR = "Image distance controls";
    public static final String ID_SOURCE = "Image distance (ID) source";
    public static final String OBJECTS_FOR_ID = "Objects for measurement (ID)";
    public static final String OBJECTS_MEASURUREMENT_FOR_ID = "Objects measurement (ID)";
    public static final String FIXED_VALUE_FOR_ID = "Fixed value (ID)";
    public static final String DISPLAY_IMAGE_ID = "Display image (ID)";
    public static final String STORE_DISTANCE_AS_MEASUREMENT_ID = "Store distance as measurement (ID)";
    public static final String IMAGE_FOR_ID = "Image for measurement (ID)";
    public static final String IMAGE_MEASUREMENT_FOR_ID = "Image measurement (ID)";

    public interface AxisModes {
        String XY = "XY";
        String XYZ = "XY and Z";
        String Z = "Z";

        String[] ALL = new String[] { XY, XYZ, Z };

    }

    public interface DistanceSources {
        String FIRST_OBJECT_MEASUREMENT = "First object measurement";
        String FIXED_VALUE = "Fixed value";
        String GUI_SELECTION_TEXT = "GUI selection (text box)";
        // String GUI_SELECTION_DRAW = "GUI selection (drawing)";
        String IMAGE_MEASUREMENT = "Image measurement";

        String[] ALL = new String[] { FIRST_OBJECT_MEASUREMENT, GUI_SELECTION_TEXT, FIXED_VALUE, IMAGE_MEASUREMENT };

    }

    public interface Measurements {
        String PD_DISTANCE = "SPATIAL_CALIBRATION // PHYSICAL_DISTANCE_{$CAL}";
        String ID_DISTANCE = "SPATIAL_CALIBRATION // IMAGE_DISTANCE_{$CAL}";

    }

    public SetSpatialCalibration(ModuleCollection modules) {
        super("Set spatial calibration", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "Update spatial calibration for XY and Z axes based on defined physical and corresponding image (pixel) distances.  Both physical and image distances can be drawn from a variety of sources including image and object measurements, fixed values and user-defined values (specified at runtime).  Calibration can be applied to XY and Z axes simultaneously or independently.";
    }

    public static double getFirstObjectMeasurement(Workspace workspace, String objectsName, String measurementName) {
        Obj firstObj = workspace.getObjectSet(objectsName).getFirst();
        if (firstObj == null) {
            MIA.log.writeWarning("No object to provide distance.  Setting calibration to NaN.");
            return Double.NaN;
        }

        Measurement meas = firstObj.getMeasurement(measurementName);
        if (meas == null) {
            MIA.log.writeWarning("No object measurement to provide distance.  Setting calibration to NaN.");
            return Double.NaN;
        }

        return meas.getValue();

    }

    public static double getGUITextValue(Image image, boolean showImage, boolean storeMeasurement, String message) {
        ImagePlus dispIpl = null;
        if (showImage) {
            dispIpl = image.getImagePlus().duplicate();
            dispIpl.show();
        }

        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(false);
        String valueString = (String) JOptionPane.showInputDialog(frame, message, "", JOptionPane.PLAIN_MESSAGE, null,
                null, 1d);

        if (showImage && dispIpl != null)
            dispIpl.close();

        return Double.parseDouble(valueString);

    }

    public static double getImageMeasurement(Workspace workspace, String imageName, String measurementName) {
        Image image = workspace.getImage(imageName);
        if (image == null) {
            MIA.log.writeWarning("No image to provide distance.  Setting calibration to NaN.");
            return Double.NaN;
        }

        Measurement meas = image.getMeasurement(measurementName);
        if (meas == null) {
            MIA.log.writeWarning("No image measurement to provide distance.  Setting calibration to NaN.");
            return Double.NaN;
        }

        return meas.getValue();

    }

    @Override
    protected Status process(Workspace workspace) {
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        String pdSource = parameters.getValue(PD_SOURCE);
        String axisMode = parameters.getValue(AXIS_MODE);
        String pdObjectsName = parameters.getValue(OBJECTS_FOR_PD);
        String pdObjectsMeasName = parameters.getValue(OBJECTS_MEASURUREMENT_FOR_PD);
        double pdFixedValue = parameters.getValue(FIXED_VALUE_FOR_PD);
        boolean pdShowImage = parameters.getValue(DISPLAY_IMAGE_PD);
        boolean pdStoreMeasurement = parameters.getValue(STORE_DISTANCE_AS_MEASUREMENT_PD);
        String pdImageName = parameters.getValue(IMAGE_FOR_PD);
        String pdImageMeasName = parameters.getValue(IMAGE_MEASUREMENT_FOR_PD);

        String idSource = parameters.getValue(ID_SOURCE);
        String idObjectsName = parameters.getValue(OBJECTS_FOR_ID);
        String idObjectsMeasName = parameters.getValue(OBJECTS_MEASURUREMENT_FOR_ID);
        double idFixedValue = parameters.getValue(FIXED_VALUE_FOR_ID);
        boolean idShowImage = parameters.getValue(DISPLAY_IMAGE_ID);
        boolean idStoreMeasurement = parameters.getValue(STORE_DISTANCE_AS_MEASUREMENT_ID);
        String idImageName = parameters.getValue(IMAGE_FOR_ID);
        String idImageMeasName = parameters.getValue(IMAGE_MEASUREMENT_FOR_ID);

        Image image = workspace.getImage(inputImageName);

        // If not using a fixed value, updating the fixed value with the correct value
        switch (pdSource) {
            case DistanceSources.FIRST_OBJECT_MEASUREMENT:
                pdFixedValue = getFirstObjectMeasurement(workspace, pdObjectsName, pdObjectsMeasName);
                break;
            case DistanceSources.GUI_SELECTION_TEXT:
                pdFixedValue = getGUITextValue(image, pdShowImage, pdStoreMeasurement,
                        "Enter physical distance value (" + SpatialUnit.getOMEUnit().getSymbol() + ")");
                break;
            case DistanceSources.IMAGE_MEASUREMENT:
                pdFixedValue = getImageMeasurement(workspace, pdImageName, pdImageMeasName);
                break;
        }

        switch (idSource) {
            case DistanceSources.FIRST_OBJECT_MEASUREMENT:
                idFixedValue = getFirstObjectMeasurement(workspace, idObjectsName, idObjectsMeasName);
                break;
            case DistanceSources.GUI_SELECTION_TEXT:
                idFixedValue = getGUITextValue(image, idShowImage, idStoreMeasurement,
                        "Enter image distance value (" + SpatialUnit.getOMEUnit().getSymbol() + ")");
                break;
            case DistanceSources.IMAGE_MEASUREMENT:
                idFixedValue = getImageMeasurement(workspace, idImageName, idImageMeasName);
                break;
        }

        // Setting image spatial calibration
        double distPerPx = pdFixedValue / idFixedValue;
        ImagePlus ipl = image.getImagePlus();
        Calibration cal = ipl.getCalibration();

        switch (axisMode) {
            case AxisModes.XY:
                cal.pixelHeight = distPerPx;
                cal.pixelWidth = distPerPx;
                break;
            case AxisModes.XYZ:
                cal.pixelHeight = distPerPx;
                cal.pixelWidth = distPerPx;
                cal.pixelDepth = distPerPx;
                break;
            case AxisModes.Z:
                cal.pixelDepth = distPerPx;
                break;
        }

        if (showOutput)
            image.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(CALIBRATION_SEPARATOR, this));
        parameters.add(new ChoiceP(AXIS_MODE, this, AxisModes.XY, AxisModes.ALL));

        parameters.add(new SeparatorP(PD_SEPARATOR, this));
        parameters.add(new ChoiceP(PD_SOURCE, this, DistanceSources.FIXED_VALUE, DistanceSources.ALL));
        parameters.add(new InputObjectsP(OBJECTS_FOR_PD, this));
        parameters.add(new ObjectMeasurementP(OBJECTS_MEASURUREMENT_FOR_PD, this));
        parameters.add(new DoubleP(FIXED_VALUE_FOR_PD, this, 1d));
        parameters.add(new BooleanP(DISPLAY_IMAGE_PD, this, true));
        parameters.add(new BooleanP(STORE_DISTANCE_AS_MEASUREMENT_PD, this, true));
        parameters.add(new InputImageP(IMAGE_FOR_PD, this));
        parameters.add(new ImageMeasurementP(IMAGE_MEASUREMENT_FOR_PD, this));

        parameters.add(new SeparatorP(ID_SEPARATOR, this));
        parameters.add(new ChoiceP(ID_SOURCE, this, DistanceSources.FIXED_VALUE, DistanceSources.ALL));
        parameters.add(new InputObjectsP(OBJECTS_FOR_ID, this));
        parameters.add(new ObjectMeasurementP(OBJECTS_MEASURUREMENT_FOR_ID, this));
        parameters.add(new DoubleP(FIXED_VALUE_FOR_ID, this, 1d));
        parameters.add(new BooleanP(DISPLAY_IMAGE_ID, this, true));
        parameters.add(new BooleanP(STORE_DISTANCE_AS_MEASUREMENT_ID, this, true));
        parameters.add(new InputImageP(IMAGE_FOR_ID, this));
        parameters.add(new ImageMeasurementP(IMAGE_MEASUREMENT_FOR_ID, this));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParams = new ParameterCollection();

        returnedParams.add(parameters.get(INPUT_SEPARATOR));
        returnedParams.add(parameters.get(INPUT_IMAGE));

        returnedParams.add(parameters.get(CALIBRATION_SEPARATOR));
        returnedParams.add(parameters.get(AXIS_MODE));

        returnedParams.add(parameters.get(PD_SEPARATOR));
        returnedParams.add(parameters.get(PD_SOURCE));
        switch ((String) parameters.getValue(PD_SOURCE)) {
            case DistanceSources.FIRST_OBJECT_MEASUREMENT:
                returnedParams.add(parameters.get(OBJECTS_FOR_PD));
                returnedParams.add(parameters.get(OBJECTS_MEASURUREMENT_FOR_PD));
                String objectsName = parameters.getValue(OBJECTS_FOR_PD);
                ObjectMeasurementP objectMeasurement = (ObjectMeasurementP) parameters
                        .get(OBJECTS_MEASURUREMENT_FOR_PD);
                objectMeasurement.setObjectName(objectsName);

                break;

            case DistanceSources.FIXED_VALUE:
                returnedParams.add(parameters.get(FIXED_VALUE_FOR_PD));
                break;

            case DistanceSources.GUI_SELECTION_TEXT:
                returnedParams.add(parameters.get(DISPLAY_IMAGE_PD));
                returnedParams.add(parameters.get(STORE_DISTANCE_AS_MEASUREMENT_PD));
                break;

            case DistanceSources.IMAGE_MEASUREMENT:
                returnedParams.add(parameters.get(IMAGE_FOR_PD));
                returnedParams.add(parameters.get(IMAGE_MEASUREMENT_FOR_PD));
                String imageName = parameters.getValue(IMAGE_FOR_PD);
                ImageMeasurementP imageMeasurement = (ImageMeasurementP) parameters.get(IMAGE_MEASUREMENT_FOR_PD);
                imageMeasurement.setImageName(imageName);
                break;
        }

        returnedParams.add(parameters.get(ID_SEPARATOR));
        returnedParams.add(parameters.get(ID_SOURCE));
        switch ((String) parameters.getValue(ID_SOURCE)) {
            case DistanceSources.FIRST_OBJECT_MEASUREMENT:
                returnedParams.add(parameters.get(OBJECTS_FOR_ID));
                returnedParams.add(parameters.get(OBJECTS_MEASURUREMENT_FOR_ID));
                String objectsName = parameters.getValue(OBJECTS_FOR_ID);
                ObjectMeasurementP objectMeasurement = (ObjectMeasurementP) parameters
                        .get(OBJECTS_MEASURUREMENT_FOR_ID);
                objectMeasurement.setObjectName(objectsName);
                break;

            case DistanceSources.FIXED_VALUE:
                returnedParams.add(parameters.get(FIXED_VALUE_FOR_ID));
                break;

            case DistanceSources.GUI_SELECTION_TEXT:
                returnedParams.add(parameters.get(DISPLAY_IMAGE_ID));
                returnedParams.add(parameters.get(STORE_DISTANCE_AS_MEASUREMENT_ID));
                break;

            case DistanceSources.IMAGE_MEASUREMENT:
                returnedParams.add(parameters.get(IMAGE_FOR_ID));
                returnedParams.add(parameters.get(IMAGE_MEASUREMENT_FOR_ID));
                String imageName = parameters.getValue(IMAGE_FOR_ID);
                ImageMeasurementP imageMeasurement = (ImageMeasurementP) parameters.get(IMAGE_MEASUREMENT_FOR_ID);
                imageMeasurement.setImageName(imageName);
                break;
        }

        return returnedParams;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

        if (parameters.getValue(PD_SOURCE).equals(DistanceSources.GUI_SELECTION_TEXT)
                && (boolean) parameters.getValue(STORE_DISTANCE_AS_MEASUREMENT_PD)) {
            ImageMeasurementRef ref = imageMeasurementRefs.getOrPut(Measurements.PD_DISTANCE);
            ref.setImageName(parameters.getValue(INPUT_IMAGE));
            returnedRefs.add(ref);
        }

        if (parameters.getValue(ID_SOURCE).equals(DistanceSources.GUI_SELECTION_TEXT)
                && (boolean) parameters.getValue(STORE_DISTANCE_AS_MEASUREMENT_ID)) {
            ImageMeasurementRef ref = imageMeasurementRefs.getOrPut(Measurements.ID_DISTANCE);
            ref.setImageName(parameters.getValue(INPUT_IMAGE));
            returnedRefs.add(ref);
        }

        return returnedRefs;

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

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE)
                .setDescription("Image to which spatial calibration specified in this module will be applied.");

        parameters.get(AXIS_MODE).setDescription(
                "Controls to which image axes the spatial calibration specified in this module will applied:<br><ul>" +

                        "<li>\"" + AxisModes.XY
                        + "\" Calibration will be applied to X and Y axes only.  Z axis will retain its existing calibration.</li>"
                        +

                        "<li>\"" + AxisModes.XYZ + "\" Calibration will be applied equally to XY and Z axes.</li>" +

                        "<li>\"" + AxisModes.Z
                        + "\" Calibration will be applied to Z axis only.  X and Y axes will retain their existing calibrations.</li></ul>");

        parameters.get(PD_SOURCE).setDescription("Source for physical distance measurement:<br><ul>" +

                "<li>\"" + DistanceSources.FIRST_OBJECT_MEASUREMENT
                + "\" Distance is taken as the measurement (specified by \"" + OBJECTS_MEASURUREMENT_FOR_PD
                + "\") for the first object in the collection \"" + OBJECTS_FOR_PD
                + "\".  The first object is taken since only one value can be used.</li>" +

                "<li>\"" + DistanceSources.FIXED_VALUE + "\" Distance is the fixed value specified by the \""
                + FIXED_VALUE_FOR_PD + "\" parameter.</li>" +

                "<li>\"" + DistanceSources.GUI_SELECTION_TEXT
                + "\" At runtime, the user is presented with a text entry dialog box, into which they can type the physical distance measurement.</li>"
                +

                "<li>\"" + DistanceSources.IMAGE_MEASUREMENT
                + "\" Distance is taken as the measurement (specified by \"" + IMAGE_MEASUREMENT_FOR_PD
                + "\") for the image \"" + IMAGE_FOR_PD + "\".</li></ul>");

        parameters.get(OBJECTS_FOR_PD).setDescription("If \"" + PD_SOURCE + "\" is set to \""
                + DistanceSources.FIRST_OBJECT_MEASUREMENT
                + "\", this is the object collection from which the first measurement will be taken as the physical distance.");

        parameters.get(OBJECTS_MEASURUREMENT_FOR_PD)
                .setDescription("If \"" + PD_SOURCE + "\" is set to \"" + DistanceSources.FIRST_OBJECT_MEASUREMENT
                        + "\", this is the measurement for the first object in the collection, \"" + OBJECTS_FOR_PD
                        + "\", that willbe used as the physical distance.");

        parameters.get(FIXED_VALUE_FOR_PD)
                .setDescription("If \"" + PD_SOURCE + "\" is set to \"" + DistanceSources.FIXED_VALUE
                        + "\", this is the fixed value that will be used as the physical distance.");

        parameters.get(DISPLAY_IMAGE_PD).setDescription("If \"" + PD_SOURCE + "\" is set to \""
                + DistanceSources.GUI_SELECTION_TEXT
                + "\", this image will be displayed to the user at the same time as the dialog box allowing the physical distance to be specified.");

        parameters.get(STORE_DISTANCE_AS_MEASUREMENT_PD).setDescription("When selected (and if \"" + PD_SOURCE
                + "\" is set to \"" + DistanceSources.GUI_SELECTION_TEXT
                + "\"), the user-specified physical distance value will be added to the input image as a measurement.  This only applies for GUI-based distance specifications as the distance isn't recorded elsewhere (e.g. as an existing image or object measurement, or as a hard-coded parameter).");

        parameters.get(IMAGE_FOR_PD)
                .setDescription("If \"" + PD_SOURCE + "\" is set to \"" + DistanceSources.IMAGE_MEASUREMENT
                        + "\", this is the image from which the measurement will be taken as the physical distance.");

        parameters.get(IMAGE_MEASUREMENT_FOR_PD)
                .setDescription("If \"" + PD_SOURCE + "\" is set to \"" + DistanceSources.IMAGE_MEASUREMENT
                        + "\", this is the measurement for the image, \"" + IMAGE_FOR_PD
                        + "\", that willbe used as the physical distance.");

        parameters.get(ID_SOURCE).setDescription("Source for image (pixel) distance measurement:<br><ul>" +

                "<li>\"" + DistanceSources.FIRST_OBJECT_MEASUREMENT
                + "\" Distance is taken as the measurement (specified by \"" + OBJECTS_MEASURUREMENT_FOR_ID
                + "\") for the first object in the collection \"" + OBJECTS_FOR_ID
                + "\".  The first object is taken since only one value can be used.</li>" +

                "<li>\"" + DistanceSources.FIXED_VALUE + "\" Distance is the fixed value specified by the \""
                + FIXED_VALUE_FOR_ID + "\" parameter.</li>" +

                "<li>\"" + DistanceSources.GUI_SELECTION_TEXT
                + "\" At runtime, the user is presented with a text entry dialog box, into which they can type the image (pixel) distance measurement.</li>"
                +

                "<li>\"" + DistanceSources.IMAGE_MEASUREMENT
                + "\" Distance is taken as the measurement (specified by \"" + IMAGE_MEASUREMENT_FOR_ID
                + "\") for the image \"" + IMAGE_FOR_ID + "\".</li></ul>");

        parameters.get(OBJECTS_FOR_ID).setDescription("If \"" + ID_SOURCE + "\" is set to \""
                + DistanceSources.FIRST_OBJECT_MEASUREMENT
                + "\", this is the object collection from which the first measurement will be taken as the image (pixel) distance.");

        parameters.get(OBJECTS_MEASURUREMENT_FOR_ID)
                .setDescription("If \"" + ID_SOURCE + "\" is set to \"" + DistanceSources.FIRST_OBJECT_MEASUREMENT
                        + "\", this is the measurement for the first object in the collection, \"" + OBJECTS_FOR_ID
                        + "\", that willbe used as the image (pixel) distance.");

        parameters.get(FIXED_VALUE_FOR_ID)
                .setDescription("If \"" + ID_SOURCE + "\" is set to \"" + DistanceSources.FIXED_VALUE
                        + "\", this is the fixed value that will be used as the image (pixel) distance.");

        parameters.get(DISPLAY_IMAGE_ID).setDescription("If \"" + ID_SOURCE + "\" is set to \""
                + DistanceSources.GUI_SELECTION_TEXT
                + "\", this image will be displayed to the user at the same time as the dialog box allowing the image (pixel) distance to be specified.");

        parameters.get(STORE_DISTANCE_AS_MEASUREMENT_ID).setDescription("When selected (and if \"" + ID_SOURCE
                + "\" is set to \"" + DistanceSources.GUI_SELECTION_TEXT
                + "\"), the user-specified image (pixel) distance value will be added to the input image as a measurement.  This only applies for GUI-based distance specifications as the distance isn't recorded elsewhere (e.g. as an existing image or object measurement, or as a hard-coded parameter).");

        parameters.get(IMAGE_FOR_ID).setDescription("If \"" + ID_SOURCE + "\" is set to \""
                + DistanceSources.IMAGE_MEASUREMENT
                + "\", this is the image from which the measurement will be taken as the image (pixel) distance.");

        parameters.get(IMAGE_MEASUREMENT_FOR_ID)
                .setDescription("If \"" + ID_SOURCE + "\" is set to \"" + DistanceSources.IMAGE_MEASUREMENT
                        + "\", this is the measurement for the image, \"" + IMAGE_FOR_ID
                        + "\", that willbe used as the image (pixel) distance.");

    }
}
