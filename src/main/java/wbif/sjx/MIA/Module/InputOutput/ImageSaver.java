package wbif.sjx.MIA.Module.InputOutput;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.AVI_Writer;
import ij.process.ImageConverter;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.FolderPathP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 26/06/2017.
 */
public class ImageSaver extends Module {
    public static final String LOADER_SEPARATOR = "Image saving";
    public static final String INPUT_IMAGE = "Input image";
    public static final String SAVE_LOCATION = "Save location";
    public static final String MIRROR_DIRECTORY_ROOT = "Mirrored directory root";
    public static final String SAVE_FILE_PATH = "File path";

    public static final String NAME_SEPARATOR = "Output image name";
    public static final String SAVE_NAME_MODE = "Save name mode";
    public static final String SAVE_FILE_NAME = "File name";
    public static final String APPEND_SERIES_MODE = "Append series mode";
    public static final String APPEND_DATETIME_MODE = "Append date/time mode";
    public static final String SAVE_SUFFIX = "Add filename suffix";

    public static final String FORMAT_SEPARATOR = "Output image format";
    public static final String FILE_FORMAT = "File format";
    public static final String CHANNEL_MODE = "Channel mode";
    public static final String SAVE_AS_RGB = "Save as RGB";
    public static final String COMPRESSION_MODE = "Compression mode";
    public static final String QUALITY = "Quality (0-100)";
    public static final String FRAME_RATE = "Frame rate (fps)";
    public static final String FLATTEN_OVERLAY = "Flatten overlay";

    public ImageSaver(ModuleCollection modules) {
        super("Save image", modules);
    }

    public interface SaveLocations {
        String MIRRORED_DIRECTORY = "Mirrored directory";
        String MATCH_OUTPUT_CONTROL = "Match Output Control";
        String SAVE_WITH_INPUT = "Save with input file";
        String SPECIFIC_LOCATION = "Specific location";

        String[] ALL = new String[] { MIRRORED_DIRECTORY, MATCH_OUTPUT_CONTROL, SAVE_WITH_INPUT, SPECIFIC_LOCATION };

    }

    public interface SaveNameModes {
        String MATCH_INPUT = "Match input file name";
        String SPECIFIC_NAME = "Specific name";

        String[] ALL = new String[] { MATCH_INPUT, SPECIFIC_NAME };

    }

    public interface AppendSeriesModes {
        String NONE = "None";
        String SERIES_NAME = "Series name";
        String SERIES_NUMBER = "Series number";

        String[] ALL = new String[] { NONE, SERIES_NAME, SERIES_NUMBER };

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

