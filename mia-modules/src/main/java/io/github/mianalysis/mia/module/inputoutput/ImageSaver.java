package io.github.mianalysis.mia.module.inputoutput;

import java.io.File;
import java.io.IOException;

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
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.IntensityMinMax;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 26/06/2017.
 */

/**
* Save an image/stack from the workspace to file.  These files can be placed in the same folder as the input file, located in a specific folder or placed in a directory structure mirroring the input structure, but based at a new location.  For greater flexibility in output file locations and filenames, the "Save image (generic)" module can be used.  To prevent overwriting of previously-saved files, the current date and time can be appended to the end of each filename.  Images can be saved in a variety of formats (AVI, TIF and Zipped TIF).
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


	/**
	* 
	*/
    public static final String FORMAT_SEPARATOR = "Output image format";

	/**
	* The format the output image will be saved as:<br><ul><li>"AVI" Video written using the stock ImageJ "<a href="https://github.com/imagej/imagej1/blob/master/ij/plugin/filter/AVI_Writer.java">AVI Writer</a>".  Videos can use different compression algorithms specified using "Compression mode".  Framerate specified by "Frame rate (fps)" parameter.</li><li>"TIF" Standard multidimensional (multi-page) TIF image saving.</li><li>"ZIP" TIF images stored using ZIP compression.  For images with large homogeneous regions of pixel intensity this can greatly reduce file size in a lossless manner.  Zipped images can be read directly back into ImageJ/Fiji without the need for prior decompression.</li></ul>
	*/
    public static final String FILE_FORMAT = "File format";

	/**
	* Control whether saved images should be in ImageJ "Composite" (display all channels simultaneously) or "Color" (display one channel at a time) mode.
	*/
    public static final String CHANNEL_MODE = "Channel mode";

	/**
	* Convert images to RGB prior to saving.  This is useful for displaying multi-channel images to a format that can be easily viewed outside ImageJ.
	*/
    public static final String SAVE_AS_RGB = "Save as RGB";

	/**
	* Compression mode used when saving AVI videos ("File format" parameter):<br><ul><li>"JPEG" Lossy video compression with quality specified by "Quality (0-100)" parameter.  This option is good when reducing video size is more important than retaining perfect image quality.</li><li>"None" Frames are stored in their raw format (uncompressed).  This gives the highest quality, but also the largest file size.</li><li>"PNG" PNG video compression.</li></ul>
	*/
    public static final String COMPRESSION_MODE = "Compression mode";
    public static final String QUALITY = "Quality (0-100)";
    public static final String FRAME_RATE = "Frame rate (fps)";

	/**
	* Flatten any overlay elements onto the image prior to saving.
	*/
    public static final String FLATTEN_OVERLAY = "Flatten overlay";

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
        String fileFormat = parameters.getValue(FILE_FORMAT, workspace);
        String channelMode = parameters.getValue(CHANNEL_MODE, workspace);
        boolean flattenOverlay = parameters.getValue(FLATTEN_OVERLAY, workspace);
        String compressionMode = parameters.getValue(COMPRESSION_MODE, workspace);
        int quality = parameters.getValue(QUALITY, workspace);
        int frameRate = parameters.getValue(FRAME_RATE, workspace);
        boolean saveAsRGB = parameters.getValue(SAVE_AS_RGB, workspace);

        // Loading the image to save
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        if (channelMode.equals(ChannelModes.COMPOSITE))
            inputImagePlus.setDisplayMode(CompositeImage.COMPOSITE);

        // If the image is being altered make a copy
        if (saveAsRGB || flattenOverlay) {
            inputImagePlus = inputImagePlus.duplicate();
            new ImageConverter(inputImagePlus).convertToRGB();
        }

        if (flattenOverlay) {
            // Flattening overlay onto image for saving
            if (inputImagePlus.getNSlices() > 1 || inputImagePlus.getNFrames() > 1) {
                IntensityMinMax.run(inputImagePlus, true);
                if (inputImagePlus.getOverlay() != null)
                    inputImagePlus.flattenStack();
            } else {
                if (inputImagePlus.getOverlay() != null)
                    inputImagePlus = inputImagePlus.flatten();
            }
        }

        String outputPath = getOutputPath(modules, workspace);        
        String outputName = getOutputName(modules, workspace);

        // Ensuring folders have been created
        new File(outputPath).mkdirs();
        
        // Adding last bits to name
        outputPath = outputPath + outputName;
        outputPath = appendSeries(outputPath, workspace, appendSeriesMode);
        outputPath = appendDateTime(outputPath, appendDateTimeMode);

        switch (fileFormat) {
            case FileFormats.AVI:
                outputPath = outputPath + suffix + ".avi";
                saveVideo(inputImagePlus, compressionMode, frameRate, quality, outputPath);
                break;
            case FileFormats.TIF:
            case FileFormats.ZIP:
                outputPath = outputPath + suffix + ".tif";
                saveImage(inputImagePlus, fileFormat, outputPath);
                break;
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

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
