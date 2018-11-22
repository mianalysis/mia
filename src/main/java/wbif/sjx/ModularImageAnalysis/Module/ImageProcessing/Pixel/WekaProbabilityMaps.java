package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import trainableSegmentation.WekaSegmentation;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.io.File;

/**
 * Created by sc13967 on 22/03/2018.
 */
public class WekaProbabilityMaps extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String CLASSIFIER_FILE = "Classifier file path";
    public static final String BLOCK_SIZE = "Block size (simultaneous slices)";


    public ImagePlus calculateProbabilityMaps(ImagePlus inputImagePlus, String outputImageName, String classifierFilePath, int blockSize) {
        WekaSegmentation wekaSegmentation = new WekaSegmentation();

        // Checking classifier can be loaded
        if (!new File(classifierFilePath).exists()) {
            System.err.println("Can't find classifier ("+classifierFilePath+")");
            Thread.currentThread().interrupt();
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
        int nBlocks = (int) Math.ceil((double) nSlices/(double) blockSize);

        // Creating the new image
        ImagePlus probabilityMaps = IJ.createHyperStack(outputImageName,width,height,nChannels*nClasses,nSlices,nFrames,32);
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

                    for (int cl = 1; cl <= nClasses; cl++) {
                        for (int z = 1; z <= (endingBlock-startingBlock+1); z++) {
                            iplSingle.setPosition(nClasses * (z - 1) + cl);
                            probabilityMaps.setPosition((nClasses * (c - 1) + cl), startingBlock+z-1, t);

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
    protected boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String classifierFilePath = parameters.getValue(CLASSIFIER_FILE);
        int blockSize = parameters.getValue(BLOCK_SIZE);

        // Running the classifier on each individual stack
        ImagePlus probabilityMaps = calculateProbabilityMaps(inputImagePlus,outputImageName,classifierFilePath,blockSize);

        // Adding the probability maps to the Workspace
        Image probabilityImage = new Image(outputImageName,probabilityMaps);
        workspace.addImage(probabilityImage);

        if (showOutput) showImage(probabilityImage);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE,Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(CLASSIFIER_FILE,Parameter.FILE_PATH,null));
        parameters.add(new Parameter(BLOCK_SIZE,Parameter.INTEGER,1));

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
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}
