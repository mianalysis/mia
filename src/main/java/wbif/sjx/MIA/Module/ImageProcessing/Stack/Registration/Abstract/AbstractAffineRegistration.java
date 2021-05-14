package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.Abstract;

import java.awt.Color;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.drew.lang.annotations.Nullable;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.process.ImageProcessor;
import mpicbg.ij.InverseTransformMapping;
import mpicbg.ij.Mapping;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.AffineModel2D;
import mpicbg.models.PointMatch;
import mpicbg.models.RigidModel2D;
import mpicbg.models.SimilarityModel2D;
import mpicbg.models.TranslationModel2D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.Convert3DStack;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ExtractSubstack;
import wbif.sjx.MIA.Module.Visualisation.Overlays.AddObjectCentroid;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.MIA.Object.Units.TemporalUnit;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;
import wbif.sjx.common.Object.Volume.SpatCal;
import wbif.sjx.common.Object.Volume.VolumeType;

public abstract class AbstractAffineRegistration<T extends RealType<T> & NativeType<T>>
        extends AbstractRegistration<T> {
    public static final String SHOW_DETECTED_POINTS = "Show detected points";
    public static final String TRANSFORMATION_MODE = "Transformation mode";

    public AbstractAffineRegistration(String name, ModuleCollection modules) {
        super(name, modules);
    }

    public interface TransformationModes {
        String AFFINE = "Affine";
        String RIGID = "Rigid";
        String SIMILARITY = "Similarity";
        String TRANSLATION = "Translation";

        String[] ALL = new String[] { AFFINE, RIGID, SIMILARITY, TRANSLATION };

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

    public abstract Param getParameters(Workspace workspace);

    public abstract AbstractAffineModel2D getAffineModel2D(ImageProcessor referenceIpr, ImageProcessor warpedIpr,
            Param param, boolean showDetectedPoints);

    public void processIndependent(Image inputImage, Image calculationImage, String referenceMode, int numPrevFrames,
            String prevFrameStatMode, Param param, String fillMode, boolean showDetectedPoints, boolean multithread,
            @Nullable Image reference) {
        // This works in a very similar manner to processLinked, except it's performed
        // one slice at a time
        for (int z = 0; z < inputImage.getImagePlus().getNSlices(); z++) {
            // Getting the current slices (input and calculation)
            Image currInputImage = ExtractSubstack.extractSubstack(inputImage, inputImage.getName(), "1-end",
                    String.valueOf(z + 1), "1-end");
            Image currCalcImage = ExtractSubstack.extractSubstack(calculationImage, calculationImage.getName(), "1-end",
                    String.valueOf(z + 1), "1-end");

            // Performing the registration on this slice
            processLinked(currInputImage, currCalcImage, referenceMode, numPrevFrames, prevFrameStatMode, param,
                    fillMode, showDetectedPoints, multithread, reference);

            // Replacing all images in this slice of the input with the registered images
            replaceSlice(inputImage, currInputImage, z);

        }
    }

    public void processLinked(Image inputImage, Image calculationImage, String referenceMode, int numPrevFrames,
            String prevFrameStatMode, Param param, String fillMode, boolean showDetectedPoints, boolean multithread,
            @Nullable Image reference) {
        // Assigning fixed reference images
        switch (referenceMode) {
            case ReferenceModes.FIRST_FRAME:
                reference = ExtractSubstack.extractSubstack(calculationImage, "Reference", "1", "1", "1");
                break;

            case ReferenceModes.SPECIFIC_IMAGE:
                if (reference == null)
                    return;
                break;
        }

        // Iterate over each time-step
        int count = 0;
        int nFrames = calculationImage.getImagePlus().getNFrames();

        for (int t = 0; t < nFrames; t++) {
            writeStatus("Processing frame " + (++count) + " of " + nFrames);

            // If the reference image is the previous frame, get this now
            switch (referenceMode) {
                case ReferenceModes.FIRST_FRAME:
                    // We don't need to align the first image if comparing to the first image
                    if (t == 0)
                        continue;
                    break;

                case ReferenceModes.PREVIOUS_N_FRAMES:
                    if (t == 0)
                        continue;

                    int minT = Math.max(1, t - numPrevFrames + 1);
                    Image referenceStack = ExtractSubstack.extractSubstack(calculationImage, "Reference", "1", "1",
                            minT + "-" + t);
                    Convert3DStack.process(referenceStack.getImagePlus(), Convert3DStack.Modes.OUTPUT_Z_STACK);
                    reference = ProjectImage.projectImageInZ(referenceStack, "Reference", prevFrameStatMode);

                    break;
            }

            // Getting the calculation image at this time-point
            Image warped = ExtractSubstack.extractSubstack(calculationImage, "Warped", "1", "1", String.valueOf(t + 1));

            // Adding any ROIs that may have been pre-selected on the images
            // warped.getImagePlus().setRoi(inputImage.getImagePlus().getRoi());
            // ipl2.setRoi(reference.getImagePlus().getRoi());

            // Calculating the transformation for this image pair
            AbstractAffineModel2D model = getAffineModel2D(reference.getImagePlus().getProcessor(),
                    warped.getImagePlus().getProcessor(), param, showDetectedPoints);

            InverseTransformMapping mapping = new InverseTransformMapping<AbstractAffineModel2D<?>>(model);

            applyTransformationToTimepoint(inputImage, t, mapping, fillMode, multithread);

            // Need to apply the warp to an external image
            if (referenceMode.equals(ReferenceModes.PREVIOUS_N_FRAMES)) {
                warped = ExtractSubstack.extractSubstack(calculationImage, "Warped", "1", "1", String.valueOf(t + 1));
                try {
                    applyTransformation(warped, mapping, fillMode, multithread);
                } catch (InterruptedException e) {
                    return;
                }
                replaceStack(calculationImage, warped, 0, t);
            }

            mapping = null;

        }
    }

    public static void showDetectedPoints(ImageProcessor referenceIpr, ImageProcessor warpedIpr,
            Collection<PointMatch> candidates) {
        SpatCal sc = new SpatCal(1, 1, "um", referenceIpr.getWidth(), referenceIpr.getHeight(), 2);
        ObjCollection oc = new ObjCollection("Im", sc, 1, 1d, TemporalUnit.getOMEUnit());
        ImagePlus showIpl = IJ.createImage("Detected points", referenceIpr.getWidth(), referenceIpr.getHeight(), 2,
                referenceIpr.getBitDepth());

        showIpl.getStack().setProcessor(referenceIpr, 1);
        for (PointMatch pm : candidates) {
            Obj obj = oc.createAndAddNewObject(VolumeType.POINTLIST);
            try {
                obj.add((int) Math.round(pm.getP2().getL()[0]), (int) Math.round(pm.getP2().getL()[1]), 0);
            } catch (PointOutOfRangeException e) {
            }
            AddObjectCentroid.addOverlay(obj, showIpl, Color.RED, AddObjectCentroid.PointSizes.MEDIUM,
                    AddObjectCentroid.PointTypes.DOT, false);
        }

        showIpl.getStack().setProcessor(warpedIpr, 2);
        for (PointMatch pm : candidates) {
            Obj obj = oc.createAndAddNewObject(VolumeType.POINTLIST);
            try {
                obj.add((int) Math.round(pm.getP1().getL()[0]), (int) Math.round(pm.getP1().getL()[1]), 1);
            } catch (PointOutOfRangeException e) {
            }
            AddObjectCentroid.addOverlay(obj, showIpl, Color.BLUE, AddObjectCentroid.PointSizes.MEDIUM,
                    AddObjectCentroid.PointTypes.DOT, false);
        }

        showIpl.duplicate().show();

    }

    public static boolean applyTransformationToTimepoint(Image inputImage, int t, InverseTransformMapping mapping,
            String fillMode, boolean multithread) {
        // Applying the transformation to the whole stack.
        // All channels should move in the same way, so are processed with the same
        // transformation.
        for (int c = 0; c < inputImage.getImagePlus().getNChannels(); c++) {
            Image warped = ExtractSubstack.extractSubstack(inputImage, "Warped", String.valueOf(c + 1), "1-end",
                    String.valueOf(t + 1));
            try {
                applyTransformation(warped, mapping, fillMode, multithread);
            } catch (InterruptedException e) {
                return false;
            }

            replaceStack(inputImage, warped, c, t);

        }

        return true;

    }

    public static void applyTransformation(Image inputImage, Mapping mapping, String fillMode, boolean multithread)
            throws InterruptedException {
        // Iterate over all images in the stack
        ImagePlus inputIpl = inputImage.getImagePlus();
        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        if (fillMode.equals(FillModes.WHITE))
            InvertIntensity.process(inputImage);

        for (int c = 1; c <= nChannels; c++) {
            for (int z = 1; z <= nSlices; z++) {
                for (int t = 1; t <= nFrames; t++) {
                    int finalC = c;
                    int finalZ = z;
                    int finalT = t;

                    Runnable task = () -> {
                        ImageProcessor slice = getSetStack(inputIpl, finalT, finalC, finalZ, null).getProcessor();
                        slice.setInterpolationMethod(ImageProcessor.BILINEAR);
                        ImageProcessor alignedSlice = slice.createProcessor(slice.getWidth(), slice.getHeight());
                        alignedSlice.setMinAndMax(slice.getMin(), slice.getMax());
                        mapping.mapInterpolated(slice, alignedSlice);

                        getSetStack(inputIpl, finalT, finalC, finalZ, alignedSlice);

                    };
                    pool.submit(task);
                }
            }
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        if (fillMode.equals(FillModes.WHITE))
            InvertIntensity.process(inputImage);

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_STACK_REGISTRATION;
    }

    @Override
    public Status process(Workspace workspace) {
        IJ.setBackgroundColor(255, 255, 255);

        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String regAxis = parameters.getValue(REGISTRATION_AXIS);
        String otherAxisMode = parameters.getValue(OTHER_AXIS_MODE);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        int numPrevFrames = parameters.getValue(NUM_PREV_FRAMES);
        String prevFramesStatMode = parameters.getValue(PREV_FRAMES_STAT_MODE);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
        String calculationSource = parameters.getValue(CALCULATION_SOURCE);
        String externalSourceName = parameters.getValue(EXTERNAL_SOURCE);
        int calculationChannel = parameters.getValue(CALCULATION_CHANNEL);
        String fillMode = parameters.getValue(FILL_MODE);
        boolean showDetectedPoints = parameters.getValue(SHOW_DETECTED_POINTS);

        // Getting the input image and duplicating if the output will be stored
        // separately
        Image inputImage = workspace.getImage(inputImageName);
        if (!applyToInput)
            inputImage = new Image(outputImageName, inputImage.getImagePlus().duplicate());

        // If comparing to a fixed image, get this now
        Image reference = referenceMode.equals(ReferenceModes.SPECIFIC_IMAGE) ? workspace.getImage(referenceImageName)
                : null;

        // Getting the image the registration will be calculated from.
        String calcC = String.valueOf(calculationChannel);
        Image calculationImage = null;
        switch (calculationSource) {
            case CalculationSources.EXTERNAL:
                Image externalImage = workspace.getImage(externalSourceName);
                calculationImage = ExtractSubstack.extractSubstack(externalImage, "CalcIm", calcC, "1-end", "1-end");
                break;

            case CalculationSources.INTERNAL:
                calculationImage = ExtractSubstack.extractSubstack(inputImage, "CalcIm", calcC, "1-end", "1-end");
                break;
        }

        // Registration will be performed in time, so ensure actual axis to be
        // registered is reordered to be in time axis
        switch (regAxis) {
            case RegistrationAxes.Z:
                changeStackOrder(inputImage);
                changeStackOrder(calculationImage);
                break;
        }

        // If non-registration dimension is "linked", calculation image potentially
        // needs to be projected. Since the images have been transformed such that the
        // registration dimension is always "Time", then this is a Z projection. A
        // maximum intensity projection is used. It only needs be performed if there is
        // at least one Z-slice.
        if (calculationImage.getImagePlus().getNSlices() > 1) {
            switch (otherAxisMode) {
                case OtherAxisModes.LINKED:
                    calculationImage = ProjectImage.projectImageInZ(calculationImage, "CalcIm",
                            ProjectImage.ProjectionModes.MAX);
                    break;
            }
        }

        // Ensuring calculation image has the correct dimensions
        if (testReferenceValidity(inputImage, calculationImage, otherAxisMode)) {
            // Setting up the parameters specific to this module
            Param param = getParameters(workspace);

            switch (otherAxisMode) {
                case OtherAxisModes.INDEPENDENT:
                    processIndependent(inputImage, calculationImage, referenceMode, numPrevFrames, prevFramesStatMode,
                            param, fillMode, showDetectedPoints, multithread, reference);
                    break;

                case OtherAxisModes.LINKED:
                    processLinked(inputImage, calculationImage, referenceMode, numPrevFrames, prevFramesStatMode, param,
                            fillMode, showDetectedPoints, multithread, reference);
                    break;
            }

            // If stack order was adjusted, now swap it back
            switch (regAxis) {
                case RegistrationAxes.Z:
                    changeStackOrder(inputImage);
                    changeStackOrder(calculationImage);
                    break;
            }

        } else {
            MIA.log.writeWarning("Input stack has not been registered");
        }

        if (showOutput) {
            if (referenceMode.equals(ReferenceModes.SPECIFIC_IMAGE)) {
                createOverlay(inputImage, reference).showImage();
            } else {
                inputImage.showImage();
            }
        }

        // Dealing with module outputs
        if (!applyToInput)
            workspace.addImage(inputImage);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new BooleanP(SHOW_DETECTED_POINTS, this, false));
        parameters.add(new ChoiceP(TRANSFORMATION_MODE, this, TransformationModes.RIGID, TransformationModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(SHOW_DETECTED_POINTS));
        returnedParameters.add(parameters.getParameter(TRANSFORMATION_MODE));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
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

    public abstract class Param {
        public String transformationMode = TransformationModes.RIGID;

    }
}
