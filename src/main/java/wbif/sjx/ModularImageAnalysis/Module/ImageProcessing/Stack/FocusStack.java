package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.ThirdParty.Stack_Focuser_;

public class FocusStack extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String RANGE = "Range";
    public static final String SHOW_IMAGE = "Show image";


    public Image focusStack(Image inputImage, String outputImageName, int range) {
        ImagePlus inputIpl = inputImage.getImagePlus();
        ImageProcessor ipr = inputIpl.getProcessor();

        // Creating the new image to hold the focused image
        ImagePlus outputIpl = createEmptyImage(inputIpl,outputImageName);
        Image outputImage = new Image(outputImageName,outputIpl);

        // Getting the image type
        int type = getImageType(ipr);

        // Initialising the stack focuser.  This requires an example stack.
        int nSlices = inputIpl.getNSlices();
        ImagePlus stack = SubHyperstackMaker.makeSubhyperstack(inputIpl,"1-1","1-"+nSlices,"1-1");
        Stack_Focuser_ focuser = new Stack_Focuser_();
        focuser.setup("ksize="+range+" hmap="+0+" rgbone="+1,stack);

        // Iterating over all timepoints and channels
        int nStacks = inputIpl.getNChannels()*inputIpl.getNFrames();
        int count = 0;
        for (int c=1;c<=inputIpl.getNChannels();c++) {
            for (int t=1;t<=inputIpl.getNFrames();t++) {
                stack = SubHyperstackMaker.makeSubhyperstack(inputIpl,c+"-"+c,1+"-"+nSlices,t+"-"+t);

                ImageProcessor focusedIpr = focuser.focusGreyStack(stack.getStack(),type);

                outputIpl.setPosition(c,1,t);
                outputIpl.setProcessor(focusedIpr);

                writeMessage("Processed "+(++count)+" of "+nStacks+" stacks");

            }
        }

        outputIpl.setPosition(1,1,1);

        return outputImage;

    }

    public int getImageType(ImageProcessor ipr) {
        // Determining the type of image
        int type = Stack_Focuser_.RGB;
        if (ipr instanceof ByteProcessor) {
            type = Stack_Focuser_.BYTE;
        } else if (ipr instanceof ShortProcessor) {
            type = Stack_Focuser_.SHORT;
        } else if (ipr instanceof FloatProcessor) {
            type = Stack_Focuser_.FLOAT;
        }

        return type;

    }

    public ImagePlus createEmptyImage(ImagePlus inputImagePlus, String outputImageName) {
        // Getting image dimensions
        int width = inputImagePlus.getWidth();
        int height = inputImagePlus.getHeight();
        int nChannels = inputImagePlus.getNChannels();
        int nSlices = inputImagePlus.getNSlices();
        int nFrames = inputImagePlus.getNFrames();
        int bitDepth = inputImagePlus.getBitDepth();

        return IJ.createHyperStack(outputImageName,width,height,nChannels,1,nFrames,bitDepth);

    }


    @Override
    public String getTitle() {
        return "Focus stack";
    }

    @Override
    public String getHelp() {
        return "Uses the StackFocuser plugin created by Mikhail Umorin.\n" +
                "Source downloaded from https://imagej.nih.gov/ij/plugins/download/Stack_Focuser_.java on 06-June-2018";
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int range = parameters.getValue(RANGE);

        // Running stack focusing
        Image outputImage = focusStack(inputImage,outputImageName,range);

        // If requested, showing image
        if (parameters.getValue(SHOW_IMAGE)) {
            ImagePlus showIpl = new Duplicator().run(outputImage.getImagePlus());
            showIpl.setTitle(outputImageName);
            showIpl.show();
        }

        // Adding output image to Workspace
        workspace.addImage(outputImage);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE,Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(RANGE,Parameter.INTEGER,11));
        parameters.add(new Parameter(SHOW_IMAGE,Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
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
