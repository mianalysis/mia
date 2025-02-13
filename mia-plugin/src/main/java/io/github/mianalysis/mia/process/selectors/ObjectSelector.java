package io.github.mianalysis.mia.process.selectors;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.drew.lang.annotations.Nullable;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.plugin.Duplicator;
import ij.process.BinaryInterpolator;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.module.objects.detect.extensions.ManualExtension;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.VolumeTypesInterface;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import io.github.mianalysis.mia.process.system.FileCrawler;

public class ObjectSelector implements ActionListener, KeyListener, MouseListener {
    private Objs outputObjects = null;
    private Objs outputTrackObjects = null;

    private boolean overflow = false;
    private String saveObjectsPath = null;
    private HashSet<ManualExtension> extensions = new HashSet<>();

    private JFrame frame;
    private JMenuBar menuBar;
    private JTextField objectNumberField;
    private JComboBox<String> autoAcceptMode;
    private CustomListModel<ObjRoi> listModel = new CustomListModel<>();
    private JList<ObjRoi> list = new JList<>(listModel);
    private JScrollPane objectsScrollPane = new JScrollPane(list);
    private JComboBox<String> overlayMode;
    private JPanel colourPanel;
    private JComboBox<String> colourMode;
    private JCheckBox labelCheck;
    private JPanel fontPanel;
    private JTextField labelFontSize;

    private ImagePlus displayIpl;
    private Overlay overlay;
    private Overlay origOverlay;
    private HashMap<Integer, ArrayList<ObjRoi>> rois;
    private int maxID;
    private String pointMode;
    private String volumeTypeString;
    private ClassSelector classSelector;
    private ArrayList<JPanel> extraPanels = new ArrayList<>();
    private int gridWidth = 4;
    private String previousOverlayMode = OverlayModes.NONE;
    private static int x0 = Prefs.getInt("MIA.ObjectSelector.x0", 100);
    private static int y0 = Prefs.getInt("MIA.ObjectSelector.y0", 100);

    // Menu bar options
    private static final String FILE = "File";
    private static final String LOAD_OBJECTS = "Load objects";
    private static final String SAVE_OBJECTS = "Save objects";
    private static final String SAVE_OBJECTS_AS = "Save objects as...";
    private static final String TOOLS = "Tools";
    private static final String SELECT_EMPTY_SPACE_AT_CLICK = "Select unlabelled space at click";
    private static final String SELECT_EMPTY_SPACE_IN_REGION = "Select unlabelled space in region";

    // GUI buttons
    private static final String ADD_NEW = "Add as new object (F1)";
    private static final String ADD_EXISTING = "Add to existing object (F2)";
    private static final String REMOVE = "Remove object(s) (F3) ";
    private static final String FINISH = "Finish adding objects (F4)";
    private static final String CHANGE_CLASS = "Change object class (F5)";

    public interface AutoAcceptModes {
        String DO_NOTHING = "Do nothing";
        String ADD_AS_NEW_OBJECT = "Add as new object";
        String ADD_TO_EXISTING_OBJECT = "Add to existing object";

        String[] ALL = new String[] { DO_NOTHING, ADD_AS_NEW_OBJECT, ADD_TO_EXISTING_OBJECT };

    }

    public interface OverlayModes {
        String NONE = "None";
        String FILL = "Fill";
        String OUTLINES = "Outlines";

        String[] ALL = new String[] { NONE, FILL, OUTLINES };

    }

    public interface ColourModesNoClass {
        String BY_ID = "By ID";
        String BLACK = "Black";
        String BLUE = "Blue";
        String CYAN = "Cyan";
        String GREEN = "Green";
        String MAGENTA = "Magenta";
        String RED = "Red";
        String WHITE = "White";
        String YELLOW = "Yellow";

        String ALL[] = new String[] { BY_ID, BLACK, BLUE, CYAN, GREEN, MAGENTA, RED, WHITE, YELLOW };

    }

    private interface ColourModesWithClass extends ColourModesNoClass {
        String BY_CLASS = "By class";

        String ALL[] = new String[] { BY_CLASS, BY_ID, BLACK, BLUE, CYAN, GREEN, MAGENTA, RED, WHITE, YELLOW };

    }

    public interface PointModes {
        String INDIVIDUAL_OBJECTS = "Individual objects";
        String SINGLE_OBJECT = "Single object";

        String[] ALL = new String[] { INDIVIDUAL_OBJECTS, SINGLE_OBJECT };

    }

    public interface ObjMetadataItems {
        String CLASS = "CLASS";
    }

    public void setClassSelector(ClassSelector classSelector) {
        this.classSelector = classSelector;
    }

