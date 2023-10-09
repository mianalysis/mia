package io.github.mianalysis.mia.module.inputoutput.abstrakt;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.module.inputoutput.ImageLoader;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FolderPathP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.Preferences;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;

public abstract class AbstractSaver extends Module {
    public static final String FILE_SAVING_SEPARATOR = "File saving controls";
    public static final String SAVE_LOCATION = "Save location";
    public static final String MIRROR_DIRECTORY_ROOT = "Mirrored directory root";
    public static final String SAVE_FILE_PATH = "File path";
    public static final String SAVE_FILE_PATH_GENERIC = "File name (generic)";
    public static final String SAVE_NAME_MODE = "Save name mode";
    public static final String SAVE_FILE_NAME = "File name";
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";
    public static final String APPEND_SERIES_MODE = "Append series mode";
    public static final String APPEND_DATETIME_MODE = "Append date/time mode";
    public static final String SAVE_SUFFIX = "Add filename suffix";

    public AbstractSaver(String name, Modules modules) {
        super(name, modules);
    }

    public interface SaveLocations {
        String MIRRORED_DIRECTORY = "Mirrored directory";
        String MATCH_OUTPUT_CONTROL = "Match Output Control";
        String SAVE_WITH_INPUT = "Save with input file";
        String SPECIFIC_LOCATION = "Specific location";
        String SPECIFIC_LOCATION_GENERIC = "Specific location (generic)";

        String[] ALL = new String[] { MIRRORED_DIRECTORY, MATCH_OUTPUT_CONTROL, SAVE_WITH_INPUT, SPECIFIC_LOCATION,
                SPECIFIC_LOCATION_GENERIC };

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

    public String getOutputPath(Modules modules, Workspace workspace) {
        String saveLocation = parameters.getValue(SAVE_LOCATION, workspace);
        String mirroredDirectoryRoot = parameters.getValue(MIRROR_DIRECTORY_ROOT, workspace);
        String filePath = parameters.getValue(SAVE_FILE_PATH, workspace);
        String filePathGeneric = parameters.getValue(SAVE_FILE_PATH_GENERIC, workspace);

        if (saveLocation.equals(SaveLocations.MATCH_OUTPUT_CONTROL)) {
            OutputControl outputControl = modules.getOutputControl();
            String exportMode = outputControl.getParameterValue(OutputControl.EXPORT_MODE, null);
            switch (exportMode) {
                case OutputControl.ExportModes.INDIVIDUAL_FILES:
                    String outputSaveLocation = outputControl.getParameterValue(OutputControl.INDIVIDUAL_SAVE_LOCATION,
                            null);
                    switch (outputSaveLocation) {
                        case OutputControl.IndividualSaveLocations.MIRRORED_DIRECTORY:
                            saveLocation = SaveLocations.MIRRORED_DIRECTORY;
                            mirroredDirectoryRoot = outputControl
                                    .getParameterValue(OutputControl.MIRRORED_DIRECTORY_ROOT, null);
                            break;

                        case OutputControl.IndividualSaveLocations.SAVE_WITH_INPUT:
                            saveLocation = SaveLocations.SAVE_WITH_INPUT;
                            break;

                        case OutputControl.IndividualSaveLocations.SPECIFIC_LOCATION:
                            saveLocation = SaveLocations.SPECIFIC_LOCATION;
                            filePath = outputControl.getParameterValue(SAVE_FILE_PATH, null);
                            break;
                    }
                    break;

                case OutputControl.ExportModes.ALL_TOGETHER:
                case OutputControl.ExportModes.GROUP_BY_METADATA:
                    outputSaveLocation = outputControl.getParameterValue(OutputControl.GROUP_SAVE_LOCATION, null);
                    switch (outputSaveLocation) {
                        case OutputControl.GroupSaveLocations.SAVE_WITH_INPUT:
                            saveLocation = SaveLocations.SAVE_WITH_INPUT;
                            break;

                        case OutputControl.GroupSaveLocations.SPECIFIC_LOCATION:
                            saveLocation = SaveLocations.SPECIFIC_LOCATION;
                            filePath = outputControl.getParameterValue(SAVE_FILE_PATH, null);
                            break;
                    }
                    break;
            }
        }

        try {
            switch (saveLocation) {
                case SaveLocations.MIRRORED_DIRECTORY:
                    return OutputControl.getMirroredDirectory(modules.getInputControl().getRootFile(),
                            workspace.getMetadata(), mirroredDirectoryRoot);

                case SaveLocations.SAVE_WITH_INPUT:
                default:
                    File rootFile = workspace.getMetadata().getFile();
                    return rootFile.getParent() + File.separator;

                case SaveLocations.SPECIFIC_LOCATION:
                    return filePath + File.separator;

                case SaveLocations.SPECIFIC_LOCATION_GENERIC:
                    filePath = ImageLoader.getGenericName(workspace.getMetadata(), filePathGeneric);
                    return filePath + File.separator;
            }
        } catch (ServiceException | DependencyException | IOException | FormatException e) {
            MIA.log.writeWarning(e);
            return null;
        }
    }