    public static String appendSeries(String inputName, Workspace workspace, String appendSeriesMode) {
        switch (appendSeriesMode) {
            case AppendSeriesModes.NONE:
            default:
                return inputName;
            case AppendSeriesModes.SERIES_NAME:
                String seriesName = workspace.getMetadata().getSeriesName();
                return inputName + "_S" + seriesName;
            case AppendSeriesModes.SERIES_NUMBER:
                int seriesNumber = workspace.getMetadata().getSeriesNumber();
                return inputName + "_S" + seriesNumber;
        }
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
    public String getPackageName() {
        return PackageNames.INPUT_OUTPUT;
    }

    @Override
    public String getDescription() {
        return "Save an image/stack from the workspace to file.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String saveLocation = parameters.getValue(SAVE_LOCATION);
        String mirroredDirectoryRoot = parameters.getValue(MIRROR_DIRECTORY_ROOT);
        String filePath = parameters.getValue(SAVE_FILE_PATH);
        String saveNameMode = parameters.getValue(SAVE_NAME_MODE);
        String saveFileName = parameters.getValue(SAVE_FILE_NAME);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE);
        String suffix = parameters.getValue(SAVE_SUFFIX);
        String fileFormat = parameters.getValue(FILE_FORMAT);
        String channelMode = parameters.getValue(CHANNEL_MODE);
        boolean flattenOverlay = parameters.getValue(FLATTEN_OVERLAY);
        String compressionMode = parameters.getValue(COMPRESSION_MODE);
        int quality = parameters.getValue(QUALITY);
        int frameRate = parameters.getValue(FRAME_RATE);
        boolean saveAsRGB = parameters.getValue(SAVE_AS_RGB);

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

        // If using the same settings as OutputControl, update saveLocation and filePath
        // (if necessary)
        if (saveLocation.equals(SaveLocations.MATCH_OUTPUT_CONTROL)) {
            OutputControl outputControl = modules.getOutputControl();
            String exportMode = outputControl.getParameterValue(OutputControl.EXPORT_MODE);
            switch (exportMode) {
                case OutputControl.ExportModes.INDIVIDUAL_FILES:
                    String outputSaveLocation = outputControl.getParameterValue(OutputControl.INDIVIDUAL_SAVE_LOCATION);
                    switch (outputSaveLocation) {
                        case OutputControl.IndividualSaveLocations.MIRRORED_DIRECTORY:
                            saveLocation = SaveLocations.MIRRORED_DIRECTORY;
                            mirroredDirectoryRoot = outputControl
                                    .getParameterValue(OutputControl.MIRRORED_DIRECTORY_ROOT);
                            break;

                        case OutputControl.IndividualSaveLocations.SAVE_WITH_INPUT:
                            saveLocation = SaveLocations.SAVE_WITH_INPUT;
                            break;

                        case OutputControl.IndividualSaveLocations.SPECIFIC_LOCATION:
                            saveLocation = SaveLocations.SPECIFIC_LOCATION;
                            filePath = outputControl.getParameterValue(SAVE_FILE_PATH);
                            break;
                    }
                    break;

                case OutputControl.ExportModes.ALL_TOGETHER:
                case OutputControl.ExportModes.GROUP_BY_METADATA:
                    outputSaveLocation = outputControl.getParameterValue(OutputControl.GROUP_SAVE_LOCATION);
                    switch (outputSaveLocation) {
                        case OutputControl.GroupSaveLocations.SAVE_WITH_INPUT:
                            saveLocation = SaveLocations.SAVE_WITH_INPUT;
                            break;

                        case OutputControl.GroupSaveLocations.SPECIFIC_LOCATION:
                            saveLocation = SaveLocations.SPECIFIC_LOCATION;
                            filePath = outputControl.getParameterValue(SAVE_FILE_PATH);
                            break;
                    }
                    break;
            }
        }

        String path;
        switch (saveLocation) {
            case SaveLocations.MIRRORED_DIRECTORY:
                path = OutputControl.getMirroredDirectory(modules.getInputControl().getRootFile(),
                        workspace.getMetadata(), mirroredDirectoryRoot);
                break;

            case SaveLocations.SAVE_WITH_INPUT:
            default:
                File rootFile = workspace.getMetadata().getFile();
                path = rootFile.getParent() + MIA.getSlashes();
                break;

            case SaveLocations.SPECIFIC_LOCATION:
                path = filePath + MIA.getSlashes();
                break;
        }

        String name;
        switch (saveNameMode) {
            case SaveNameModes.MATCH_INPUT:
            default:
                File rootFile = workspace.getMetadata().getFile();
                name = FilenameUtils.removeExtension(rootFile.getName());
                break;

            case SaveNameModes.SPECIFIC_NAME:
                name = FilenameUtils.removeExtension(saveFileName);
                break;
        }

        // Adding last bits to name
        path = path + name;
        path = appendSeries(path, workspace, appendSeriesMode);
        path = appendDateTime(path, appendDateTimeMode);

        switch (fileFormat) {
            case FileFormats.AVI:
                path = path + suffix + ".avi";
                saveVideo(inputImagePlus, compressionMode, frameRate, quality, path);
                break;
            case FileFormats.TIF:
            case FileFormats.ZIP:
                path = path + suffix + ".tif";
                saveImage(inputImagePlus, fileFormat, path);
                break;
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new ChoiceP(SAVE_LOCATION, this, SaveLocations.SAVE_WITH_INPUT, SaveLocations.ALL));
        parameters.add(new FolderPathP(MIRROR_DIRECTORY_ROOT, this));
        parameters.add(new FolderPathP(SAVE_FILE_PATH, this));

        parameters.add(new SeparatorP(NAME_SEPARATOR, this));
        parameters.add(new ChoiceP(SAVE_NAME_MODE, this, SaveNameModes.MATCH_INPUT, SaveNameModes.ALL));
        parameters.add(new StringP(SAVE_FILE_NAME, this));
        parameters.add(new ChoiceP(APPEND_SERIES_MODE, this, AppendSeriesModes.SERIES_NUMBER, AppendSeriesModes.ALL));
        parameters.add(new ChoiceP(APPEND_DATETIME_MODE, this, AppendDateTimeModes.NEVER, AppendDateTimeModes.ALL));
        parameters.add(new StringP(SAVE_SUFFIX, this));

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
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(SAVE_LOCATION));

