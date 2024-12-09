package io.github.mianalysis.mia.module.images.transform.registration;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ch.epfl.biop.fiji.imageplusutils.ImagePlusFunctions;
import ch.epfl.biop.wrappers.elastix.Elastix;
import ch.epfl.biop.wrappers.elastix.RegisterHelper;
import ch.epfl.biop.wrappers.elastix.ij2commands.Mod_Elastix_Register;
import ch.epfl.biop.wrappers.transformix.DefaultTransformixTask;
import ch.epfl.biop.wrappers.transformix.Mod_TransformHelper;
import ch.epfl.biop.wrappers.transformix.Transformix;
import ij.ImagePlus;
import ij.Prefs;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageTypeConverter;
import io.github.mianalysis.mia.module.images.transform.registration.abstrakt.AbstractRegistration;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ElastixRegistration<T extends RealType<T> & NativeType<T>>
        extends AbstractRegistration<T> {
    public static final String ELASTIX_SEPARATOR = "Elastix controls";

    public static final String ELASTIX_EXE = "Elastix executable";

    public static final String TRANSFORMIX_EXE = "Transformix executable";

    public static final String AFFINE = "Affine";

    public static final String FAST_AFFINE = "Fast affine";

    public static final String RIGID = "Rigid";

    public static final String SPLINE = "Spline";

    public static final String SPLINE_GRID_SPACING = "Spline grid spacing";

    public ElastixRegistration(Modules modules) {
        super("Elastix registration", modules);
    }

    @Override
    public void getParameters(Param param, WorkspaceI workspace) {
        // Setting up the parameters
        ElastixParam elastixParam = (ElastixParam) param;

        elastixParam.affine = parameters.getValue(AFFINE, workspace);
        elastixParam.fastAffine = parameters.getValue(FAST_AFFINE, workspace);
        elastixParam.rigid = parameters.getValue(RIGID, workspace);
        elastixParam.spline = parameters.getValue(SPLINE, workspace);
        elastixParam.splineGridSpacing = parameters.getValue(SPLINE_GRID_SPACING, workspace);
        elastixParam.enableMultiThreading = parameters.getValue(ENABLE_MULTITHREADING, workspace);

    }

    @Override
    public ImageProcessor applyTransform(ImageProcessor inputIpr, Transform transform, int fillValue) {
        ElastixTransform elastixTransform = (ElastixTransform) transform;
        RegisterHelper rh = elastixTransform.registerHelper;

        inputIpr.setColor(fillValue);

        ImagePlus outputIpl = ImagePlusFunctions.splitApplyRecompose(
                imp -> {
                    Mod_TransformHelper th = new Mod_TransformHelper();
                    th.setDefaultOutputDir();
                    th.setTransformFile(rh);
                    th.setImage(imp);
                    th.transform(new DefaultTransformixTask());
                    th.setNThreads(elastixTransform.enableMultiThreading ? Prefs.getThreads() : 1);
                    return ((ImagePlus) (th.getTransformedImage().to(ImagePlus.class)));
                }, new ImagePlus("Warped", inputIpr));

        ImageTypeConverter.process(outputIpl, inputIpr.getBitDepth(), ImageTypeConverter.ScalingModes.CLIP);

        return outputIpl.getProcessor();

    }

    @Override
    public Param createParameterSet() {
        return new ElastixParam();
    }

    @Override
    public Transform getTransform(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param,
            boolean showDetectedPoints) {
        ElastixParam p = (ElastixParam) param;

        Mod_Elastix_Register elastixRegister = new Mod_Elastix_Register();
        elastixRegister.affine = p.affine;
        elastixRegister.fast_affine = p.fastAffine;
        elastixRegister.rigid = p.rigid;
        elastixRegister.spline = p.spline;
        elastixRegister.splineGridSpacing = p.splineGridSpacing;
        elastixRegister.fixedImage = new ImagePlus("Fixed", referenceIpr);
        elastixRegister.movingImage = new ImagePlus("Moving", warpedIpr);
        elastixRegister.nThreads = p.enableMultiThreading ? Prefs.getThreads() : 1;

        elastixRegister.run();

        ElastixTransform transform = new ElastixTransform();
        transform.registerHelper = elastixRegister.rh;
        transform.enableMultiThreading = p.enableMultiThreading;

        return transform;

    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(ELASTIX_SEPARATOR, this));

        parameters.add(new FilePathP(ELASTIX_EXE, this, Prefs.get(Elastix.class.getName() + ".exePath", "")));
        parameters.add(new FilePathP(TRANSFORMIX_EXE, this, Prefs.get(Transformix.class.getName() + ".exePath", "")));
        parameters.add(new BooleanP(AFFINE, this, true));
        parameters.add(new BooleanP(FAST_AFFINE, this, false));
        parameters.add(new BooleanP(RIGID, this, false));
        parameters.add(new BooleanP(SPLINE, this, false));
        parameters.add(new IntegerP(SPLINE_GRID_SPACING, this, 3));

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(ELASTIX_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ELASTIX_EXE));
        returnedParameters.add(parameters.getParameter(TRANSFORMIX_EXE));
        returnedParameters.add(parameters.getParameter(AFFINE));
        returnedParameters.add(parameters.getParameter(FAST_AFFINE));
        returnedParameters.add(parameters.getParameter(RIGID));
        returnedParameters.add(parameters.getParameter(SPLINE));

        if ((boolean) parameters.getValue(SPLINE, workspace))
            returnedParameters.add(parameters.getParameter(SPLINE_GRID_SPACING));

        return returnedParameters;

    }

    public class ElastixParam extends Param {
        public boolean affine = false;
        public boolean fastAffine = false;
        public boolean rigid = false;
        public boolean spline = false;
        public int splineGridSpacing = 3;
        public boolean enableMultiThreading = true;

    }

    public class ElastixTransform extends Transform {
        public RegisterHelper registerHelper = null;
        public boolean enableMultiThreading = true;
    }
}