    public String getOutputName(Modules modules, Workspace workspace) {
        String saveNameMode = parameters.getValue(SAVE_NAME_MODE, workspace);
        String saveFileName = parameters.getValue(SAVE_FILE_NAME, workspace);
        try {
            switch (saveNameMode) {
                case SaveNameModes.MATCH_INPUT:
                default:
                    File rootFile = workspace.getMetadata().getFile();
                    return FilenameUtils.removeExtension(rootFile.getName());

                case SaveNameModes.SPECIFIC_NAME:
                    saveFileName = ImageLoader.getGenericName(workspace.getMetadata(), saveFileName);
                    return FilenameUtils.removeExtension(saveFileName);
            }
        } catch (ServiceException | DependencyException | IOException | FormatException e) {
            MIA.log.writeWarning(e);
            return null;
        }
        }


    @Override
    protected void initialiseParameters() {
        Preferences preferences = MIA.getPreferences();
        boolean darkMode = preferences == null ? false : preferences.darkThemeEnabled();

        parameters.add(new SeparatorP(FILE_SAVING_SEPARATOR, this));
        parameters.add(new ChoiceP(SAVE_LOCATION, this, SaveLocations.SAVE_WITH_INPUT, SaveLocations.ALL));
        parameters.add(new FolderPathP(MIRROR_DIRECTORY_ROOT, this));
        parameters.add(new FolderPathP(SAVE_FILE_PATH, this));
        parameters.add(new StringP(SAVE_FILE_PATH_GENERIC, this));
        parameters.add(new ChoiceP(SAVE_NAME_MODE, this, SaveNameModes.MATCH_INPUT, SaveNameModes.ALL));
        parameters.add(new StringP(SAVE_FILE_NAME, this));
        parameters.add(new MessageP(AVAILABLE_METADATA_FIELDS, this, Colours.getDarkBlue(darkMode), 170));
        parameters.add(new ChoiceP(APPEND_SERIES_MODE, this, AppendSeriesModes.SERIES_NUMBER, AppendSeriesModes.ALL));
        parameters.add(new ChoiceP(APPEND_DATETIME_MODE, this, AppendDateTimeModes.NEVER, AppendDateTimeModes.ALL));
        parameters.add(new StringP(SAVE_SUFFIX, this));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(FILE_SAVING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SAVE_LOCATION));

        switch ((String) parameters.getValue(SAVE_LOCATION, workspace)) {
            case SaveLocations.MIRRORED_DIRECTORY:
                returnedParameters.add(parameters.getParameter(MIRROR_DIRECTORY_ROOT));
                break;

            case SaveLocations.SPECIFIC_LOCATION:
                returnedParameters.add(parameters.getParameter(SAVE_FILE_PATH));
                break;

            case SaveLocations.SPECIFIC_LOCATION_GENERIC:
                returnedParameters.add(parameters.getParameter(SAVE_FILE_PATH_GENERIC));
                returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                metadataRefs = modules.getMetadataRefs(this);
                parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());
                break;

        }

        returnedParameters.add(parameters.getParameter(SAVE_NAME_MODE));
        switch ((String) parameters.getValue(SAVE_NAME_MODE, workspace)) {
            case SaveNameModes.SPECIFIC_NAME:
                returnedParameters.add(parameters.getParameter(SAVE_FILE_NAME));
                if (!((String) parameters.getValue(SAVE_LOCATION, workspace))
                        .equals(SaveLocations.SPECIFIC_LOCATION_GENERIC)) {
                    returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                    metadataRefs = modules.getMetadataRefs(this);
                    parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());
                }
                break;
        }

        returnedParameters.add(parameters.getParameter(APPEND_SERIES_MODE));
        returnedParameters.add(parameters.getParameter(APPEND_DATETIME_MODE));
        returnedParameters.add(parameters.getParameter(SAVE_SUFFIX));

        return returnedParameters;

    }

    protected void addParameterDescriptions() {
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

        parameters.get(APPEND_DATETIME_MODE).setDescription(
                "Controls under what conditions the time and date will be appended on to the end of the image filename.  This can be used to prevent accidental over-writing of images from previous runs:<br><ul>"

                        + "<li>\"" + AppendDateTimeModes.ALWAYS
                        + "\" Always append the time and date on to the end of the filename.</li>"

                        + "<li>\"" + AppendDateTimeModes.IF_FILE_EXISTS
                        + "\" Only append the time and date if the results file already exists.</li>"

                        + "<li>\"" + AppendDateTimeModes.NEVER
                        + "\" Never append time and date (unless the file is open and unwritable).</li></ul>");

        parameters.get(SAVE_SUFFIX).setDescription("A custom suffix to be added to each filename.");

    }
}
