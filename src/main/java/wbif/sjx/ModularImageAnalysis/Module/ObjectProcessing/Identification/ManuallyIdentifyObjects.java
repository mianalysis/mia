package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import ij.process.BinaryInterpolator;
import ij.process.ImageProcessor;
import ij.process.LUT;
import ij.process.StackStatistics;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
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
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by sc13967 on 27/02/2018.
 */
public class ManuallyIdentifyObjects extends Module implements ActionListener {
    private JFrame frame;
    private JTextField objectNumberField;

    private Workspace workspace;
    private ImagePlus displayImagePlus;
    private Overlay overlay;

    private String outputObjectsName;
    private ObjCollection outputObjects;

    private double dppXY;
    private double dppZ;
    private String calibrationUnits;
    private boolean twoD;


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
        ImagePlus ipl = IJ.createImage("dfsd",400,300,1,8);
        ipl.show();

        IJ.runMacro("waitForUser");
        System.out.println(ipl.getRoi());

        new ManuallyIdentifyObjects().showOptionsPanel();
    }

    private void showOptionsPanel() {
        GridLayout gridLayout = new GridLayout(0, 1);
        frame = new JFrame();
        frame.setLayout(gridLayout);

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));

        JLabel headerLabel = new JLabel("<html>Draw round an object, then select one of the following" +
                "<br>(or click \"Finish adding objects\" at any time)." +
                "<br>Different timepoints must be added as new objects.</html>");
        headerPanel.add(headerLabel);

        frame.add(headerPanel);

        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

        JButton newObjectButton = new JButton("Add as new object");
        newObjectButton.addActionListener(this);
        newObjectButton.setActionCommand(ADD_NEW);
        buttonsPanel.add(newObjectButton);

        JButton existingObjectButton = new JButton("Add to existing object");
        existingObjectButton.addActionListener(this);
        existingObjectButton.setActionCommand(ADD_EXISTING);
        buttonsPanel.add(existingObjectButton);

        JButton finishButton = new JButton("Finish adding objects");
        finishButton.addActionListener(this);
        finishButton.setActionCommand(FINISH);
        buttonsPanel.add(finishButton);

        frame.add(buttonsPanel);

        // Object number panel
        JPanel objectNumberPanel = new JPanel();
        objectNumberPanel.setLayout(new BoxLayout(objectNumberPanel, BoxLayout.X_AXIS));

        JLabel objectNumberLabel = new JLabel("Existing object number");
        objectNumberPanel.add(objectNumberLabel);

        objectNumberField = new JTextField();
        objectNumberPanel.add(objectNumberField);

        frame.add(objectNumberPanel);

        frame.pack();
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
    protected void run(Workspace workspace) throws GenericMIAException {
        // Local access to this is required for the action listeners
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
            dispIpl.show();
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE, null));
        parameters.add(new Parameter(OUTPUT_OBJECTS, Parameter.OUTPUT_OBJECTS, null));
        parameters.add(new Parameter(INTERPOLATION_MODE,Parameter.CHOICE_ARRAY,InterpolationModes.SPATIAL,InterpolationModes.ALL));

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
    public void addRelationships(RelationshipCollection relationships) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case (ADD_NEW):
                // Getting points
                Roi roi = displayImagePlus.getRoi();
                Point[] points = roi.getContainedPoints();
                int ID = outputObjects.getNextID();

                // Adding the new object
                Obj outputObject = new Obj(outputObjectsName,ID,dppXY,dppZ,calibrationUnits,twoD);
                outputObject.setT(displayImagePlus.getT()-1);
                for (Point point:points) {
                    int x = (int) Math.round(point.getX());
                    int y = (int) Math.round(point.getY());
                    int z = displayImagePlus.getZ();
                    if (x >= 0 && x < displayImagePlus.getWidth() && y >= 0 && y < displayImagePlus.getHeight()) {
                        outputObject.addCoord(x, y, z - 1);
                    }
                }

                // Adding overlay showing ROI and its ID number
                overlay.add(roi);
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

                // Setting the number field to this number
                objectNumberField.setText(String.valueOf(ID));

                outputObjects.add(outputObject);

                break;

            case (ADD_EXISTING):
                // Getting points
                roi = displayImagePlus.getRoi();
                points = roi.getContainedPoints();
                ID = Integer.parseInt(objectNumberField.getText());

                // Adding the new object
                outputObject = outputObjects.get(ID);
                outputObject.setT(displayImagePlus.getT()-1);
                for (Point point:points) {
                    int x = (int) Math.round(point.getX());
                    int y = (int) Math.round(point.getY());
                    int z = displayImagePlus.getZ();
                    outputObject.addCoord(x,y,z-1);
                }

                // Adding overlay showing ROI and its ID number
                overlay.add(roi);
                centroid = roi.getContourCentroid();
                textRoi = new TextRoi(centroid[0],centroid[1],String.valueOf(ID));
                if (displayImagePlus.isHyperStack()) {
                    textRoi.setPosition(1, displayImagePlus.getZ(), displayImagePlus.getT());
                } else {
                    int pos = Math.max(Math.max(1,displayImagePlus.getZ()),displayImagePlus.getT());
                    textRoi.setPosition(pos);
                }
                overlay.add(textRoi);
                displayImagePlus.updateAndDraw();

                outputObjects.add(outputObject);

                break;

            case (FINISH):
                frame.dispose();
                frame = null;
                displayImagePlus.close();

                break;
        }
    }
}
