package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.Abstract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import ij.gui.PointRoi;
import ij.process.ImageProcessor;
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
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Process.Interactable.PointPairSelector.PointPair;

public abstract class AbstractAffineRegistration<T extends RealType<T> & NativeType<T>>
        extends AbstractRegistration<T> {    
    public static final String TRANSFORMATION_MODE = "Transformation mode";

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

    public static ArrayList<PointPair> convertPointMatchToPointPair(Collection<PointMatch> matches) {
        ArrayList<PointPair> pairs = new ArrayList<>();

        int count = 1;
        for (PointMatch match : matches) {
            PointRoi point1 = new PointRoi(match.getP1().getL()[0], match.getP1().getL()[1]);
            PointRoi point2 = new PointRoi(match.getP2().getL()[0], match.getP2().getL()[1]);
            pairs.add(new PointPair(point1,point2,count++));
        }            

        return pairs;

    }

    @Override
    public void getParameters(Param param, Workspace workspace) {       
        AffineParam affineParam = (AffineParam) param;
        affineParam.transformationMode = parameters.getValue(TRANSFORMATION_MODE);

    }
    
    @Override
    public ImageProcessor applyTransform(ImageProcessor inputIpr, Transform transform) {
        inputIpr.setInterpolationMethod(ImageProcessor.BILINEAR);        
        ImageProcessor outputIpr = inputIpr.createProcessor(inputIpr.getWidth(), inputIpr.getHeight());
        
        ((AffineTransform) transform).mapping.mapInterpolated(inputIpr, outputIpr);

        return outputIpr;
        
    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new ChoiceP(TRANSFORMATION_MODE, this, TransformationModes.RIGID, TransformationModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(TRANSFORMATION_MODE));

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

    }

    public abstract class AffineParam implements Param {
        public String transformationMode = TransformationModes.RIGID;

    }

    public class AffineTransform implements Transform {
        public InverseTransformMapping mapping = null;
    }
}
