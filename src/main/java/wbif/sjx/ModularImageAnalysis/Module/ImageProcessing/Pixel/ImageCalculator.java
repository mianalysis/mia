package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 19/09/2017.
 */
public class ImageCalculator extends Module {
    public static final String INPUT_IMAGE1 = "Input image 1";
    public static final String INPUT_IMAGE2 = "Input image 2";
    public static final String OVERWRITE_MODE = "Overwrite mode";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_32BIT = "Output 32-bit image";
    public static final String CALCULATION_METHOD = "Calculation method";
    public static final String SET_NAN_TO_ZERO = "Set NaN values to zero";

    public interface OverwriteModes {
        String CREATE_NEW = "Create new image";
        String OVERWRITE_IMAGE1 = "Overwrite image 1";
        String OVERWRITE_IMAGE2 = "Overwrite image 2";

        String[] ALL = new String[]{CREATE_NEW,OVERWRITE_IMAGE1,OVERWRITE_IMAGE2};

    }

    public interface CalculationMethods {
        String ADD = "Add image 1 and image 2";
        String DIFFERENCE = "Difference of image 1 and image 2";
        String DIVIDE = "Divide image 1 by image 2";
        String MULTIPLY = "Multiply image 1 and image 1";
        String SUBTRACT = "Subtract image 2 from image 1";

        String[] ALL = new String[]{ADD,DIFFERENCE,DIVIDE,MULTIPLY,SUBTRACT};

    }

    private void removeNaNs(ImagePlus inputImagePlus1, ImagePlus inputImagePlus2) {
        int width = inputImagePlus1.getWidth();
        int height = inputImagePlus1.getHeight();
        int nChannels = inputImagePlus1.getNChannels();
        int nSlices = inputImagePlus1.getNSlices();
        int nFrames = inputImagePlus1.getNFrames();

        for (int z = 1; z <= nSlices; z++) {
            for (int c = 1; c <= nChannels; c++) {
                for (int t = 1; t <= nFrames; t++) {
                    inputImagePlus1.setPosition(c,z,t);
                    ImageProcessor imageProcessor1 = inputImagePlus1.getProcessor();

                    inputImagePlus2.setPosition(c,z,t);
                    ImageProcessor imageProcessor2 = inputImagePlus2.getProcessor();

                    for (int x=0;x<width;x++) {
                        for (int y = 0; y < height; y++) {
                            if (Float.isNaN(imageProcessor1.getf(x, y))) {
                                imageProcessor1.set(x,y,0);
                            }

                            if (Float.isNaN(imageProcessor2.getf(x, y))) {
                                imageProcessor2.set(x,y,0);
                            }
                        }
                    }
                }
            }
        }
    }

    public ImagePlus process(ImagePlus inputImagePlus1, ImagePlus inputImagePlus2, String calculationMethod,
                        String overwriteMode, boolean output32Bit, boolean setNaNToZero) {
        // If applying to a new image, the input image is duplicated
        switch (overwriteMode) {
            case OverwriteModes.CREATE_NEW:
                inputImagePlus1 = new Duplicator().run(inputImagePlus1);
                break;
        }

        // If necessary, converting to 32-bit image
        if (output32Bit) {
            switch (overwriteMode) {
                case OverwriteModes.CREATE_NEW:
                case OverwriteModes.OVERWRITE_IMAGE1:
                    IJ.run(inputImagePlus1, "32-bit", null);
                    break;

                case OverwriteModes.OVERWRITE_IMAGE2:
                    IJ.run(inputImagePlus2, "32-bit", null);
                    break;
            }
        }

        int width = inputImagePlus1.getWidth();
        int height = inputImagePlus1.getHeight();
        int nChannels = inputImagePlus1.getNChannels();
        int nSlices = inputImagePlus1.getNSlices();
        int nFrames = inputImagePlus1.getNFrames();

        // If necessary, converting NaN values to zero
        if (setNaNToZero && (inputImagePlus1.getBitDepth() == 32 || inputImagePlus2.getBitDepth() == 32)) {
            removeNaNs(inputImagePlus1,inputImagePlus2);
        }

        // Checking the number of dimensions.  If a dimension of image2 is 1 this dimension is used for all images.
        int nImages = nSlices*nChannels*nFrames;
        int count = 0;

        for (int z = 1; z <= nSlices; z++) {
            for (int c = 1; c <= nChannels; c++) {
                for (int t = 1; t <= nFrames; t++) {
                    writeMessage("Processing "+(++count)+" of "+nImages+" images");

                    inputImagePlus1.setPosition(c,z,t);
                    ImageProcessor imageProcessor1 = inputImagePlus1.getProcessor();

                    inputImagePlus2.setPosition(c,z,t);
                    ImageProcessor imageProcessor2 = inputImagePlus2.getProcessor();

                    for (int x=0;x<width;x++) {
                        for (int y=0;y<height;y++) {
                            double val = 0;
                            switch (calculationMethod) {
                                case CalculationMethods.ADD:
                                    val = imageProcessor1.getPixelValue(x,y) + imageProcessor2.getPixelValue(x,y);
                                    break;

                                case CalculationMethods.DIFFERENCE:
                                    val = Math.abs(imageProcessor1.getPixelValue(x,y) - imageProcessor2.getPixelValue(x,y));
                                    break;

                                case CalculationMethods.DIVIDE:
                                    if (output32Bit) {
                                        val = imageProcessor1.getPixelValue(x, y) / imageProcessor2.getPixelValue(x, y);
                                    } else {
                                        if (imageProcessor2.getPixelValue(x, y) == 0) {
                                            val = Math.pow(2, imageProcessor1.getBitDepth()) - 1;
                                        } else {
                                            // Using "floor" to maintain consistency with ImageJ's calculator
                                            val = Math.floor(imageProcessor1.getPixelValue(x, y) / imageProcessor2.getPixelValue(x, y));
                                        }
                                    }
                                    break;

                                case CalculationMethods.MULTIPLY:
                                    val = imageProcessor1.getPixelValue(x,y)*imageProcessor2.getPixelValue(x,y);
                                    break;

                                case CalculationMethods.SUBTRACT:
                                    val = imageProcessor1.getPixelValue(x,y) - imageProcessor2.getPixelValue(x,y);
                                    break;

                            }

                            switch (overwriteMode) {
                                case OverwriteModes.CREATE_NEW:
                                case OverwriteModes.OVERWRITE_IMAGE1:
                                    imageProcessor1.putPixelValue(x,y,val);
                                    break;

                                case OverwriteModes.OVERWRITE_IMAGE2:
                                    imageProcessor2.putPixelValue(x,y,val);
                                    break;

                            }
                        }
                    }
                }
            }
        }

        inputImagePlus1.setPosition(1,1,1);
        inputImagePlus2.setPosition(1,1,1);

        // If the image is being saved as a new image, adding it to the workspace
        switch (overwriteMode) {
            case OverwriteModes.CREATE_NEW:
                return inputImagePlus1;

            case OverwriteModes.OVERWRITE_IMAGE1:
            case OverwriteModes.OVERWRITE_IMAGE2:
                return null;
        }

        return null;

    }

