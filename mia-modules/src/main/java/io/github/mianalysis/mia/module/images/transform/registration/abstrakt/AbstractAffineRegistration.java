package io.github.mianalysis.mia.module.images.transform.registration.abstrakt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ij.Prefs;
import ij.gui.PointRoi;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.registration.AffineFixedTransform;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.PointPair;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import mpicbg.ij.InverseTransformMapping;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.AffineModel2D;
import mpicbg.models.PointMatch;
import mpicbg.models.RigidModel2D;
import mpicbg.models.SimilarityModel2D;
import mpicbg.models.TranslationModel2D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public abstract class AbstractAffineRegistration<T extends RealType<T> & NativeType<T>>
        extends AbstractRegistration<T> {
    protected ResultsTable resultsTable;

    public static final String TRANSFORMATION_MODE = "Transformation mode";
    public static final String TEST_FLIP = "Test flip (mirror image)";
    public static final String INDEPENDENT_ROTATION = "Independent rotation";
    public static final String ORIENTATION_INCREMENT = "Orientation increment (degs)";
    public static final String SHOW_TRANSFORMATION = "Show transformation(s)";
    public static final String CLEAR_BETWEEN_IMAGES = "Clear between images";

    public AbstractAffineRegistration(String name, Modules modules) {
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

    public ArrayList<Integer> getOrientations(int orientationIncrement) {
        ArrayList<Integer> orientations = new ArrayList<>();

        // Prevent infinite loops
        if (orientationIncrement == 0) {
            orientations.add(0);
            return orientations;
        }

        // Prevent increments going backwards
        orientationIncrement = Math.abs(orientationIncrement);

        int orientation = 0;
        while (orientation < 360) {
            orientations.add(orientation);
            orientation += orientationIncrement;
        }

        return orientations;

    }

    ImageProcessor applyRotation(ImageProcessor iprIn, int orientation, int fillValue) {
        ImageProcessor iprOut = iprIn.duplicate();
        iprOut.setBackgroundValue(fillValue);

        if (orientation != 0)
            iprOut.rotate(orientation);

        return iprOut;

    }

    Object[] testTransforms(ImageProcessor referenceIpr, ImageProcessor warpedIpr, AffineParam param,
            boolean isFlipped, AffineTransform transform, Object[] resBest) throws InterruptedException {

        int nThreads = param.multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        int fillValue = getFillValue(param.fillMode, warpedIpr);

        HashMap<Integer, Object[]> results = new HashMap<>();
        // Iterating over all orientations in normal (unflipped) mode
        for (int orientation : param.orientations) {
            // Apply orientation
            ImageProcessor rotatedIpr = applyRotation(warpedIpr, orientation, fillValue);

            Runnable task = () -> {
                results.put(orientation, fitModel(referenceIpr, rotatedIpr, param));
            };
            pool.submit(task);

        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        for (int orientation : results.keySet()) {
            Object[] resCandidate = results.get(orientation);

            if (resCandidate == null)
                continue;

            // If no best alignment has been determined, use this one
            if (resBest == null) {
                resBest = resCandidate;
                continue;
            }

            // Checking which is the best (lowest cost) match
            if (((AbstractAffineModel2D) resCandidate[0]).getCost() < ((AbstractAffineModel2D) resBest[0]).getCost()) {
                resBest = resCandidate;
                transform.flip = isFlipped;
                transform.orientation = orientation;
            }
        }

        return resBest;

    }

    @Override
    public Transform getTransform(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param,
            boolean showDetectedPoints) {
        AffineTransform transform = new AffineTransform();
        AffineParam aParam = (AffineParam) param;

        Object[] resBest = null;
        try {
            resBest = testTransforms(referenceIpr, warpedIpr, aParam, false, transform, resBest);
            if (((AffineParam) param).testFlip) {
                warpedIpr = flip(warpedIpr);
                resBest = testTransforms(referenceIpr, warpedIpr, aParam, true, transform, resBest);
            }
        } catch (Exception e) {
            MIA.log.writeError(e);
        }

        if (resBest == null)
            return null;

        if (showDetectedPoints) {
            ArrayList<PointPair> pairs = convertPointMatchToPointPair((Vector<PointMatch>) resBest[1]);
            showDetectedPoints(referenceIpr, warpedIpr, pairs);
        }

        if (((AffineParam) param).showTransform) {
            AbstractAffineModel2D model = (AbstractAffineModel2D) resBest[0];
            double[] matrix = new double[6];
            model.toArray(matrix);

            resultsTable.addRow();
            resultsTable.addValue("TIMEPOINT", param.t);
            resultsTable.addValue("M00", matrix[0]);
            resultsTable.addValue("M10", matrix[1]);
            resultsTable.addValue("M01", matrix[2]);
            resultsTable.addValue("M11", matrix[3]);
            resultsTable.addValue("M02", matrix[4]);
            resultsTable.addValue("M12", matrix[5]);
            resultsTable.show("Affine transformations (" + ((AffineParam) param).imageName + ")");

        }

        transform.mapping = new InverseTransformMapping<AbstractAffineModel2D<?>>((AbstractAffineModel2D) resBest[0]);

        return transform;

    }

    @Override
    public void getParameters(Param param, Workspace workspace) {
        super.getParameters(param, workspace);

        AffineParam affineParam = (AffineParam) param;
        affineParam.transformationMode = parameters.getValue(TRANSFORMATION_MODE,workspace);
        affineParam.testFlip = parameters.getValue(TEST_FLIP,workspace);

        if ((boolean) parameters.getValue(INDEPENDENT_ROTATION,workspace)) {
            int orientationIncrement = parameters.getValue(ORIENTATION_INCREMENT,workspace);
            ArrayList<Integer> orientations = getOrientations(orientationIncrement);
            affineParam.orientations = orientations;
        } else {
            affineParam.orientations = new ArrayList<>();
            affineParam.orientations.add(0);
        }

        affineParam.multithread = parameters.getValue(ENABLE_MULTITHREADING,workspace);
        affineParam.showTransform = parameters.getValue(SHOW_TRANSFORMATION,workspace);
        affineParam.clearBetweenImages = parameters.getValue(CLEAR_BETWEEN_IMAGES,workspace);
        affineParam.imageName = parameters.getValue(INPUT_IMAGE,workspace);

        if (affineParam.showTransform)
            if (resultsTable == null || affineParam.clearBetweenImages)
                resultsTable = new ResultsTable();
    }

    @Override
    public ImageProcessor applyTransform(ImageProcessor inputIpr, Transform transform, int fillValue) {
        if (((AffineTransform) transform).flip)
            inputIpr.flipHorizontal();

        // Applying rotation
        int orientation = ((AffineTransform) transform).orientation;
        inputIpr = applyRotation(inputIpr, orientation, fillValue);

        ImageProcessor outputIpr = inputIpr.createProcessor(inputIpr.getWidth(), inputIpr.getHeight());
        outputIpr.setColor(fillValue);
        outputIpr.fill();

        // inputIpr.setInterpolationMethod(ImageProcessor.BILINEAR);
        ((AffineTransform) transform).mapping.mapInterpolated(inputIpr, outputIpr);

        return outputIpr;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new ChoiceP(TRANSFORMATION_MODE, this, TransformationModes.RIGID, TransformationModes.ALL));
        parameters.add(new BooleanP(TEST_FLIP, this, false));
        parameters.add(new BooleanP(INDEPENDENT_ROTATION, this, false));
        parameters.add(new IntegerP(ORIENTATION_INCREMENT, this, 10));
        parameters.add(new BooleanP(SHOW_TRANSFORMATION, this, false));
        parameters.add(new BooleanP(CLEAR_BETWEEN_IMAGES, this, false));

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        // Adding all default parameters and adding transformation mode just after
        // registration separator
        Parameters defaultParameters = super.updateAndGetParameters();
        for (Parameter parameter : defaultParameters.values()) {
            returnedParameters.add(parameter);
            if (parameter.getName().equals(REGISTRATION_SEPARATOR))
                returnedParameters.add(parameters.getParameter(TRANSFORMATION_MODE));

            if (parameter.getName().equals(FILL_MODE)) {
                returnedParameters.add(parameters.getParameter(TEST_FLIP));
                returnedParameters.add(parameters.getParameter(INDEPENDENT_ROTATION));
                if ((boolean) parameters.getValue(INDEPENDENT_ROTATION,workspace))
                    returnedParameters.add(parameters.getParameter(ORIENTATION_INCREMENT));
            }

            if (parameter.getName().equals(SHOW_DETECTED_POINTS)) {
                returnedParameters.add(parameters.getParameter(SHOW_TRANSFORMATION));
                if ((boolean) parameters.getValue(SHOW_TRANSFORMATION,workspace))
                    returnedParameters.add(parameters.getParameter(CLEAR_BETWEEN_IMAGES));
            }
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

        parameters.get(INDEPENDENT_ROTATION).setDescription(
                "When selected, the image will be rotated multiple times, with registration optimised at each orientation.  The orientation with the best score will be retained.  This is useful for algorithms which perform poorly with rotated features (e.g. block matching).  The increment between rotations is controlled by \""
                        + ORIENTATION_INCREMENT + "\".");

        parameters.get(ORIENTATION_INCREMENT).setDescription(
                "If \"" + INDEPENDENT_ROTATION
                        + "\" is enabled, this is the angular increment between rotations.  The increment is specified in degree units.");

        parameters.get(SHOW_TRANSFORMATION).setDescription(
                "When selected, the affine transform will be displayed in the results table.  Fixed affine transform values such as these can be applied using the \""
                        + new AffineFixedTransform(null).getName() + "\" module.");

        parameters.get(CLEAR_BETWEEN_IMAGES).setDescription(
                "If \"" + SHOW_TRANSFORMATION
                        + "\" is enabled, this parameter can be used to reset the displayed affine transform in the results table.  If this option isn't selected, the new transform will be added to the bottom of the results table.");

    }

    public abstract class AffineParam extends Param {
        public String transformationMode = TransformationModes.RIGID;
        public boolean testFlip = false;
        public ArrayList<Integer> orientations = new ArrayList<>();

        public boolean multithread = false;
        public boolean showTransform = false;
        public boolean clearBetweenImages = false;
        public String imageName = "";

    }

    public class AffineTransform extends Transform {
        public boolean flip = false;
        public int orientation = 0;
        public InverseTransformMapping mapping = null;

    }
}
