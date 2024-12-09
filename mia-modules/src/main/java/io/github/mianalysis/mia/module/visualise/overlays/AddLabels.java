package io.github.mianalysis.mia.module.visualise.overlays;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.TextRoi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.binary.BinaryOperations2D;
import io.github.mianalysis.mia.module.images.process.binary.DistanceMap;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.ObjectMetadataP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.PartnerObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.LabelFactory;

/**
 * Adds an overlay to the specified input image with each object represented by
 * a text label. The label can include information such as measurements,
 * associated object counts or ID numbers.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class AddLabels extends AbstractOverlay {
    TextRoi textRoi = null;

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image and object input";

    /**
     * Image onto which overlay will be rendered. Input image will only be updated
     * if "Apply to input image" is enabled, otherwise the image containing the
     * overlay will be stored as a new image with name specified by "Output image".
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * Objects to represent as overlays.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
    * 
    */
    public static final String OUTPUT_SEPARATOR = "Image output";

    /**
     * Determines if the modifications made to the input image (added overlay
     * elements) will be applied to that image or directed to a new image. When
     * selected, the input image will be updated.
     */
    public static final String APPLY_TO_INPUT = "Apply to input image";

    /**
     * If the modifications (overlay) aren't being applied directly to the input
     * image, this control will determine if a separate image containing the overlay
     * should be saved to the workspace.
     */
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";

    /**
     * The name of the new image to be saved to the workspace (if not applying the
     * changes directly to the input image).
     */
    public static final String OUTPUT_IMAGE = "Output image";

    /**
    * 
    */
    public static final String CONTENT_SEPARATOR = "Overlay content";

    /**
     * Controls what information each label displays:<br>
     * <ul>
     * <li>"Child count" The number of children from a specific object collection
     * for each object. The children to summarise are selected using the "Child
     * objects for label" parameter.</li>
     * <li>"ID" The ID number of the object.</li>
     * <li>"Measurement value" A measurement associated with the object. The
     * measurement is selected using the "Measurement for label" parameter.</li>
     * <li>"Parent ID" The ID number of a parent of the object. The parent object is
     * selected using the "Parent object for label" parameter.</li>
     * <li>"Parent measurement value" A measurement associated with a parent of the
     * object. The measurement is selected using the "Measurement for label"
     * parameter and the parent object with the "Parent object for label"
     * parameter.</li>
     * <li>"Partner count" The number of partners from a specific object collection
     * for each object. The partners to summarise are selected using the "Partner
     * objects for label" parameter.</li>
     * </ul>
     */
    public static final String LABEL_MODE = "Label mode";

    /**
     * Number of decimal places to use when displaying numeric values.
     */
    public static final String DECIMAL_PLACES = "Decimal places";

    /**
     * When enabled, numeric values will be displayed in the format <i>1.23E-3</i>.
     * Otherwise, the same value would appear as <i>0.00123</i>.
     */
    public static final String USE_SCIENTIFIC = "Use scientific notation";

    /**
     * Font size of the text label.
     */
    public static final String LABEL_SIZE = "Label size";

    /**
     * Offset the label by this number of pixels horizontally (along the x-axis).
     * Increasingly positive numbers move the label right.
     */
    public static final String X_OFFSET = "X-offset";

    /**
     * Offset the label by this number of pixels vertically (along the y-axis).
     * Increasingly positive numbers move the label down.
     */
    public static final String Y_OFFSET = "Y-offset";

    /**
    * 
    */
    public static final String CENTRE_TEXT = "Centre text";

    /**
     * If "Label mode" is set to "Child count", these are the child objects which
     * will be counted and displayed on the label. These objects will be children of
     * the input objects.
     */
    public static final String CHILD_OBJECTS_FOR_LABEL = "Child objects for label";

    /**
     * If "Label mode" is set to either "Parent ID" or "Parent measurement value",
     * these are the parent objects which will be used. These objects will be
     * parents of the input objects.
     */
    public static final String PARENT_OBJECT_FOR_LABEL = "Parent object for label";

    /**
     * If "Label mode" is set to "Partner count", these are the partner objects
     * which will be counted and displayed on the label. These objects will be
     * partners of the input objects.
     */
    public static final String PARTNER_OBJECTS_FOR_LABEL = "Partner objects for label";

    /**
     * If "Label mode" is set to either "Measurement value" or "Parent measurement
     * value", these are the measurements which will be used.
     */
    public static final String MEASUREMENT_FOR_LABEL = "Measurement for label";

    public static final String OBJECT_METADATA_FOR_LABEL = "Metadata item for label";

    /**
     * Text to display at beginning of every label
     */
    public static final String PREFIX = "Prefix";

    /**
     * Text to display at end of every label
     */
    public static final String SUFFIX = "Suffix";

    /**
    * 
    */
    public static final String LOCATION_SEPARATOR = "Overlay location";

    /**
     * Determines the method used for placing the label overlay for each object:<br>
     * <ul>
     * <li>"Centre of object" Labels will be placed at the centroid (average
     * coordinate location) of each object. This position won't necessarily coincide
     * with a region corresponding to that object. For example, the centroid of a
     * crescent shape won't lie on the crescent itself.</li>
     * <li>"Fixed value" Labels will be placed at the x,y,z location specified by
     * the user.</li>
     * <li>"Inside largest part of object" Labels will be placed coincident with the
     * largest region of each object. This ensures the label is placed directly over
     * the relevant object.</li>
     * <li>"Object measurement" Labels will be placed coincident with the specified
     * measurements for each object.</li>
     * </ul>
     */
    public static final String LABEL_POSITION = "Label position";

    /**
     * Object measurement specifying the X-position of the label. Measurement value
     * must be specified in pixel units.
     */
    public static final String X_POSITION_MEASUREMENT = "X-position measurement";

    /**
     * Object measurement specifying the Y-position of the label. Measurement value
     * must be specified in pixel units.
     */
    public static final String Y_POSITION_MEASUREMENT = "Y-position measurement";

    /**
     * Object measurement specifying the Z-position (slice) of the label.
     * Measurement value must be specified in slice units.
     */
    public static final String Z_POSITION_MEASUREMENT = "Z-position measurement";

    /**
     * Fixed value specifying the X-position of the label. Specified in pixel units.
     */
    public static final String X_POSITION = "X-position";

    /**
     * Fixed value specifying the Y-position of the label. Specified in pixel units.
     */
    public static final String Y_POSITION = "Y-position";

    /**
     * Fixed value specifying the Z-position of the label. Specified in pixel units.
     */
    public static final String Z_POSITION = "Z-position";

    /**
     * Display overlay elements in all slices corresponding to that object.
     */
    public static final String RENDER_IN_ALL_OBJECT_SLICES = "Render in all object slices";

    /**
     * Display overlay elements in all frames, irrespective of whether each object
     * is present in that frame.
     */
    public static final String RENDER_IN_ALL_FRAMES = "Render in all frames";

    /**
    * 
    */
    public static final String EXECUTION_SEPARATOR = "Execution controls";

    /**
     * Process multiple overlay elements simultaneously. This can provide a speed
     * improvement when working on a computer with a multi-core CPU.
     */
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public AddLabels(Modules modules) {
        super("Add labels", modules);
    }

    public interface LabelModes extends LabelFactory.LabelModes {
    }

    public interface LabelPositions {
        String CENTRE = "Centre of object";
        String FIXED_VALUE = "Fixed value";
        String INSIDE = "Inside largest part of object";
        String OBJECT_MEASUREMENT = "Object measurement";

        String[] ALL = new String[] { CENTRE, FIXED_VALUE, INSIDE, OBJECT_MEASUREMENT };

    }

    public static double[] getMeanObjectLocation(Obj object) {
        double xMean = object.getXMean(true);
        double yMean = object.getYMean(true);
        double zMean = object.getZMean(true, false);
        int z = (int) Math.round(zMean + 1);

        return new double[] { xMean, yMean, z };

    }

    public static double[] getInsideObjectLocation(Obj obj) {
        // Binarise object and calculate its distance map
        Image binaryImage = obj.getAsImage("Binary", false);
        BinaryOperations2D.process(binaryImage, BinaryOperations2D.OperationModes.ERODE, 1, 1, true);
        ImagePlus distanceIpl = DistanceMap
                .process(binaryImage, "Distance", true, DistanceMap.WeightModes.WEIGHTS_3_4_5_7, true, false)
                .getImagePlus();
        ImageStack distanceIst = distanceIpl.getStack();

        // Get location of largest value
        Point<Integer> bestPoint = null;
        double distance = Double.MIN_VALUE;
        for (Point<Integer> point : obj.getCoordinateSet()) {
            int idx = distanceIpl.getStackIndex(1, point.getZ() + 1, obj.getT() + 1);
            float currDistance = distanceIst.getProcessor(idx).getf(point.getX(), point.getY());

            if (currDistance > distance) {
                distance = currDistance;
                bestPoint = point;
            }
        }

        // Returning this point
        return new double[] { bestPoint.getX(), bestPoint.getY(), bestPoint.getZ() + 1 };

    }

    public static double[] getMeasurementObjectLocation(Obj obj, String[] measurements) {
        double x;
        double y;
        double z;

        if (obj.getMeasurement(measurements[0]) != null)
            x = obj.getMeasurement(measurements[0]).getValue();
        else
            x = 0;

        if (obj.getMeasurement(measurements[1]) != null)
            y = obj.getMeasurement(measurements[1]).getValue();
        else
            y = 0;

        if (obj.getMeasurement(measurements[2]) != null)
            z = obj.getMeasurement(measurements[2]).getValue();
        else
            z = 0;

        // Returning this point
        return new double[] { x, y, z };

    }

    public static void addOverlay(ImagePlus ipl, Objs inputObjects, String labelPosition,
            HashMap<Integer, String> labels, int labelSize, int xOffset, int yOffset, boolean centreText,
            HashMap<Integer, Color> colours,
            boolean renderInAllSlices, boolean renderInAllFrames, boolean multithread,
            @Nullable String[] measurementNames, @Nullable int[] positions) {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
        // be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1))
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());

        // Adding the overlay element
        try {
            int nThreads = multithread ? Prefs.getThreads() : 1;
            ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>());

            // Running through each object, adding it to the overlay along with an ID label
            for (Obj object : inputObjects.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    int t1 = object.getT() + 1;
                    int t2 = object.getT() + 1;
                    if (renderInAllFrames) {
                        t1 = 1;
                        t2 = finalIpl.getNFrames();
                    }

                    Color colour = colours.get(object.getID());
                    String label = labels == null ? "" : labels.get(object.getID());

                    double[] location;
                    switch (labelPosition) {
                        case LabelPositions.CENTRE:
                        default:
                            location = getMeanObjectLocation(object);
                            break;
                        case LabelPositions.FIXED_VALUE:
                            location = new double[] { positions[0], positions[1], positions[2] };
                            break;
                        case LabelPositions.INSIDE:
                            location = getInsideObjectLocation(object);
                            break;
                        case LabelPositions.OBJECT_MEASUREMENT:
                            location = getMeasurementObjectLocation(object, measurementNames);
                            break;
                    }

                    location[0] = location[0] + xOffset;
                    location[1] = location[1] + yOffset;

                    for (int t = t1; t <= t2; t++) {
                        if (renderInAllSlices) {
                            double[][] extents = object.getExtents(true, false);
                            int zMin = (int) Math.round(extents[2][0]);
                            int zMax = (int) Math.round(extents[2][1]);
                            for (int z = zMin; z <= zMax; z++) {
                                location[2] = z + 1;
                                addOverlay(finalIpl, label, location, t, colour, labelSize, centreText);
                            }
                        } else {
                            addOverlay(finalIpl, label, location, t, colour, labelSize, centreText);
                        }
                    }
                };
                pool.submit(task);
            }

            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
        }
    }

    public static void addOverlay(ImagePlus ipl, String label, double[] labelCoords, int t, Color colour, int labelSize,
            boolean centreText) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        // Adding text label
        TextRoi text = new TextRoi(labelCoords[0], labelCoords[1], label,
                new Font(Font.SANS_SERIF, Font.PLAIN, labelSize));
        text.setStrokeColor(colour);
        text.setAntialiased(true);

        if (ipl.isHyperStack()) {
            text.setPosition(1, (int) labelCoords[2], t);
        } else {
            text.setPosition((int) Math.max(Math.max(1, labelCoords[2]), t));
        }

        if (centreText) {
            text.setLocation(text.getXBase() - text.getFloatWidth() / 2 + 1,
                    text.getYBase() - text.getFloatHeight() / 2 + 1);
        }
        ipl.getOverlay().addElement(text);

    }

    public HashMap<Integer, String> getLabels(Objs inputObjects, String labelMode, DecimalFormat df,
            String childObjectsForLabelName, String parentObjectsForLabelName, String partnerObjectsForLabelName,
            String measurementForLabel, String objectMetadataForLabel) {
        switch (labelMode) {
            case LabelModes.CHILD_COUNT:
                return LabelFactory.getChildCountLabels(inputObjects, childObjectsForLabelName, df);
            case LabelModes.ID:
                return LabelFactory.getIDLabels(inputObjects, df);
            case LabelModes.MEASUREMENT_VALUE:
                return LabelFactory.getMeasurementLabels(inputObjects, measurementForLabel, df);
            case LabelModes.OBJECT_METADATA_ITEM:
                return LabelFactory.getObjectMetadataLabels(inputObjects, objectMetadataForLabel, df);
            case LabelModes.PARENT_ID:
                return LabelFactory.getParentIDLabels(inputObjects, parentObjectsForLabelName, df);
            case LabelModes.PARENT_MEASUREMENT_VALUE:
                return LabelFactory.getParentMeasurementLabels(inputObjects, parentObjectsForLabelName,
                        measurementForLabel,
                        df);
            case LabelModes.PARTNER_COUNT:
                return LabelFactory.getPartnerCountLabels(inputObjects, partnerObjectsForLabelName, df);
        }

        return null;

    }

    public void appendPrefixSuffix(HashMap<Integer, String> labels, String prefix, String suffix) {
        if (prefix.equals("") && suffix.equals(""))
            return;

        for (int key : labels.keySet()) {
            String label = labels.get(key);
            labels.put(key, prefix + label + suffix);
        }
    }

    @Override
    public Category getCategory() {
        return Categories.VISUALISATION_OVERLAYS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Adds an overlay to the specified input image with each object represented by a text label.  The label can include information such as measurements, associated object counts or ID numbers.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting label settings
        String labelMode = parameters.getValue(LABEL_MODE, workspace);
        int labelSize = parameters.getValue(LABEL_SIZE, workspace);
        int xOffset = parameters.getValue(X_OFFSET, workspace);
        int yOffset = parameters.getValue(Y_OFFSET, workspace);
        boolean centreText = parameters.getValue(CENTRE_TEXT, workspace);
        int decimalPlaces = parameters.getValue(DECIMAL_PLACES, workspace);
        boolean useScientific = parameters.getValue(USE_SCIENTIFIC, workspace);
        String childObjectsForLabelName = parameters.getValue(CHILD_OBJECTS_FOR_LABEL, workspace);
        String parentObjectsForLabelName = parameters.getValue(PARENT_OBJECT_FOR_LABEL, workspace);
        String partnerObjectsForLabelName = parameters.getValue(PARTNER_OBJECTS_FOR_LABEL, workspace);
        String measurementForLabel = parameters.getValue(MEASUREMENT_FOR_LABEL, workspace);
        String objectMetadataForLabel = parameters.getValue(OBJECT_METADATA_FOR_LABEL, workspace);
        String prefix = parameters.getValue(PREFIX, workspace);
        String suffix = parameters.getValue(SUFFIX, workspace);
        String labelPosition = parameters.getValue(LABEL_POSITION, workspace);
        String xPosMeas = parameters.getValue(X_POSITION_MEASUREMENT, workspace);
        String yPosMeas = parameters.getValue(Y_POSITION_MEASUREMENT, workspace);
        String zPosMeas = parameters.getValue(Z_POSITION_MEASUREMENT, workspace);
        int xPos = parameters.getValue(X_POSITION, workspace);
        int yPos = parameters.getValue(Y_POSITION, workspace);
        int zPos = parameters.getValue(Z_POSITION, workspace);
        boolean renderInAllSlices = parameters.getValue(RENDER_IN_ALL_OBJECT_SLICES, workspace);
        boolean renderInAllFrames = parameters.getValue(RENDER_IN_ALL_FRAMES, workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);

        String[] measurementNames = new String[] { xPosMeas, yPosMeas, zPosMeas };
        int[] positions = new int[] { xPos, yPos, zPos };

        // Only add output to workspace if not applying to input
        if (applyToInput)
            addOutputToWorkspace = false;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
            ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer, Color> colours = getColours(inputObjects, workspace);
        DecimalFormat df = LabelFactory.getDecimalFormat(decimalPlaces, useScientific);
        HashMap<Integer, String> labels = getLabels(inputObjects, labelMode, df, childObjectsForLabelName,
                parentObjectsForLabelName, partnerObjectsForLabelName, measurementForLabel, objectMetadataForLabel);
        appendPrefixSuffix(labels, prefix, suffix);

        addOverlay(ipl, inputObjects, labelPosition, labels, labelSize, xOffset, yOffset, centreText, colours,
                renderInAllSlices,
                renderInAllFrames, multithread, measurementNames, positions);

        Image outputImage = ImageFactory.createImage(outputImageName, ipl);

        // If necessary, adding output image to workspace. This also allows us to show
        // it.
        if (addOutputToWorkspace)
            workspace.addImage(outputImage);
        if (showOutput)
            outputImage.show();

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(CONTENT_SEPARATOR, this));
        parameters.add(new ChoiceP(LABEL_MODE, this, LabelModes.ID, LabelModes.ALL));
        parameters.add(new IntegerP(DECIMAL_PLACES, this, 0));
        parameters.add(new BooleanP(USE_SCIENTIFIC, this, false));
        parameters.add(new IntegerP(LABEL_SIZE, this, 8));
        parameters.add(new IntegerP(X_OFFSET, this, 0));
        parameters.add(new IntegerP(Y_OFFSET, this, 0));
        parameters.add(new BooleanP(CENTRE_TEXT, this, true));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS_FOR_LABEL, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_LABEL, this));
        parameters.add(new PartnerObjectsP(PARTNER_OBJECTS_FOR_LABEL, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_LABEL, this));
        parameters.add(new ObjectMetadataP(OBJECT_METADATA_FOR_LABEL, this));
        parameters.add(new StringP(PREFIX, this));
        parameters.add(new StringP(SUFFIX, this));

        parameters.add(new SeparatorP(LOCATION_SEPARATOR, this));
        parameters.add(new ChoiceP(LABEL_POSITION, this, LabelPositions.CENTRE, LabelPositions.ALL));
        parameters.add(new ObjectMeasurementP(X_POSITION_MEASUREMENT, this));
        parameters.add(new ObjectMeasurementP(Y_POSITION_MEASUREMENT, this));
        parameters.add(new ObjectMeasurementP(Z_POSITION_MEASUREMENT, this));
        parameters.add(new IntegerP(X_POSITION, this, 0));
        parameters.add(new IntegerP(Y_POSITION, this, 0));
        parameters.add(new IntegerP(Z_POSITION, this, 0));
        parameters.add(new BooleanP(RENDER_IN_ALL_OBJECT_SLICES, this, false));
        parameters.add(new BooleanP(RENDER_IN_ALL_FRAMES, this, false));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT_FOR_LABEL, workspace);

        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE, workspace)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

            }
        }

        returnedParameters.add(parameters.getParameter(CONTENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LABEL_MODE));
        switch ((String) parameters.getValue(LABEL_MODE, workspace)) {
            case LabelModes.CHILD_COUNT:
                returnedParameters.add(parameters.getParameter(CHILD_OBJECTS_FOR_LABEL));
                ((ChildObjectsP) parameters.getParameter(CHILD_OBJECTS_FOR_LABEL))
                        .setParentObjectsName(inputObjectsName);
                break;

            case LabelModes.MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_LABEL));
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_FOR_LABEL)).setObjectName(inputObjectsName);
                break;

            case LabelModes.OBJECT_METADATA_ITEM:
                returnedParameters.add(parameters.getParameter(OBJECT_METADATA_FOR_LABEL));
                ((ObjectMetadataP) parameters.getParameter(OBJECT_METADATA_FOR_LABEL)).setObjectName(inputObjectsName);
                break;

            case LabelModes.PARENT_ID:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_LABEL));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_LABEL))
                        .setChildObjectsName(inputObjectsName);
                break;

            case LabelModes.PARENT_MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_LABEL));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_LABEL))
                        .setChildObjectsName(inputObjectsName);

                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_LABEL));
                if (parentObjectsName != null) {
                    ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_FOR_LABEL))
                            .setObjectName(parentObjectsName);
                }
                break;

            case LabelModes.PARTNER_COUNT:
                returnedParameters.add(parameters.getParameter(PARTNER_OBJECTS_FOR_LABEL));
                ((PartnerObjectsP) parameters.getParameter(PARTNER_OBJECTS_FOR_LABEL))
                        .setPartnerObjectsName(inputObjectsName);
                break;
        }

        if (!((String) parameters.getValue(LABEL_MODE, workspace)).equals(LabelModes.OBJECT_METADATA_ITEM)) {
            returnedParameters.add(parameters.getParameter(DECIMAL_PLACES));
            returnedParameters.add(parameters.getParameter(USE_SCIENTIFIC));
        }
        
        returnedParameters.add(parameters.getParameter(LABEL_SIZE));
        returnedParameters.add(parameters.getParameter(X_OFFSET));
        returnedParameters.add(parameters.getParameter(Y_OFFSET));
        returnedParameters.add(parameters.getParameter(CENTRE_TEXT));
        returnedParameters.add(parameters.getParameter(PREFIX));
        returnedParameters.add(parameters.getParameter(SUFFIX));

        returnedParameters.addAll(super.updateAndGetParameters(inputObjectsName));

        returnedParameters.add(parameters.getParameter(LOCATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LABEL_POSITION));
        switch ((String) parameters.getValue(LABEL_POSITION, workspace)) {
            default:
                returnedParameters.add(parameters.getParameter(RENDER_IN_ALL_OBJECT_SLICES));
                break;

            case LabelPositions.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(X_POSITION));
                returnedParameters.add(parameters.getParameter(Y_POSITION));
                returnedParameters.add(parameters.getParameter(RENDER_IN_ALL_OBJECT_SLICES));
                if (!(boolean) parameters.getValue(RENDER_IN_ALL_OBJECT_SLICES, workspace))
                    returnedParameters.add(parameters.getParameter(Z_POSITION));
                break;

            case LabelPositions.OBJECT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT)).setObjectName(inputObjectsName);
                ((ObjectMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT)).setObjectName(inputObjectsName);

                returnedParameters.add(parameters.getParameter(RENDER_IN_ALL_OBJECT_SLICES));
                if (!(boolean) parameters.getValue(RENDER_IN_ALL_OBJECT_SLICES, workspace)) {
                    returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT));
                    ((ObjectMeasurementP) parameters.getParameter(Z_POSITION_MEASUREMENT))
                            .setObjectName(inputObjectsName);
                }
                break;
        }

        returnedParameters.add(parameters.getParameter(RENDER_IN_ALL_FRAMES));

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

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

        parameters.get(LABEL_MODE).setDescription("Controls what information each label displays:<br><ul>"

                + "<li>\"" + LabelModes.CHILD_COUNT
                + "\" The number of children from a specific object collection for each object.  The children to summarise are selected using the \""
                + CHILD_OBJECTS_FOR_LABEL + "\" parameter.</li>"

                + "<li>\"" + LabelModes.ID + "\" The ID number of the object.</li>"

                + "<li>\"" + LabelModes.MEASUREMENT_VALUE
                + "\" A measurement associated with the object.  The measurement is selected using the \""
                + MEASUREMENT_FOR_LABEL + "\" parameter.</li>"

                + "<li>\"" + LabelModes.PARENT_ID
                + "\" The ID number of a parent of the object.  The parent object is selected using the \""
                + PARENT_OBJECT_FOR_LABEL + "\" parameter.</li>"

                + "<li>\"" + LabelModes.PARENT_MEASUREMENT_VALUE
                + "\" A measurement associated with a parent of the object.  The measurement is selected using the \""
                + MEASUREMENT_FOR_LABEL + "\" parameter and the parent object with the \"" + PARENT_OBJECT_FOR_LABEL
                + "\" parameter.</li>"

                + "<li>\"" + LabelModes.PARTNER_COUNT
                + "\" The number of partners from a specific object collection for each object.  The partners to summarise are selected using the \""
                + PARTNER_OBJECTS_FOR_LABEL + "\" parameter.</li></ul>");

        parameters.get(DECIMAL_PLACES)
                .setDescription("Number of decimal places to use when displaying numeric values.");

        parameters.get(USE_SCIENTIFIC).setDescription(
                "When enabled, numeric values will be displayed in the format <i>1.23E-3</i>.  Otherwise, the same value would appear as <i>0.00123</i>.");

        parameters.get(LABEL_SIZE).setDescription("Font size of the text label.");

        parameters.get(X_OFFSET).setDescription(
                "Offset the label by this number of pixels horizontally (along the x-axis).  Increasingly positive numbers move the label right.");

        parameters.get(Y_OFFSET).setDescription(
                "Offset the label by this number of pixels vertically (along the y-axis).  Increasingly positive numbers move the label down.");

        parameters.get(PREFIX).setDescription("Text to display at beginning of every label");

        parameters.get(SUFFIX).setDescription("Text to display at end of every label");

        parameters.get(CHILD_OBJECTS_FOR_LABEL).setDescription("If \"" + LABEL_MODE + "\" is set to \""
                + LabelModes.CHILD_COUNT
                + "\", these are the child objects which will be counted and displayed on the label.  These objects will be children of the input objects.");

        parameters.get(PARENT_OBJECT_FOR_LABEL).setDescription("If \"" + LABEL_MODE + "\" is set to either \""
                + LabelModes.PARENT_ID + "\" or \"" + LabelModes.PARENT_MEASUREMENT_VALUE
                + "\", these are the parent objects which will be used.  These objects will be parents of the input objects.");

        parameters.get(PARTNER_OBJECTS_FOR_LABEL).setDescription("If \"" + LABEL_MODE + "\" is set to \""
                + LabelModes.PARTNER_COUNT
                + "\", these are the partner objects which will be counted and displayed on the label.  These objects will be partners of the input objects.");

        parameters.get(MEASUREMENT_FOR_LABEL)
                .setDescription("If \"" + LABEL_MODE + "\" is set to either \"" + LabelModes.MEASUREMENT_VALUE
                        + "\" or \"" + LabelModes.PARENT_MEASUREMENT_VALUE
                        + "\", these are the measurements which will be used.");

        parameters.get(LABEL_POSITION)
                .setDescription("Determines the method used for placing the label overlay for each object:<br><ul>"

                        + "<li>\"" + LabelPositions.CENTRE
                        + "\" Labels will be placed at the centroid (average coordinate location) of each object.  This position won't necessarily coincide with a region corresponding to that object.  For example, the centroid of a crescent shape won't lie on the crescent itself.</li>"

                        + "<li>\"" + LabelPositions.FIXED_VALUE
                        + "\" Labels will be placed at the x,y,z location specified by the user.</li>"

                        + "<li>\"" + LabelPositions.INSIDE
                        + "\" Labels will be placed coincident with the largest region of each object.  This ensures the label is placed directly over the relevant object.</li></ul>"

                        + "<li>\"" + LabelPositions.OBJECT_MEASUREMENT
                        + "\" Labels will be placed coincident with the specified measurements for each object.</li></ul>");

        parameters.get(X_POSITION_MEASUREMENT).setDescription(
                "Object measurement specifying the X-position of the label.  Measurement value must be specified in pixel units.");

        parameters.get(Y_POSITION_MEASUREMENT).setDescription(
                "Object measurement specifying the Y-position of the label.  Measurement value must be specified in pixel units.");

        parameters.get(Z_POSITION_MEASUREMENT).setDescription(
                "Object measurement specifying the Z-position (slice) of the label.  Measurement value must be specified in slice units.");

        parameters.get(X_POSITION).setDescription(
                "Fixed value specifying the X-position of the label.  Specified in pixel units.");

        parameters.get(Y_POSITION).setDescription(
                "Fixed value specifying the Y-position of the label.  Specified in pixel units.");

        parameters.get(Z_POSITION).setDescription(
                "Fixed value specifying the Z-position of the label.  Specified in pixel units.");

        parameters.get(RENDER_IN_ALL_OBJECT_SLICES)
                .setDescription("Display overlay elements in all slices corresponding to that object.");

        parameters.get(RENDER_IN_ALL_FRAMES).setDescription(
                "Display overlay elements in all frames, irrespective of whether each object is present in that frame.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple overlay elements simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
