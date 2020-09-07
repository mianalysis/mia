package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Process.CommaSeparatedStringInterpreter;

public class FilterSpecificObjectIDs extends AbstractObjectFilter implements ActionListener {
    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String SHOW_IMAGE = "Show image";
    public static final String DISPLAY_IMAGE_NAME = "Image to display";
    public static final String STORE_RESULTS = "Store filter results";

    private static final String OK = "OK";

    private JFrame frame;
    private JTextField numbersField;
    private int elementHeight = 30;
    private boolean active = false;

    public FilterSpecificObjectIDs(ModuleCollection modules) {
        super("Objects with specific IDs", modules);
    }

    public String getMetadataName(String inputObjectsName) {
        return "FILTER // NUM_" + inputObjectsName + "_BY_ID";

    }

    private void showOptionsPanel() {
        active = true;
        frame = new JFrame();
        frame.setAlwaysOnTop(true);

        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        // Header panel
        JLabel headerLabel = new JLabel("<html>Specify object IDs to remove (comma separated)</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        headerLabel.setPreferredSize(new Dimension(300, elementHeight));
        frame.add(headerLabel, c);

        numbersField = new JTextField();
        numbersField.setPreferredSize(new Dimension(200, elementHeight));
        c.gridy++;
        c.insets = new Insets(0, 5, 5, 5);
        frame.add(numbersField, c);

        JButton okButton = new JButton(OK);
        okButton.addActionListener(this);
        okButton.setActionCommand(OK);
        okButton.setPreferredSize(new Dimension(300, elementHeight));
        c.gridy++;
        frame.add(okButton, c);

        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

    }

    public int filter(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, boolean remove,
            @Nullable Image image) {
        ImagePlus ipl = null;
        if (image != null) {
            ipl = image.getImagePlus().duplicate();
            ipl.show();
        }

        showOptionsPanel();

        // All the while the control is open, do nothing
        while (active) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int[] ids = CommaSeparatedStringInterpreter.interpretIntegers(numbersField.getText(), true);

        frame.dispose();
        frame = null;
        if (ipl != null)
            ipl.close();

        int count = 0;
        for (int id : ids) {
            Obj obj = inputObjects.get(id);
            count++;
            if (remove) {
                obj.removeRelationships();
                if (outputObjects != null) {
                    obj.setName(outputObjects.getName());
                    outputObjects.add(obj);
                }
                inputObjects.remove(id);
            }
        }

        return count;

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT_FILTER_OBJECTS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);
        boolean showImage = parameters.getValue(SHOW_IMAGE);
        String displayImageName = parameters.getValue(DISPLAY_IMAGE_NAME);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        ObjCollection outputObjects = moveObjects ? new ObjCollection(outputObjectsName, inputObjects) : null;
        Image displayImage = showImage ? workspace.getImage(displayImageName) : null;

        int count = filter(inputObjects, outputObjects, remove, displayImage);

        // If moving objects, addRef them to the workspace
        if (moveObjects)
            workspace.addObjects(outputObjects);

        // If storing the result, create a new metadata item for it
        String metadataName = getMetadataName(inputObjectsName);
        workspace.getMetadata().put(metadataName, count);

        // Showing objects
        if (showOutput)
            inputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new ParamSeparatorP(FILTER_SEPARATOR, this));
        parameters.add(new BooleanP(SHOW_IMAGE, this, true));
        parameters.add(new InputImageP(DISPLAY_IMAGE_NAME, this));
        parameters.add(new BooleanP(STORE_RESULTS, this, false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addAll(super.updateAndGetParameters());
        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SHOW_IMAGE));
        if ((boolean) parameters.getValue(SHOW_IMAGE)) {
            returnedParameters.add(parameters.getParameter(DISPLAY_IMAGE_NAME));
        }

        returnedParameters.add(parameters.getParameter(STORE_RESULTS));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return super.updateAndGetObjectMeasurementRefs();
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        MetadataRefCollection returnedRefs = new MetadataRefCollection();

        // Filter results are stored as a metadata item since they apply to the whole
        // set
        if ((boolean) parameters.getValue(STORE_RESULTS)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

            String metadataName = getMetadataName(inputObjectsName);

            returnedRefs.add(metadataRefs.getOrPut(metadataName));

        }

        return returnedRefs;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case (OK):
                active = false;
                break;
        }
    }
}