        switch ((String) parameters.getValue(SAVE_LOCATION)) {
            case SaveLocations.SPECIFIC_LOCATION:
                returnedParameters.add(parameters.getParameter(SAVE_FILE_PATH));
                break;

            case SaveLocations.MIRRORED_DIRECTORY:
                returnedParameters.add(parameters.getParameter(MIRROR_DIRECTORY_ROOT));
                break;

        }

        returnedParameters.add(parameters.getParameter(NAME_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SAVE_NAME_MODE));
        switch ((String) parameters.getValue(SAVE_NAME_MODE)) {
            case SaveNameModes.SPECIFIC_NAME:
                returnedParameters.add(parameters.getParameter(SAVE_FILE_NAME));
                break;
        }

        returnedParameters.add(parameters.getParameter(APPEND_SERIES_MODE));
        returnedParameters.add(parameters.getParameter(APPEND_DATETIME_MODE));
        returnedParameters.add(parameters.getParameter(SAVE_SUFFIX));

        returnedParameters.add(parameters.getParameter(FORMAT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILE_FORMAT));

        switch ((String) parameters.getValue(FILE_FORMAT)) {
            case FileFormats.TIF:
            case FileFormats.ZIP:
                returnedParameters.add(parameters.getParameter(CHANNEL_MODE));
                returnedParameters.add(parameters.getParameter(FLATTEN_OVERLAY));
                if (!((boolean) parameters.getValue(FLATTEN_OVERLAY))) {
                    returnedParameters.add(parameters.getParameter(SAVE_AS_RGB));
                }

                break;

            case FileFormats.AVI:
                returnedParameters.add(parameters.getParameter(COMPRESSION_MODE));
                if ((boolean) parameters.getValue(COMPRESSION_MODE).equals(CompressionModes.JPEG)) {
                    returnedParameters.add(parameters.getParameter(QUALITY));
                }
                returnedParameters.add(parameters.getParameter(FRAME_RATE));
                returnedParameters.add(parameters.getParameter(FLATTEN_OVERLAY));
                break;
        }

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
        parameters.get(INPUT_IMAGE).setDescription("Image to be saved to file.");

        parameters.get(SAVE_LOCATION).setDescription("Select where the image should be saved.<br><ul>" +

                "<li>\"" + SaveLocations.MIRRORED_DIRECTORY
                + "\" Save the image to a new directory structure which has the same layout as the input.  This is useful when batch processing from a multi-layer folder structure.  The subdirectory layout will match that of the input structure, but will have its root at the folder specified in \""
                + MIRROR_DIRECTORY_ROOT + "\".</li>" +

                "<li>\"" + SaveLocations.MATCH_OUTPUT_CONTROL
                + "\" Save the image to the folder specified by the \"Save location\" parameter in \"Output control\".</li>"
                +

                "<li>\"" + SaveLocations.SAVE_WITH_INPUT
                + "\" Save the image in the same file as the root file for this workspace (i.e. the image specified in \"Input control\".</li>"
                +

                "<li>\"" + SaveLocations.SPECIFIC_LOCATION + "\" Save the image to a specific folder.</li></ul>");

        parameters.get(MIRROR_DIRECTORY_ROOT).setDescription(
                "The root path for the mirrored directory structure.  This path is the equivalent of the folder specified in \"Input control\".  All subfolders will be in the same relative locations to their input counterparts.");

        parameters.get(SAVE_FILE_PATH).setDescription("Path to folder where images will be saved.");

