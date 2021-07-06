package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.Abstract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import mpicbg.ij.InverseTransformMapping;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.AffineModel2D;
import mpicbg.models.PointMatch;
import mpicbg.models.RigidModel2D;
import mpicbg.models.SimilarityModel2D;
import mpicbg.models.TranslationModel2D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Process.Interactable.PointPairSelector.PointPair;

public abstract class AbstractAffineRegistration<T extends RealType<T> & NativeType<T>>
        extends AbstractRegistration<T> {
    public static final String TRANSFORMATION_MODE = "Transformation mode";
    public static final String TEST_FLIP = "Test flip (mirror image)";

    public AbstractAffineRegistration(String name, ModuleCollection modules) {
        super(name, modules);
    }

    public interface TransformationModes {
        String AFFINE = "Affine (trans., rot., scale, shear)";
        String SIMILARITY = "Similarity (trans., rot., iso-scale)";
        String RIGID = "Rigid (trans., rot.)";
        String TRANSLATION = "Translation";

        String[] ALL = new String[] { AFFINE, SIMILARITY, RIGID, TRANSLATION };

    }

    public static AbstractAffineModel2D getModel(String transformationMode) {
        switch (transformationMode) {
            case TransformationModes.AFFINE:
                return new AffineModel2D();
            case TransformationModes.RIGID:
            default:
                return new RigidModel2D();
            case TransformationModes.SIMILARITY:
                return new SimilarityModel2D();
            case TransformationModes.TRANSLATION:
                return new TranslationModel2D();
        }
    }

    protected abstract Object[] fitModel(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param);

    public static ArrayList<PointPair> convertPointMatchToPointPair(Collection<PointMatch> matches) {
        ArrayList<PointPair> pairs = new ArrayList<>();

        int count = 1;
        for (PointMatch match : matches) {
            PointRoi point1 = new PointRoi(match.getP1().getL()[0], match.getP1().getL()[1]);
            PointRoi point2 = new PointRoi(match.getP2().getL()[0], match.getP2().getL()[1]);
            pairs.add(new PointPair(point1, point2, count++));
        }

        return pairs;

    }

    public static ImageProcessor flip(ImageProcessor iprIn) {
        ImageProcessor iprOut;

        switch (iprIn.getBitDepth()) {
            case 8:
            default:
                iprOut = new ByteProcessor(iprIn.getWidth(), iprIn.getHeight());
                break;
            case 16:
                iprOut = new ShortProcessor(iprIn.getWidth(), iprIn.getHeight());
                break;
            case 32:
                iprOut = new FloatProcessor(iprIn.getWidth(), iprIn.getHeight());
                break;
        }

        for (int x = 0; x < iprIn.getWidth(); x++)
            for (int y = 0; y < iprIn.getHeight(); y++)
                iprOut.putPixel(iprIn.getWidth() - x, y, iprIn.getPixel(x, y));
            
        return iprOut;

    }

    @Override
    public Transform getTransform(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param,
            boolean showDetectedPoints) {
        AffineTransform transform = new AffineTransform();

        // Test normal
        Object[] res1 = fitModel(referenceIpr, warpedIpr, param);
        if (res1 == null)
            return null;
            
        if (((AffineParam) param).testFlip) {
            warpedIpr = flip(warpedIpr);
            Object[] res2 = fitModel(referenceIpr, warpedIpr, param);
            if (res2 == null)
                return null;

            if (((AbstractAffineModel2D) res2[0]).getCost() < ((AbstractAffineModel2D) res1[0]).getCost()) {
                res1 = res2;
                transform.flip = true;
            } else {
                transform.flip = false;
            }
        }

        if (showDetectedPoints) {
            ArrayList<PointPair> pairs = convertPointMatchToPointPair((Vector<PointMatch>) res1[1]);
            showDetectedPoints(referenceIpr, warpedIpr, pairs);
        }

        transform.mapping = new InverseTransformMapping<AbstractAffineModel2D<?>>((AbstractAffineModel2D) res1[0]);
        
        return transform;

    }

    @Override
    public void getParameters(Param param, Workspace workspace) {
        AffineParam affineParam = (AffineParam) param;
        affineParam.transformationMode = parameters.getValue(TRANSFORMATION_MODE);
        affineParam.testFlip = parameters.getValue(TEST_FLIP);

    }

    @Override
    public ImageProcessor applyTransform(ImageProcessor inputIpr, Transform transform) {
        if (((AffineTransform) transform).flip)
            inputIpr.flipHorizontal();

        inputIpr.setInterpolationMethod(ImageProcessor.BILINEAR);
        ImageProcessor outputIpr = inputIpr.createProcessor(inputIpr.getWidth(), inputIpr.getHeight());

        ((AffineTransform) transform).mapping.mapInterpolated(inputIpr, outputIpr);

        return outputIpr;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new ChoiceP(TRANSFORMATION_MODE, this, TransformationModes.RIGID, TransformationModes.ALL));
        parameters.add(new BooleanP(TEST_FLIP, this, false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        // Adding all default parameters and adding transformation mode just after
        // registration separator
        ParameterCollection defaultParameters = super.updateAndGetParameters();
        for (Parameter parameter : defaultParameters.values()) {
            returnedParameters.add(parameter);
            if (parameter.getName().equals(REGISTRATION_SEPARATOR))
                returnedParameters.add(parameters.getParameter(TRANSFORMATION_MODE));

            if (parameter.getName().equals(FILL_MODE))
                returnedParameters.add(parameters.getParameter(TEST_FLIP));
        }

        return returnedParameters;

    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(TRANSFORMATION_MODE).setDescription("Controls the type of registration being applied:<br><ul>"

                + "<li>\"" + TransformationModes.AFFINE
                + "\" Applies the full affine transformation, whereby the input image can undergo translation, rotation, reflection, scaling and shear.</li>"

                + "<li>\"" + TransformationModes.RIGID
                + "\" Applies only translation and rotation to the input image.  As such, all features should remain the same size.</li>"

                + "<li>\"" + TransformationModes.SIMILARITY
                + "\" Applies translation, rotating and linear scaling to the input image.</li>"

                + "<li>\"" + TransformationModes.TRANSLATION
                + "\" Applies only translation (motion within the 2D plane) to the input image.</li></ul>");

                parameters.get(TEST_FLIP).setDescription(
                        "When selected, alignment will be tested for both the \"normal\" and \"flipped\" (mirror) states of the image.  The state yielding the lower cost to alignment will be retained.");
                
    }

    public abstract class AffineParam extends Param {
        public String transformationMode = TransformationModes.RIGID;
        public boolean testFlip = false;

    }

    public class AffineTransform extends Transform {
        public boolean flip = false;
        public InverseTransformMapping mapping = null;
    }
}
