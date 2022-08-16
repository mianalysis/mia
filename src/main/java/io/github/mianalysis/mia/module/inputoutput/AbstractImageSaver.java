package io.github.mianalysis.mia.module.inputoutput;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.AVI_Writer;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
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

/**
 * Created by sc13967 on 26/06/2017.
 */
public abstract class AbstractImageSaver extends Module {
    public static final String LOADER_SEPARATOR = "Image saving";
    public static final String INPUT_IMAGE = "Input image";

    public static final String NAME_SEPARATOR = "Output image name";
    public static final String APPEND_DATETIME_MODE = "Append date/time mode";

    public static final String FORMAT_SEPARATOR = "Output image format";
    public static final String FILE_FORMAT = "File format";
    public static final String CHANNEL_MODE = "Channel mode";
    public static final String SAVE_AS_RGB = "Save as RGB";
    public static final String COMPRESSION_MODE = "Compression mode";
    public static final String QUALITY = "Quality (0-100)";
    public static final String FRAME_RATE = "Frame rate (fps)";
    public static final String FLATTEN_OVERLAY = "Flatten overlay";

    public AbstractImageSaver(String name, Modules modules) {
        super(name, modules);
    }

    public interface AppendDateTimeModes {
        String ALWAYS = "Always";
        String IF_FILE_EXISTS = "If file exists";
        String NEVER = "Never";

        String[] ALL = new String[] { ALWAYS, IF_FILE_EXISTS, NEVER };

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

    public static String appendDateTime(String inputName, String appendDateTimeMode) {
        switch (appendDateTimeMode) {
            case AppendDateTimeModes.IF_FILE_EXISTS:
                File file = new File(inputName);
                if (!file.exists())
                    return inputName;
            case AppendDateTimeModes.ALWAYS:
                String nameWithoutExtension = FilenameUtils.removeExtension(inputName);
                String extension = FilenameUtils.getExtension(inputName);
                String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                return nameWithoutExtension + "_(" + dateTime + ")." + extension;
            case AppendDateTimeModes.NEVER:
            default:
                return inputName;
        }
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
    public String getDescription() {
        return "Save an image/stack from the workspace to file.";
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(NAME_SEPARATOR, this));
        parameters.add(new ChoiceP(APPEND_DATETIME_MODE, this, AppendDateTimeModes.NEVER, AppendDateTimeModes.ALL));

        parameters.add(new SeparatorP(FORMAT_SEPARATOR, this));
        parameters.add(new ChoiceP(FILE_FORMAT, this, FileFormats.TIF, FileFormats.ALL));
        parameters.add(new ChoiceP(CHANNEL_MODE, this, ChannelModes.COMPOSITE, ChannelModes.ALL));
        parameters.add(new BooleanP(SAVE_AS_RGB, this, false));
        parameters.add(new ChoiceP(COMPRESSION_MODE, this, CompressionModes.NONE, CompressionModes.ALL));
        parameters.add(new IntegerP(QUALITY, this, 100));
        parameters.add(new IntegerP(FRAME_RATE, this, 25));
        parameters.add(new BooleanP(FLATTEN_OVERLAY, this, false));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

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

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("Image to be saved to file.");

        parameters.get(APPEND_DATETIME_MODE).setDescription(
                "Controls under what conditions the time and date will be appended on to the end of the image filename.  This can be used to prevent accidental over-writing of images from previous runs:<br><ul>"

                        + "<li>\"" + AppendDateTimeModes.ALWAYS
                        + "\" Always append the time and date on to the end of the filename.</li>"

                        + "<li>\"" + AppendDateTimeModes.IF_FILE_EXISTS
                        + "\" Only append the time and date if the results file already exists.</li>"

                        + "<li>\"" + AppendDateTimeModes.NEVER
                        + "\" Never append time and date (unless the file is open and unwritable).</li></ul>");

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
