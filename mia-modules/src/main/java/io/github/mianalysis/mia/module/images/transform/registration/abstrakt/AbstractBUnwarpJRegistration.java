package io.github.mianalysis.mia.module.images.transform.registration.abstrakt;

import bunwarpj.bUnwarpJ_;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageTypeConverter;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

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

    public AbstractBUnwarpJRegistration(String name, Modules modules) {
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
        String registrationMode = parameters.getValue(REGISTRATION_MODE, workspace);
        bUnwarpParam.bParam = new bunwarpj.Param();
        bUnwarpParam.bParam.mode = getRegistrationMode(registrationMode);
        bUnwarpParam.bParam.img_subsamp_fact = parameters.getValue(SUBSAMPLE_FACTOR, workspace);
        bUnwarpParam.bParam.min_scale_deformation = getInitialDeformationMode(
                parameters.getValue(INITIAL_DEFORMATION_MODE, workspace));
        bUnwarpParam.bParam.max_scale_deformation = getFinalDeformationMode(
                parameters.getValue(FINAL_DEFORMATION_MODE, workspace));
        bUnwarpParam.bParam.divWeight = parameters.getValue(DIVERGENCE_WEIGHT, workspace);
        bUnwarpParam.bParam.curlWeight = parameters.getValue(CURL_WEIGHT, workspace);
        bUnwarpParam.bParam.landmarkWeight = parameters.getValue(LANDMARK_WEIGHT, workspace);
        bUnwarpParam.bParam.imageWeight = parameters.getValue(IMAGE_WEIGHT, workspace);
        bUnwarpParam.bParam.stopThreshold = parameters.getValue(STOP_THRESHOLD, workspace);
        if (registrationMode.equals(RegistrationModes.MONO))
            bUnwarpParam.bParam.consistencyWeight = 10.0;
        else
            bUnwarpParam.bParam.consistencyWeight = parameters.getValue(CONSISTENCY_WEIGHT, workspace);
    }

    @Override
    public ImageProcessor applyTransform(ImageProcessor inputIpr, Transform transform, int fillValue) {
        BUnwarpJTransform bUnwarpJTransform = (BUnwarpJTransform) transform;
        ImagePlus outputIpl = new ImagePlus("Output", inputIpr.duplicate());

        for (int i = 0; i < outputIpl.getStack().size(); i++)
            outputIpl.getStack().getProcessor(i + 1).setColor(fillValue);

        bUnwarpJ_.applyTransformToSource(bUnwarpJTransform.transformPath, outputIpl, outputIpl);
        ImageTypeConverter.process(outputIpl, inputIpr.getBitDepth(), ImageTypeConverter.ScalingModes.CLIP);

        return outputIpl.getProcessor();

    }

    @Override
    public void initialiseParameters() {
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

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

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

        switch ((String) parameters.getValue(REGISTRATION_MODE, workspace)) {
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

        String siteRef = "Description taken from <a href=\"https://imagej.net/BUnwarpJ\">https://imagej.net/BUnwarpJ</a>";

        parameters.get(REGISTRATION_MODE).setDescription(
                "\"The registration mode can be \"Accurate\", \"Fast\" and \"Mono\". The registration mode \"Mono\" makes the program to perform only unidirectional registration, i.e. from source to target. The two registration modes \"Accurate\" and \"Fast\" involve performing bidirectional registration and affect the stopping criteria internally used by the program.\"  "
                        + siteRef);

        parameters.get(SUBSAMPLE_FACTOR).setDescription(
                "\"The registration will be calculated using subsampled versions of the images but the results will be applied to the original ones. The image subsampling parameter can be chosen between 0 and 7, i.e. the image dimensions can be reduced by a factor of 2^0 = 1 to 2^7 = 128. This is very useful when registering large images.\"  "
                        + siteRef);

        parameters.get(INITIAL_DEFORMATION_MODE).setDescription(
                "\"Determines the level of detail of the initial deformation. In bUnwarpJ this is defined by the number of B-splines used to represent the deformations:<br><br><table style=\"font:sans-serif;font-size:12\"><tr><th>Deformation</th><th>Num. intervals in grid</th></tr><tr><td>Very coarse</td><td>1x1</td></tr><tr><td>Coarse</td><td>2x2</td></tr><tr><td>Fine</td><td>3x3</td></tr><tr><td>Very fine</td><td>8x8</td></tr><tr><td>Super fine</td><td>16x16</td></tr></table><font face=\"sans-serif\" size=\"3\"><br>If images start very far away from the right alignment, it is usually a good idea to go from \"Very Coarse\" to \"Very Fine\". If they start close to the right alignment, using a very coarse initial deformation could cause the algorithm to fail. So, in that case, it would be enough to set initial deformation to \"Fine\" and final deformation to \"Very Fine\". Use \"Super Fine\" only when you need a very high level of accuracy, because it makes the algorithm quite slower depending on the image sizes.  "
                        + siteRef);

        parameters.get(FINAL_DEFORMATION_MODE)
                .setDescription("See description for \"" + INITIAL_DEFORMATION_MODE + "\"");

        parameters.get(DIVERGENCE_WEIGHT).setDescription(
                "Regularizes the deformation by penalizing the divergence of the deformation vector field.  If you see that your transformations get too rough, it is a good idea to use this parameter.  A value of 0.1 is usually good if there's no prior knowledge about the deformation shape.  "
                        + siteRef);

        parameters.get(CURL_WEIGHT).setDescription(
                "Regularizes the deformation by penalizing the curl of the deformation vector field.  If you see that your transformations get too rough, it is a good idea to use this parameter.  A value of 0.1 is usually good if there's no prior knowledge about the deformation shape.  "
                        + siteRef);

        parameters.get(LANDMARK_WEIGHT).setDescription(
                "Forces the deformations to fit the landmark points.  Set it to 1.0 unless you're not using landmarks.  "
                        + siteRef);

        parameters.get(IMAGE_WEIGHT).setDescription(
                "The weight to control the pixel values difference.  Leave it at 1.0 unless you want to do, for instance, landmark-only registration.  "
                        + siteRef);

        parameters.get(CONSISTENCY_WEIGHT).setDescription(
                "Forces the resulting deformations to be one (source to target) as close as possible to the inverse of the other one (target to source). Values between 10.0 and 30.0 usually work fine. It is only taken into account for registration modes \"Fast\" or \"Accurate\".  "
                        + siteRef);

        parameters.get(STOP_THRESHOLD).setDescription(
                "Stops the optimization process at each multiresolution level when the error relative change is not larger than this threshold.  "
                        + siteRef);

    }

    public abstract class BUnwarpJParam extends Param {
        public bunwarpj.Param bParam = null;
    }

    public class BUnwarpJTransform extends Transform {
        public String transformPath = "";
    }
}