    public void addExtraPanel(JPanel extraPanel) {
        extraPanels.add(extraPanel);
    }

    public void setExtensions(HashSet<ManualExtension> extensions) {
        this.extensions = extensions;
    }

    public void addExtension(ManualExtension extension) {
        extensions.add(extension);
    }

    public void initialise(ImagePlus inputIpl, String outputObjectsName, String messageOnImage, String instructionText,
            String volumeTypeString, String pointMode, @Nullable String outputTrackObjectsName,
            boolean showControlOnCreation) {
        this.pointMode = pointMode;
        this.volumeTypeString = volumeTypeString;

        if (classSelector != null)
            gridWidth = 5;

        displayIpl = new Duplicator().run(inputIpl);
        displayIpl.setCalibration(null);
        displayIpl.setTitle(messageOnImage);
        displayIpl.setDisplayMode(CompositeImage.COMPOSITE);

        // Clearing any ROIs stored from previous runs
        rois = new HashMap<>();
        listModel.clear();

        origOverlay = displayIpl.getOverlay();
        overlay = new Overlay();
        displayIpl.setOverlay(overlay);
        displayIpl.setHideOverlay(false);
        updateOverlay();

        // Initialising output objects
        outputObjects = new Objs(outputObjectsName, inputIpl);
        if (outputTrackObjectsName != null)
            outputTrackObjects = new Objs(outputTrackObjectsName, inputIpl);

        // Displaying the image and showing the control
        createControlPanel(instructionText);
        frame.setVisible(showControlOnCreation);

    }

    public void setControlPanelVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public void setImageVisible(boolean visible) {
        if (visible) {
            displayIpl.show();

            displayIpl.getWindow().getCanvas().addMouseListener(this);

            boolean listenerExists = false;
            for (KeyListener listener:displayIpl.getWindow().getCanvas().getKeyListeners())
                if (listener == this) {
                    listenerExists = true;
                    break;
                }

            if (!listenerExists)
                displayIpl.getWindow().getCanvas().addKeyListener(this);
            
        } else {
            displayIpl.hide();
        }
    }

    public boolean isVisible() {
        return frame.isVisible();
    }

    public boolean isActive() {
        return frame != null;
    }

    public ImagePlus getDisplayIpl() {
        return displayIpl;
    }

    public Objs getObjects() {
        return outputObjects;
    }

    public Objs getTrackObjects() {
        return outputTrackObjects;
    }

    public String getAutoAcceptMode() {
        return (String) autoAcceptMode.getSelectedItem();
    }

