package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.*;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import ij.process.BinaryInterpolator;
import ij.process.LUT;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.Object.LUTs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sc13967 on 27/02/2018.
 */
public class ManuallyIdentifyObjects extends Module implements ActionListener {
    private JFrame frame;
    private JTextField objectNumberField;
    private final JPanel objectsPanel = new JPanel();
    JScrollPane objectsScrollPane = new JScrollPane(objectsPanel);
    private final GridBagConstraints objectsC = new GridBagConstraints();

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

    private int elementHeight = 40;

    private static final String ADD_NEW = "Add new";
    private static final String ADD_EXISTING = "Add existing";
    private static final String FINISH = "Finish";

    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String INTERPOLATION_MODE = "Interpolation mode";


    public interface InterpolationModes {
        String NONE = "None";
        String SPATIAL = "Spatial";
        String TEMPORAL = "Temporal";
        String SPATIAL_AND_TEMPORAL = "Spatial and temporal";

        String[] ALL = new String[]{NONE, SPATIAL, TEMPORAL, SPATIAL_AND_TEMPORAL};

    }


    public static void main(String[] args) {
        new ImageJ();

        Workspace workspace = new Workspace(0,null,0);

        ImagePlus ipl = IJ.createImage("dfsd",400,300,1,8);
        Image image = new Image("Im",ipl);
        workspace.addImage(image);

        ManuallyIdentifyObjects manuallyIdentifyObjects = new ManuallyIdentifyObjects();
        manuallyIdentifyObjects.updateParameterValue(ManuallyIdentifyObjects.INPUT_IMAGE,"Im");
        manuallyIdentifyObjects.setShowOutput(true);
        manuallyIdentifyObjects.run(workspace);

    }

