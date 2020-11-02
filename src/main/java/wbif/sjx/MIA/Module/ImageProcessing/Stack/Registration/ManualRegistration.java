package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import mpicbg.ij.InverseTransformMapping;
import mpicbg.ij.util.Util;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.PointMatch;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ConcatenateStacks;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ExtractSubstack;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.MIA.Process.Interactable.Interactable;
import wbif.sjx.MIA.Process.Interactable.PointPairSelector;
import wbif.sjx.MIA.Process.Interactable.PointPairSelector.PointPair;

public class ManualRegistration<T extends RealType<T> & NativeType<T>> extends CoreRegistrationHandler implements Interactable {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String REGISTRATION_SEPARATOR = "Registration controls";
    // public static final String REGISTRATION_AXIS = "Registration axis";
    // public static final String OTHER_AXIS_MODE = "Other axis mode";
    public static final String TRANSFORMATION_MODE = "Transformation mode";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";
    public static final String FILL_MODE = "Fill mode";

    public static final String REFERENCE_SEPARATOR = "Reference image source";
    public static final String REFERENCE_IMAGE = "Reference image";

    private Image inputImage;
    private Image reference;

    public ManualRegistration(ModuleCollection modules) {
        super("Manual registration", modules);
    }

    public interface RegistrationAxes {
        String TIME = "Time";
        String Z = "Z";

        String[] ALL = new String[] { TIME, Z };

    }

    public interface OtherAxisModes {
        String INDEPENDENT = "Independent";
        String LINKED = "Linked";

        String[] ALL = new String[] { INDEPENDENT, LINKED };

    }

    public interface TransformationModes {
        String AFFINE = "Affine";
        String RIGID = "Rigid";
        String SIMILARITY = "Similarity";
        String TRANSLATION = "Translation";

        String[] ALL = new String[] { AFFINE, RIGID, SIMILARITY, TRANSLATION };

    }

    public interface FillModes {
        String BLACK = "Black";
        String WHITE = "White";

        String[] ALL = new String[] { BLACK, WHITE };

    }

    public interface Measurements {
        String TRANSLATE_X = "REGISTER // TRANSLATE_X";
        String TRANSLATE_Y = "REGISTER // TRANSLATE_Y";
        String SCALE_X = "REGISTER // SCALE_X";
        String SCALE_Y = "REGISTER // SCALE_Y";
        String SHEAR_X = "REGISTER // SHEAR_X";
        String SHEAR_Y = "REGISTER // SHEAR_Y";

    }

    public void process(Image inputImage, String transformationMode, boolean multithread, String fillMode,
            Image reference) {
        // Creating a reference image
        Image projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference",
                ProjectImage.ProjectionModes.MAX);

        // Creating a projection of the main image
        Image projectedWarped = ProjectImage.projectImageInZ(inputImage, "ProjectedWarped",
                ProjectImage.ProjectionModes.MAX);

        ImagePlus ipl1 = new Duplicator().run(projectedWarped.getImagePlus());
        ImagePlus ipl2 = new Duplicator().run(projectedReference.getImagePlus());
        ArrayList<PointPair> pairs = new PointPairSelector(this, true).getPointPairs(ipl1, ipl2);

        // Getting transform
        Object[] output = getLandmarkTransformation(pairs, transformationMode);
        InverseTransformMapping mapping = (InverseTransformMapping) output[0];
        AbstractAffineModel2D model = (AbstractAffineModel2D) output[1];

        // Iterate over each time-step
        int count = 0;
        int total = inputImage.getImagePlus().getNFrames();
        for (int t = 1; t <= inputImage.getImagePlus().getNFrames(); t++) {
            writeStatus("Processing timepoint " + (++count) + " of " + total);

            // Applying the transformation to the whole stack.
            // All channels should move in the same way, so are processed with the same
            // transformation.
            for (int c = 1; c <= inputImage.getImagePlus().getNChannels(); c++) {
                Image warped = ExtractSubstack.extractSubstack(inputImage, "Warped", String.valueOf(c), "1-end",
                        String.valueOf(t));
                try {
                    applyTransformation(warped, mapping, fillMode, multithread);
                } catch (InterruptedException e) {
                    return;
                }
                replaceStack(inputImage, warped, c, t);
            }

            mapping = null;

        }

