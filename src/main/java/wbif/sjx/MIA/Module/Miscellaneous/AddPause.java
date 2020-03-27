package wbif.sjx.MIA.Module.Miscellaneous;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;

import javax.swing.*;

/**
 * Created by sc13967 on 09/02/2018.
 */
public class AddPause extends Module {
    public static final String SHOW_IMAGE = "Show image";
    public static final String INPUT_IMAGE = "Input image";

    private static final String RESUME = "Resume";
    private static final String TERMINATE = "Terminate";

    public AddPause(ModuleCollection modules) {
        super("Add pause",modules);
    }


    @Override
    public String getPackageName() {
        return PackageNames.MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting parameters
        boolean showImage = parameters.getValue(SHOW_IMAGE);
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        if (showImage) {
            Image inputImage = workspace.getImage(inputImageName);
            ImagePlus showIpl = new Duplicator().run(inputImage.getImagePlus());
            showIpl.setTitle(inputImageName);
            showIpl.show();
        }

        String[] options = {RESUME,TERMINATE};
        JOptionPane optionPane = new JOptionPane("Execution paused.  What would you like to do?",JOptionPane.QUESTION_MESSAGE,JOptionPane.OK_CANCEL_OPTION,null,options);
        JDialog dialog = optionPane.createDialog(null, "Execution paused");
        dialog.setModal(false);
        dialog.setVisible(true);

        writeMessage("Execution paused");

        while (optionPane.getValue() == JOptionPane.UNINITIALIZED_VALUE) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        switch ((String) optionPane.getValue()) {
            case RESUME:
                return true;

            case TERMINATE:
                return false;

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new BooleanP(SHOW_IMAGE,this,true));
        parameters.add(new InputImageP(INPUT_IMAGE,this));
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(SHOW_IMAGE));
        if ((boolean) parameters.getValue(SHOW_IMAGE)) {
            returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        }

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
    public ParentChildRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