    private void createControlPanel(String instructionText) {
        rois = new HashMap<>();
        maxID = 0;

        frame = new JFrame();

        JMenu fileMenu = new JMenu(FILE);
        JMenuItem loadMenuItem = new JMenuItem(LOAD_OBJECTS);
        loadMenuItem.addActionListener(this);
        fileMenu.add(loadMenuItem);
        JMenuItem saveMenuItem = new JMenuItem(SAVE_OBJECTS);
        saveMenuItem.addActionListener(this);
        fileMenu.add(saveMenuItem);
        JMenuItem saveAsMenuItem = new JMenuItem(SAVE_OBJECTS_AS);
        saveAsMenuItem.addActionListener(this);
        fileMenu.add(saveAsMenuItem);

        JMenu toolsMenu = new JMenu(TOOLS);
        JMenuItem toolMenuItem = new JMenuItem(SELECT_EMPTY_SPACE_AT_CLICK);
        toolMenuItem.addActionListener(this);
        toolsMenu.add(toolMenuItem);

        toolMenuItem = new JMenuItem(SELECT_EMPTY_SPACE_IN_REGION);
        toolMenuItem.addActionListener(this);
        toolsMenu.add(toolMenuItem);

        menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        frame.setJMenuBar(menuBar);

        frame.setAlwaysOnTop(true);
        frame.setFocusable(true);
        frame.addKeyListener(this);
        list.addKeyListener(this);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                processObjectsAndFinish();
            };
        });

        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                displayObjects(list.getSelectedValuesList());
            }
        });

        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = gridWidth;
        c.gridheight = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 5, 5, 5);

        instructionText = instructionText.replace("\n", "<br>");
        JLabel headerLabel = new JLabel("<html>" + instructionText + "</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        frame.add(headerLabel, c);

        JLabel sliceNotice = new JLabel(
                "Note: ROIs must be selected slice-by-slice.  Slices can be combined in 3D using \"" + ADD_EXISTING
                        + "\".");
        c.gridy++;
        frame.add(sliceNotice, c);

        JButton newObjectButton = new JButton(ADD_NEW);
        newObjectButton.addActionListener(this);
        newObjectButton.setActionCommand(ADD_NEW);
        c.gridy++;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        frame.add(newObjectButton, c);

        JButton existingObjectButton = new JButton(ADD_EXISTING);
        existingObjectButton.addActionListener(this);
        existingObjectButton.setActionCommand(ADD_EXISTING);
        c.gridx++;
        frame.add(existingObjectButton, c);

        JButton removeObjectButton = new JButton(REMOVE);
        removeObjectButton.addActionListener(this);
        removeObjectButton.setActionCommand(REMOVE);
        c.gridx++;
        frame.add(removeObjectButton, c);

        JButton finishButton = new JButton(FINISH);
        finishButton.addActionListener(this);
        finishButton.setActionCommand(FINISH);
        c.gridx++;
        frame.add(finishButton, c);

        if (classSelector != null) {
            JButton changeObjectClassButton = new JButton(CHANGE_CLASS);
            changeObjectClassButton.addActionListener(this);
            changeObjectClassButton.setActionCommand(CHANGE_CLASS);
            c.gridx++;
            frame.add(changeObjectClassButton, c);
        }

        // Object number panel
        JLabel objectNumberLabel = new JLabel("Existing object number");
        objectNumberLabel.setHorizontalAlignment(JLabel.RIGHT);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        frame.add(objectNumberLabel, c);

        objectNumberField = new JTextField();
        c.gridx++;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        frame.add(objectNumberField, c);

        // Auto accept panel
        JLabel autoAcceptLabel = new JLabel("When region drawn");
        autoAcceptLabel.setHorizontalAlignment(JLabel.RIGHT);
        c.gridx = gridWidth - 2;
        c.anchor = GridBagConstraints.EAST;
        frame.add(autoAcceptLabel, c);

        autoAcceptMode = new JComboBox<>(AutoAcceptModes.ALL);
        autoAcceptMode.setSelectedItem(Prefs.get("MIA.ObjectSelector.AutoAccept", AutoAcceptModes.DO_NOTHING));
        autoAcceptMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Prefs.set("MIA.ObjectSelector.AutoAccept", (String) autoAcceptMode.getSelectedItem());
            }
        });
        c.gridx++;
        c.anchor = GridBagConstraints.WEST;
        frame.add(autoAcceptMode, c);

        // Extra tools panels
        for (JPanel extraPanel : extraPanels) {
            c.gridx = 0;
            c.gridy++;
            c.gridwidth = gridWidth;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(0, 0, 0, 0);

            frame.add(extraPanel, c);

        }

        c.insets = new Insets(5, 5, 5, 5);

        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        objectsScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = gridWidth;
        c.gridheight = 3;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        frame.add(objectsScrollPane, c);

        JPanel overlayPanel = createOverlayPanel();
        c.gridy++;
        c.gridy++;
        c.gridy++;
        c.gridwidth = 5;
        c.gridheight = 1;
        c.weighty = 0;
        frame.add(overlayPanel, c);

        displayIpl.setHideOverlay(!overlayMode.equals(OverlayModes.NONE));

        frame.setMinimumSize(new Dimension(800, 480));
        frame.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
        frame.pack();
        frame.setLocation(new Point(x0, y0));
        frame.setResizable(true);
        frame.setVisible(true);

    }

    protected JPanel createOverlayPanel() {
        JPanel overlayPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;

        overlayMode = new JComboBox<>(OverlayModes.ALL);
        overlayMode.setSelectedItem(Prefs.get("MIA.ObjectSelector.OverlayMode", OverlayModes.FILL));
        overlayMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Prefs.set("MIA.ObjectSelector.OverlayMode", (String) overlayMode.getSelectedItem());

                boolean showOverlay = !overlayMode.getSelectedItem().equals(OverlayModes.NONE);

                displayIpl.setHideOverlay(!showOverlay);
                Arrays.stream(colourPanel.getComponents()).forEach(v -> v.setEnabled(showOverlay));
                labelCheck.setEnabled(showOverlay);
                if (labelCheck.isSelected())
                    Arrays.stream(fontPanel.getComponents()).forEach(v -> v.setEnabled(showOverlay));

                if (showOverlay & !overlayMode.getSelectedItem().equals(previousOverlayMode)) {
                    previousOverlayMode = (String) overlayMode.getSelectedItem();
                    updateOverlay();
                }
            }
        });

        overlayPanel.add(new JLabel("Overlay"), c);
        c.gridx++;
        overlayPanel.add(overlayMode, c);

        colourPanel = new JPanel();
        colourPanel.add(new JLabel("Colour mode"));
        if (classSelector == null) {
            colourMode = new JComboBox<>(ColourModesNoClass.ALL);
            try {
                colourMode.setSelectedItem(Prefs.get("MIA.ObjectSelector.ColourModeNoClass", ColourModesNoClass.BY_ID));
            } catch (Exception e) {
                colourMode.setSelectedItem(ColourModesNoClass.BY_ID);
            }
        } else {
            colourMode = new JComboBox<>(ColourModesWithClass.ALL);
            colourMode.setSelectedItem(
                    Prefs.get("MIA.ObjectSelector.ColourModeWithClass", ColourModesWithClass.BY_CLASS));
        }
        colourMode.setEnabled(true);
        colourMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (classSelector == null)
                    Prefs.set("MIA.ObjectSelector.ColourModeNoClass", (String) colourMode.getSelectedItem());
                else
                    Prefs.set("MIA.ObjectSelector.ColourModeWithClass", (String) colourMode.getSelectedItem());

                updateOverlay();
            }
        });
        colourPanel.add(colourMode);

        c.gridx++;
        overlayPanel.add(colourPanel, c);

        labelCheck = new JCheckBox("Show labels");
        labelCheck.setSelected(Prefs.get("MIA.ObjectSelector.ShowLabels", true));
        labelCheck.setEnabled(true);
        labelCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Prefs.set("MIA.ObjectSelector.ShowLabels", labelCheck.isSelected());
                Arrays.stream(fontPanel.getComponents()).forEach(v -> v.setEnabled(labelCheck.isSelected()));

                updateOverlay();
            }
        });
        c.gridx++;
        overlayPanel.add(labelCheck, c);

        fontPanel = new JPanel();
        fontPanel.add(new JLabel("Font size"));
        int defaultSize = (int) Math
                .round(Math.max(12, Math.max(displayIpl.getWidth(), displayIpl.getHeight()) / 50));
        labelFontSize = new JTextField(String.valueOf(defaultSize));
        labelFontSize.setEnabled(true);
        labelFontSize.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                updateOverlay();
            }

        });
        fontPanel.add(labelFontSize);
        c.gridx++;
        overlayPanel.add(fontPanel, c);

        return overlayPanel;

    }

    public static void applyTemporalInterpolation(Objs inputObjects, Objs trackObjects, String type)
            throws IntegerOverflowException {
        for (Obj trackObj : trackObjects.values()) {
            // Keeping a record of frames which have an object (these will be unchanged, so
            // don't need a new object)
            ArrayList<Integer> timepoints = new ArrayList<>();

            // Creating a blank image for this track
            Image binaryImage = trackObj.getAsImage("Track", false);

            // Adding each timepoint object (child) to this image
            for (Obj childObj : trackObj.getChildren(inputObjects.getName()).values()) {
                childObj.addToImage(binaryImage, Float.MAX_VALUE);
                timepoints.add(childObj.getT());
            }

            applyTemporalInterpolation(binaryImage);

            // Converting binary image back to objects
            Objs interpObjs = binaryImage.convertImageToObjects(type, inputObjects.getName(), true);

            // Transferring new timepoint objects to inputObjects Objs
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
            ImagePlus sliceIpl = ExtractSubstack.extractSubstack(binaryIpl, "Substack", "1-1", z + "-" + z,
                    "1-" + nFrames);
            // ImagePlus sliceIpl = SubHyperstackMaker.makeSubhyperstack(binaryIpl, "1-1", z
            // + "-" + z, "1-" + nFrames);
            ImageStack sliceIst = sliceIpl.getStack();

            if (!checkStackForInterpolation(sliceIst))
                continue;

            binaryInterpolator.run(sliceIst);

            // Replacing interpolated slices in binaryImage
            for (int t = 1; t <= sliceIst.size(); t++) {
                int idx = binaryIpl.getStackIndex(1, z, t);
                binaryIpl.getStack().setProcessor(sliceIst.getProcessor(t), idx);
            }
        }
    }

    /**
     * Verifies that at least two images in the stack contain non-zero pixels
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
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case (ADD_NEW):
                new Thread(() -> {
                    addNewObject();
                }).start();
                break;

            case (ADD_EXISTING):
                addToExistingObject();
                break;

            case (CHANGE_CLASS):
                new Thread(() -> {
                    changeObjectClass();
                }).start();
                break;

            case (REMOVE):
                removeObjects();
                break;

            case (FINISH):
                processObjectsAndFinish();
                break;

            case (LOAD_OBJECTS):
                loadObjects();
                break;

            case (SAVE_OBJECTS):
                if (saveObjectsPath == null)
                    saveObjectsPath = getSavePath();
                saveObjects(saveObjectsPath);
                break;

            case (SAVE_OBJECTS_AS):
                saveObjectsPath = getSavePath();
                saveObjects(saveObjectsPath);
                break;

            case (SELECT_EMPTY_SPACE_AT_CLICK):
                new NonOverlaySelector(displayIpl);
                break;

            case (SELECT_EMPTY_SPACE_IN_REGION):
                Roi currRoi = displayIpl.getRoi();
                if (currRoi == null)
                    MIA.log.writeMessage("Please select a region of interest first");

                ImagePlus binaryIpl = NonOverlaySelector.convertOverlayToBinary(displayIpl);
                Roi selectedRoi = NonOverlaySelector.getRegionInsideRoi(binaryIpl, currRoi);

                if (selectedRoi != null)
                    displayIpl.setRoi(selectedRoi);

        }
    }

    public void addNewObject() {
        // Getting the ROI
        Roi roi = displayIpl.getRoi();

        if (roi == null)
            return;

        if (roi.getType() == Roi.POINT)
            switch (pointMode) {
                case PointModes.INDIVIDUAL_OBJECTS:
                    Point[] points = ((PointRoi) roi).getContainedPoints();
                    for (Point point : points)
                        addSingleRoi(new PointRoi(new int[] { point.x }, new int[] { point.y }, 1));
                    break;
                case PointModes.SINGLE_OBJECT:
                    addSingleRoi(roi);
                    break;
            }
        else
            addSingleRoi(roi);

        // Displaying the ROI on the overlay
        updateOverlay();

        // Setting the number field to this number
        objectNumberField.setText(String.valueOf(maxID));

        // Deselecting the current ROI. This can be re-enabled by selecting it from the
        // list.
        displayIpl.killRoi();

    }

    public void addSingleRoi(Roi roi) {
        int ID = ++maxID;
        ArrayList<ObjRoi> currentRois = new ArrayList<>();

        String assignedClass = null;
        if (classSelector != null) {
            classSelector.setVisible(true);

            while (classSelector.isActive())
                try {
                    Thread.sleep(100);
                } catch (Exception e) {

                }
            assignedClass = classSelector.getLastSelectedClass();
        }

        ObjRoi objRoi = new ObjRoi(ID, roi, displayIpl.getT() - 1, displayIpl.getZ() - 1, assignedClass);
        currentRois.add(objRoi);
        rois.put(ID, currentRois);

        addObjectToList(objRoi, ID);

    }

    public void addToExistingObject() {
        // If there are no existing objects, add as new object
        if (rois.size() == 0 || objectNumberField.getText().equals("")) {
            MIA.log.writeDebug("No ROIs, so adding as new object");
            new Thread(() -> {
                addNewObject();
            }).start();
            return;
        }

        int ID = -1;
        try {
            ID = Integer.parseInt(objectNumberField.getText());
        } catch (NumberFormatException e) {
            MIA.log.writeDebug("No existing object number present, so adding as new object");
            new Thread(() -> {
                addNewObject();
            }).start();
            return;
        }

        // Getting points
        Roi roi = displayIpl.getRoi();

        if (roi == null) {
            frame.setAlwaysOnTop(false);
            IJ.error("Select a ROI first using the ImageJ ROI tools.");
            frame.setAlwaysOnTop(true);
            return;
        }

        // Adding the ROI to our current collection
        ArrayList<ObjRoi> currentRois = rois.get(ID);
        String assignedClass = null;
        for (ObjRoi currentRoi : currentRois) {
            if (currentRoi.getAssignedClass() != null) {
                assignedClass = currentRoi.getAssignedClass();
                break;
            }
        }

        // If there's already a ROI in this image with the same ID, combine them
        int t = displayIpl.getT() - 1;
        int z = displayIpl.getZ() - 1;
        ListIterator<ObjRoi> iterator = currentRois.listIterator();
        while (iterator.hasNext()) {
            ObjRoi currentRoi = iterator.next();
            if (currentRoi.getT() == t && currentRoi.getZ() == z) {
                currentRoi.setRoi(new ShapeRoi(roi).or(new ShapeRoi(currentRoi.getRoi())));
                updateOverlay();
                return;
            }
        }

        // If no ROI was found, this will add it as a new ROI
        ObjRoi objRoi = new ObjRoi(ID, roi, displayIpl.getT() - 1, displayIpl.getZ() - 1,
                assignedClass);
        iterator.add(objRoi);
        rois.put(ID, currentRois);

        addObjectToList(objRoi, ID);
        updateOverlay();

    }

    public void changeObjectClass() {
        classSelector.setVisible(true);

        // If a ClassSelector has been provided, show it and wait for response
        String assignedClass = null;
        if (classSelector != null) {
            while (classSelector.isActive())
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            assignedClass = classSelector.getLastSelectedClass();

        }

        // Get selected ROIs
        List<ObjRoi> selected = list.getSelectedValuesList();

        for (ObjRoi objRoi : selected)
            objRoi.setAssignedClass(assignedClass);

        listModel.redraw();

        updateOverlay();

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
        Point location = frame.getLocation();
        x0 = (int) Math.round(location.getX());
        y0 = (int) Math.round(location.getY());
        Prefs.set("MIA.ObjectSelector.x0", x0);
        Prefs.set("MIA.ObjectSelector.y0", y0);

        // If the frame isn't already closed, hide it while we finish processing the
        // objects
        if (frame != null)
            frame.setVisible(false);

        displayIpl.changes = false;
        displayIpl.close();

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
    }

    public void processSpatialOnlyObjects() throws IntegerOverflowException {
        // Processing each list of Rois, then converting them to objects
        for (int ID : rois.keySet()) {
            ArrayList<ObjRoi> currentRois = rois.get(ID);

            // This Obj may be empty; if so, skip it
            if (currentRois.size() == 0)
                continue;

            // Creating the new object
            VolumeType volumeType = VolumeTypesInterface.getVolumeType(volumeTypeString);
            Obj outputObject = outputObjects.createAndAddNewObject(volumeType, ID);

            for (ObjRoi objRoi : currentRois) {
                Roi roi = objRoi.getRoi();
                Point[] points = roi.getContainedPoints();

                int t = objRoi.getT();
                int z = objRoi.getZ();

                outputObject.setT(t);
                for (Point point : points) {
                    int x = (int) Math.round(point.getX());
                    int y = (int) Math.round(point.getY());
                    if (x >= 0 && x < displayIpl.getWidth() && y >= 0 && y < displayIpl.getHeight()) {
                        try {
                            outputObject.add(x, y, z);
                        } catch (PointOutOfRangeException e) {
                        }
                    }
                }

                if (objRoi.getAssignedClass() != null)
                    outputObject.addMetadataItem(new ObjMetadata(ObjMetadataItems.CLASS, objRoi.getAssignedClass()));

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
            Obj outputTrack = outputTrackObjects.createAndAddNewObject(VolumeType.POINTLIST, ID);

            // Creating the new object
            VolumeType volumeType = VolumeTypesInterface.getVolumeType(volumeTypeString);

            HashMap<Integer, Obj> objectsByT = new HashMap<>();
            for (ObjRoi objRoi : currentRois) {
                int t = objRoi.getT();

                Obj outputObject;
                if (objectsByT.containsKey(t)) {
                    // Getting a previously-defined Obj
                    outputObject = objectsByT.get(t);
                } else {
                    // Creating a new object
                    outputObject = outputObjects.createAndAddNewObject(volumeType);
                    outputObject.setT(t);
                    outputObject.addParent(outputTrack);
                    outputTrack.addChild(outputObject);

                    objectsByT.put(t, outputObject);
                }

                int z = objRoi.getZ();
                Roi roi = objRoi.getRoi();
                Point[] points = roi.getContainedPoints();
                for (Point point : points) {
                    int x = (int) Math.round(point.getX());
                    int y = (int) Math.round(point.getY());
                    if (x >= 0 && x < displayIpl.getWidth() && y >= 0 && y < displayIpl.getHeight()) {
                        try {
                            outputObject.add(x, y, z);
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
        Roi overlayRoi = ObjRoi.duplicateRoi(roi);
        if (displayIpl.isHyperStack())
            overlayRoi.setPosition(1, objRoi.getZ() + 1, objRoi.getT() + 1);
        else
            overlayRoi.setPosition(Math.max(Math.max(1, objRoi.getZ() + 1), objRoi.getT() + 1));

        // Setting colour
        Color colour = null;
        switch ((String) colourMode.getSelectedItem()) {
            case ColourModesWithClass.BY_CLASS:
                float hue = objRoi.getAssignedClass() == null ? 0.167f
                        : new Random(objRoi.getAssignedClass().hashCode() * 2000 * 31).nextFloat();
                colour = Color.getHSBColor(hue, 1, 1);
                break;
            case ColourModesWithClass.BY_ID:
            default:
                hue = new Random(objRoi.getID() * 1000 * 31).nextFloat();
                colour = Color.getHSBColor(hue, 1, 1);
                break;
            case ColourModesWithClass.BLACK:
                colour = Color.BLACK;
                break;
            case ColourModesWithClass.BLUE:
                colour = Color.BLUE;
                break;
            case ColourModesWithClass.CYAN:
                colour = Color.CYAN;
                break;
            case ColourModesWithClass.GREEN:
                colour = Color.GREEN;
                break;
            case ColourModesWithClass.MAGENTA:
                colour = Color.MAGENTA;
                break;
            case ColourModesWithClass.RED:
                colour = Color.RED;
                break;
            case ColourModesWithClass.WHITE:
                colour = Color.WHITE;
                break;
            case ColourModesWithClass.YELLOW:
                colour = Color.YELLOW;
                break;
        }

        switch (overlayRoi.getType()) {
            // Lines are a special case when it comes to rendering
            case Roi.LINE:
            case Roi.FREELINE:
            case Roi.POLYLINE:
                overlayRoi.setStrokeColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), 64));
                break;

            // Everything else is rendered as the normal fill or outlines
            default:
                switch ((String) overlayMode.getSelectedItem()) {
                    case OverlayModes.FILL:
                        overlayRoi.setFillColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), 64));
                        break;
                    case OverlayModes.OUTLINES:
                        overlayRoi.setStrokeColor(colour);
                        break;
                }
                break;
        }

        if (!overlayMode.equals(OverlayModes.NONE))
            overlay.add(overlayRoi);

        // Adding label (if necessary)
        if (labelCheck.isSelected()) {
            double[] centroid = roi.getContourCentroid();
            int fontSize = Integer.parseInt(labelFontSize.getText());

            TextRoi text = new TextRoi(centroid[0], centroid[1], String.valueOf(ID),
                    new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));

            // Setting stack location
            if (displayIpl.isHyperStack())
                text.setPosition(1, objRoi.getZ() + 1, objRoi.getT() + 1);
            else
                text.setPosition(Math.max(Math.max(1, objRoi.getZ() + 1), objRoi.getT() + 1));

            // Ensuring text is in the centred
            text.setLocation(text.getXBase() - text.getFloatWidth() / 2 + 1,
                    text.getYBase() - text.getFloatHeight() / 2 + 1);

            overlay.add(text);

        }

        displayIpl.updateAndDraw();

    }

    void displayObjects(List<ObjRoi> objRois) {
        ShapeRoi roi = null;
        for (ObjRoi objRoi : objRois) {
            if (roi == null) {
                roi = new ShapeRoi(objRoi.getRoi());
                continue;
            }

            roi.or(new ShapeRoi(objRoi.getRoi()));
        }

        displayIpl.setRoi(roi);

    }

    String getSavePath() {
        String previousPath = Prefs.get("MIA.PreviousPath", "");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save objects to file");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (previousPath != null) {
            String path = new File(previousPath).getPath();
            path = FileCrawler.checkPath(path);
            fileChooser.setCurrentDirectory(new File(path));
        }
        fileChooser.showDialog(null, "Save");

        if (fileChooser.getSelectedFile() == null)
            return null;

        String outPath = fileChooser.getSelectedFile().getAbsolutePath();

        if (!outPath.substring(0, outPath.length() - 4).equals(".zip"))
            outPath = outPath + ".zip";

        return outPath;

    }

    void saveObjects(String outPath) {
        try {
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outPath)));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(zos));
            RoiEncoder re = new RoiEncoder(out);

            for (int ID : rois.keySet()) {
                ArrayList<ObjRoi> currentRois = rois.get(ID);

                // This Obj may be empty; if so, skip it
                if (currentRois.size() == 0)
                    continue;

                for (ObjRoi currentRoi : currentRois) {
                    String label = currentRoi.getShortString() + ".roi";

                    Roi roi = currentRoi.getRoi();
                    roi.setPosition(1, currentRoi.getZ() - 1, currentRoi.getT() - 1);

                    zos.putNextEntry(new ZipEntry(label));
                    re.write(roi);
                    out.flush();
                }

            }

            out.close();

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    String getLoadPath() {
        String previousPath = Prefs.get("MIA.PreviousPath", "");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Objects file to load");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (previousPath != null) {
            String path = new File(previousPath).getPath();
            path = FileCrawler.checkPath(path);
            fileChooser.setCurrentDirectory(new File(path));
        }
        fileChooser.showDialog(null, "Load");

        if (fileChooser.getSelectedFile() == null)
            return null;

        return fileChooser.getSelectedFile().getAbsolutePath();

    }

    public void addObjects(Objs inputObjects, @Nullable String metadataForClass) {
        TreeSet<String> existingClasses = classSelector != null ? new TreeSet<>() : null;

        for (Obj inputObject : inputObjects.values()) {
            String assignedClass = null;
            if (metadataForClass != null) {
                ObjMetadata metadataItem = inputObject.getMetadataItem(metadataForClass);
                if (metadataItem == null)
                    assignedClass = "None";
                else
                    assignedClass = metadataItem.getValue();
            }

            if (existingClasses != null && assignedClass != null)
                existingClasses.add(assignedClass);

            int ID = inputObject.getID();
            rois.putIfAbsent(ID, new ArrayList<ObjRoi>());

            ArrayList<ObjRoi> currentRois = rois.get(ID);
            for (Entry<Integer, Roi> entry : inputObject.getRois().entrySet()) {
                ObjRoi objRoi = new ObjRoi(ID, entry.getValue(), inputObject.getT(), entry.getKey(), assignedClass);
                currentRois.add(objRoi);

                // Adding to the list of objects
                addObjectToList(objRoi, ID);

            }

            // Updating maxID if necessary
            maxID = Math.max(maxID, ID);

        }

        if (classSelector != null)
            classSelector.addExistingClasses(existingClasses);

        updateOverlay();

    }

    void loadObjects() {
        String inPath = getLoadPath();

        Pattern pattern = Pattern.compile("ID([0-9]+)_TR([\\-[0-9]]+)_T([0-9]+)_Z([0-9]+)_?(.*)");

        TreeSet<String> existingClasses = classSelector != null ? new TreeSet<>() : null;

        try {
            ZipInputStream in = new ZipInputStream(new FileInputStream(inPath));
            byte[] buf = new byte[1024];
            int len;
            ZipEntry entry = in.getNextEntry();
            while (entry != null) {
                String name = entry.getName();

                if (name.endsWith(".roi")) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    while ((len = in.read(buf)) > 0)
                        out.write(buf, 0, len);
                    out.close();
                    byte[] bytes = out.toByteArray();
                    RoiDecoder rd = new RoiDecoder(bytes, name);
                    Roi roi = rd.getRoi();
                    if (roi != null) {
                        name = name.substring(0, name.length() - 4);

                        Matcher matcher = pattern.matcher(name);
                        if (matcher.matches()) {
                            int ID = Integer.parseInt(matcher.group(1));
                            // No need to load track (group 2)
                            int t = Integer.parseInt(matcher.group(3)) - 1;
                            int z = Integer.parseInt(matcher.group(4)) - 1;
                            String assignedClass = matcher.group(5);

                            if (existingClasses != null)
                                existingClasses.add(assignedClass);

                            rois.putIfAbsent(ID, new ArrayList<ObjRoi>());

                            ArrayList<ObjRoi> currentRois = rois.get(ID);
                            ObjRoi objRoi = new ObjRoi(ID, roi, t, z, assignedClass);
                            currentRois.add(objRoi);

                            // Adding to the list of objects
                            addObjectToList(objRoi, ID);

                            // Updating maxID if necessary
                            maxID = Math.max(maxID, ID);

                        }
                    }
                }
                entry = in.getNextEntry();
            }

            in.close();

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (classSelector != null)
            classSelector.addExistingClasses(existingClasses);

        // Displaying the ROI on the overlay
        updateOverlay();

    }

    public boolean hadOverflow() {
        return overflow;
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        String keyText = arg0.getKeyText(arg0.getKeyCode());

        switch (keyText) {
            case "F1":
                addNewObject();
                return;

            case "F2":
                addToExistingObject();
                return;

            case "F3":
                removeObjects();
                return;

            case "F4":
                processObjectsAndFinish();
                return;

            case "F5":
                if (classSelector != null)
                    changeObjectClass();
                return;
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    public class CustomListModel<E> extends DefaultListModel<E> {
        public void redraw() {
            fireContentsChanged(this, 0, getSize());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Checking if any extension prevents auto accepting. This may be because that
        // extension will itself automatically accept the object.
        for (ManualExtension extension : extensions)
            if (extension.skipAutoAccept())
                return;

        switch ((String) autoAcceptMode.getSelectedItem()) {
            case AutoAcceptModes.DO_NOTHING:
            default:
                // No action required
                break;
            case AutoAcceptModes.ADD_AS_NEW_OBJECT:
                new Thread(() -> {
                    addNewObject();
                }).start();
                break;
            case AutoAcceptModes.ADD_TO_EXISTING_OBJECT:
                addToExistingObject();
                break;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}