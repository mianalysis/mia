package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.*;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import ij.process.BinaryInterpolator;
import ij.process.LUT;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.LUTs;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 27/02/2018.
 */
public class ManuallyIdentifyObjects extends Module implements ActionListener {
    private JFrame frame;
    private JTextField objectNumberField;
    private DefaultListModel<ObjRoi> listModel = new DefaultListModel<>();
    private JList<ObjRoi> list = new JList<>(listModel);
    private JScrollPane objectsScrollPane = new JScrollPane(list);

    private Workspace workspace;
    private ImagePlus displayImagePlus;
    private Overlay overlay;
    private HashMap<Integer,ArrayList<ObjRoi>> rois;
    private int maxID;

    private String outputObjectsName;
    private ObjCollection outputObjects;

    private double dppXY;
    private double dppZ;
    private String calibrationUnits;
    private boolean twoD;
    private boolean overflow = false;

    private int elementHeight = 40;

    private static final String ADD_NEW = "Add new";
    private static final String ADD_EXISTING = "Add existing";
    private static final String REMOVE = "Remove";
    private static final String FINISH = "Finish";

    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String INTERPOLATION_MODE = "Interpolation mode";

    public ManuallyIdentifyObjects(ModuleCollection modules) {
        super("Manually identify objects",modules);
    }


    public interface InterpolationModes {
        String NONE = "None";
        String SPATIAL = "Spatial";
        String TEMPORAL = "Temporal";
        String SPATIAL_AND_TEMPORAL = "Spatial and temporal";

        String[] ALL = new String[]{NONE, SPATIAL, TEMPORAL, SPATIAL_AND_TEMPORAL};

    }


