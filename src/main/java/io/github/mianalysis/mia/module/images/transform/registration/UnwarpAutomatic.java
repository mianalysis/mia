package io.github.mianalysis.mia.module.images.transform.registration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import bunwarpj.Transformation;
import bunwarpj.bUnwarpJ_;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.registration.abstrakt.AbstractBUnwarpJRegistration;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.process.interactable.Interactable;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class UnwarpAutomatic extends AbstractBUnwarpJRegistration implements Interactable {
    public UnwarpAutomatic(Modules modules) {
        super("Unwarp (automatic)", modules);
    }

    @Override
    public String getDescription() {
        return "Apply slice-by-slice (2D) B-spline unwarping-based image registration to a multi-dimensional stack.  Images can be aligned relative to the first frame in the stack, the previous frame or a separate image in the workspace.  The registration transform can also be calculated from a separate stack to the one that it will be applied to.  Registration can be performed along either the time or Z axes.  The non-registered axis (e.g. time axis when registering in Z) can be \"linked\" (all frames given the same registration) or \"independent\" (each stack registered separately)."

                + "<br><br>This module uses the <a href=\"https://imagej.net/BUnwarpJ\">BUnwarpJ</a> plugin to calculate and apply the necessary 2D transforms.  Detailed information about how the BUnwarpJ process works can be found at <a href=\"https://imagej.net/BUnwarpJ\">https://imagej.net/BUnwarpJ</a>.";
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
            MIA.log.writeError(e);
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

        addParameterDescriptions();
        
    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

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