    @Override
    public String getTitle() {
        return "Image calculator";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input images
        String inputImageName1 = parameters.getValue(INPUT_IMAGE1);
        Image inputImage1 = workspace.getImages().get(inputImageName1);
        ImagePlus inputImagePlus1 = inputImage1.getImagePlus();

        String inputImageName2 = parameters.getValue(INPUT_IMAGE2);
        Image inputImage2 = workspace.getImages().get(inputImageName2);
        ImagePlus inputImagePlus2 = inputImage2.getImagePlus();

        // Getting parameters
        String overwriteMode = parameters.getValue(OVERWRITE_MODE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean output32Bit = parameters.getValue(OUTPUT_32BIT);
        String calculationMethod = parameters.getValue(CALCULATION_METHOD);
        boolean setNaNToZero = parameters.getValue(SET_NAN_TO_ZERO);

        ImagePlus newIpl = process(inputImagePlus1,inputImagePlus2,calculationMethod,overwriteMode,output32Bit,setNaNToZero);

        // If the image is being saved as a new image, adding it to the workspace
        switch (overwriteMode) {
            case OverwriteModes.CREATE_NEW:
                Image outputImage = new Image(outputImageName,newIpl);
                workspace.addImage(outputImage);
                if (showOutput) {
                    ImagePlus showIpl = new Duplicator().run(newIpl);
                    showIpl.setTitle(outputImageName);
                    showIpl.show();
                }
                break;

            case OverwriteModes.OVERWRITE_IMAGE1:
                if (showOutput) {
                    ImagePlus showIpl = new Duplicator().run(inputImagePlus1);
                    showIpl.setTitle(outputImageName);
                    showIpl.show();
                }
                break;

            case OverwriteModes.OVERWRITE_IMAGE2:
                if (showOutput) {
                    ImagePlus showIpl = new Duplicator().run(inputImagePlus2);
                    showIpl.setTitle(outputImageName);
                    showIpl.show();
                }
                break;
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE1,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INPUT_IMAGE2,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OVERWRITE_MODE,Parameter.CHOICE_ARRAY,OverwriteModes.CREATE_NEW,OverwriteModes.ALL));
        parameters.add(new Parameter(OUTPUT_IMAGE,Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_32BIT,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(CALCULATION_METHOD,Parameter.CHOICE_ARRAY,CalculationMethods.ADD,CalculationMethods.ALL));
        parameters.add(new Parameter(SET_NAN_TO_ZERO,Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE1));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE2));
        returnedParameters.add(parameters.getParameter(OVERWRITE_MODE));

        if (parameters.getValue(OVERWRITE_MODE).equals(OverwriteModes.CREATE_NEW)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(OUTPUT_32BIT));
        returnedParameters.add(parameters.getParameter(CALCULATION_METHOD));
        returnedParameters.add(parameters.getParameter(SET_NAN_TO_ZERO));

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