    private void showOptionsPanel() {
        rois = new HashMap<>();
        maxID = 0;
        frame = new JFrame();
        frame.setAlwaysOnTop(true);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                List<ObjRoi> selected = list.getSelectedValuesList();
                for (ObjRoi objRoi:selected) displayObject(objRoi);
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
        c.insets = new Insets(5,5,5,5);

        JLabel headerLabel = new JLabel("<html>Draw round an object, then select one of the following" +
                "<br>(or click \"Finish adding objects\" at any time)." +
                "<br>Different timepoints must be added as new objects.</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        frame.add(headerLabel,c);

        JButton newObjectButton = new JButton("Add as new object");
        newObjectButton.addActionListener(this);
        newObjectButton.setActionCommand(ADD_NEW);
        c.gridy++;
        c.gridwidth = 1;
        frame.add(newObjectButton,c);

        JButton existingObjectButton = new JButton("Add to existing object");
        existingObjectButton.addActionListener(this);
        existingObjectButton.setActionCommand(ADD_EXISTING);
        c.gridx++;
        frame.add(existingObjectButton,c);

        JButton removeObjectButton = new JButton("Remove object (s)");
        removeObjectButton.addActionListener(this);
        removeObjectButton.setActionCommand(REMOVE);
        c.gridx++;
        frame.add(removeObjectButton,c);

        JButton finishButton = new JButton("Finish adding objects");
        finishButton.addActionListener(this);
        finishButton.setActionCommand(FINISH);
        c.gridx++;
        frame.add(finishButton,c);

        // Object number panel
        JLabel objectNumberLabel = new JLabel("Existing object number");
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        frame.add(objectNumberLabel,c);

        objectNumberField = new JTextField();
        c.gridx++;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        frame.add(objectNumberField,c);

        objectsScrollPane.setPreferredSize(new Dimension(0,200));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 4;
        c.gridheight = 3;
        c.fill = GridBagConstraints.BOTH;
        frame.add(objectsScrollPane,c);

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
        frame.add(overlayCheck,c);

        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

    }

    public ObjCollection applyInterpolation(ObjCollection outputObjects, Image templateImage, String interpolationMode) throws IntegerOverflowException {
        // Create a binary image of the objects
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(outputObjects,ColourFactory.SingleColours.WHITE);
        Image binaryImage = outputObjects.convertObjectsToImage("Binary",templateImage,hues,8,false);
        ImagePlus binaryIpl = binaryImage.getImagePlus();

        switch (interpolationMode) {
            case InterpolationModes.SPATIAL:
                applySpatialInterpolation(binaryIpl);
                break;

            case InterpolationModes.TEMPORAL:
                applyTemporalInterpolation(binaryIpl);
                break;

            case InterpolationModes.SPATIAL_AND_TEMPORAL:
                applySpatialInterpolation(binaryIpl);
                applyTemporalInterpolation(binaryIpl);
                break;
        }

        // Converting binary image back to objects
        return binaryImage.convertImageToObjects(outputObjects.getName(),false);

    }

    void applyTemporalInterpolation(ImagePlus binaryIpl) {
        int nSlices = binaryIpl.getNSlices();
        int nFrames = binaryIpl.getNFrames();

        BinaryInterpolator binaryInterpolator = new BinaryInterpolator();

        // We only want to interpolate in time, so need to processAutomatic each Z-slice of the stack separately
        for (int z=1;z<=nSlices;z++) {
            // Extracting the slice and interpolating
            ImagePlus sliceIpl = SubHyperstackMaker.makeSubhyperstack(binaryIpl, "1-1", z + "-" + z, "1-" + nFrames);
            if (!checkStackForInterpolation(sliceIpl.getStack())) continue;
            binaryInterpolator.run(sliceIpl.getStack());
        }
    }

    void applySpatialInterpolation(ImagePlus binaryIpl) {
        int nSlices = binaryIpl.getNSlices();
        int nFrames = binaryIpl.getNFrames();

        BinaryInterpolator binaryInterpolator = new BinaryInterpolator();

        // We only want to interpolate in z, so need to processAutomatic each timepoint separately
        for (int t=1;t<=nFrames;t++) {
            // Extracting the slice and interpolating
            ImagePlus sliceIpl = SubHyperstackMaker.makeSubhyperstack(binaryIpl, "1-1", "1-" + nSlices, t + "-" + t);
            if (!checkStackForInterpolation(sliceIpl.getStack())) continue;
            binaryInterpolator.run(sliceIpl.getStack());
        }
    }

    /**
     * Verifies that at least two images in the stack contain non-zero pixels
     * @param stack
     * @return
     */
    boolean checkStackForInterpolation(ImageStack stack) {
        int count = 0;
        for (int i=1;i<=stack.getSize();i++) {
            if (stack.getProcessor(i).getStatistics().max > 0) count++;
        }

        return count >= 2;

    }


    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public boolean process(Workspace workspace) {// Local access to this is required for the action listeners
        this.workspace = workspace;

        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String interpolationMode = parameters.getValue(INTERPOLATION_MODE);

        // Getting input image
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        dppXY = inputImagePlus.getCalibration().pixelWidth;
        dppZ = inputImagePlus.getCalibration().pixelDepth;

        displayImagePlus = new Duplicator().run(inputImagePlus);
        displayImagePlus.setCalibration(null);
        displayImagePlus.setTitle("Draw objects on this image");

        // Checking if we have a 2D or 3D image
        twoD = displayImagePlus.getNSlices() == 1;

        overlay = displayImagePlus.getOverlay();
        if (overlay == null) {
            overlay = new Overlay();
            displayImagePlus.setOverlay(overlay);
        }

        // Clearing any ROIs stored from previous runs
        rois = new HashMap<>();
        listModel.clear();

        // Initialising output objects
        outputObjects = new ObjCollection(outputObjectsName);

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

        // If more pixels than Integer.MAX_VALUE were assigned, return false (IntegerOverflowException).
        if (overflow) return false;

        // If necessary, apply interpolation
        switch (interpolationMode) {
            case InterpolationModes.SPATIAL:
            case InterpolationModes.TEMPORAL:
            case InterpolationModes.SPATIAL_AND_TEMPORAL:
                try {
                    outputObjects = applyInterpolation(outputObjects, inputImage, interpolationMode);
                } catch (IntegerOverflowException e) {
                    return false;
                }
                break;
        }

        workspace.addObjects(outputObjects);

        // Showing the selected objects
        if (showOutput) {
            HashMap<Integer,Float> hues = ColourFactory.getRandomHues(outputObjects);
            ImagePlus dispIpl = outputObjects.convertObjectsToImage("Objects",inputImage,hues,8,false).getImagePlus();
            dispIpl.setLut(LUTs.Random(true));
            dispIpl.setPosition(1,1,1);
            dispIpl.updateChannelAndDraw();
            dispIpl.show();
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new ChoiceP(INTERPOLATION_MODE,this,InterpolationModes.NONE,InterpolationModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
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
                try {
                    processObjects();
                } catch (IntegerOverflowException e1) {
                    overflow = true;
                }
                frame.dispose();
                frame = null;
                displayImagePlus.close();

                break;
        }
    }

    public void addNewObject() {
        // Getting the ROI
        Roi roi = displayImagePlus.getRoi();
        int ID = ++maxID;

        // Adding the ROI to our current collection
        ArrayList<ObjRoi> currentRois = new ArrayList<>();
        ObjRoi objRoi = new ObjRoi(ID, roi,displayImagePlus.getT()-1,displayImagePlus.getZ());
        currentRois.add(objRoi);
        rois.put(ID,currentRois);

        // Displaying the ROI on the overlay
        updateOverlay();

        // Setting the number field to this number
        objectNumberField.setText(String.valueOf(ID));

        // Adding to the list of objects
        addObjectToList(objRoi,ID);

    }

    public void addToExistingObject() {
        // Getting points
        Roi roi = displayImagePlus.getRoi();
        Point[] points = roi.getContainedPoints();
        int ID = Integer.parseInt(objectNumberField.getText());

        // Adding the ROI to our current collection
        ArrayList<ObjRoi> currentRois = rois.get(ID);
        ObjRoi objRoi = new ObjRoi(ID, roi,displayImagePlus.getT()-1,displayImagePlus.getZ());
        currentRois.add(objRoi);
        rois.put(ID,currentRois);

        // Displaying the ROI on the overlay
        updateOverlay();

        // Setting the number field to this number
        objectNumberField.setText(String.valueOf(ID));

        // Adding to the list of objects
        addObjectToList(objRoi,ID);

    }

    public void removeObjects() {
        // Get selected ROIs
        List<ObjRoi> selected = list.getSelectedValuesList();

        for (ObjRoi objRoi:selected) {
            // Get objects matching this ID
            int ID = objRoi.getID();
            rois.get(ID).remove(objRoi);

            listModel.removeElement(objRoi);

        }

        updateOverlay();

    }

    public void processObjects() throws IntegerOverflowException {
        // Processing each list of Rois, then converting them to objects
        for (int ID:rois.keySet()) {
            ArrayList<ObjRoi> currentRois = rois.get(ID);

            // This Obj may be empty; if so, skip it
            if (currentRois.size() == 0) continue;

            // Creating the new object
            Obj outputObject = new Obj(outputObjectsName, ID, dppXY, dppZ, calibrationUnits, twoD);
            outputObjects.add(outputObject);

            for (ObjRoi objRoi:currentRois) {
                Roi roi = objRoi.getRoi();
                Point[] points = roi.getContainedPoints();

                int t = objRoi.getT();
                int z = objRoi.getZ();

                outputObject.setT(t);
                for (Point point : points) {
                    int x = (int) Math.round(point.getX());
                    int y = (int) Math.round(point.getY());
                    if (x >= 0 && x < displayImagePlus.getWidth() && y >= 0 && y < displayImagePlus.getHeight()) {
                        outputObject.addCoord(x, y, z - 1);
                    }
                }
            }
        }
    }

    public void addObjectToList(ObjRoi objRoi, int ID) {
        listModel.addElement(objRoi);

        // Ensuring the scrollbar is visible if necessary and moving to the bottom
        JScrollBar scrollBar = objectsScrollPane.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum()-1);
        objectsScrollPane.revalidate();

    }

    public void updateOverlay() {
        overlay.clear();

        for (ArrayList<ObjRoi> groups:rois.values()) {
            for (ObjRoi objRoi:groups) {
                addToOverlay(objRoi);
            }
        }
    }

    public void addToOverlay(ObjRoi objRoi) {
        Roi roi = objRoi.getRoi();
        int ID = objRoi.getID();

        // Adding overlay showing ROI and its ID number
        overlay.add(ObjRoi.duplicateRoi(roi));

        double[] centroid = roi.getContourCentroid();
        TextRoi textRoi = new TextRoi(centroid[0],centroid[1],String.valueOf(ID));

        if (displayImagePlus.isHyperStack()) {
            textRoi.setPosition(1, displayImagePlus.getZ(), displayImagePlus.getT());
        } else {
            int pos = Math.max(Math.max(1,displayImagePlus.getZ()),displayImagePlus.getT());
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
                    newRoi = new OvalRoi(bounds.x,bounds.y,bounds.width,bounds.height);
                    break;

                case Roi.FREEROI:
                case Roi.POLYGON:
                    PolygonRoi polyRoi = (PolygonRoi) roi;
                    int[] x = polyRoi.getXCoordinates();
                    int[] xx = new int[x.length];
                    for (int i=0;i<x.length;i++) xx[i] = x[i]+ (int) polyRoi.getXBase();

                    int[] y = polyRoi.getYCoordinates();
                    int[] yy = new int[x.length];
                    for (int i=0;i<y.length;i++) yy[i] = y[i]+ (int) polyRoi.getYBase();

                    newRoi = new PolygonRoi(xx,yy,polyRoi.getNCoordinates(),roi.getType());
                    break;

                case Roi.FREELINE:
                case Roi.POLYLINE:
                    polyRoi = (PolygonRoi) roi;

                    if (polyRoi.getStrokeWidth() > 0) System.err.println("Thick lines currently unsupported.  Using backbone only.");

                    x = polyRoi.getXCoordinates();
                    xx = new int[x.length];
                    for (int i=0;i<x.length;i++) xx[i] = x[i]+ (int) polyRoi.getXBase();

                    y = polyRoi.getYCoordinates();
                    yy = new int[x.length];
                    for (int i=0;i<y.length;i++) yy[i] = y[i]+ (int) polyRoi.getYBase();

                    newRoi = new PolygonRoi(xx,yy,polyRoi.getNCoordinates(),roi.getType());
                    break;

                case Roi.LINE:
                    Line line = (Line) roi;

                    if (line.getStrokeWidth() > 0) System.err.println("Thick lines currently unsupported.  Using backbone only.");

                    newRoi = new Line(line.x1,line.y1,line.x2,line.y2);
                    break;

                case Roi.POINT:
                    PointRoi pointRoi = (PointRoi) roi;

                    Point[] points = pointRoi.getContainedPoints();
                    int[] xxx = new int[points.length];
                    int[] yyy = new int[points.length];
                    for (int i=0;i<points.length;i++) {
                        xxx[i] = points[i].x;
                        yyy[i] = points[i].y;
                    }

                    newRoi = new PointRoi(xxx,yyy,points.length);
                    break;

                default:
                    System.err.println("ROI type unsupported.  Using bounding box for selection.");
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
            return "Object "+String.valueOf(ID)+", T = "+(t+1)+", Z = "+z;
        }
    }
}
