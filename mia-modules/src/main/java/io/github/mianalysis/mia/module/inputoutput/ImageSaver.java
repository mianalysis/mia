package io.github.mianalysis.mia.module.inputoutput;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.AVI_Writer;
import ij.process.ImageConverter;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.imagej.IntensityMinMax;

/**
 * Created by sc13967 on 26/06/2017.
 */

/**
 * Save an image/stack from the workspace to file. These files can be placed in
 * the same folder as the input file, located in a specific folder or placed in
 * a directory structure mirroring the input structure, but based at a new
 * location. For greater flexibility in output file locations and filenames, the
 * "Save image (generic)" module can be used. To prevent overwriting of
 * previously-saved files, the current date and time can be appended to the end
 * of each filename. Images can be saved in a variety of formats (AVI, TIF and
 * Zipped TIF).
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ImageSaver extends AbstractSaver {

    /**
    * 
    */
    public static final String LOADER_SEPARATOR = "Image saving";

    /**
     * Image to be saved to file.
     */
    public static final String INPUT_IMAGE = "Input image";

    public static final String IMAGE_SEQUENCE_CONTROLS = "Image sequence controls";

    public static final String IMAGE_SEQUENCE_MODE = "Image sequence mode";

    public static final String SPLIT_CHANNELS = "Split channels";

    public static final String SPLIT_Z_STACKS = "Split z-stacks";

    public static final String SPLIT_TIMESERIES = "Split timeseries";

    public static final String NUMBER_OF_ZEROS = "Number of zeros";

    public static final String STARTING_INDEX = "Starting index";

    /**
    * 
    */
    public static final String FORMAT_SEPARATOR = "Output image format";

    /**
     * The format the output image will be saved as:<br>
     * <ul>
     * <li>"AVI" Video written using the stock ImageJ "<a href=
     * "https://github.com/imagej/imagej1/blob/master/ij/plugin/filter/AVI_Writer.java">AVI
     * Writer</a>". Videos can use different compression algorithms specified using
     * "Compression mode". Framerate specified by "Frame rate (fps)" parameter.</li>
     * <li>"TIF" Standard multidimensional (multi-page) TIF image saving.</li>
     * <li>"ZIP" TIF images stored using ZIP compression. For images with large
     * homogeneous regions of pixel intensity this can greatly reduce file size in a
     * lossless manner. Zipped images can be read directly back into ImageJ/Fiji
     * without the need for prior decompression.</li>
     * </ul>
     */
    public static final String FILE_FORMAT = "File format";

    /**
     * Control whether saved images should be in ImageJ "Composite" (display all
     * channels simultaneously) or "Color" (display one channel at a time) mode.
     */
    public static final String CHANNEL_MODE = "Channel mode";

    /**
     * Convert images to RGB prior to saving. This is useful for displaying
     * multi-channel images to a format that can be easily viewed outside ImageJ.
     */
    public static final String SAVE_AS_RGB = "Save as RGB";

    /**
     * Compression mode used when saving AVI videos ("File format" parameter):<br>
     * <ul>
     * <li>"JPEG" Lossy video compression with quality specified by "Quality
     * (0-100)" parameter. This option is good when reducing video size is more
     * important than retaining perfect image quality.</li>
     * <li>"None" Frames are stored in their raw format (uncompressed). This gives
     * the highest quality, but also the largest file size.</li>
     * <li>"PNG" PNG video compression.</li>
     * </ul>
     */
    public static final String COMPRESSION_MODE = "Compression mode";
    public static final String QUALITY = "Quality (0-100)";
    public static final String FRAME_RATE = "Frame rate (fps)";

    /**
     * Flatten any overlay elements onto the image prior to saving.
     */
    public static final String FLATTEN_OVERLAY = "Flatten overlay";

    public interface ImageSequenceModes {
        String SINGLE_FILE = "Single file";
        String SINGLE_INDEX = "Single index";
        String MULTIPLE_INDICES = "Multiple indices";

        String[] ALL = new String[] { SINGLE_FILE, SINGLE_INDEX, MULTIPLE_INDICES };

    }

    public interface FileFormats {
        String AVI = "AVI"; // avi
        // String BITMAP = "Bitmap"; // bmp
        // String FITS = "FITS"; // fits
        // String JPEG = "JPEG"; // jpeg
        // String PGM = "PGM"; // pgm
        // String PNG = "PNG"; // png
        // String RAW = "Raw"; // raw
        // String TEXT_IMAGE = "Text image"; // text image
        String TIF = "TIF"; // tif
        String ZIP = "ZIP"; // zip

        String[] ALL = new String[] { AVI, TIF, ZIP };

    }

    public interface ChannelModes {
        String COLOUR = "Colour (separate channels)";
        String COMPOSITE = "Composite";

        String[] ALL = new String[] { COLOUR, COMPOSITE };

    }

    public interface CompressionModes {
        String NONE = "None";
        String JPEG = "JPEG";
        String PNG = "PNG";

        String[] ALL = new String[] { NONE, JPEG, PNG };

    }

    public ImageSaver(Modules modules) {
        super("Save image", modules);
    }

    public static void saveImage(Image inputImage, String fileFormat, String path) {
        saveImage(inputImage.getImagePlus(), fileFormat, path);
    }

    public static void saveImage(ImagePlus inputImagePlus, String fileFormat, String path) {
        switch (fileFormat) {
            case FileFormats.TIF:
                IJ.saveAs(inputImagePlus, "tif", path);
                break;
            case FileFormats.ZIP:
                IJ.saveAs(inputImagePlus, "zip", path);
                break;
        }
    }

    public static void saveVideo(ImagePlus inputImagePlus, String compressionMode, int frameRate, int jpegQuality,
            String path) {
        AVI_Writer writer = new AVI_Writer();
        int compressionType = getVideoCompressionType(compressionMode);
        inputImagePlus.getCalibration().fps = frameRate;

        try {
            writer.writeImage(inputImagePlus, path, compressionType, jpegQuality);
        } catch (IOException e) {
            MIA.log.writeError(e);
        }
    }

    public static int getVideoCompressionType(String compressionMode) {
        switch (compressionMode) {
            case CompressionModes.NONE:
            default:
                return AVI_Writer.NO_COMPRESSION;
            case CompressionModes.JPEG:
                return AVI_Writer.JPEG_COMPRESSION;
            case CompressionModes.PNG:
                return AVI_Writer.PNG_COMPRESSION;
        }
    }

    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Save an image/stack from the workspace to file.  These files can be placed in the same folder as the input file, located in a specific folder or placed in a directory structure mirroring the input structure, but based at a new location.  For greater flexibility in output file locations and filenames, the \""
                + new GenericImageSaver(null).getName()
                + "\" module can be used.  To prevent overwriting of previously-saved files, the current date and time can be appended to the end of each filename.  Images can be saved in a variety of formats (AVI, TIF and Zipped TIF).";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE, workspace);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
        String suffix = parameters.getValue(SAVE_SUFFIX, workspace);
        String imageSequenceMode = parameters.getValue(IMAGE_SEQUENCE_MODE, workspace);
        boolean splitChannels = parameters.getValue(SPLIT_CHANNELS, workspace);
        boolean splitZStack = parameters.getValue(SPLIT_Z_STACKS, workspace);
        boolean splitTimeseries = parameters.getValue(SPLIT_TIMESERIES, workspace);
        int numZeros = parameters.getValue(NUMBER_OF_ZEROS, workspace);
        int startingIndex = parameters.getValue(STARTING_INDEX, workspace);
        String fileFormat = parameters.getValue(FILE_FORMAT, workspace);
        String channelMode = parameters.getValue(CHANNEL_MODE, workspace);
        boolean flattenOverlay = parameters.getValue(FLATTEN_OVERLAY, workspace);
        String compressionMode = parameters.getValue(COMPRESSION_MODE, workspace);
        int quality = parameters.getValue(QUALITY, workspace);
        int frameRate = parameters.getValue(FRAME_RATE, workspace);
        boolean saveAsRGB = parameters.getValue(SAVE_AS_RGB, workspace);

        // Loading the image to save
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputIpl = inputImage.getImagePlus();

        if (channelMode.equals(ChannelModes.COMPOSITE))
            inputIpl.setDisplayMode(CompositeImage.COMPOSITE);

        // If the image is being altered make a copy
        if (saveAsRGB || flattenOverlay) {
            inputIpl = inputIpl.duplicate();
            new ImageConverter(inputIpl).convertToRGB();
        }

        if (flattenOverlay) {
            // Flattening overlay onto image for saving
            if (inputIpl.getNSlices() > 1 || inputIpl.getNFrames() > 1) {
                IntensityMinMax.run(inputIpl, true);
                if (inputIpl.getOverlay() != null)
                    inputIpl.flattenStack();
            } else {
                if (inputIpl.getOverlay() != null)
                    inputIpl = inputIpl.flatten();
            }
        }

        String outputPath = getOutputPath(modules, workspace);
        String outputName = getOutputName(modules, workspace);

        // Ensuring folders have been created
        new File(outputPath).mkdirs();

        String zerosString = "";
        for (int i = 0; i < numZeros; i++)
            zerosString += "0";
        DecimalFormat df = new DecimalFormat(zerosString);

        int count = 0;
        int nChannelsOut = (!imageSequenceMode.equals(ImageSequenceModes.SINGLE_FILE) && splitChannels) ? inputIpl.getNChannels() : 1;
        int nSlicesOut = (!imageSequenceMode.equals(ImageSequenceModes.SINGLE_FILE) && splitZStack) ? inputIpl.getNSlices() : 1;
        int nFramesOut = (!imageSequenceMode.equals(ImageSequenceModes.SINGLE_FILE) && splitTimeseries) ? inputIpl.getNFrames() : 1;

        for (int t = 0; t < nFramesOut; t++) {
            for (int z = 0; z < nSlicesOut; z++) {
                for (int c = 0; c < nChannelsOut; c++) {
                    // Getting image to save
                    ImagePlus outputIpl;
                    switch (imageSequenceMode) {
                        case ImageSequenceModes.SINGLE_FILE:
                        default:
                            outputIpl = inputIpl;
                            break;
                        case ImageSequenceModes.SINGLE_INDEX:
                        case ImageSequenceModes.MULTIPLE_INDICES:
                            String channelsStr = splitChannels ? String.valueOf(c + 1) : "1-end";
                            String slicesStr = splitZStack ? String.valueOf(z + 1) : "1-end";
                            String framesStr = splitTimeseries ? String.valueOf(t + 1) : "1-end";

                            outputIpl = ExtractSubstack.extractSubstack(inputIpl, inputImageName, channelsStr,
                                    slicesStr, framesStr);

                            break;

                    }

                    // Adding last bits to name
                    String finalOutputPath = outputPath + outputName;
                    finalOutputPath = appendSeries(finalOutputPath, workspace, appendSeriesMode);
                    finalOutputPath = appendDateTime(finalOutputPath, appendDateTimeMode);
                    finalOutputPath = finalOutputPath + suffix;

                    // Optionally, appending sequence number
                    switch (imageSequenceMode) {
                        case ImageSequenceModes.SINGLE_INDEX:
                            finalOutputPath = finalOutputPath + "_" + df.format(startingIndex + count);
                            break;

                        case ImageSequenceModes.MULTIPLE_INDICES:
                            finalOutputPath = finalOutputPath + "_C" + df.format(startingIndex + c) + "_Z"
                                    + df.format(startingIndex + z) + "_T" + df.format(startingIndex + t);
                            break;
                    }

                    switch (fileFormat) {
                        case FileFormats.AVI:
                            finalOutputPath = finalOutputPath + ".avi";
                            saveVideo(outputIpl, compressionMode, frameRate, quality, finalOutputPath);
                            break;
                        case FileFormats.TIF:
                        case FileFormats.ZIP:
                            finalOutputPath = finalOutputPath + ".tif";
                            saveImage(outputIpl, fileFormat, finalOutputPath);
                            break;
                    }

                    count++;

                }
            }
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(IMAGE_SEQUENCE_CONTROLS, this));
        parameters.add(new ChoiceP(IMAGE_SEQUENCE_MODE, this, ImageSequenceModes.SINGLE_FILE, ImageSequenceModes.ALL));
        parameters.add(new BooleanP(SPLIT_CHANNELS, this, true));
        parameters.add(new BooleanP(SPLIT_Z_STACKS, this, true));
        parameters.add(new BooleanP(SPLIT_TIMESERIES, this, true));
        parameters.add(new IntegerP(NUMBER_OF_ZEROS, this, 4));
        parameters.add(new IntegerP(STARTING_INDEX, this, 0));

        parameters.add(new SeparatorP(FORMAT_SEPARATOR, this));
        parameters.add(new ChoiceP(FILE_FORMAT, this, FileFormats.TIF, FileFormats.ALL));
        parameters.add(new ChoiceP(CHANNEL_MODE, this, ChannelModes.COMPOSITE, ChannelModes.ALL));
        parameters.add(new BooleanP(SAVE_AS_RGB, this, false));
        parameters.add(new ChoiceP(COMPRESSION_MODE, this, CompressionModes.NONE, CompressionModes.ALL));
        parameters.add(new IntegerP(QUALITY, this, 100));
        parameters.add(new IntegerP(FRAME_RATE, this, 25));
        parameters.add(new BooleanP(FLATTEN_OVERLAY, this, false));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(IMAGE_SEQUENCE_CONTROLS));
        returnedParameters.add(parameters.getParameter(IMAGE_SEQUENCE_MODE));

        switch ((String) parameters.getValue(IMAGE_SEQUENCE_MODE, workspace)) {
            case ImageSequenceModes.SINGLE_INDEX:
            case ImageSequenceModes.MULTIPLE_INDICES:
                returnedParameters.add(parameters.getParameter(SPLIT_CHANNELS));
                returnedParameters.add(parameters.getParameter(SPLIT_Z_STACKS));
                returnedParameters.add(parameters.getParameter(SPLIT_TIMESERIES));
                returnedParameters.add(parameters.getParameter(NUMBER_OF_ZEROS));
                returnedParameters.add(parameters.getParameter(STARTING_INDEX));
                break;

        }

        returnedParameters.add(parameters.getParameter(FORMAT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILE_FORMAT));

        switch ((String) parameters.getValue(FILE_FORMAT, workspace)) {
            case FileFormats.TIF:
            case FileFormats.ZIP:
                returnedParameters.add(parameters.getParameter(CHANNEL_MODE));
                returnedParameters.add(parameters.getParameter(FLATTEN_OVERLAY));
                if (!((boolean) parameters.getValue(FLATTEN_OVERLAY, workspace))) {
                    returnedParameters.add(parameters.getParameter(SAVE_AS_RGB));
                }

                break;

            case FileFormats.AVI:
                returnedParameters.add(parameters.getParameter(COMPRESSION_MODE));
                if ((boolean) parameters.getValue(COMPRESSION_MODE, workspace).equals(CompressionModes.JPEG)) {
                    returnedParameters.add(parameters.getParameter(QUALITY));
                }
                returnedParameters.add(parameters.getParameter(FRAME_RATE));
                returnedParameters.add(parameters.getParameter(FLATTEN_OVERLAY));
                break;
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(INPUT_IMAGE).setDescription("Image to be saved to file.");

        parameters.get(FILE_FORMAT).setDescription("The format the output image will be saved as:<br><ul>"

                + "<li>\"" + FileFormats.AVI
                + "\" Video written using the stock ImageJ \"<a href=\"https://github.com/imagej/imagej1/blob/master/ij/plugin/filter/AVI_Writer.java\">AVI Writer</a>\".  Videos can use different compression algorithms specified using \""
                + COMPRESSION_MODE + "\".  Framerate specified by \"" + FRAME_RATE + "\" parameter.</li>"

                + "<li>\"" + FileFormats.TIF + "\" Standard multidimensional (multi-page) TIF image saving.</li>"

                + "<li>\"" + FileFormats.ZIP
                + "\" TIF images stored using ZIP compression.  For images with large homogeneous regions of pixel intensity this can greatly reduce file size in a lossless manner.  Zipped images can be read directly back into ImageJ/Fiji without the need for prior decompression.</li></ul>");

        parameters.get(CHANNEL_MODE).setDescription(
                "Control whether saved images should be in ImageJ \"Composite\" (display all channels simultaneously) or \"Color\" (display one channel at a time) mode.");

        parameters.get(SAVE_AS_RGB).setDescription(
                "Convert images to RGB prior to saving.  This is useful for displaying multi-channel images to a format that can be easily viewed outside ImageJ.");

        parameters.get(COMPRESSION_MODE).setDescription("Compression mode used when saving AVI videos (\"" + FILE_FORMAT
                + "\" parameter):<br><ul>"

                + "<li>\"" + CompressionModes.JPEG + "\" Lossy video compression with quality specified by \"" + QUALITY
                + "\" parameter.  This option is good when reducing video size is more important than retaining perfect image quality.</li>"

                + "<li>\"" + CompressionModes.NONE
                + "\" Frames are stored in their raw format (uncompressed).  This gives the highest quality, but also the largest file size.</li>"

                + "<li>\"" + CompressionModes.PNG + "\" PNG video compression.</li></ul>");

        parameters.get(QUALITY).setDescription(
                "Quality of output JPEG-compressed video (values in range 0-100).  For reference, saving AVIs via ImageJ's \"File > Save As...\" menu uses a quality of 90.");

        parameters.get(FRAME_RATE).setDescription("Output video framerate (frames per second).");

        parameters.get(FLATTEN_OVERLAY).setDescription("Flatten any overlay elements onto the image prior to saving.");

    }
}
