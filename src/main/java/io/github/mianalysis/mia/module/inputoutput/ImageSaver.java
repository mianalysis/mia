package io.github.mianalysis.mia.module.inputoutput;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.process.ImageConverter;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.Colours;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FolderPathP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.sjcross.sjcommon.process.IntensityMinMax;

/**
 * Created by sc13967 on 26/06/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class ImageSaver extends AbstractImageSaver {
    public static final String SAVE_LOCATION = "Save location";
    public static final String MIRROR_DIRECTORY_ROOT = "Mirrored directory root";
    public static final String SAVE_FILE_PATH = "File path";

    public static final String SAVE_NAME_MODE = "Save name mode";
    public static final String SAVE_FILE_NAME = "File name";
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";
    public static final String APPEND_SERIES_MODE = "Append series mode";
    public static final String SAVE_SUFFIX = "Add filename suffix";

    public ImageSaver(Modules modules) {
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


    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
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
                path = rootFile.getParent() + File.separator;
                break;

            case SaveLocations.SPECIFIC_LOCATION:
                path = filePath + File.separator;
                break;
        }

        String name="";
        try {            
            switch (saveNameMode) {
                case SaveNameModes.MATCH_INPUT:
                default:
                    File rootFile = workspace.getMetadata().getFile();
                    name = FilenameUtils.removeExtension(rootFile.getName());
                    break;

                case SaveNameModes.SPECIFIC_NAME:
                    saveFileName = ImageLoader.getGenericName(workspace.getMetadata(), saveFileName);
                    name = FilenameUtils.removeExtension(saveFileName);
                    break;
            }
        } catch (ServiceException | DependencyException | IOException | FormatException e) {
            MIA.log.writeWarning(e);
            return Status.FAIL;
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
        super.initialiseParameters();

        parameters.add(new ChoiceP(SAVE_LOCATION, this, SaveLocations.SAVE_WITH_INPUT, SaveLocations.ALL));
        parameters.add(new FolderPathP(MIRROR_DIRECTORY_ROOT, this));
        parameters.add(new FolderPathP(SAVE_FILE_PATH, this));

        parameters.add(new ChoiceP(SAVE_NAME_MODE, this, SaveNameModes.MATCH_INPUT, SaveNameModes.ALL));
        parameters.add(new StringP(SAVE_FILE_NAME, this));
        parameters.add(new MessageP(AVAILABLE_METADATA_FIELDS, this, Colours.DARK_BLUE, 170));
        parameters.add(new ChoiceP(APPEND_SERIES_MODE, this, AppendSeriesModes.SERIES_NUMBER, AppendSeriesModes.ALL));
        parameters.add(new StringP(SAVE_SUFFIX, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

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
                returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                metadataRefs = modules.getMetadataRefs(this);
                parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());
                break;
        }

        returnedParameters.add(parameters.getParameter(APPEND_SERIES_MODE));
        returnedParameters.add(parameters.getParameter(APPEND_DATETIME_MODE));
        returnedParameters.add(parameters.getParameter(SAVE_SUFFIX));

        returnedParameters.addAll(super.updateAndGetParameters());

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
        super.addParameterDescriptions();

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

        parameters.get(SAVE_FILE_NAME).setDescription(
                "Filename for saved image.  Care should be taken with this when working in batch mode as it's easy to continuously write over output images.");

        parameters.get(AVAILABLE_METADATA_FIELDS).setDescription(
                "List of the currently-available metadata values for this workspace.  These can be used when compiling a generic filename.");
            
        parameters.get(APPEND_SERIES_MODE).setDescription(
                "Controls if any series information should be appended to the end of the filename.  This is useful when working with multi-series files, as it should help prevent writing files from multiple runs with the same filename.  Series numbers are prepended by \"S\".  Choices are: "
                        + String.join(", ", AppendSeriesModes.ALL) + ".");

        parameters.get(SAVE_SUFFIX).setDescription("A custom suffix to be added to each filename.");

    }
}
