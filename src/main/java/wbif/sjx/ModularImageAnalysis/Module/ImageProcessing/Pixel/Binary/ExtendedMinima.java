package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.Binary;

import com.drew.lang.annotations.Nullable;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.SubHyperstackMaker;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sc13967 on 07/03/2018.
 */
public class ExtendedMinima extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String DYNAMIC = "Dynamic";
    public static final String CONNECTIVITY_3D = "Connectivity (3D)";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";


    public interface Connectivity {
        String SIX = "6";
        String TWENTYSIX = "26";

        String[] ALL = new String[]{SIX,TWENTYSIX};

    }

    public Image process(Image inputImage, String outputImageName, int dynamic, int connectivity, boolean multithread) throws InterruptedException {
        ImagePlus inputIpl = inputImage.getImagePlus();
        int width = inputIpl.getWidth();
        int height = inputIpl.getHeight();
        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();

        // Creating output image
        ImagePlus outputIpl = IJ.createHyperStack(outputImageName,width,height,nChannels,nSlices,nFrames,8);

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        int nTotal = nChannels*nFrames;
        AtomicInteger count = new AtomicInteger();

        for (int c = 1; c <= nChannels; c++) {
            for (int t = 1; t <= nFrames; t++) {
                int finalT = t;
                int finalC = c;

                Runnable task = () -> {
                    // Getting maskIpl for this timepoint
                    ImageStack timepoint = getSetStack(inputIpl, finalT, finalC, null);
                    if (timepoint == null) return;
                    timepoint = MinimaAndMaxima3D.extendedMinima(timepoint, dynamic, connectivity);

                    //  Replacing the maskIpl intensity
                    getSetStack(outputIpl, finalT, finalC, timepoint);

                    writeMessage("Processed " + (count.incrementAndGet()) + " of " + nTotal + " stacks");

                };
                pool.submit(task);
            }
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        // MorphoLibJ gives white objects on a black background.  Inverting this to match the logic of ImageJ
        IJ.run(outputIpl,"Invert","stack");

        return new Image(outputImageName,outputIpl);

    }

    synchronized private static ImageStack getSetStack(ImagePlus inputImagePlus, int timepoint, int channel, @Nullable ImageStack toPut) {
        int nSlices = inputImagePlus.getNSlices();
        if (toPut == null) {
            // Get mode
            return SubHyperstackMaker.makeSubhyperstack(inputImagePlus, channel + "-" + channel, "1-" + nSlices, timepoint + "-" + timepoint).getStack();
        } else {
            for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
                inputImagePlus.setPosition(channel,z,timepoint);
                inputImagePlus.setProcessor(toPut.getProcessor(z));
            }

            return null;
        }
    }


    @Override
    public String getTitle() {
        return "Extended minima";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int dynamic = parameters.getValue(DYNAMIC);
        int connectivity = Integer.parseInt(parameters.getValue(CONNECTIVITY_3D));
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // Getting region minima
        Image outputImage;
        try {
            outputImage = process(inputImage,outputImageName,dynamic,connectivity, multithread);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeMessage("Adding image ("+outputImageName+") to workspace");
            workspace.addImage(outputImage);
        } else {
            inputImage.setImagePlus(outputImage.getImagePlus());
        }

        if (showOutput) showImage(outputImage);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new IntegerP(DYNAMIC, this,1));
        parameters.add(new ChoiceP(CONNECTIVITY_3D, this, Connectivity.SIX, Connectivity.ALL));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(DYNAMIC));
        returnedParameters.add(parameters.getParameter(CONNECTIVITY_3D));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementReferences() {
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
