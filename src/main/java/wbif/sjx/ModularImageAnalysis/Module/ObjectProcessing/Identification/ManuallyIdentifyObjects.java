package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 27/02/2018.
 */
public class ManuallyIdentifyObjects extends Module implements ActionListener {
    private JFrame frame = new JFrame();
    private JTextField objectNumberField;

    private Workspace workspace;
    private ImagePlus displayImagePlus;
    private Overlay overlay;

    private String outputObjectsName;
    private ObjCollection outputObjects;

    private double dppXY;
    private double dppZ;
    private String calibrationUnits;


    private static final String ADD_NEW = "Add new";
    private static final String ADD_EXISTING = "Add existing";
    private static final String FINISH = "Finish";

    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";


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
        frame.setLayout(gridLayout);

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));

        JLabel headerLabel = new JLabel("<html>Draw round an object, then select one of the following<br>(or click \"Finish adding objects\" at any time)</html>");
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

    @Override
    public String getTitle() {
        return "Manually identify objects";
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

        // Initialising output objects
        outputObjects = new ObjCollection(outputObjectsName);
        workspace.addObjects(outputObjects);

        // Displaying the image and showing the control
        displayImagePlus.show();
        showOptionsPanel();

        while (frame != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE, null));
        parameters.add(new Parameter(OUTPUT_OBJECTS, Parameter.OUTPUT_OBJECTS, null));

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
                Obj outputObject = new Obj(outputObjectsName,ID,dppXY,dppZ,calibrationUnits);
                for (Point point:points) {
                    int x = (int) Math.round(point.getX());
                    int y = (int) Math.round(point.getY());
                    int z = displayImagePlus.getZ();
                    outputObject.addCoord(x,y,z);
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

                outputObjects.add(outputObject);

                break;

            case (ADD_EXISTING):
                // Getting points
                roi = displayImagePlus.getRoi();
                points = roi.getContainedPoints();
                ID = Integer.parseInt(objectNumberField.getText());

                // Adding the new object
                outputObject = outputObjects.get(ID);
                for (Point point:points) {
                    int x = (int) Math.round(point.getX());
                    int y = (int) Math.round(point.getY());
                    int z = displayImagePlus.getZ();
                    outputObject.addCoord(x,y,z);
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

                break;
        }
    }
}