        addManualMeasurements(inputImage, model);

    }

    public static Object[] getLandmarkTransformation(List<PointPair> pairs, String transformationMode) {
        // Getting registration model
        AbstractAffineModel2D model = getModel(transformationMode);
        InverseTransformMapping mapping = new InverseTransformMapping<AbstractAffineModel2D<?>>(model);
        final ArrayList<PointMatch> candidates = new ArrayList<PointMatch>();

        for (PointPair pair : pairs) {
            candidates.addAll(Util.pointRoisToPointMatches(pair.getPoint1(), pair.getPoint2()));
        }

        try {
            model.fit(candidates);
        } catch (NotEnoughDataPointsException | IllDefinedDataPointsException e) {
            e.printStackTrace();
            return null;
        }

        return new Object[] { mapping, model };

    }

    static void addManualMeasurements(Image image, AbstractAffineModel2D model) {
        AffineTransform transform = model.createAffine();

        image.addMeasurement(new Measurement(Measurements.TRANSLATE_X, transform.getTranslateX()));
        image.addMeasurement(new Measurement(Measurements.TRANSLATE_Y, transform.getTranslateY()));
        image.addMeasurement(new Measurement(Measurements.SCALE_X, transform.getScaleX()));
        image.addMeasurement(new Measurement(Measurements.SCALE_Y, transform.getScaleY()));
        image.addMeasurement(new Measurement(Measurements.SHEAR_X, transform.getShearX()));
        image.addMeasurement(new Measurement(Measurements.SHEAR_Y, transform.getShearY()));

    }

    @Override
    public void doAction(Object[] objects) {
        writeStatus("Running test registration");

        String transformationMode = parameters.getValue(TRANSFORMATION_MODE);
        String fillMode = parameters.getValue(FILL_MODE);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        ArrayList<PointPair> pairs = (ArrayList<PointPair>) objects[0];

        // Creating a reference image
        Image projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference",
                ProjectImage.ProjectionModes.MAX);

        // Duplicating image
        ImagePlus dupIpl = inputImage.getImagePlus().duplicate();
        Image<T> dupImage = new Image<T>("Registered", dupIpl);

        // Getting transform
        Object[] output = getLandmarkTransformation(pairs, transformationMode);
        InverseTransformMapping mapping = (InverseTransformMapping) output[0];

        // Iterate over each time-step
        int count = 0;
        int total = dupImage.getImagePlus().getNFrames();
        for (int t = 1; t <= dupImage.getImagePlus().getNFrames(); t++) {
            writeStatus("Processing timepoint " + (++count) + " of " + total);

            // Applying the transformation to the whole stack.
            // All channels should move in the same way, so are processed with the same
            // transformation.
            for (int c = 1; c <= dupImage.getImagePlus().getNChannels(); c++) {
                Image warped = ExtractSubstack.extractSubstack(dupImage, "Warped", String.valueOf(c), "1-end",
                        String.valueOf(t));
                try {
                    applyTransformation(warped, mapping, fillMode, multithread);
                } catch (InterruptedException e) {
                    return;
                }
                replaceStack(dupImage, warped, c, t);
            }

            mapping = null;

        }

        ArrayList<Image<T>> images = new ArrayList<>();
        images.add(reference);
        images.add(dupImage);
        ConcatenateStacks.concatenateImages(images, ConcatenateStacks.AxisModes.CHANNEL, "Registration comparison")
                .showImage();

    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK_REGISTRATION;
    }

    @Override
    public String getDescription() {
        return "Uses SIFT image registration toolbox";
    }

    @Override
    public Status process(Workspace workspace) {
        IJ.setBackgroundColor(255, 255, 255);

        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        // String regAxis = parameters.getValue(REGISTRATION_AXIS);
        // String otherAxisMode = parameters.getValue(OTHER_AXIS_MODE);
        String transformationMode = parameters.getValue(TRANSFORMATION_MODE);
        String fillMode = parameters.getValue(FILL_MODE);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);

        inputImage = workspace.getImage(inputImageName);
        if (!applyToInput)
            inputImage = new Image(outputImageName, inputImage.getImagePlus().duplicate());

        reference = workspace.getImage(referenceImageName);
        process(inputImage, transformationMode, multithread, fillMode, reference);

        if (showOutput)
            inputImage.showImage();

        // Dealing with module outputs
        if (!applyToInput)
            workspace.addImage(inputImage);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(REGISTRATION_SEPARATOR, this));
        // parameters.add(new ChoiceP(REGISTRATION_AXIS, this, RegistrationAxes.TIME, RegistrationAxes.ALL));
        // parameters.add(new ChoiceP(OTHER_AXIS_MODE, this, OtherAxisModes.INDEPENDENT, OtherAxisModes.ALL));
        parameters.add(new ChoiceP(TRANSFORMATION_MODE, this, TransformationModes.RIGID, TransformationModes.ALL));
        parameters.add(new ChoiceP(FILL_MODE, this, FillModes.BLACK, FillModes.ALL));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        parameters.add(new SeparatorP(REFERENCE_SEPARATOR, this));
        parameters.add(new InputImageP(REFERENCE_IMAGE, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(REFERENCE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.TRANSLATE_X).setImageName(outputImageName));
        returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.TRANSLATE_Y).setImageName(outputImageName));
        returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.SCALE_X).setImageName(outputImageName));
        returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.SCALE_Y).setImageName(outputImageName));
        returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.SHEAR_X).setImageName(outputImageName));
        returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.SHEAR_Y).setImageName(outputImageName));

        return returnedRefs;

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
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
