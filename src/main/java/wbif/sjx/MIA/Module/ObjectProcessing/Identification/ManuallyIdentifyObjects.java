package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.gui.Toolbar;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import ij.process.BinaryInterpolator;
import ij.process.LUT;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.ObjectProcessing.Relationships.TrackObjects;
import wbif.sjx.MIA.Module.Visualisation.Overlays.AbstractOverlay;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputTrackObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;
import wbif.sjx.common.Object.Volume.SpatCal;
import wbif.sjx.common.Object.Volume.VolumeType;

/**
 * Created by sc13967 on 27/02/2018.
 */
public class ManuallyIdentifyObjects extends Module implements ActionListener {
    private JFrame frame;
    private JTextField objectNumberField;
    private DefaultListModel<ObjRoi> listModel = new DefaultListModel<>();
    private JList<ObjRoi> list = new JList<>(listModel);
    private JScrollPane objectsScrollPane = new JScrollPane(list);

    private ImagePlus displayImagePlus;
    private Overlay overlay;
    private Overlay origOverlay;
    private HashMap<Integer, ArrayList<ObjRoi>> rois;
    private int maxID;

    private ObjCollection outputObjects;
    private ObjCollection outputTrackObjects;

    private SpatCal calibration;
    private int nFrames;
    private boolean overflow = false;

    private static final String ADD_NEW = "Add new";
    private static final String ADD_EXISTING = "Add existing";
    private static final String REMOVE = "Remove";
    private static final String FINISH = "Finish";

    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";

    public static final String OUTPUT_SEPARATOR = "Object output";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String VOLUME_TYPE = "Volume type";
    public static final String OUTPUT_TRACKS = "Output tracks";
    public static final String OUTPUT_TRACK_OBJECTS = "Output track objects";
    public static final String SPATIAL_INTERPOLATION = "Spatial interpolation";
    public static final String TEMPORAL_INTERPOLATION = "Temporal interpolation";

    public static final String SELECTION_SEPARATOR = "Object selection controls";
    public static final String SELECTOR_TYPE = "Default selector type";
    public static final String MESSAGE_ON_IMAGE = "Message on image";

    public ManuallyIdentifyObjects(ModuleCollection modules) {
        super("Manually identify objects", modules);
    }

    public interface SelectorTypes {
        String FREEHAND_LINE = "Freehand line";
        String FREEHAND_REGION = "Freehand region";
        String LINE = "Line";
        String OVAL = "Oval";
        String POINTS = "Points";
        String POLYGON = "Polygon";
        String RECTANGLE = "Rectangle";
        String SEGMENTED_LINE = "Segmented line";
        String WAND = "Wand (tracing) tool";

        String[] ALL = new String[] { FREEHAND_LINE, FREEHAND_REGION, LINE, OVAL, POINTS, POLYGON, RECTANGLE,
                SEGMENTED_LINE, WAND };

    }

    public interface SpatialInterpolationModes {
        String NONE = "None";
        String SPATIAL = "Spatial";

        String[] ALL = new String[] { NONE, SPATIAL };

    }

    public interface SpatioTemporalInterpolationModes extends SpatialInterpolationModes {
        String NONE = "None";
        String TEMPORAL = "Temporal";
        String SPATIAL_AND_TEMPORAL = "Spatial and temporal";

        String[] ALL = new String[] { NONE, SPATIAL, TEMPORAL, SPATIAL_AND_TEMPORAL };

    }

    public interface VolumeTypes extends Image.VolumeTypes {
    }

    VolumeType getVolumeType(String volumeType) {
        switch (volumeType) {
            case Image.VolumeTypes.OCTREE:
                return VolumeType.OCTREE;
            // case Image.VolumeTypes.OPTIMISED:
            default:
            case Image.VolumeTypes.POINTLIST:
                return VolumeType.POINTLIST;
            case Image.VolumeTypes.QUADTREE:
                return VolumeType.QUADTREE;
        }
    }

