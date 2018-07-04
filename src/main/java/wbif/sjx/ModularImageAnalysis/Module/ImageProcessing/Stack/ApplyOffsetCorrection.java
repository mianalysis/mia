package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import fiji.stacks.Hyperstack_rearranger;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.ModularImageAnalysisPlugin;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

public class ApplyOffsetCorrection< T extends RealType< T > & NativeType< T >> extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String X_SHIFT = "Shift in x";
    public static final String Y_SHIFT = "Shift in y";
    public static final String Z_SHIFT = "Shift in z";
    public static final String CALIBRATED_UNITS = "Calibrated units";


    int[] getPixelShifts(Image image) {
        double xShift = parameters.getValue(X_SHIFT);
        double yShift = parameters.getValue(Y_SHIFT);
        double zShift = parameters.getValue(Z_SHIFT);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);

        int[] shifts = new int[3];
        if (calibratedUnits) {
            Calibration calibration = image.getImagePlus().getCalibration();
            shifts[0] = (int) Math.round(xShift/calibration.pixelWidth);
            shifts[1] = (int) Math.round(yShift/calibration.pixelWidth);
            shifts[2] = (int) Math.round(zShift/calibration.pixelDepth);
        } else {
            shifts[0] = (int) Math.round(xShift);
            shifts[1] = (int) Math.round(yShift);
            shifts[2] = (int) Math.round(zShift);
        }

        return shifts;

    }

    void shiftImage(Image inputImage, int[] shifts) {
        Img<T> inputImg = inputImage.getImg();

        // Getting the dimensions of the input image
        long[] dims = new long[inputImg.numDimensions()];
        for (int i=0;i<inputImg.numDimensions();i++) dims[i] = inputImg.dimension(i);

        // Creating the composite image
        T type = inputImg.firstElement();
        final ImgFactory< T > factory = new ArrayImgFactory<>();
        Img<T> shiftedImg = factory.create(dims, type);

        // Getting dimensions for cropped region
        for (int i=0;i<3;i++) dims[i] = dims[i] - Math.abs(shifts[i]);

        // Getting the offset for the first three dimensions.  All others are simply set to 0.
        long[] offsetIn = new long[inputImg.numDimensions()];
        long[] offsetShifted = new long[inputImg.numDimensions()];
        for (int i=0;i<3;i++) {
            if (shifts[i] < 0) {
                offsetShifted[i] = -shifts[i];
                offsetIn[i] = 0;
            } else {
                offsetIn[i] = shifts[i];
                offsetShifted[i] = 0;
            }
        }

        // Copying the pixel information from one image to the other
        Cursor<T> inputCursor = Views.offsetInterval(inputImg, offsetIn, dims).cursor();

        // Setting the zero offsets for the shifted image
        Cursor<T> shiftedCursor = Views.offsetInterval(shiftedImg, offsetShifted, dims).cursor();

        // Copying the data from inputImg to shiftedImg
        while (inputCursor.hasNext()) shiftedCursor.next().set(inputCursor.next());

        if (ModularImageAnalysisPlugin.isImagePlusMode()) {
            ImagePlus inputIpl = inputImage.getImagePlus();
            ImagePlus ipl = ImageJFunctions.wrap(shiftedImg, inputImage.getName());
            ipl = HyperStackConverter.toHyperStack(ipl, inputIpl.getNChannels(), inputIpl.getNSlices(), inputIpl.getNFrames());
            inputImage.setImagePlus(ipl);
        } else {
            // Putting the Img back into the input Image
            inputImage.setImg(shiftedImg);
        }
    }

    @Override
    public String getTitle() {
        return "Apply offset correction";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Getting pixel shifts
        int[] shifts = getPixelShifts(inputImage);

        // Processing the input image
        if (!applyToInput) {
            inputImagePlus = new Duplicator().run(inputImagePlus);
            inputImage = new Image(outputImageName,inputImagePlus);
        }
        Calibration calibration = inputImagePlus.getCalibration();
        shiftImage(inputImage,shifts);
        inputImage.getImagePlus().setCalibration(calibration);

        // If necessary, adding the output image to the workspace
        if (!applyToInput) {
            workspace.addImage(inputImage);
        }

        // If necessary, showing the shifted image
        if (showOutput) {
            ImagePlus dispIpl = new Duplicator().run(inputImage.getImagePlus());
            IntensityMinMax.run(dispIpl,true);
            dispIpl.show();
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(X_SHIFT,Parameter.DOUBLE,0.0));
        parameters.add(new Parameter(Y_SHIFT,Parameter.DOUBLE,0.0));
        parameters.add(new Parameter(Z_SHIFT,Parameter.DOUBLE,0.0));
        parameters.add(new Parameter(CALIBRATED_UNITS,Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(X_SHIFT));
        returnedParameters.add(parameters.getParameter(Y_SHIFT));
        returnedParameters.add(parameters.getParameter(Z_SHIFT));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
