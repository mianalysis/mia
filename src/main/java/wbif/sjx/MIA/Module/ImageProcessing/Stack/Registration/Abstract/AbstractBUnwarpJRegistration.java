package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.Abstract;

import bunwarpj.bUnwarpJ_;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ImageTypeConverter;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;

public abstract class AbstractBUnwarpJRegistration<T extends RealType<T> & NativeType<T>>
        extends AbstractRegistration<T> {
    public static final String FEATURE_SEPARATOR = "Feature detection";    
    public static final String REGISTRATION_MODE = "Registration mode";
    public static final String SUBSAMPLE_FACTOR = "Subsample factor";
    public static final String INITIAL_DEFORMATION_MODE = "Initial deformation mode";
    public static final String FINAL_DEFORMATION_MODE = "Final deformation mode";
    public static final String DIVERGENCE_WEIGHT = "Divergence weight";
    public static final String CURL_WEIGHT = "Curl weight";
    public static final String LANDMARK_WEIGHT = "Landmark weight";
    public static final String IMAGE_WEIGHT = "Image weight";
    public static final String CONSISTENCY_WEIGHT = "Consistency weight";
    public static final String STOP_THRESHOLD = "Stop threshold";

    public interface RegistrationModes {
        final String FAST = "Fast";
        final String ACCURATE = "Accurate";
        final String MONO = "Mono";

        final String[] ALL = new String[] { FAST, ACCURATE, MONO };

    }

    public interface InitialDeformationModes {
        final String VERY_COARSE = "Very Coarse";
        final String COARSE = "Coarse";
        final String FINE = "Fine";
        final String VERY_FINE = "Very Fine";

        final String[] ALL = new String[] { VERY_COARSE, COARSE, FINE, VERY_FINE };

    }

    public interface FinalDeformationModes {
        final String VERY_COARSE = "Very Coarse";
        final String COARSE = "Coarse";
        final String FINE = "Fine";
        final String VERY_FINE = "Very Fine";
        final String SUPER_FINE = "Super Fine";

        final String[] ALL = new String[] { VERY_COARSE, COARSE, FINE, VERY_FINE, SUPER_FINE };

    }

    public AbstractBUnwarpJRegistration(String name, ModuleCollection modules) {
        super(name, modules);
    }

    protected int getRegistrationMode(String registrationMode) {
        switch (registrationMode) {
        case RegistrationModes.FAST:
        default:
            return 0;
        case RegistrationModes.ACCURATE:
            return 1;
        case RegistrationModes.MONO:
            return 2;
        }
    }

    protected int getInitialDeformationMode(String initialDeformationMode) {
        switch (initialDeformationMode) {
        case InitialDeformationModes.VERY_COARSE:
        default:
            return 0;
        case InitialDeformationModes.COARSE:
            return 1;
        case InitialDeformationModes.FINE:
            return 2;
        case InitialDeformationModes.VERY_FINE:
            return 3;
        }
    }

    protected int getFinalDeformationMode(String finalDeformationMode) {
        switch (finalDeformationMode) {
            case FinalDeformationModes.VERY_COARSE:
            default:
                return 0;
            case FinalDeformationModes.COARSE:
                return 1;
            case FinalDeformationModes.FINE:
                return 2;
            case FinalDeformationModes.VERY_FINE:
                return 3;
            case FinalDeformationModes.SUPER_FINE:
                return 4;
        }
    }

    @Override
    public void getParameters(Param param, Workspace workspace) {
        // Setting up the parameters
        BUnwarpJParam bUnwarpParam = (BUnwarpJParam) param;        

        // Adding bUnwarpJ-specific parameter object
        String registrationMode = parameters.getValue(REGISTRATION_MODE);
        bUnwarpParam.bParam = new bunwarpj.Param();
        bUnwarpParam.bParam.mode = getRegistrationMode(registrationMode);
        bUnwarpParam.bParam.img_subsamp_fact = parameters.getValue(SUBSAMPLE_FACTOR);
        bUnwarpParam.bParam.min_scale_deformation = getInitialDeformationMode(parameters.getValue(INITIAL_DEFORMATION_MODE));
        bUnwarpParam.bParam.max_scale_deformation = getFinalDeformationMode(parameters.getValue(FINAL_DEFORMATION_MODE));
        bUnwarpParam.bParam.divWeight = parameters.getValue(DIVERGENCE_WEIGHT);
        bUnwarpParam.bParam.curlWeight = parameters.getValue(CURL_WEIGHT);
        bUnwarpParam.bParam.landmarkWeight = parameters.getValue(LANDMARK_WEIGHT);
        bUnwarpParam.bParam.imageWeight = parameters.getValue(IMAGE_WEIGHT);
        bUnwarpParam.bParam.stopThreshold = parameters.getValue(STOP_THRESHOLD);
        if (registrationMode.equals(RegistrationModes.MONO))
            bUnwarpParam.bParam.consistencyWeight = 10.0;
        else
            bUnwarpParam.bParam.consistencyWeight = parameters.getValue(CONSISTENCY_WEIGHT);
    }
    
    @Override
    public ImageProcessor applyTransform(ImageProcessor inputIpr, Transform transform) {
        BUnwarpJTransform bUnwarpJTransform = (BUnwarpJTransform) transform;
        ImagePlus outputIpl = new ImagePlus("Output",inputIpr.duplicate());
        bUnwarpJ_.applyTransformToSource(bUnwarpJTransform.transformPath, outputIpl, outputIpl);
        ImageTypeConverter.process(outputIpl, inputIpr.getBitDepth(), ImageTypeConverter.ScalingModes.CLIP);

        return outputIpl.getProcessor();

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(FEATURE_SEPARATOR, this));        
        parameters.add(new ChoiceP(REGISTRATION_MODE, this, RegistrationModes.FAST, RegistrationModes.ALL));
        parameters.add(new IntegerP(SUBSAMPLE_FACTOR, this, 0));
        parameters.add(new ChoiceP(INITIAL_DEFORMATION_MODE, this, InitialDeformationModes.VERY_COARSE,
                InitialDeformationModes.ALL));
        parameters
                .add(new ChoiceP(FINAL_DEFORMATION_MODE, this, FinalDeformationModes.FINE, FinalDeformationModes.ALL));
        parameters.add(new DoubleP(DIVERGENCE_WEIGHT, this, 0d));
        parameters.add(new DoubleP(CURL_WEIGHT, this, 0d));
        parameters.add(new DoubleP(LANDMARK_WEIGHT, this, 0d));
        parameters.add(new DoubleP(IMAGE_WEIGHT, this, 1d));
        parameters.add(new DoubleP(CONSISTENCY_WEIGHT, this, 10d));
        parameters.add(new DoubleP(STOP_THRESHOLD, this, 0.01));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FEATURE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REGISTRATION_MODE));
        returnedParameters.add(parameters.getParameter(SUBSAMPLE_FACTOR));
        returnedParameters.add(parameters.getParameter(INITIAL_DEFORMATION_MODE));
        returnedParameters.add(parameters.getParameter(FINAL_DEFORMATION_MODE));
        returnedParameters.add(parameters.getParameter(DIVERGENCE_WEIGHT));
        returnedParameters.add(parameters.getParameter(CURL_WEIGHT));
        returnedParameters.add(parameters.getParameter(LANDMARK_WEIGHT));
        returnedParameters.add(parameters.getParameter(IMAGE_WEIGHT));

        switch ((String) parameters.getValue(REGISTRATION_MODE)) {
            case RegistrationModes.ACCURATE:
            case RegistrationModes.FAST:
                returnedParameters.add(parameters.getParameter(CONSISTENCY_WEIGHT));
                break;
        }

        returnedParameters.add(parameters.getParameter(STOP_THRESHOLD));

        return returnedParameters;

    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

    }

    public abstract class BUnwarpJParam implements Param {
        public bunwarpj.Param bParam = null;
    }

    public class BUnwarpJTransform implements Transform {
        public String transformPath = "";
    }
}
