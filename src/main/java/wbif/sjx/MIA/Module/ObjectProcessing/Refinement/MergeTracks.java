package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import ij.IJ;
import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Process.CommaSeparatedStringInterpreter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

public class MergeTracks extends Module implements ActionListener {
    public static final String INPUT_TRACK_OBJECTS = "Input track objects";
    public static final String INPUT_SPOT_OBJECTS = "Input spot objects";
    public static final String SHOW_IMAGE = "Show image";
    public static final String DISPLAY_IMAGE_NAME = "Image to display";

    private static final String MERGE = "Merge";
    private static final String FINISH_ADDING = "Finish adding";

    private JFrame frame;
    private JTextField numbersField;
    private int elementHeight = 30;
    private boolean active = false;

    private ObjCollection trackObjects;
    private String spotObjectsName;


    private void showOptionsPanel() {
        active = true;
        frame = new JFrame();
        frame.setAlwaysOnTop(true);

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
        ObjCollection dominantSpots = dominantTrack.getChildren(spotObjectsName);

        // Iterate over each track to be combined
        for (int i = 1;i<ids.length;i++) {
            // If this track ID doesn't exist (maybe it was already removed/merged) continue to the next one
            if (!trackObjects.containsKey(ids[i])) continue;

            Obj subTrack = trackObjects.get(ids[i]);

            // This has to be done one at a time to resolve instances where spots are present in the same frame
            ObjCollection subSpots = subTrack.getChildren(spotObjectsName);
            Iterator<Obj> subSpotIterator = subSpots.values().iterator();
            while (subSpotIterator.hasNext()) {
                Obj subSpot = subSpotIterator.next();

                // If present in the same frame, remove the subSpot object
                for (Obj dominantSpot:dominantSpots.values()) {
                    if (subSpot.getT() == dominantSpot.getT()) {
                        subSpot.removeRelationships();
                        subSpotIterator.remove();
                    }
                }

                // Moving the spot object to the dominant track
                subSpot.removeParent(subTrack.getName());
                subSpot.addParent(dominantTrack);
                dominantTrack.addChild(subSpot);

            }

            // Deleting this track from the original collection
            trackObjects.remove(ids[i]);

        }
    }


    @Override
    public String getTitle() {
        return "Merge tracks";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    protected boolean process(Workspace workspace) {
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

        return false;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_TRACK_OBJECTS,this));
        parameters.add(new ChildObjectsP(INPUT_SPOT_OBJECTS,this));
        parameters.add(new BooleanP(SHOW_IMAGE, this, true));
        parameters.add(new InputImageP(DISPLAY_IMAGE_NAME, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_TRACK_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_SPOT_OBJECTS));

        String objectName = parameters.getValue(INPUT_TRACK_OBJECTS);
        ((ChildObjectsP) parameters.getParameter(INPUT_SPOT_OBJECTS)).setParentObjectsName(objectName);

        returnedParameters.add(parameters.getParameter(SHOW_IMAGE));
        returnedParameters.add(parameters.getParameter(DISPLAY_IMAGE_NAME));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetImageMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
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
}
