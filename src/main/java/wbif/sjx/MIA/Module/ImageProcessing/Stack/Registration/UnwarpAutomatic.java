package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import bunwarpj.Transformation;
import bunwarpj.bUnwarpJ_;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.Abstract.AbstractBUnwarpJRegistration;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Process.Interactable.Interactable;

public class UnwarpAutomatic extends AbstractBUnwarpJRegistration implements Interactable {
    public UnwarpAutomatic(ModuleCollection modules) {
        super("Unwarp (automatic)", modules);
    }

    @Override
    public String getDescription() {
        return "";

    }

    @Override
    public AutomaticBUnwarpJParam createParameterSet() {
        return new AutomaticBUnwarpJParam();
    }

    @Override
    public void getParameters(Param param, Workspace workspace) {
        super.getParameters(param, workspace);
    }

    @Override
    public Transform getTransform(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param,
            boolean showDetectedPoints) {

        AutomaticBUnwarpJParam p = (AutomaticBUnwarpJParam) param;

        ImagePlus referenceIpl = new ImagePlus("Reference", referenceIpr.duplicate());
        ImagePlus warpedIpl = new ImagePlus("Warped", warpedIpr.duplicate());

        Transformation transformation = bUnwarpJ_.computeTransformationBatch(referenceIpl, warpedIpl, null, null,
                p.bParam);

        try {
            File tempFile = File.createTempFile("unwarp", ".tmp");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile));
            bufferedWriter.close();

            String tempPath = tempFile.getAbsolutePath();
            transformation.saveDirectTransformation(tempPath);

            BUnwarpJTransform transform = new BUnwarpJTransform();
            transform.transformPath = tempPath;

            return transform;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void doAction(Object[] objects) {
        writeStatus("Running test registration");

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addAll(super.updateAndGetParameters());

        // This approach can't show any points
        returnedParameters.remove(SHOW_DETECTED_POINTS);

        return returnedParameters;

    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

    }

    public class AutomaticBUnwarpJParam extends BUnwarpJParam {

    }
}