    private void showOptionsPanel() {
        rois = new HashMap<>();
        maxID = 0;
        frame = new JFrame();

        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
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
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        frame.add(objectNumberField,c);

        objectsC.gridx = 0;
        objectsC.gridy = 0;
        objectsC.weightx = 1;
        objectsC.weighty = 1;
        objectsC.anchor = GridBagConstraints.NORTHWEST;
        objectsC.fill = GridBagConstraints.HORIZONTAL;
        objectsPanel.setLayout(new GridBagLayout());

        objectsScrollPane.setPreferredSize(new Dimension(0,200));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
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

    public ObjCollection applyInterpolation(ObjCollection outputObjects, Image templateImage, String interpolationMode) {
        // Create a binary image of the objects
        String colourMode = ObjCollection.ColourModes.SINGLE_COLOUR;
        String source = ObjCollection.SingleColours.WHITE;
        HashMap<Integer, Float> hues = outputObjects.getHues(colourMode, source, false);
        Image binaryImage = outputObjects.convertObjectsToImage("Binary",templateImage,colourMode,hues);
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

        // We only want to interpolate in time, so need to process each Z-slice of the stack separately
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

        // We only want to interpolate in z, so need to process each timepoint separately
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
    public String getTitle() {
        return "Manually identify objects";
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
    protected void run(Workspace workspace) {// Local access to this is required for the action listeners
        this.workspace = workspace;

        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String interpolationMode = parameters.getValue(INTERPOLATION_MODE);

        // Getting input image
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        displayImagePlus = new Duplicator().run(inputImagePlus);
        displayImagePlus.setCalibration(null);
        displayImagePlus.setTitle("Draw objects on this image");
        overlay = displayImagePlus.getOverlay();
        if (overlay == null) {
            overlay = new Overlay();
            displayImagePlus.setOverlay(overlay);
        }

        // Storing the image calibration
        Calibration calibration = inputImagePlus.getCalibration();
        dppXY = calibration.getX(1);
        dppZ = calibration.getZ(1);
        calibrationUnits = calibration.getUnits();
        twoD = inputImagePlus.getNSlices()==1;

        // Clearing any ROIs stored from previous runs
        rois = new HashMap<>();
        objectsPanel.removeAll();

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

        // If necessary, apply interpolation
        switch (interpolationMode) {
            case InterpolationModes.SPATIAL:
            case InterpolationModes.TEMPORAL:
            case InterpolationModes.SPATIAL_AND_TEMPORAL:
                outputObjects = applyInterpolation(outputObjects, inputImage, interpolationMode);
                break;
        }

        workspace.addObjects(outputObjects);

        // Showing the selected objects
        if (showOutput) {
            HashMap<Integer,Float> hues = outputObjects.getHues(ObjCollection.ColourModes.RANDOM_COLOUR,"",false);
            String mode = ConvertObjectsToImage.ColourModes.RANDOM_COLOUR;
            ImagePlus dispIpl = outputObjects.convertObjectsToImage("Objects",inputImage,mode,hues).getImagePlus();
            dispIpl.setLut(LUTs.Random(true));
            dispIpl.setPosition(1,1,1);
            dispIpl.updateChannelAndDraw();
            dispIpl.show();
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE, null));
        parameters.add(new Parameter(OUTPUT_OBJECTS, Parameter.OUTPUT_OBJECTS, null));
        parameters.add(new Parameter(INTERPOLATION_MODE,Parameter.CHOICE_ARRAY,InterpolationModes.NONE,InterpolationModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

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

            case (FINISH):
                processObjects();
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
        ObjRoi objRoi = new ObjRoi(roi,displayImagePlus.getT()-1,displayImagePlus.getZ());
        currentRois.add(objRoi);
        rois.put(ID,currentRois);

        // Displaying the ROI on the overlay
        addToOverlay(roi,ID);

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
        ObjRoi objRoi = new ObjRoi(roi,displayImagePlus.getT()-1,displayImagePlus.getZ());
        currentRois.add(objRoi);
        rois.put(ID,currentRois);

        // Displaying the ROI on the overlay
        addToOverlay(roi,ID);

        // Setting the number field to this number
        objectNumberField.setText(String.valueOf(ID));

        // Adding to the list of objects
        addObjectToList(objRoi,ID);

    }

    public void processObjects() {
        // Processing each list of Rois, then converting them to objects
        for (int ID:rois.keySet()) {
            ArrayList<ObjRoi> currentRois = rois.get(ID);

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
        JButton button = new JButton();
        button.setText("Object "+String.valueOf(ID)+", T = "+(objRoi.getT()+1)+", Z = "+objRoi.getZ());
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Show the object on the image
                displayObject(objRoi);
            }
        });

        objectsPanel.add(button,objectsC);

        objectsC.gridy++;
        objectsC.weighty = objectsC.weighty*1000;

        objectsPanel.validate();
        objectsPanel.repaint();

        // Ensuring the scrollbar is visible if necessary and moving to the bottom
        JScrollBar scrollBar = objectsScrollPane.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum()-1);
        objectsScrollPane.revalidate();

    }

    public void addToOverlay(Roi roi, int ID) {
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
}

class ObjRoi {
    private final Roi roi;
    private final int t;
    private final int z;

    ObjRoi(Roi roi, int t, int z) {
        this.roi = duplicateRoi(roi);
        this.t = t;
        this.z = z;

    }

    public static Roi duplicateRoi(Roi roi) {
        Roi newRoi;
        // Need to process Roi depending on its type
        switch (roi.getType()) {
            case Roi.RECTANGLE:
                newRoi = new Roi(roi.getBounds());
                break;

            case Roi.OVAL:
                Rectangle bounds = roi.getBounds();
                newRoi = new OvalRoi(bounds.x,bounds.y,bounds.width,bounds.height);
                break;

            case Roi.POLYGON:
            case Roi.FREEROI:
                PolygonRoi polyRoi = (PolygonRoi) roi;
                int[] x = polyRoi.getXCoordinates();
                int[] xx = new int[x.length];
                for (int i=0;i<x.length;i++) xx[i] = x[i]+ (int) polyRoi.getXBase();

                int[] y = polyRoi.getYCoordinates();
                int[] yy = new int[x.length];
                for (int i=0;i<y.length;i++) yy[i] = y[i]+ (int) polyRoi.getYBase();

                newRoi = new PolygonRoi(xx,yy,polyRoi.getNCoordinates(),roi.getType());
                break;

            default:
                newRoi = new Roi(roi.getBounds());
                break;
        }

        return newRoi;

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
}