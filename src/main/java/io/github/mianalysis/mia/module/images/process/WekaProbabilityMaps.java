package io.github.mianalysis.mia.module.images.process;

import java.io.File;
import java.io.IOException;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.RGBStackConverter;
import ij.plugin.SubstackMaker;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.inputoutput.ImageLoader;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.sjcross.sjcommon.metadataextractors.Metadata;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import trainableSegmentation.WekaSegmentation;

/**
 * Created by sc13967 on 22/03/2018.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class WekaProbabilityMaps extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String CONVERT_TO_RGB = "Convert to RGB";
    public static final String OUTPUT_SEPARATOR = "Probability image output";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_BIT_DEPTH = "Output bit depth";
    public static final String OUTPUT_SINGLE_CLASS = "Output single class";
    public static final String OUTPUT_CLASS = "Output class";
    public static final String CLASSIFIER_SEPARATOR = "Classifier settings";
    public static final String PATH_TYPE = "Path type";
    public static final String GENERIC_FORMAT = "Generic format";
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";
    public static final String CLASSIFIER_FILE = "Classifier file path";
    public static final String SIMULTANEOUS_SLICES = "Simultaneous slices";
    public static final String TILE_FACTOR = "Tile factor";

    public WekaProbabilityMaps(Modules modules) {
        super("Weka probability maps", modules);
    }

    public interface OutputBitDepths {
        String EIGHT = "8";
        String SIXTEEN = "16";
        String THIRTY_TWO = "32";

        String[] ALL = new String[] { EIGHT, SIXTEEN, THIRTY_TWO };

    }

    public interface PathTypes {
        String MATCHING_FORMAT = "Matching format";
        String SPECIFIC_FILE = "Specific file";

        String[] ALL = new String[] { MATCHING_FORMAT, SPECIFIC_FILE };

    }

    public ImagePlus calculateProbabilityMaps(ImagePlus inputImagePlus, String outputImageName,
            String classifierFilePath, int nSimSlices, int tileFactor, int bitDepth) {
        return calculateProbabilityMaps(inputImagePlus, outputImageName, classifierFilePath, nSimSlices, tileFactor,
                bitDepth, -1);
    }

    public ImagePlus calculateProbabilityMaps(ImagePlus inputImagePlus, String outputImageName,
            String classifierFilePath, int nSimSlices, int tileFactor, int bitDepth, int outputClass) {
        // Checking classifier can be loaded
        if (!new File(classifierFilePath).exists()) {
            MIA.log.writeError("Can't find classifier (" + classifierFilePath + ")");
            return null;
        }

        WekaSegmentation wekaSegmentation = new WekaSegmentation();
        wekaSegmentation.setTrainingImage(inputImagePlus);
        wekaSegmentation.loadClassifier(classifierFilePath);

        int width = inputImagePlus.getWidth();
        int height = inputImagePlus.getHeight();
        int nChannels = inputImagePlus.getNChannels();
        int nSlices = inputImagePlus.getNSlices();
        int nFrames = inputImagePlus.getNFrames();
        int nClasses = wekaSegmentation.getNumOfClasses();
        int nOutputClasses = outputClass == -1 ? wekaSegmentation.getNumOfClasses() : 1;

        int nBlocks = (int) Math.ceil((double) (nChannels * nSlices * nFrames) / (double) nSimSlices);

        // Creating the new image
        ImagePlus probabilityMaps = IJ.createHyperStack(outputImageName, width, height, nChannels * nOutputClasses,
                nSlices, nFrames, bitDepth);
        probabilityMaps.setCalibration(inputImagePlus.getCalibration());

        ImageStack inputStack = inputImagePlus.getStack();
        int slices = inputStack.getSize();

        int nThreads = Prefs.getThreads();

        int count = 0;
        for (int b = 1; b <= nBlocks; b++) {
            int startingBlock = (b - 1) * nSimSlices + 1;
            int endingBlock = Math.min((b - 1) * nSimSlices + nSimSlices, slices);

            ImagePlus iplSingle = new SubstackMaker().makeSubstack(new ImagePlus("Tempstack", inputStack),
                    startingBlock + "-" + endingBlock);

            wekaSegmentation.setTrainingImage(iplSingle);
            if (wekaSegmentation.getTrainingInstances() == null)
                wekaSegmentation.loadClassifier(classifierFilePath);

            if (tileFactor == 1) {
                wekaSegmentation.applyClassifier(true);
                iplSingle = wekaSegmentation.getClassifiedImage();
            } else {
                iplSingle = wekaSegmentation.applyClassifier(iplSingle, new int[] { tileFactor, tileFactor }, nThreads,
                        true);
            }

            // Converting probability image to specified bit depth (it will be 32-bit by
            // default)
            ImageTypeConverter.process(iplSingle, bitDepth, ImageTypeConverter.ScalingModes.SCALE);
            for (int cl = 1; cl <= nOutputClasses; cl++) {
                for (int z = 1; z <= (endingBlock - startingBlock + 1); z++) {
                    int[] pos = inputImagePlus.convertIndexToPosition(startingBlock + z - 1);
                    if (outputClass == -1) {
                        // If outputting all classes
                        iplSingle.setPosition(nOutputClasses * (z - 1) + cl);
                        probabilityMaps.setPosition((nOutputClasses * (pos[0] - 1) + cl), pos[1], pos[2]);
                    } else {
                        // If outputting a single class
                        iplSingle.setPosition(nClasses * (z - 1) + outputClass);
                        probabilityMaps.setPosition(1, pos[1], pos[2]);
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
            writeProgressStatus(count, slices, "images");

        }

        // Clearing the segmentation model from memory
        wekaSegmentation = null;

        return probabilityMaps;

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    @Override
    public String getDescription() {
        return "Performs pixel classification using the WEKA Trainable Segmentation plugin."
                + "<br><br>This module loads a previously-saved WEKA classifier model and applies it to the input image.  It then returns the multi-channel probability map."
                + "<br><br>Image stacks are processed in 2D, one slice at a time.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean convertToRGB = parameters.getValue(CONVERT_TO_RGB,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        String outputBitDepth = parameters.getValue(OUTPUT_BIT_DEPTH,workspace);
        boolean outputSingleClass = parameters.getValue(OUTPUT_SINGLE_CLASS,workspace);
        int outputClass = parameters.getValue(OUTPUT_CLASS,workspace);
        String pathType = parameters.getValue(PATH_TYPE,workspace);
        String genericFormat = parameters.getValue(GENERIC_FORMAT,workspace);
        String classifierFilePath = parameters.getValue(CLASSIFIER_FILE,workspace);
        int nSimSlices = parameters.getValue(SIMULTANEOUS_SLICES,workspace);
        int tileFactor = parameters.getValue(TILE_FACTOR,workspace);

        // Converting to RGB if requested
        if (convertToRGB) {
            inputImagePlus = inputImagePlus.duplicate();
            RGBStackConverter.convertToRGB(inputImagePlus);
        }

        // Converting the bit depth to an integer
        int bitDepth = Integer.parseInt(outputBitDepth);

        // If all channels are to be output, set output channel to -1
        if (!outputSingleClass)
            outputClass = -1;

        switch (pathType) {
            case PathTypes.MATCHING_FORMAT:
                Metadata metadata = (Metadata) workspace.getMetadata().clone();
                try {
                    classifierFilePath = ImageLoader.getGenericName(metadata, genericFormat);
                } catch (ServiceException | DependencyException | FormatException | IOException e) {
                    MIA.log.writeError(e);
                }
                break;
        }

        // Running the classifier on each individual stack
        ImagePlus probabilityMaps = calculateProbabilityMaps(inputImagePlus, outputImageName, classifierFilePath,
                nSimSlices, tileFactor, bitDepth, outputClass);

        // If the classification failed, a null object is returned
        if (probabilityMaps == null)
            return Status.FAIL;

        // Adding the probability maps to the Workspace
        Image probabilityImage = new Image(outputImageName, probabilityMaps);
        workspace.addImage(probabilityImage);

        if (showOutput)
            probabilityImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image to apply pixel classification to."));
        parameters.add(new BooleanP(CONVERT_TO_RGB, this, false,
                "Converts a composite image to RGB format.  This should be set to match the image-type used for generation of the model."));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "", "Output probability map image."));
        parameters.add(new ChoiceP(OUTPUT_BIT_DEPTH, this, OutputBitDepths.THIRTY_TWO, OutputBitDepths.ALL,
                "By default images will be saved as floating point 32-bit (probabilities in the range 0-1); however, they can be converted to 8-bit (probabilities in the range 0-255) or 16-bit (probabilities in the range 0-65535).  This is useful for saving memory or if the output probability map will be passed to image threshold module."));
        parameters.add(new BooleanP(OUTPUT_SINGLE_CLASS, this, false,
                "Allows a single class (image channel) to be output.  This is another feature for reducing memory usage."));
        parameters.add(new IntegerP(OUTPUT_CLASS, this, 1,
                "Class (image channel) to be output.  Channel numbering starts at 1."));
        parameters.add(new SeparatorP(CLASSIFIER_SEPARATOR, this));
        parameters.add(new ChoiceP(PATH_TYPE, this, PathTypes.SPECIFIC_FILE, PathTypes.ALL,
                "Method to use for generation of the classifier filename:<br><ul>" + "<li>\""
                        + PathTypes.MATCHING_FORMAT
                        + "\" Will generate a name from metadata values stored in the current workspace.  This is useful if the classifier varies from input file to input file.</li>"
                        + "<li>\"" + PathTypes.SPECIFIC_FILE
                        + "\" Will load the classifier file at a specific location.  This is useful if the same file is to be used for all input files.</li></ul>"));
        parameters.add(new StringP(GENERIC_FORMAT, this, "",
                "Format for a generic filename.  Plain text can be mixed with global variables or metadata values currently stored in the workspace.  Global variables are specified using the \"V{name}\" notation, where \"name\" is the name of the variable to insert.  Similarly, metadata values are specified with the \"M{name}\" notation."));
        parameters.add(new TextAreaP(AVAILABLE_METADATA_FIELDS, this, false,
                "List of the currently-available metadata values for this workspace.  These can be used when compiling a generic filename."));
        parameters.add(new FilePathP(CLASSIFIER_FILE, this, "",
                "Path to the classifier file (.model extension).  This file needs to be created manually using the WEKA Trainable Segmentation plugin included with Fiji."));
        parameters.add(new IntegerP(SIMULTANEOUS_SLICES, this, 1,
                "Number of image slices to process at any given time.  This reduces the memory footprint of the module, but can slow down processing."));
        parameters.add(new IntegerP(TILE_FACTOR, this, 1,
                "Number of tiles per dimension each image will be subdivided into for processing.  For example, a tile factor of 2 will divide the image into a 2x2 grid of tiles.  This reduces the memory footprint of the module."));
    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(CONVERT_TO_RGB));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_BIT_DEPTH));

        returnedParameters.add(parameters.getParameter(OUTPUT_SINGLE_CLASS));
        if ((boolean) parameters.getValue(OUTPUT_SINGLE_CLASS,workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_CLASS));
        }

        returnedParameters.add(parameters.getParameter(CLASSIFIER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(PATH_TYPE));
        switch ((String) parameters.getValue(PATH_TYPE,workspace)) {
            case PathTypes.MATCHING_FORMAT:
                returnedParameters.add(parameters.getParameter(GENERIC_FORMAT));
                returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                MetadataRefs metadataRefs = modules.getMetadataRefs(this);
                parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());
                break;
            case PathTypes.SPECIFIC_FILE:
                returnedParameters.add(parameters.getParameter(CLASSIFIER_FILE));
                break;
        }
        returnedParameters.add(parameters.getParameter(SIMULTANEOUS_SLICES));
        returnedParameters.add(parameters.getParameter(TILE_FACTOR));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
public MetadataRefs updateAndGetMetadataReferences() {
Workspace workspace = null;
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
