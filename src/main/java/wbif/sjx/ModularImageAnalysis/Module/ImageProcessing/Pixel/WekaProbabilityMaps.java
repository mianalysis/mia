package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.HyperStackMaker;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import trainableSegmentation.WekaSegmentation;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;
import weka.classifiers.Classifier;
import weka.core.Instances;

import java.io.PrintStream;

/**
 * Created by sc13967 on 22/03/2018.
 */
public class WekaProbabilityMaps extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String CLASSIFIER_FILE = "Classifier file path";


    public ImagePlus calculateProbabilityMaps(ImagePlus inputImagePlus, String outputImageName, String classifierFilePath) {
        // Initialising the training system, which shows a warning, so temporarily diverting output to error stream
        WekaSegmentation wekaSegmentation = new WekaSegmentation();

        writeMessage("Loading classifier");
//        PrintStream ps = System.out;
//        System.setOut(System.err);
        wekaSegmentation.loadClassifier(classifierFilePath);
//        System.setOut(ps);

        int nThreads = Prefs.getThreads();
        int width = inputImagePlus.getWidth();
        int height = inputImagePlus.getHeight();
        int nChannels = inputImagePlus.getNChannels();
        int nSlices = inputImagePlus.getNSlices();
        int nFrames = inputImagePlus.getNFrames();
        int nClasses = wekaSegmentation.getNumOfClasses();

        // Creating the new image
        ImagePlus probabilityMaps = IJ.createHyperStack(outputImageName,width,height,nChannels*nClasses,nSlices,nFrames,32);
        probabilityMaps.setCalibration(inputImagePlus.getCalibration());

        writeMessage("Calculating probabilities");
        int count = 0;
        int nStacks = nChannels*nFrames;
        for (int c=1;c<=nChannels;c++) {
            for (int t = 1; t <= nFrames; t++) {
                ImagePlus iplSingle = SubHyperstackMaker.makeSubhyperstack(inputImagePlus,c+"-"+c,"1-"+nSlices,t+"-"+t);

                wekaSegmentation.setTrainingImage(iplSingle);
                wekaSegmentation.applyClassifier(true);
                iplSingle = wekaSegmentation.getClassifiedImage();

                for (int cl=1;cl<=nClasses;cl++) {
                    for (int z = 1; z <= nSlices; z++) {
                        iplSingle.setPosition(nClasses*(z-1)+cl);
                        probabilityMaps.setPosition((nClasses*(c-1)+cl), z, t);

                        ImageProcessor iprSingle = iplSingle.getProcessor();
                        ImageProcessor iprProbability = probabilityMaps.getProcessor();

                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iprProbability.setf(x, y, iprSingle.getf(x, y));
                            }
                        }
                    }
                }

                writeMessage("Processed "+(++count)+" of "+nStacks+" stacks");

            }
        }

        return probabilityMaps;

    }

    @Override
    public String getTitle() {
        return "Weka probability maps";
    }

    @Override
    public String getHelp() {
        return "Loads a saved WEKA classifier model and applies it to the input image.  Returns the " +
                "\nmulti-channel probability map";
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String classifierFilePath = parameters.getValue(CLASSIFIER_FILE);

        // Running the classifier on each individual stack
        ImagePlus probabilityMaps = calculateProbabilityMaps(inputImagePlus,outputImageName,classifierFilePath);

        // Adding the probability maps to the Workspace
        workspace.addImage(new Image(outputImageName,probabilityMaps));

        if (showOutput) {
            ImagePlus dispIpl = new Duplicator().run(probabilityMaps);
            IntensityMinMax.run(dispIpl,true);
            dispIpl.setTitle(outputImageName);
            dispIpl.show();
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE,Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(CLASSIFIER_FILE,Parameter.FILE_PATH,null));

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
