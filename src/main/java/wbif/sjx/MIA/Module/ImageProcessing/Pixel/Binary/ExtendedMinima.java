package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.drew.lang.annotations.Nullable;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.SubHyperstackMaker;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by sc13967 on 07/03/2018.
 */
public class ExtendedMinima extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String EXTENDED_MINIMA_SEPARATOR = "Extended minima controls";
    public static final String DYNAMIC = "Dynamic";
    public static final String CONNECTIVITY_3D = "Connectivity (3D)";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public ExtendedMinima(ModuleCollection modules) {
        super("Extended minima",modules);
    }


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

        // Setting the calibration from the input image
        outputIpl.setCalibration(inputImage.getImagePlus().getCalibration());

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

                    writeStatus("Processed " + (count.incrementAndGet()) + " of " + nTotal + " stacks");

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
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getDescription() {
        return "Applies a 3D (or 2D for single slice images) binary skeletonisation operation to an image in the workspace.  All foreground labelled regions will be reduced to a single, pixel-wide strip, which runs along the former centre of the region."

        +"<br><br>This image must be 8-bit and have the logic black foreground (intensity 0) and white background (intensity 255).  The skeletonise operation uses the plugin \"<a href=\"https://imagej.net/Skeletonize3D\">Skeletonize3D</a>\".";
        
    }

    @Override
    public Status process(Workspace workspace) {
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
            return Status.FAIL;
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeStatus("Adding image ("+outputImageName+") to workspace");
            workspace.addImage(outputImage);
        } else {
            inputImage.setImagePlus(outputImage.getImagePlus());
        }

        if (showOutput) outputImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(EXTENDED_MINIMA_SEPARATOR,this));
        parameters.add(new IntegerP(DYNAMIC, this,1));
        parameters.add(new ChoiceP(CONNECTIVITY_3D, this, Connectivity.TWENTYSIX, Connectivity.ALL));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR,this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

returnedParameters.add(parameters.getParameter(EXTENDED_MINIMA_SEPARATOR));
        returnedParameters.add(parameters.getParameter(DYNAMIC));
        returnedParameters.add(parameters.getParameter(CONNECTIVITY_3D));

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

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

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription(
                "Image from workspace to extended minima operation to.  This must be an 8-bit binary image (255 = background, 0 = foreground).");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \"" + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
                + "\" is not selected, the post-operation image will be saved to the workspace with this name.  This image will be 8-bit with black minima (intensity 0) on a white background (intensity 255).");

        parameters.get(DYNAMIC).setDescription("This parameter specifies the maximum permitted pixel intensity difference for a single minima.  Local intensity differences greater than this will result in creation of more minima.  The smaller the dynamic value is, the more minima will be created.  As the dynamic value increases, minima will increase in size.");

        parameters.get(CONNECTIVITY_3D).setDescription("Controls which adjacent pixels are considered:<br><ul>"

        +"<li>\""+Connectivity.SIX+"\" Only pixels immediately next to the active pixel are considered.  These are the pixels on the four \"cardinal\" directions plus the pixels immediately above and below the current pixel.  If working in 2D, 4-way connectivity is used.</li>"

        +"<li>\""+Connectivity.TWENTYSIX+"\" In addition to the core 6-pixels, all immediately diagonal pixels are used.  If working in 2D, 8-way connectivity is used.</li>");

        parameters.get(ENABLE_MULTITHREADING).setDescription("Process multiple 3D stacks simultaneously.  Since the extended minima operation is applied on a single 3D stack at a time, multithreading only works for images with multiple channels or timepoints (other stacks will still work, but won't see a speed improvement).  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
