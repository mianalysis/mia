package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.MergeObjects;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import ij.IJ;
import ij.ImagePlus;
import wbif.sjx.MIA.GUI.Colours;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChildObjectsP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.InputTrackObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.MessageP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.MIA.Process.CommaSeparatedStringInterpreter;

public class MergeTracks extends Module implements ActionListener, WindowListener {
    public static final String INPUT_SEPARATOR = "Objects input";
    public static final String INPUT_TRACK_OBJECTS = "Input track objects";
    public static final String INPUT_SPOT_OBJECTS = "Input spot objects";

    public static final String DISPLAY_SEPARATOR = "Display controls";
    public static final String SHOW_IMAGE = "Show image";
    public static final String DISPLAY_IMAGE_NAME = "Image to display";
    public static final String MEASUREMENT_WARNING = "Measurement warning";

    private static final String MERGE = "Merge";
    private static final String FINISH_ADDING = "Finish adding";

    private JFrame frame;
    private JTextField numbersField;
    private int elementHeight = 30;
    private boolean active = false;

    private ObjCollection trackObjects;
    private String spotObjectsName;

    public MergeTracks(ModuleCollection modules) {
        super("Merge tracks",modules);
    }


    private void showOptionsPanel() {
        active = true;
        frame = new JFrame();
        frame.setAlwaysOnTop(true);
        frame.addWindowListener(this);

        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5,5,5,5);

        // Header panel
        JLabel headerLabel = new JLabel("<html>Specify track IDs to merge (comma separated)<br>" +
                "First ID will be dominant (measurements and relationships used in conflicts)<br>" +
                "n.b. Existing track measurements will need to be recalculated</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        frame.add(headerLabel,c);

        numbersField = new JTextField();
        numbersField.setPreferredSize(new Dimension(200, elementHeight));
        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0.5;
        c.insets = new Insets(0, 5, 5, 5);
        frame.add(numbersField,c);

        JButton mergeButton = new JButton(MERGE);
        mergeButton.addActionListener(this);
        mergeButton.setActionCommand(MERGE);
        mergeButton.setPreferredSize(new Dimension(200,elementHeight));
        c.gridx++;
        frame.add(mergeButton,c);

        JButton finishButton = new JButton(FINISH_ADDING);
        finishButton.addActionListener(this);
        finishButton.setActionCommand(FINISH_ADDING);
        finishButton.setPreferredSize(new Dimension(400,elementHeight));
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weightx = 1;
        frame.add(finishButton,c);

        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

    }

    private static void mergeObjects(ObjCollection trackObjects, String spotObjectsName, String idList) {
        int[] ids = CommaSeparatedStringInterpreter.interpretIntegers(idList,false);

        if (ids.length < 2) IJ.error("Enter at least 2 comma-separated track IDs");

        // Get dominant track
        Obj dominantTrack = trackObjects.get(ids[0]);

        // Iterate over each track to be combined
        for (int i = 1;i<ids.length;i++) {
            // If this track ID doesn't exist (maybe it was already removed/merged) continue to the next one
            if (!trackObjects.containsKey(ids[i])) continue;

            Obj subTrack = trackObjects.get(ids[i]);

            // Adding subTrack's spots to the dominantTrack
            for (Obj subSpot:subTrack.getChildren(spotObjectsName).values()) {
                subSpot.addParent(dominantTrack);
                dominantTrack.addChild(subSpot);
            }

            // Removing all relationships from this track, then deleting from the track collection
            subTrack.removeRelationships();
            trackObjects.remove(ids[i]);

        }
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT_MERGE_OBJECTS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input track objects
        String inputTrackObjectsName = parameters.getValue(INPUT_TRACK_OBJECTS);
        trackObjects = workspace.getObjects().get(inputTrackObjectsName);

        // Getting input spot objects
        spotObjectsName = parameters.getValue(INPUT_SPOT_OBJECTS);

        boolean showImage = parameters.getValue(SHOW_IMAGE);
        String displayImageName = parameters.getValue(DISPLAY_IMAGE_NAME);
        Image displayImage = showImage ? workspace.getImage(displayImageName) : null;

        ImagePlus ipl = null;
        if (displayImage!= null) {
            ipl = displayImage.getImagePlus().duplicate();
            ipl.show();
        }

        // Get list of object ID pairs
        showOptionsPanel();

        // All the while the control is open, do nothing
        while (active) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        frame.dispose();
        frame = null;
        if (ipl != null) ipl.close();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputTrackObjectsP(INPUT_TRACK_OBJECTS,this));
        parameters.add(new ChildObjectsP(INPUT_SPOT_OBJECTS,this));

        parameters.add(new ParamSeparatorP(DISPLAY_SEPARATOR,this));
        parameters.add(new BooleanP(SHOW_IMAGE, this, true));
        parameters.add(new InputImageP(DISPLAY_IMAGE_NAME, this));
        parameters.add(new MessageP(MEASUREMENT_WARNING,this,"Previously-acquired measurements for merged objects may become invalid.  During merging, the measurement associated with the object specified first will be retained.", Colours.RED));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_TRACK_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_SPOT_OBJECTS));

        String objectName = parameters.getValue(INPUT_TRACK_OBJECTS);
        ((ChildObjectsP) parameters.getParameter(INPUT_SPOT_OBJECTS)).setParentObjectsName(objectName);

        returnedParameters.add(parameters.getParameter(DISPLAY_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SHOW_IMAGE));
        returnedParameters.add(parameters.getParameter(DISPLAY_IMAGE_NAME));
        returnedParameters.add(parameters.getParameter(MEASUREMENT_WARNING));

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

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case MERGE:
                String idList = numbersField.getText();
                mergeObjects(trackObjects,spotObjectsName,idList);

                // Resetting the text field to blank
                numbersField.setText("");
                break;

            case FINISH_ADDING:
                active = false;
                break;
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        active = false;
    }

    @Override
    public void windowClosed(WindowEvent e) {
        active = false;
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