        parameters.get(SAVE_NAME_MODE).setDescription("Controls how saved image names will be generated.<br><ul>" +

                "<li>\"" + SaveNameModes.MATCH_INPUT
                + "\" Use the same name as the root file for this workspace (i.e. the input file in \"Input control\".</li>"

                + "<li>\"" + SaveNameModes.SPECIFIC_NAME
                + "\" Use a specific name for the output file.  Care should be taken with this when working in batch mode as it's easy to continuously write over output images.</li></ul>");

        parameters.get(SAVE_FILE_NAME).setDescription("Filename for saved image.  Care should be taken with this when working in batch mode as it's easy to continuously write over output images.");

        parameters.get(APPEND_SERIES_MODE).setDescription("Controls if any series information should be appended to the end of the filename.  This is useful when working with multi-series files, as it should help prevent writing files from multiple runs with the same filename.  Series numbers are prepended by \"S\".  Choices are: " +String.join(", ", AppendSeriesModes.ALL) + ".");

        parameters.get(APPEND_DATETIME_MODE).setDescription("Controls under what conditions the time and date will be appended on to the end of the image filename.  This can be used to prevent accidental over-writing of images from previous runs:<br><ul>"

                + "<li>\"" + AppendDateTimeModes.ALWAYS
                + "\" Always append the time and date on to the end of the filename.</li>"

                + "<li>\"" + AppendDateTimeModes.IF_FILE_EXISTS
                + "\" Only append the time and date if the results file already exists.</li>"

                + "<li>\"" + AppendDateTimeModes.NEVER
                + "\" Never append time and date (unless the file is open and unwritable).</li></ul>");

        parameters.get(SAVE_SUFFIX).setDescription("A custom suffix to be added to each filename.");

        parameters.get(FILE_FORMAT).setDescription("The format the output image will be saved as:<br><ul>"

                + "<li>\"" + FileFormats.AVI + "\" Video written using the stock ImageJ \"<a href=\"https://github.com/imagej/imagej1/blob/master/ij/plugin/filter/AVI_Writer.java\">AVI Writer</a>\".  Videos can use different compression algorithms specified using \""+COMPRESSION_MODE+"\".  Framerate specified by \""+FRAME_RATE+"\" parameter.</li>"

                + "<li>\"" + FileFormats.TIF + "\" Standard multidimensional (multi-page) TIF image saving.</li>"

                +"<li>\""+FileFormats.ZIP+"\" TIF images stored using ZIP compression.  For images with large homogeneous regions of pixel intensity this can greatly reduce file size in a lossless manner.  Zipped images can be read directly back into ImageJ/Fiji without the need for prior decompression.</li></ul>");

        parameters.get(CHANNEL_MODE).setDescription("Control whether saved images should be in ImageJ \"Composite\" (display all channels simultaneously) or \"Color\" (display one channel at a time) mode.");

        parameters.get(SAVE_AS_RGB).setDescription("Convert images to RGB prior to saving.  This is useful for displaying multi-channel images to a format that can be easily viewed outside ImageJ.");

        parameters.get(COMPRESSION_MODE).setDescription("Compression mode used when saving AVI videos (\""+FILE_FORMAT+"\" parameter):<br><ul>"

                + "<li>\"" + CompressionModes.JPEG + "\" Lossy video compression with quality specified by \"" + QUALITY
                + "\" parameter.  This option is good when reducing video size is more important than retaining perfect image quality.</li>"

                + "<li>\"" + CompressionModes.NONE
                + "\" Frames are stored in their raw format (uncompressed).  This gives the highest quality, but also the largest file size.</li>"

                + "<li>\"" + CompressionModes.PNG + "\" PNG video compression.</li></ul>");

        parameters.get(QUALITY).setDescription("Quality of output JPEG-compressed video (values in range 0-100).  For reference, saving AVIs via ImageJ's \"File > Save As...\" menu uses a quality of 90.");

        parameters.get(FRAME_RATE).setDescription("Output video framerate (frames per second).");

        parameters.get(FLATTEN_OVERLAY).setDescription("Flatten any overlay elements onto the image prior to saving.");

    }
}