    void setSelector(String selectorType) {
        switch (selectorType) {
            case SelectorTypes.FREEHAND_LINE:
                IJ.setTool(Toolbar.FREELINE);
                return;
            default:
            case SelectorTypes.FREEHAND_REGION:
                IJ.setTool(Toolbar.FREEROI);
                return;
            case SelectorTypes.LINE:
                IJ.setTool(Toolbar.LINE);
                return;
            case SelectorTypes.OVAL:
                IJ.setTool(Toolbar.OVAL);
                return;
            case SelectorTypes.POINTS:
                IJ.setTool(Toolbar.POINT);
                return;
            case SelectorTypes.RECTANGLE:
                IJ.setTool(Toolbar.RECTANGLE);
                return;
            case SelectorTypes.SEGMENTED_LINE:
                IJ.setTool(Toolbar.POLYLINE);
                return;
            case SelectorTypes.POLYGON:
                IJ.setTool(Toolbar.POLYGON);
                return;
            case SelectorTypes.WAND:
                IJ.setTool(Toolbar.WAND);
                return;
        }
    }

    private void showOptionsPanel() {
        rois = new HashMap<>();
        maxID = 0;
        frame = new JFrame();
        frame.setAlwaysOnTop(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                processObjectsAndFinish();
            };
        });

        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                List<ObjRoi> selected = list.getSelectedValuesList();
                for (ObjRoi objRoi : selected)
                    displayObject(objRoi);
            }
        });

        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        c.gridheight = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 5, 5, 5);

        JLabel headerLabel = new JLabel("<html>Draw round an object, then select one of the following"
                + "<br>(or click \"Finish adding objects\" at any time)."
                + "<br>Different timepoints must be added as new objects.</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        frame.add(headerLabel, c);

        JButton newObjectButton = new JButton("Add as new object");
        newObjectButton.addActionListener(this);
        newObjectButton.setActionCommand(ADD_NEW);
        c.gridy++;
        c.gridwidth = 1;
        frame.add(newObjectButton, c);

        JButton existingObjectButton = new JButton("Add to existing object");
        existingObjectButton.addActionListener(this);
        existingObjectButton.setActionCommand(ADD_EXISTING);
        c.gridx++;
        frame.add(existingObjectButton, c);

        JButton removeObjectButton = new JButton("Remove object (s)");
        removeObjectButton.addActionListener(this);
        removeObjectButton.setActionCommand(REMOVE);
        c.gridx++;
        frame.add(removeObjectButton, c);

        JButton finishButton = new JButton("Finish adding objects");
        finishButton.addActionListener(this);
        finishButton.setActionCommand(FINISH);
        c.gridx++;
        frame.add(finishButton, c);

        // Object number panel
        JLabel objectNumberLabel = new JLabel("Existing object number");
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        frame.add(objectNumberLabel, c);

        objectNumberField = new JTextField();
        c.gridx++;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        frame.add(objectNumberField, c);

        objectsScrollPane.setPreferredSize(new Dimension(0, 200));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 4;
        c.gridheight = 3;
        c.fill = GridBagConstraints.BOTH;
        frame.add(objectsScrollPane, c);

        JCheckBox overlayCheck = new JCheckBox("Display overlay");
        overlayCheck.setSelected(true);
        overlayCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayImagePlus.setHideOverlay(!overlayCheck.isSelected());
            }
        });
        c.gridy++;
        c.gridy++;
        c.gridy++;
        c.gridwidth = 1;
        c.gridheight = 1;
        frame.add(overlayCheck, c);

        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

    }

    public static void applySpatialInterpolation(ObjCollection inputObjects, String type)
            throws IntegerOverflowException {
        for (Obj inputObj : inputObjects.values()) {
            Image binaryImage = inputObj.getAsTightImage("BinaryTight");

            // We need at least 3 slices to make interpolation worthwhile
            if (binaryImage.getImagePlus().getNSlices() < 3)
                continue;

            applySpatialInterpolation(binaryImage);

            // Converting binary image back to objects
            Obj interpObj = binaryImage.convertImageToObjects(type, inputObj.getName(), true).getFirst();
            double[][] extents = inputObj.getExtents(true, false);

            interpObj.translateCoords((int) Math.round(extents[0][0]), (int) Math.round(extents[1][0]),
                    (int) Math.round(extents[2][0]));
            inputObj.setCoordinateSet(interpObj.getCoordinateSet());

        }
    }

    static void applySpatialInterpolation(Image binaryImage) {
        ImagePlus binaryIpl = binaryImage.getImagePlus();
        int nSlices = binaryIpl.getNSlices();
        int nFrames = binaryIpl.getNFrames();

        BinaryInterpolator binaryInterpolator = new BinaryInterpolator();

        // We only want to interpolate in z, so need to processAutomatic each timepoint
        // separately
        for (int t = 1; t <= nFrames; t++) {
            // Extracting the slice and interpolating
            ImagePlus sliceIpl = SubHyperstackMaker.makeSubhyperstack(binaryIpl, "1-1", "1-" + nSlices, t + "-" + t);
            if (!checkStackForInterpolation(sliceIpl.getStack()))
                continue;
            binaryInterpolator.run(sliceIpl.getStack());
        }
    }

    public static void applyTemporalInterpolation(ObjCollection inputObjects, ObjCollection trackObjects, String type)
            throws IntegerOverflowException {
        for (Obj trackObj : trackObjects.values()) {
            // Keeping a record of frames which have an object (these will be unchanged, so
            // don't need a new object)
            ArrayList<Integer> timepoints = new ArrayList<>();

            // Creating a blank image for this track
            Image binaryImage = trackObj.convertObjToImage("Track");

            // Adding each timepoint object (child) to this image
            for (Obj childObj : trackObj.getChildren(inputObjects.getName()).values()) {
                childObj.addToImage(binaryImage, Float.MAX_VALUE);
                timepoints.add(childObj.getT());
            }
            applyTemporalInterpolation(binaryImage);

            // Converting binary image back to objects
            ObjCollection interpObjs = binaryImage.convertImageToObjects(type, inputObjects.getName(), true);

            // Transferring new timepoint objects to inputObjects ObjCollection
            Iterator<Obj> iterator = interpObjs.values().iterator();
            while (iterator.hasNext()) {
                Obj interpObj = iterator.next();

                // If this timepoint already existed, it doesn't need a new object
                if (timepoints.contains(interpObj.getT()))
                    continue;

                // Adding this object to the original collection
                interpObj.setID(inputObjects.getAndIncrementID());

                interpObj.setSpatialCalibration(inputObjects.getSpatialCalibration());
                interpObj.addParent(trackObj);
                trackObj.addChild(interpObj);
                inputObjects.add(interpObj);
                iterator.remove();

            }
        }
    }

    static void applyTemporalInterpolation(Image binaryImage) {
        ImagePlus binaryIpl = binaryImage.getImagePlus();
        int nSlices = binaryIpl.getNSlices();
        int nFrames = binaryIpl.getNFrames();

        BinaryInterpolator binaryInterpolator = new BinaryInterpolator();

        // We only want to interpolate in time, so need to processAutomatic each Z-slice
        // of the stack separately
        for (int z = 1; z <= nSlices; z++) {
            // Extracting the slice and interpolating
            ImagePlus sliceIpl = SubHyperstackMaker.makeSubhyperstack(binaryIpl, "1-1", z + "-" + z, "1-" + nFrames);
            if (!checkStackForInterpolation(sliceIpl.getStack()))
                continue;

            binaryInterpolator.run(sliceIpl.getStack());
        }
    }

    /**
     * Verifies that at least two images in the stack contain non-zero pixels
     * 
     * @param stack
     * @return
     */
    static boolean checkStackForInterpolation(ImageStack stack) {
        int count = 0;
        for (int i = 1; i <= stack.getSize(); i++) {
            if (stack.getProcessor(i).getStatistics().max > 0)
                count++;
        }

        return count >= 2;

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getDescription() {
        return "Manually create objects using the ImageJ selection tools.  Selected regions can be interpolated in Z and T to speed up the object creation process."
                + "<br><br>This module will display a control panel and an image onto which selections are made.  "
                + "<br><br>Following selection of a region to be included in the object, the user can either add this region to a new object (\""
                + ADD_NEW + "\" button), or add it to an existing object (\"" + ADD_EXISTING + "\" button).  "
                + "The target object for adding to an existing object is specified using the \"Existing object number\" control (a list of existing object IDs is shown directly below this control)."
                + "<br><br>References to each selection are displayed below the controls.  Previously-added regions can be re-selected by clicking the relevant reference.  This allows selections to be deleted or used as a basis for further selections."
                + "<br><br>Once all selections have been made, objects are added to the workspace with the \"" + FINISH
                + "\" button.";
    }

    @Override
    public Status process(Workspace workspace) {// Local access to this is required for the action listeners
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String type = parameters.getValue(VOLUME_TYPE);
        boolean outputTracks = parameters.getValue(OUTPUT_TRACKS);
        String outputTrackObjectsName = parameters.getValue(OUTPUT_TRACK_OBJECTS);
        boolean spatialInterpolation = parameters.getValue(SPATIAL_INTERPOLATION);
        boolean temporalInterpolation = parameters.getValue(TEMPORAL_INTERPOLATION);
        String selectorType = parameters.getValue(SELECTOR_TYPE);
        String messageOnImage = parameters.getValue(MESSAGE_ON_IMAGE);

        // Getting input image
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();
        calibration = SpatCal.getFromImage(inputImagePlus);
        nFrames = inputImagePlus.getNFrames();

        setSelector(selectorType);

        displayImagePlus = new Duplicator().run(inputImagePlus);
        displayImagePlus.setCalibration(null);
        displayImagePlus.setTitle(messageOnImage);

        // Clearing any ROIs stored from previous runs
        rois = new HashMap<>();
        listModel.clear();

        origOverlay = displayImagePlus.getOverlay();
        overlay = new Overlay();
        displayImagePlus.setOverlay(overlay);
        updateOverlay();

        // Initialising output objects
        outputObjects = new ObjCollection(outputObjectsName, calibration, nFrames);
        if (outputTracks) {
            outputTrackObjects = new ObjCollection(outputTrackObjectsName, calibration, nFrames);
        }

        // Displaying the image and showing the control
        displayImagePlus.setLut(LUT.createLutFromColor(Color.WHITE));
        displayImagePlus.show();
        showOptionsPanel();

        // All the while the control is open, do nothing
        while (frame != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // If more pixels than Integer.MAX_VALUE were assigned, return false
        // (IntegerOverflowException).
        if (overflow)
            return Status.FAIL;

        // If necessary, apply interpolation
        try {
            if (spatialInterpolation)
                applySpatialInterpolation(outputObjects, type);
            if (outputTracks && temporalInterpolation)
                applyTemporalInterpolation(outputObjects, outputTrackObjects, type);
        } catch (IntegerOverflowException e) {
            return Status.FAIL;
        }

        workspace.addObjects(outputObjects);

        // Showing the selected objects
        if (showOutput) {
            if (outputTracks) {
                TrackObjects.showObjects(outputObjects, outputTrackObjectsName, AbstractOverlay.ColourModes.PARENT_ID);
            } else {
                outputObjects.convertToImageRandomColours().showImage();
            }
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new ChoiceP(VOLUME_TYPE, this, VolumeTypes.POINTLIST, VolumeTypes.ALL));
        parameters.add(new BooleanP(OUTPUT_TRACKS, this, false));
        parameters.add(new OutputTrackObjectsP(OUTPUT_TRACK_OBJECTS, this));
        parameters.add(new BooleanP(SPATIAL_INTERPOLATION, this, false));
        parameters.add(new BooleanP(TEMPORAL_INTERPOLATION, this, false));

        parameters.add(new SeparatorP(SELECTION_SEPARATOR, this));
        parameters.add(new ChoiceP(SELECTOR_TYPE, this, SelectorTypes.FREEHAND_REGION, SelectorTypes.ALL));
        parameters.add(new StringP(MESSAGE_ON_IMAGE, this, "Draw objects on this image"));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(INPUT_IMAGE));

        returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.get(OUTPUT_OBJECTS));
        returnedParameters.add(parameters.get(VOLUME_TYPE));
        returnedParameters.add(parameters.get(OUTPUT_TRACKS));

        if ((boolean) parameters.getValue(OUTPUT_TRACKS)) {
            returnedParameters.add(parameters.get(OUTPUT_TRACK_OBJECTS));
            returnedParameters.add(parameters.get(SPATIAL_INTERPOLATION));
            returnedParameters.add(parameters.get(TEMPORAL_INTERPOLATION));
        } else {
            returnedParameters.add(parameters.get(SPATIAL_INTERPOLATION));
        }

        returnedParameters.add(parameters.get(SELECTION_SEPARATOR));
        returnedParameters.add(parameters.get(SELECTOR_TYPE));
        returnedParameters.add(parameters.get(MESSAGE_ON_IMAGE));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
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
        ParentChildRefCollection returnedRelationships = new ParentChildRefCollection();

        if ((boolean) parameters.getValue(OUTPUT_TRACKS))
            returnedRelationships.add(parentChildRefs.getOrPut(parameters.getValue(OUTPUT_TRACK_OBJECTS),
                    parameters.getValue(OUTPUT_OBJECTS)));

        return returnedRelationships;

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
        parameters.get(INPUT_IMAGE).setDescription(
                "Image onto which selections will be drawn.  This will be displayed automatically when the module runs.");

        parameters.get(OUTPUT_OBJECTS).setDescription("Objects created by this module.");

        parameters.get(VOLUME_TYPE).setDescription(
                "The method used to store pixel coordinates.  This only affects performance and memory usage, there is no difference in results obtained using difference storage methods.<br><ul>"
                        + "<li>\"" + VolumeTypes.POINTLIST
                        + "\" (default) stores object coordinates as a list of XYZ coordinates.  This is most efficient for small objects, very thin objects or objects with lots of holes.</li>"
                        + "<li>\"" + VolumeTypes.OCTREE
                        + "\" stores objects in an octree format.  Here, the coordinate space is broken down into cubes of different sizes, each of which is marked as foreground (i.e. an object) or background.  Octrees are most efficient when there are lots of large cubic regions of the same label, as the space can be represented by larger (and thus fewer) cubes.  This is best used when there are large, completely solid objects.  If z-axis sampling is much larger than xy-axis sampling, it's typically best to opt for the quadtree method.</li>"
                        + "<li>\"" + VolumeTypes.QUADTREE
                        + "\" stores objects in a quadtree format.  Here, each Z-plane of the object is broken down into squares of different sizes, each of which is marked as foreground (i.e. an object) or background.  Quadtrees are most efficient when there are lots of large square regions of the same label, as the space can be represented by larger (and thus fewer) squares.  This is best used when there are large, completely solid objects.</li></ul>");

        parameters.get(OUTPUT_TRACKS).setDescription(
                "When selected, the same object can be identified across multiple timepoints.  The same ID should be used for all objects in this \"track\" - this will become the ID of the track object itself, while each timepoint instance will be assigned its own unique ID.  This feature also enables the use of temporal intepolation of objects.");

        parameters.get(OUTPUT_TRACK_OBJECTS).setDescription(
                "Name of track objects to be added to the workspace.  These will be parents of the individual timepoint instances and provide a way of grouping all the individual timepoint instances of a particular object.  Track objects themselves do not contain any coordinate information.");

        parameters.get(SPATIAL_INTERPOLATION).setDescription(
                "Interpolate objects in Z.  Objects assigned the same ID will be interpolated to appear in all slices between the top-most and bottom-most specific slices.  Specified regions must contain a degree of overlap (higher overlap will give better results).");

        parameters.get(TEMPORAL_INTERPOLATION).setDescription(
                "Interpolate objects across multiple frames.  Objects assigned the same ID will be interpolated to appear in all frames between the first and last specified timepoints.  Specified regions must contain a degree of overlap (higher overlap will give better results).");

        parameters.get(SELECTOR_TYPE).setDescription(
                "Default region drawing tool to enable.  This tool can be changed by the user when selecting regions.  Choices are: "
                        + String.join(", ", SelectorTypes.ALL) + ".");

        parameters.get(MESSAGE_ON_IMAGE).setDescription("Message to display in title of image.");

    }

    @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case (ADD_NEW):
                    addNewObject();
                    break;

                case (ADD_EXISTING):
                    addToExistingObject();
                    break;

                case (REMOVE):
                    removeObjects();
                    break;

                case (FINISH):
                    processObjectsAndFinish();
                    break;
            }
        }

    public void addNewObject() {
        // Getting the ROI
        Roi roi = displayImagePlus.getRoi();

        if (roi == null) {
            frame.setAlwaysOnTop(false);
            IJ.error("Select a ROI first using the ImageJ ROI tools.");
            frame.setAlwaysOnTop(true);
            return;
        }

        int ID = ++maxID;

        // Adding the ROI to our current collection
        ArrayList<ObjRoi> currentRois = new ArrayList<>();
        ObjRoi objRoi = new ObjRoi(ID, roi, displayImagePlus.getT() - 1, displayImagePlus.getZ());
        currentRois.add(objRoi);
        rois.put(ID, currentRois);

        // Displaying the ROI on the overlay
        updateOverlay();

        // Setting the number field to this number
        objectNumberField.setText(String.valueOf(ID));

        // Adding to the list of objects
        addObjectToList(objRoi, ID);

    }

    public void addToExistingObject() {
        // Getting points
        Roi roi = displayImagePlus.getRoi();

        if (roi == null) {
            frame.setAlwaysOnTop(false);
            IJ.error("Select a ROI first using the ImageJ ROI tools.");
            frame.setAlwaysOnTop(true);
            return;
        }

        int ID = Integer.parseInt(objectNumberField.getText());

        // Adding the ROI to our current collection
        ArrayList<ObjRoi> currentRois = rois.get(ID);
        ObjRoi objRoi = new ObjRoi(ID, roi, displayImagePlus.getT() - 1, displayImagePlus.getZ());
        currentRois.add(objRoi);
        rois.put(ID, currentRois);

        // Displaying the ROI on the overlay
        updateOverlay();

        // Setting the number field to this number
        objectNumberField.setText(String.valueOf(ID));

        // Adding to the list of objects
        addObjectToList(objRoi, ID);

    }

    public void removeObjects() {
        // Get selected ROIs
        List<ObjRoi> selected = list.getSelectedValuesList();

        for (ObjRoi objRoi : selected) {
            // Get objects matching this ID
            int ID = objRoi.getID();
            rois.get(ID).remove(objRoi);

            listModel.removeElement(objRoi);

        }

        updateOverlay();

    }

    public void processObjectsAndFinish() {
        try {
            if (outputTrackObjects == null) {
                processSpatialOnlyObjects();
            } else {
                processTemporalObjects();
            }
        } catch (IntegerOverflowException e1) {
            overflow = true;
        }

        // If the frame isn't already closed, close it
        if (frame != null) {
            frame.dispose();
            frame = null;
        }
        displayImagePlus.close();

    }

    public void processSpatialOnlyObjects() throws IntegerOverflowException {
        // Processing each list of Rois, then converting them to objects
        for (int ID : rois.keySet()) {
            ArrayList<ObjRoi> currentRois = rois.get(ID);

            // This Obj may be empty; if so, skip it
            if (currentRois.size() == 0)
                continue;

            // Creating the new object
            String type = parameters.getValue(VOLUME_TYPE);
            VolumeType volumeType = getVolumeType(type);
            Obj outputObject = new Obj(volumeType, outputObjects.getName(), ID, calibration, nFrames);
            outputObjects.add(outputObject);

            for (ObjRoi objRoi : currentRois) {
                Roi roi = objRoi.getRoi();
                Point[] points = roi.getContainedPoints();

                int t = objRoi.getT();
                int z = objRoi.getZ();

                outputObject.setT(t);
                for (Point point : points) {
                    int x = (int) Math.round(point.getX());
                    int y = (int) Math.round(point.getY());
                    if (x >= 0 && x < displayImagePlus.getWidth() && y >= 0 && y < displayImagePlus.getHeight()) {
                        try {
                            outputObject.add(x, y, z - 1);
                        } catch (PointOutOfRangeException e) {
                        }
                    }
                }
            }
        }
    }

    public void processTemporalObjects() throws IntegerOverflowException {
        // Processing each list of Rois, then converting them to objects
        for (int ID : rois.keySet()) {
            ArrayList<ObjRoi> currentRois = rois.get(ID);
            // This Obj may be empty; if so, skip it
            if (currentRois.size() == 0)
                continue;

            // Creating current track object
            Obj outputTrack = new Obj(VolumeType.POINTLIST, outputTrackObjects.getName(), ID, calibration, nFrames);
            outputTrackObjects.add(outputTrack);

            // Creating the new object
            String type = parameters.getValue(VOLUME_TYPE);
            VolumeType volumeType = getVolumeType(type);

            HashMap<Integer, Obj> objectsByT = new HashMap<>();
            for (ObjRoi objRoi : currentRois) {
                int t = objRoi.getT();

                Obj outputObject;
                if (objectsByT.containsKey(t)) {
                    // Getting a previously-defined Obj
                    outputObject = objectsByT.get(t);
                } else {
                    // Creating a new object
                    outputObject = new Obj(volumeType, outputObjects.getName(), outputObjects.getAndIncrementID(),
                            calibration, nFrames);
                    outputObject.setT(t);
                    outputObject.addParent(outputTrack);
                    outputTrack.addChild(outputObject);

                    outputObjects.add(outputObject);
                    objectsByT.put(t, outputObject);
                }

                int z = objRoi.getZ();
                Roi roi = objRoi.getRoi();
                Point[] points = roi.getContainedPoints();
                for (Point point : points) {
                    int x = (int) Math.round(point.getX());
                    int y = (int) Math.round(point.getY());
                    if (x >= 0 && x < displayImagePlus.getWidth() && y >= 0 && y < displayImagePlus.getHeight()) {
                        try {
                            outputObject.add(x, y, z - 1);
                        } catch (PointOutOfRangeException e) {
                        }
                    }
                }
            }
        }
    }

    public void addObjectToList(ObjRoi objRoi, int ID) {
        listModel.addElement(objRoi);

        // Ensuring the scrollbar is visible if necessary and moving to the bottom
        JScrollBar scrollBar = objectsScrollPane.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum() - 1);
        objectsScrollPane.revalidate();

    }

    public void updateOverlay() {
        overlay.clear();

        // Adding existing overlay components        
        if (origOverlay != null)
            for (Roi roi : origOverlay)
                overlay.add(roi);

        // Adding overlays for selected objects
        if (rois != null)
            for (ArrayList<ObjRoi> groups : rois.values())
                for (ObjRoi objRoi : groups)
                    addToOverlay(objRoi);

    }

    public void addToOverlay(ObjRoi objRoi) {
        Roi roi = objRoi.getRoi();
        int ID = objRoi.getID();

        // Adding overlay showing ROI and its ID number
        overlay.add(ObjRoi.duplicateRoi(roi));

        double[] centroid = roi.getContourCentroid();
        TextRoi textRoi = new TextRoi(centroid[0], centroid[1], String.valueOf(ID));

        if (displayImagePlus.isHyperStack()) {
            textRoi.setPosition(1, displayImagePlus.getZ(), displayImagePlus.getT());
        } else {
            int pos = Math.max(Math.max(1, displayImagePlus.getZ()), displayImagePlus.getT());
            textRoi.setPosition(pos);
        }
        overlay.add(textRoi);
        displayImagePlus.updateAndDraw();

    }

    void displayObject(ObjRoi objRoi) {
        displayImagePlus.setRoi(ObjRoi.duplicateRoi(objRoi.getRoi()));
    }

    static class ObjRoi {
        private final int ID;
        private final Roi roi;
        private final int t;
        private final int z;

        ObjRoi(int ID, Roi roi, int t, int z) {
            this.ID = ID;
            this.roi = duplicateRoi(roi);
            this.t = t;
            this.z = z;

        }

        public static Roi duplicateRoi(Roi roi) {
            Roi newRoi;

            // Need to processAutomatic Roi depending on its type
            switch (roi.getType()) {
                case Roi.RECTANGLE:
                    newRoi = new Roi(roi.getBounds());
                    break;

                case Roi.OVAL:
                    Rectangle bounds = roi.getBounds();
                    newRoi = new OvalRoi(bounds.x, bounds.y, bounds.width, bounds.height);
                    break;

                case Roi.FREEROI:
                case Roi.POLYGON:
                case Roi.TRACED_ROI:
                    PolygonRoi polyRoi = (PolygonRoi) roi;
                    int[] x = polyRoi.getXCoordinates();
                    int[] xx = new int[x.length];
                    for (int i = 0; i < x.length; i++)
                        xx[i] = x[i] + (int) polyRoi.getXBase();

                    int[] y = polyRoi.getYCoordinates();
                    int[] yy = new int[x.length];
                    for (int i = 0; i < y.length; i++)
                        yy[i] = y[i] + (int) polyRoi.getYBase();

                    newRoi = new PolygonRoi(xx, yy, polyRoi.getNCoordinates(), roi.getType());
                    break;

                case Roi.FREELINE:
                case Roi.POLYLINE:
                    polyRoi = (PolygonRoi) roi;

                    if (polyRoi.getStrokeWidth() > 0)
                        MIA.log.writeWarning("Thick lines currently unsupported.  Using backbone only.");

                    x = polyRoi.getXCoordinates();
                    xx = new int[x.length];
                    for (int i = 0; i < x.length; i++)
                        xx[i] = x[i] + (int) polyRoi.getXBase();

                    y = polyRoi.getYCoordinates();
                    yy = new int[x.length];
                    for (int i = 0; i < y.length; i++)
                        yy[i] = y[i] + (int) polyRoi.getYBase();

                    newRoi = new PolygonRoi(xx, yy, polyRoi.getNCoordinates(), roi.getType());
                    break;

                case Roi.LINE:
                    Line line = (Line) roi;

                    if (line.getStrokeWidth() > 0)
                        MIA.log.writeWarning("Thick lines currently unsupported.  Using backbone only.");

                    newRoi = new Line(line.x1, line.y1, line.x2, line.y2);
                    break;

                case Roi.POINT:
                    PointRoi pointRoi = (PointRoi) roi;

                    Point[] points = pointRoi.getContainedPoints();
                    int[] xxx = new int[points.length];
                    int[] yyy = new int[points.length];
                    for (int i = 0; i < points.length; i++) {
                        xxx[i] = points[i].x;
                        yyy[i] = points[i].y;
                    }

                    newRoi = new PointRoi(xxx, yyy, points.length);
                    break;

                default:
                    MIA.log.writeWarning("ROI type unsupported.  Using bounding box for selection.");
                    newRoi = new Roi(roi.getBounds());
                    break;
            }

            return newRoi;

        }

        public int getID() {
            return ID;
        }

        public Roi getRoi() {
            return roi;
        }

        public int getT() {
            return t;
        }

        public int getZ() {
            return z;
        }

        @Override
        public String toString() {
            return "Object " + String.valueOf(ID) + ", T = " + (t + 1) + ", Z = " + z;
        }
    }
}