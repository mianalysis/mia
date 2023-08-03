package io.github.mianalysis.mia.module.objects.filter;

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
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.visualise.overlays.AddLabels;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.process.CommaSeparatedStringInterpreter;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class FilterSpecificObjectIDs extends AbstractObjectFilter implements ActionListener {

	/**
	* 
	*/
    public static final String FILTER_SEPARATOR = "Object filtering";

	/**
	* When selected, a specific image will be displayed when this module executes.  This can be used to display a pre-prepared, object ID-labelled image to the user, thus acting as a reference for which object IDs to remove.  The image to be displayed is set using the "Image to display" parameter.
	*/
    public static final String SHOW_IMAGE = "Show image";

	/**
	* Image to display when the module executes.  For example, this could be a pre-prepared image with object IDs inserted as text overlays using the "Add labels" module.
	*/
    public static final String DISPLAY_IMAGE_NAME = "Image to display";

	/**
	* When selected, the number of removed (or moved) objects is counted and stored as a metadata item (name in the format "FILTER // NUM_[inputObjectsName]_BY_ID").
	*/
    public static final String STORE_RESULTS = "Store filter results";

    private static final String OK = "OK";

    private JFrame frame;
    private JTextField numbersField;
    private int elementHeight = 30;
    private boolean active = false;

    public FilterSpecificObjectIDs(Modules modules) {
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

    public int filter(Objs inputObjects, @Nullable Objs outputObjects, boolean remove,
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
                // Do nothing as the user has selected this
            }
        }

        int[] ids = CommaSeparatedStringInterpreter.interpretIntegers(numbersField.getText(), true, 0);

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
                    outputObjects.add(obj);
                    obj.setObjectCollection(outputObjects);
                }
                inputObjects.remove(id);
            }
        }

        return count;

    }


    @Override
    public Category getCategory() {
        return Categories.OBJECTS_FILTER;
    }

    @Override
    public String getDescription() {
        return "Filter an object collection based on user-defined list of object ID numbers.  When the module executes, the user is presented with a dialog box where they can enter a comma-separated list of object IDs to remove.  Once the list is complete, the user presses \"OK\" to proceed.  All objects with ID numbers matching those in the list can be removed from the input collection, moved to another collection (and removed from the input collection) or simply counted (but retained in the input collection).  To assist with selection of ID numbers, an optional image can be displayed - this could be pre-prepared to display object ID numbers using the \""
                + new AddLabels(null).getName()
                + "\" module.  The number of objects specified for removal can be stored as a metadata value.";
        
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE,workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS,workspace);
        boolean showImage = parameters.getValue(SHOW_IMAGE,workspace);
        String displayImageName = parameters.getValue(DISPLAY_IMAGE_NAME,workspace);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        Objs outputObjects = moveObjects ? new Objs(outputObjectsName, inputObjects) : null;
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
            inputObjects.convertToImageIDColours().show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(FILTER_SEPARATOR, this));
        parameters.add(new BooleanP(SHOW_IMAGE, this, true));
        parameters.add(new InputImageP(DISPLAY_IMAGE_NAME, this));
        parameters.add(new BooleanP(STORE_RESULTS, this, false));

        addParameterDescriptions();
       
    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();
        returnedParameters.addAll(super.updateAndGetParameters());
        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SHOW_IMAGE));
        if ((boolean) parameters.getValue(SHOW_IMAGE,workspace)) {
            returnedParameters.add(parameters.getParameter(DISPLAY_IMAGE_NAME));
        }

        returnedParameters.add(parameters.getParameter(STORE_RESULTS));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
Workspace workspace = null;
        return super.updateAndGetObjectMeasurementRefs();
    }

    @Override
public MetadataRefs updateAndGetMetadataReferences() {
Workspace workspace = null;
        MetadataRefs returnedRefs = new MetadataRefs();

        // Filter results are stored as a metadata item since they apply to the whole
        // set
        if ((boolean) parameters.getValue(STORE_RESULTS,workspace)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);

            String metadataName = getMetadataName(inputObjectsName);

            returnedRefs.add(metadataRefs.getOrPut(metadataName));

        }

        return returnedRefs;
    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();
        
        parameters.get(SHOW_IMAGE).setDescription("When selected, a specific image will be displayed when this module executes.  This can be used to display a pre-prepared, object ID-labelled image to the user, thus acting as a reference for which object IDs to remove.  The image to be displayed is set using the \""+DISPLAY_IMAGE_NAME+"\" parameter.");

        parameters.get(DISPLAY_IMAGE_NAME).setDescription("Image to display when the module executes.  For example, this could be a pre-prepared image with object IDs inserted as text overlays using the \""+new AddLabels(null).getName()+"\" module.");

        String metadataName = getMetadataName("[inputObjectsName]");
        parameters.get(STORE_RESULTS).setDescription(
                "When selected, the number of removed (or moved) objects is counted and stored as a metadata item (name in the format \""
                        + metadataName + "\").");

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
