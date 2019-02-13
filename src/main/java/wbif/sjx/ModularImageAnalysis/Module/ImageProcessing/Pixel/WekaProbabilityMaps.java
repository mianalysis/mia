package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import trainableSegmentation.WekaSegmentation;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack.ImageTypeConverter;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;

import java.io.File;

/**
 * Created by sc13967 on 22/03/2018.
 */
public class WekaProbabilityMaps extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_BIT_DEPTH = "Output bit depth";
    public static final String OUTPUT_SINGLE_CLASS = "Output single class";
    public static final String OUTPUT_CLASS = "Output class";
    public static final String CLASSIFIER_FILE = "Classifier file path";
    public static final String BLOCK_SIZE = "Block size (simultaneous slices)";


    public interface OutputBitDepths {
        String EIGHT = "8";
        String SIXTEEN = "16";
        String THIRTY_TWO = "32";

        String[] ALL = new String[]{EIGHT,SIXTEEN,THIRTY_TWO};

    }


    public ImagePlus calculateProbabilityMaps(ImagePlus inputImagePlus, String outputImageName, String classifierFilePath, int blockSize, int bitDepth) {
        return calculateProbabilityMaps(inputImagePlus,outputImageName,classifierFilePath,blockSize,bitDepth,-1);
    }

    public ImagePlus calculateProbabilityMaps(ImagePlus inputImagePlus, String outputImageName, String classifierFilePath, int blockSize, int bitDepth, int outputClass) {
        WekaSegmentation wekaSegmentation = new WekaSegmentation();

        // Checking classifier can be loaded
        if (!new File(classifierFilePath).exists()) {
            System.err.println("Can't find classifier ("+classifierFilePath+")");
            return null;
        }

        writeMessage("Loading classifier");
        wekaSegmentation.loadClassifier(classifierFilePath);
        writeMessage("Classifier loaded");

        int nThreads = Prefs.getThreads();
        int width = inputImagePlus.getWidth();
        int height = inputImagePlus.getHeight();
        int nChannels = inputImagePlus.getNChannels();
        int nSlices = inputImagePlus.getNSlices();
        int nFrames = inputImagePlus.getNFrames();
        int nClasses = wekaSegmentation.getNumOfClasses();
        int nOutputClasses = outputClass == -1 ? wekaSegmentation.getNumOfClasses() : 1;
        int nBlocks = (int) Math.ceil((double) nSlices/(double) blockSize);

        // Creating the new image
        ImagePlus probabilityMaps = IJ.createHyperStack(outputImageName,width,height,nChannels*nOutputClasses,nSlices,nFrames,bitDepth);
        probabilityMaps.setCalibration(inputImagePlus.getCalibration());

        writeMessage("Calculating probabilities");
        int count = 0;
        int nImages = nChannels*nFrames*nSlices;
        for (int c=1;c<=nChannels;c++) {
            for (int t = 1; t <= nFrames; t++) {
                for (int b=1;b<=nBlocks;b++) {
                    int startingBlock = (b-1)*blockSize+1;
                    int endingBlock = Math.min((b-1)*blockSize+blockSize,nSlices);

                    ImagePlus iplSingle = SubHyperstackMaker.makeSubhyperstack(inputImagePlus, c + "-" + c, startingBlock + "-" + endingBlock, t + "-" + t);

                    wekaSegmentation.setTrainingImage(iplSingle);
                    wekaSegmentation.applyClassifier(true);
                    iplSingle = wekaSegmentation.getClassifiedImage();

                    // Converting probability image to specified bit depth (it will be 32-bit by default)
                    ImageTypeConverter.applyConversion(iplSingle,bitDepth,ImageTypeConverter.ScalingModes.SCALE);

                    // If outputting all channels
                    for (int cl = 1; cl <= nOutputClasses; cl++) {
                        for (int z = 1; z <= (endingBlock - startingBlock + 1); z++) {
                            if (outputClass == -1) {
                                // If outputting all classes
                                iplSingle.setPosition(nOutputClasses * (z - 1) + cl);
                                probabilityMaps.setPosition((nOutputClasses * (c - 1) + cl), startingBlock + z - 1, t);
                            } else {
                                // If outputting a single class
                                iplSingle.setPosition(nClasses * (z - 1) + outputClass);
                                probabilityMaps.setPosition(1, startingBlock + z - 1, t);
                            }

                            ImageProcessor iprSingle = iplSingle.getProcessor();
                            ImageProcessor iprProbability = probabilityMaps.getProcessor();

                            for (int x = 0; x < width; x++) {
                                for (int y = 0; y < height; y++) {
                                    iprProbability.setf(x, y, iprSingle.getf(x, y));
                                }
                            }
                        }
                    }

                    count = count + endingBlock - startingBlock + 1;
                    writeMessage("Processed "+count+" of "+nImages+" images");

                }
            }
        }

        // Clearing the segmentation model from memory
//        wekaSegmentation.shutDownNow();
        wekaSegmentation = null;

        return probabilityMaps;

    }


    @Override
    public String getTitle() {
        return "Weka probability maps";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getHelp() {
        return "Loads a saved WEKA classifier model and applies it to the input image.  Returns the " +
                "\nmulti-channel probability map";
    }

    @Override
    public boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String outputBitDepth = parameters.getValue(OUTPUT_BIT_DEPTH);
        boolean outputSingleClass = parameters.getValue(OUTPUT_SINGLE_CLASS);
        int outputClass = parameters.getValue(OUTPUT_CLASS);
        String classifierFilePath = parameters.getValue(CLASSIFIER_FILE);
        int blockSize = parameters.getValue(BLOCK_SIZE);

        // Converting the bit depth to an integer
        int bitDepth = Integer.parseInt(outputBitDepth);

        // If all channels are to be output, set output channel to -1
        if (!outputSingleClass) outputClass = -1;

        // Running the classifier on each individual stack
        ImagePlus probabilityMaps = calculateProbabilityMaps(inputImagePlus,outputImageName,classifierFilePath,blockSize,bitDepth,outputClass);

        // If the classification failed, a null object is returned
        if (probabilityMaps == null) return false;

        // Adding the probability maps to the Workspace
        Image probabilityImage = new Image(outputImageName,probabilityMaps);
        workspace.addImage(probabilityImage);

        if (showOutput) probabilityImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new ChoiceP(OUTPUT_BIT_DEPTH,this,OutputBitDepths.THIRTY_TWO,OutputBitDepths.ALL));
        parameters.add(new BooleanP(OUTPUT_SINGLE_CLASS,this,false));
        parameters.add(new IntegerP(OUTPUT_CLASS,this,1));
        parameters.add(new FilePathP(CLASSIFIER_FILE,this));
        parameters.add(new IntegerP(BLOCK_SIZE,this,1));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_BIT_DEPTH));

        returnedParameters.add(parameters.getParameter(OUTPUT_SINGLE_CLASS));
        if (parameters.getValue(OUTPUT_SINGLE_CLASS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_CLASS));
        }

        returnedParameters.add(parameters.getParameter(CLASSIFIER_FILE));
        returnedParameters.add(parameters.getParameter(BLOCK_SIZE));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}
