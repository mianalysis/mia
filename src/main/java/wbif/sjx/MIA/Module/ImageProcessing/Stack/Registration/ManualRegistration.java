package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
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
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ConcatenateStacks;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ExtractSubstack;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
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

public class ManualRegistration<T extends RealType<T> & NativeType<T>> extends AbstractRegistrationHandler
        implements Interactable {
    public static final String REFERENCE_SEPARATOR = "Reference image source";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String POINT_SELECTION_MODE = "Point selection mode";

    public interface PointSelectionModes {
        String PRESELECTED = "Pre-selected points";
        String RUNTIME = "Select at runtime";

        String[] ALL = new String[] { PRESELECTED, RUNTIME };

    }

    private Image inputImage;
    private Image reference;

    public ManualRegistration(ModuleCollection modules) {
        super("Manual registration", modules);
    }

    public interface Measurements {
        String TRANSLATE_X = "REGISTER // TRANSLATE_X";
        String TRANSLATE_Y = "REGISTER // TRANSLATE_Y";
        String SCALE_X = "REGISTER // SCALE_X";
        String SCALE_Y = "REGISTER // SCALE_Y";
        String SHEAR_X = "REGISTER // SHEAR_X";
        String SHEAR_Y = "REGISTER // SHEAR_Y";

    }

    public void process(Image inputImage, String transformationMode, String pointSelectionMode, boolean multithread,
            String fillMode, Image reference) {
        // Creating a reference image
        Image projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference",
                ProjectImage.ProjectionModes.MAX);

        // Creating a projection of the main image
        Image projectedWarped = ProjectImage.projectImageInZ(inputImage, "ProjectedWarped",
                ProjectImage.ProjectionModes.MAX);

        ImagePlus ipl1 = new Duplicator().run(projectedWarped.getImagePlus());
        ImagePlus ipl2 = new Duplicator().run(projectedReference.getImagePlus());

        // Adding any ROIs that may have been pre-selected on the images
        ipl1.setRoi(inputImage.getImagePlus().getRoi());
        ipl2.setRoi(reference.getImagePlus().getRoi());

        ArrayList<PointPair> pairs = null;
        switch (pointSelectionMode) {
        case PointSelectionModes.PRESELECTED:
            pairs = PointPairSelector.getPreselectedPoints(inputImage, reference);
            break;
        case PointSelectionModes.RUNTIME:
        default:
            pairs = new PointPairSelector(this, true).getPointPairs(ipl1, ipl2);
            break;
        }

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
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_STACK_REGISTRATION;
    }

    @Override
    public String getDescription() {
        return "Align an image from the workspace to another image from the workspace using manually-selected reference points.  When the image runs, the input and reference images are displayed.  The user then selects matching points on each image and clicks \"Add pair(s)\".  Points must be added in the same order on each image (ID numbers next to each point provide a reference).  Points are shown in the control window and can be deleted by highlighting the relevant entry and clicking \"Remove pair\".  The alignment can be tested by clicking \"Test process\".  Finally, the alignment is accepted by clicking \"Finish adding pairs\", at which point the images are closed and the transform is applied.  The transformed input image can either overwrite the input image in the workspace, or be saved to the workspace with a new name."

                + "Alignments are calculated using the <a href=\"https://github.com/axtimwalde/mpicbg\">MPICBG</a> image transformation library."

                + "<br><br>Note: This module currently only aligns single slice images; however, a future update will add multi-slice processing.";

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
        String pointSelectionMode = parameters.getValue(POINT_SELECTION_MODE);

        inputImage = workspace.getImage(inputImageName);
        if (!applyToInput) {
            Roi roi = inputImage.getImagePlus().getRoi();
            inputImage = new Image(outputImageName, inputImage.getImagePlus().duplicate());
            inputImage.getImagePlus().setRoi(roi);
        }


        reference = workspace.getImage(referenceImageName);
        process(inputImage, transformationMode, pointSelectionMode, multithread, fillMode, reference);

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

        parameters.add(new SeparatorP(REFERENCE_SEPARATOR, this));
        parameters.add(new InputImageP(REFERENCE_IMAGE, this));
        parameters.add(new ChoiceP(POINT_SELECTION_MODE, this, PointSelectionModes.RUNTIME, PointSelectionModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(REFERENCE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
        returnedParameters.add(parameters.getParameter(POINT_SELECTION_MODE));

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

    @Override
    protected void addParameterDescriptions() {
        parameters.get(REFERENCE_IMAGE).setDescription(
                "Reference image against which the input image will be aligned.  When the module runs, this image and the reference image will be displayed.  The user selects matching points on the two images, which will determine the final transform to be applied.");
    }
}
