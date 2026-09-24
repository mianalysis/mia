package io.github.mianalysis.mia.module.images.transform.registration;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ch.epfl.biop.fiji.imageplusutils.ImagePlusFunctions;
import ch.epfl.biop.wrappers.elastix.Elastix;
import ch.epfl.biop.wrappers.elastix.Mod_RegisterHelper;
import ch.epfl.biop.wrappers.elastix.RHZipFile;
import ch.epfl.biop.wrappers.elastix.RegisterHelper;
import ch.epfl.biop.wrappers.elastix.ij2commands.Mod_Elastix_Register;
import ch.epfl.biop.wrappers.transformix.DefaultTransformixTask;
import ch.epfl.biop.wrappers.transformix.Mod_TransformHelper;
import ch.epfl.biop.wrappers.transformix.Transformix;
import ij.ImagePlus;
import ij.Prefs;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageTypeConverter;
import io.github.mianalysis.mia.module.images.transform.registration.abstrakt.AbstractRegistration;
import io.github.mianalysis.mia.module.inputoutput.LoadImage;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ElastixRegistration<T extends RealType<T> & NativeType<T>>
        extends AbstractRegistration<T> {
    public static final String ELASTIX_SEPARATOR = "Elastix controls";

    public static final String ELASTIX_EXE = "Elastix executable";

    public static final String TRANSFORMIX_EXE = "Transformix executable";

    public static final String TRANSFORM_MODE = "Transform mode";

    public static final String TRANSFORM_FILE_PATH = "Transform file path";

    public static final String AFFINE = "Affine";

    public static final String FAST_AFFINE = "Fast affine";

    public static final String RIGID = "Rigid";

    public static final String SPLINE = "Spline";

    public static final String SPLINE_GRID_SPACING = "Spline grid spacing";

    public static final String TRANSFORM_SAVING_SEPARATOR = "Transform saving controls";

    public static final String SAVE_TRANSFORM = "Save transform";

    public static final String SAVE_WARNING = "Save warning";

    public interface TransformModes {
        String CALCULATE = "Calculate";
        String LOAD_FROM_FILE = "Load from file";

        String[] ALL = new String[] { CALCULATE, LOAD_FROM_FILE };

    }

    public ElastixRegistration(Modules modules) {
        super("Elastix registration", modules);
    }

    @Override
    public void getParameters(Param param, Workspace workspace) {
        // Setting up the parameters
        ElastixParam elastixParam = (ElastixParam) param;

        elastixParam.transformMode = parameters.getValue(TRANSFORM_MODE, workspace);
        switch (elastixParam.transformMode) {
            case TransformModes.CALCULATE:
                elastixParam.affine = parameters.getValue(AFFINE, workspace);
                elastixParam.fastAffine = parameters.getValue(FAST_AFFINE, workspace);
                elastixParam.rigid = parameters.getValue(RIGID, workspace);
                elastixParam.spline = parameters.getValue(SPLINE, workspace);
                elastixParam.splineGridSpacing = parameters.getValue(SPLINE_GRID_SPACING, workspace);
                elastixParam.saveTransform = parameters.getValue(SAVE_TRANSFORM, workspace);
                break;
            case TransformModes.LOAD_FROM_FILE:
                String fileName = parameters.getValue(TRANSFORM_FILE_PATH, workspace);
                try {
                    elastixParam.transformFilePath = LoadImage.getGenericName(workspace.getMetadata(), fileName);
                } catch (ServiceException | DependencyException | FormatException | IOException e) {
                    MIA.log.writeError(e);
                }
                break;
        }

        elastixParam.enableMultiThreading = parameters.getValue(ENABLE_MULTITHREADING, workspace);

        if (elastixParam.saveTransform) {
            elastixParam.outputPath = getOutputPath(modules, workspace);
            elastixParam.outputName = getOutputName(modules, workspace);
            elastixParam.workspace = workspace;
            elastixParam.appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE, workspace);
            elastixParam.appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
            elastixParam.suffix = parameters.getValue(SAVE_SUFFIX, workspace);
        }
    }

    @Override
    public ImageProcessor applyTransform(ImageProcessor inputIpr, Transform transform, int fillValue) {
        ElastixTransform elastixTransform = (ElastixTransform) transform;
        RegisterHelper rh = elastixTransform.registerHelper;

        inputIpr.setColor(fillValue);

        ImagePlus outputIpl = ImagePlusFunctions.splitApplyRecompose(
                imp -> {
                    Mod_TransformHelper th = new Mod_TransformHelper();
                    th.setImage(imp);
                    th.setTransformFile(rh);
                    th.setDefaultOutputDir();
                    th.setNThreads(elastixTransform.enableMultiThreading ? Prefs.getThreads() : 1);
                    th.transform(new DefaultTransformixTask());
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

        ElastixTransform transform = new ElastixTransform();

        switch (p.transformMode) {
            case TransformModes.CALCULATE:
                Mod_Elastix_Register elastixRegister = new Mod_Elastix_Register();
                elastixRegister.fixed_image = new ImagePlus("Fixed", referenceIpr);
                elastixRegister.moving_image = new ImagePlus("Moving", warpedIpr);
                elastixRegister.nThreads = p.enableMultiThreading ? Prefs.getThreads() : 1;
                elastixRegister.affine = p.affine;
                elastixRegister.fast_affine = p.fastAffine;
                elastixRegister.rigid = p.rigid;
                elastixRegister.spline = p.spline;
                elastixRegister.spline_grid_spacing = p.splineGridSpacing;
                elastixRegister.run();

                transform.registerHelper = elastixRegister.rh;

                break;
            case TransformModes.LOAD_FROM_FILE:
                Mod_RegisterHelper rh = new Mod_RegisterHelper();
                rh = rh.loadFromZip(new RHZipFile(new File(p.transformFilePath)));
                rh.setFixedImage(new ImagePlus("Fixed", referenceIpr));
                rh.setMovingImage(new ImagePlus("Moving", warpedIpr));

                transform.registerHelper = rh;

                break;
        }

        transform.enableMultiThreading = p.enableMultiThreading;

        if (p.saveTransform) {
            String outputPath = p.outputPath;
            String outputName = p.outputName;
            Workspace workspace = p.workspace;
            String appendSeriesMode = p.appendSeriesMode;
            String appendDateTimeMode = p.appendDateTimeMode;
            String suffix = p.suffix;

            String finalOutputPath = outputPath + outputName;
            finalOutputPath = appendSeries(finalOutputPath, workspace, appendSeriesMode);
            finalOutputPath = appendDateTime(finalOutputPath, appendDateTimeMode);
            finalOutputPath = finalOutputPath + suffix;
            finalOutputPath = finalOutputPath + ".zip";

            File outputFile = new File(finalOutputPath);
            outputFile.getParentFile().mkdirs();

            RHZipFile zipFile = transform.registerHelper.saveToZip(transform.registerHelper);
            try {
                FileUtils.copyFile(zipFile.f, outputFile);
            } catch (IOException e) {
                MIA.log.writeWarning("Could not save transform to file \"" + finalOutputPath + "\"");
                e.printStackTrace();
            }

        }

        return transform;

    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();
        super.initialiseSavingParameters();

        parameters.add(new SeparatorP(ELASTIX_SEPARATOR, this));
        parameters.add(new FilePathP(ELASTIX_EXE, this, Prefs.get(Elastix.class.getName() + ".exePath", "")));
        parameters.add(new FilePathP(TRANSFORMIX_EXE, this, Prefs.get(Transformix.class.getName() + ".exePath", "")));
        parameters.add(new ChoiceP(TRANSFORM_MODE, this, TransformModes.CALCULATE, TransformModes.ALL));
        parameters.add(new FilePathP(TRANSFORM_FILE_PATH, this, ""));
        parameters.add(new BooleanP(AFFINE, this, true));
        parameters.add(new BooleanP(FAST_AFFINE, this, false));
        parameters.add(new BooleanP(RIGID, this, false));
        parameters.add(new BooleanP(SPLINE, this, false));
        parameters.add(new IntegerP(SPLINE_GRID_SPACING, this, 3));

        parameters.add(new SeparatorP(TRANSFORM_SAVING_SEPARATOR, this));
        parameters.add(new BooleanP(SAVE_TRANSFORM, this, false));
        parameters.add(new MessageP(SAVE_WARNING, this,
                "Saving is intended for static transforms, not ones which vary over time or slice.  As such, only the final calculated transform(s) will be saved.",
                ParameterState.WARNING));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(ELASTIX_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ELASTIX_EXE));
        returnedParameters.add(parameters.getParameter(TRANSFORMIX_EXE));

        returnedParameters.add(parameters.getParameter(TRANSFORM_MODE));
        switch ((String) parameters.getValue(TRANSFORM_MODE, workspace)) {
            case TransformModes.CALCULATE:
                returnedParameters.add(parameters.getParameter(AFFINE));
                returnedParameters.add(parameters.getParameter(FAST_AFFINE));
                returnedParameters.add(parameters.getParameter(RIGID));
                returnedParameters.add(parameters.getParameter(SPLINE));

                if ((boolean) parameters.getValue(SPLINE, workspace))
                    returnedParameters.add(parameters.getParameter(SPLINE_GRID_SPACING));

                returnedParameters.add(parameters.getParameter(TRANSFORM_SAVING_SEPARATOR));
                returnedParameters.add(parameters.getParameter(SAVE_TRANSFORM));
                if ((Boolean) parameters.getValue(SAVE_TRANSFORM, workspace)) {
                    returnedParameters.add(parameters.getParameter(SAVE_WARNING));
                    Parameters savingParameters = super.updateAndGetSavingParameters();
                    savingParameters.remove(AbstractSaver.FILE_SAVING_SEPARATOR);
                    returnedParameters.addAll(savingParameters);
                }
                break;
            case TransformModes.LOAD_FROM_FILE:
                returnedParameters.add(parameters.getParameter(TRANSFORM_FILE_PATH));
                break;
        }

        Elastix.setExePath(new File((String) parameters.getValue(ELASTIX_EXE, workspace)));
        Transformix.setExePath(new File((String) parameters.getValue(TRANSFORMIX_EXE, workspace)));

        return returnedParameters;

    }

    public class ElastixParam extends Param {
        public String transformMode = "";
        public String transformFilePath = "";
        public boolean affine = false;
        public boolean fastAffine = false;
        public boolean rigid = false;
        public boolean spline = false;
        public int splineGridSpacing = 3;

        public boolean enableMultiThreading = true;

        public boolean saveTransform = false;
        public String outputPath = "";
        public String outputName = "";
        public Workspace workspace = null;
        public String appendSeriesMode = "";
        public String appendDateTimeMode = "";
        public String suffix = "";

    }

    public class ElastixTransform extends Transform {
        public RegisterHelper registerHelper = null;
        public boolean enableMultiThreading = true;
    }
}
