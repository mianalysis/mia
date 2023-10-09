package io.github.mianalysis.mia.module.core;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.macro.Interpreter;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.macro.MacroHandler;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.script.AbstractMacroRunner;
import io.github.mianalysis.mia.module.script.RunMacro.MacroModes;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.metadata.Metadata;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.FolderPathP;
import io.github.mianalysis.mia.object.parameters.GenericButtonP;
import io.github.mianalysis.mia.object.parameters.MetadataItemP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
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
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by Stephen on 29/07/2017.
 */

/**
* Controls data export for each analysis run (job) as well as providing the option to run a final macro on all data.  If running a single file analysis, by default, the spreadsheet will be saved with the same name as the input file and stored at the same location.  Whereas, in batch mode (running multiple files from a folder) the spreadsheet will be saved in that folder with the same name as the input folder.  Using the "Group save location" parameter it's possible to redirect the output spreadsheet(s) to a specific location.<br><br>Data can be collated into a single Excel spreadsheet, or exported as one spreadsheet per analysis run (job).  Exported spreadsheets are separated into multiple sheets:<br><ul><li>"Parameters" An overview of the analysis setup (path to workflow file, date run, computer operating system) along with a list of all modules and parameters, their values and states.  The information here should be sufficient to reconstruct the analysis workflow in the absence of the original workflow file.</li><li>"Log" A list of any error messages presented to the user while the analysis was running.</li><li>"Summary" Each analysis run is summarised by a single line (or one line per timepoint or metadata value, if selected) containing metadata values, image measurements, the number of objects detected as well as statistics for object collections (e.g. mean of a particular measurement).  The summary sheet is intended to facilitate quick analysis; all data contained in this sheet (with the exception of image measurements) can be manually compiled from the individual object sheets.  This sheet can be enabled/disabled using the "Export summary" parameter.</li><li>"[Object-specific sheets]" Each object collection can export to a separate sheet.  These sheets contain one row per object in that collection and include metadata values, along with all measurements and relationships for that object.  This sheet can be enabled/disabled using the "Export individual objects" parameter.</li></ul><br>It's also possible to select the data to be exported for each sheet, including the metadata values (filename, series number, etc.), individual measurements and object collection statistics.
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class OutputControl extends AbstractMacroRunner {

	/**
	* 
	*/
    public static final String POSTPROCESSING_SEPARATOR = "Post-processing";

	/**
	* When selected, a final macro can be run once all the analysis runs (jobs) have been completed.  By using the workspace handling macros ("MIA_GetListOfWorkspaceIDs" and "MIA_SetActiveWorkspace") it's possible to switch between workspaces, thus facilitating dataset-wide analyses.  This macro will be executed only once as part of the final data exporting phase of the analysis.
	*/
    public static final String RUN_MACRO = "Run macro";

	/**
	* Select the source for the macro code:<br><ul><li>"Macro file" Load the macro from the file specified by the "Macro file" parameter.</li><li>"Macro text" Macro code is written directly into the "Macro text" box.</li></ul>
	*/
    public static final String MACRO_MODE = "Macro mode";

	/**
	* Macro code to be executed.  MIA macro commands are enabled using the "run("Enable MIA Extensions");" command which is included by default.  This should always be the first line of a macro if these commands are needed.
	*/
    public static final String MACRO_TEXT = "Macro text";

	/**
	* Select a macro file (.ijm) to run once, after all analysis runs have completed.
	*/
    public static final String MACRO_FILE = "Macro file";

	/**
	* Runs the macro on the currently-active workspace.  This requires that the analysis has been run at least once already, either by clicking the "Run" button or by evaluating all modules (or some, if not all required for macro) using the arrow buttons.
	*/
    public static final String TEST_BUTTON = "Test macro";


	/**
	* 
	*/
    public static final String EXPORT_SEPARATOR = "Core export controls";

	/**
	* Controls the number of results spreadsheets that are exported and what they contain:<br><ul><li>"All together" Results from all current analysis runs are grouped into a single spreadsheet.  The same sheets are used for all runs, so it's necessary to include a filename (and where necessary, a series name/number) metadata identifier.  Unless "Save name mode" is set to "Specific name", this file is named after the input file/folder.</li><li>"Group by metadata" Results are grouped and exported by a specific metadata item associated with each analysis run.  Unless "Save name mode" is set to "Specific name", the files are named in the format "[input file/folder name]_[metadata name]-[metadata value]".  The metadata item to group on is specified by the "Metadata item for grouping" parameter.</li><li>"Individual files" A separate results spreadsheet is saved for each analysis run.</li><li>"None" No results spreadsheets are exported.</li></ul>
	*/
    public static final String EXPORT_MODE = "Export mode";

	/**
	* If "Export mode" is set to "Group by metadata" this parameter controls where the grouped (by metadata) results files are saved:<br><ul><li>"Save with input file" The files are saved in the same folder as the input file.  If a folder was selected as the input, the result files are saved directly inside that folder.</li><li>"Specific location" The files are all saved in a single folder specified by the "File path" parameter.</li></ul>
	*/
    public static final String GROUP_SAVE_LOCATION = "Group save location";

	/**
	* If "Export mode" is set to "Individual files" this parameter controls where the individual results files are saved:<br><ul><li>"Mirrored directory" The files are saved in a mirrored directory structure.  This structure has the same folder layout as all subfolders of the specified input folder.  The root location of the mirrored structure is specified by the "Mirrored directory root" parameter.</li><li>"Save with input file" The files are saved in the same folder as the input file.  If a folder was selected as the input, the result files are saved directly inside that folder.</li><li>"Specific location" The files are all saved in a single folder specified by the "File path" parameter.</li></ul>
	*/
    public static final String INDIVIDUAL_SAVE_LOCATION = "Individual save location";

	/**
	* If using a mirrored directory structure for the results files, this parameter specifies the output structure root.  Subfolders will be created within this root folder that have identical structure to the subfolders of the input folder.
	*/
    public static final String MIRRORED_DIRECTORY_ROOT = "Mirrored directory root";

	/**
	* The path to the folder where results will be saved if using a specific folder path.
	*/
    public static final String SAVE_FILE_PATH = "File path";

	/**
	* Controls how the output results filename is generated:<br><ul><li>"Match input file/folder name" Results files are stored with the same name as the input file/folder (depending on the "Export mode" parameter).</li><li>"Specific name" Results files are stored with a specific name, specified by "File name".</li></ul>
	*/
    public static final String SAVE_NAME_MODE = "Save name mode";

	/**
	* Name to save the results file with if saving results files with a specific name ("Save name mode" parameter).
	*/
    public static final String SAVE_FILE_NAME = "File name";

	/**
	* If "Export mode" is set to "Group by metadata", results will be grouped and saved by the value of this metadata item associated with each analysis run.  There will be one results spreadsheet for each unique value of this metadata item.
	*/
    public static final String METADATA_ITEM_FOR_GROUPING = "Metadata item for grouping";

	/**
	* When selected, the results spreadsheet(s) can be exported at intervals during a multi-analysis run.  They will be exported every N runs, where N is controlled by the "Save every n files" parameter.  The spreadsheet(s) will still be stored when all analysis runs have completed.
	*/
    public static final String CONTINUOUS_DATA_EXPORT = "Continuous data export";

	/**
	* If "Continuous data export" is enabled, the current version of the spreadsheet will be exported after every N analysis runs.  This means if the analysis fails or the computer crashes, results collected so far are not lost.
	*/
    public static final String SAVE_EVERY_N = "Save every n files";

	/**
	* Controls under what conditions the time and date will be appended on to the end of the results file filename.  This can be used to prevent accidental over-writing of results files from previous runs:<br><ul><li>"Always" Always append the time and date on to the end of the filename.</li><li>"If file exists" Only append the time and date if the results file already exists.</li><li>"Never" Never append time and date (unless the file is open and unwritable).</li></ul>
	*/
    public static final String APPEND_DATETIME_MODE = "Append date/time mode";


	/**
	* 
	*/
    public static final String SUMMARY_SEPARATOR = "Summary sheet";

	/**
	* When selected, a summary sheet will be added to the results spreadsheet.  The summary sheet contains either (1) one row per input image file, (2) one row per timepoint per image file or (3) one row per metadata item.  The export mode is controlled by the "Summary mode" parameter.  This sheet is given the name "SUMMARY" and contains statistics for measurements (mean, min, max, standard deviation and sum), object counts and metadata items.
	*/
    public static final String EXPORT_SUMMARY = "Export summary";

	/**
	* Controls the form of the summary sheet (if "Export summary" is selected):<br><ul><li>"Per timepoint per input file" Each timepoint of each input image file is summarised in a separate row.</li><li>"Group by metadata" All files matching a specific metadata value are averaged and summarised in a separate row (i.e. one row per unique metadata item).</li><li>"Per input file" Each input image file is summarised in a separate row.</li></ul>
	*/
    public static final String SUMMARY_MODE = "Summary mode";

	/**
	* The metadata item to group the rows of the summary sheet by if "Summary mode" is set to "Group by metadata".
	*/
    public static final String METADATA_ITEM_FOR_SUMMARY = "Metadata item for summary";

	/**
	* When selected, the "Summary" results sheet displays columns reporting the number of objects per object collection.
	*/
    public static final String SHOW_OBJECT_COUNTS = "Show object counts";

	/**
	* When selected, individual results sheets will be created for each object collection.  In these sheets, each object in that collection is summarised per row.  The individual object sheets have the same name as their corresponding object collection.
	*/
    public static final String EXPORT_INDIVIDUAL_OBJECTS = "Export individual objects";


	/**
	* 
	*/
    public static final String MEASUREMENT_SEPARATOR = "Measurement selection";

    public OutputControl(Modules modules) {
        super("Output control", modules);
    }

    public interface IndividualSaveLocations {
        String MIRRORED_DIRECTORY = "Mirrored directory";
        String SAVE_WITH_INPUT = "Save with input file";
        String SPECIFIC_LOCATION = "Specific location";

        String[] ALL = new String[] { MIRRORED_DIRECTORY, SAVE_WITH_INPUT, SPECIFIC_LOCATION };

    }

    public interface GroupSaveLocations {
        String SAVE_WITH_INPUT = "Save with input file";
        String SPECIFIC_LOCATION = "Specific location";

        String[] ALL = new String[] { SAVE_WITH_INPUT, SPECIFIC_LOCATION };

    }

    public interface SaveNameModes {
        String MATCH_INPUT = "Match input file/folder name";
        String SPECIFIC_NAME = "Specific name";

        String[] ALL = new String[] { MATCH_INPUT, SPECIFIC_NAME };

    }

    public interface ExportModes {
        String ALL_TOGETHER = "All together";
        String GROUP_BY_METADATA = "Group by metadata";
        String INDIVIDUAL_FILES = "Individual files";
        String NONE = "None";

        String[] ALL = new String[] { ALL_TOGETHER, GROUP_BY_METADATA, INDIVIDUAL_FILES, NONE };

    }

    public interface SummaryModes {
        String ONE_AVERAGE_PER_FILE = "Per input file";
        String AVERAGE_PER_TIMEPOINT = "Per timepoint per input file";
        String GROUP_BY_METADATA = "Group by metadata";

        String[] ALL = new String[] { ONE_AVERAGE_PER_FILE, AVERAGE_PER_TIMEPOINT, GROUP_BY_METADATA };

    }

    public interface AppendDateTimeModes {
        String ALWAYS = "Always";
        String IF_FILE_EXISTS = "If file exists";
        String NEVER = "Never";

        String[] ALL = new String[] { ALWAYS, IF_FILE_EXISTS, NEVER };

    }

    public boolean isExportIndividual() {
        return parameters.getValue(EXPORT_MODE,null).equals(ExportModes.INDIVIDUAL_FILES);
    }

    public boolean isExportAllTogether() {
        return parameters.getValue(EXPORT_MODE,null).equals(ExportModes.ALL_TOGETHER);
    }

    public boolean isExportGroupedByMetadata() {
        return parameters.getValue(EXPORT_MODE,null).equals(ExportModes.GROUP_BY_METADATA);
    }

    public String getIndividualOutputPath(Metadata metadata) {
        String mirroredRoot = getParameterValue(MIRRORED_DIRECTORY_ROOT,null);
        String saveLocation = getParameterValue(INDIVIDUAL_SAVE_LOCATION,null);
        String saveFilePath = getParameterValue(SAVE_FILE_PATH,null);

        File inputFile = metadata.getFile();

        // Determining the file path
        String path = "";
        switch (saveLocation) {
            case IndividualSaveLocations.MIRRORED_DIRECTORY:
                path = getMirroredDirectory(modules.getInputControl().getRootFile(), metadata, mirroredRoot);
                break;

            case IndividualSaveLocations.SAVE_WITH_INPUT:
                if (inputFile.isFile())
                    path = inputFile.getParent() + File.separator;
                else
                    path = inputFile.getAbsolutePath() + File.separator;
                break;

            case IndividualSaveLocations.SPECIFIC_LOCATION:
                path = saveFilePath + File.separator;
                break;
        }

        String suffix = getOutputSuffix();
        String name = getOutputFilename(inputFile);

        return path + name + suffix;

    }

    public static String getMirroredDirectory(File rootFile, Metadata metadata, String mirroredDirectoryRoot) {
        int fileDepth = metadata.containsKey("FILE_DEPTH") ? (int) metadata.get("FILE_DEPTH") : 0;

        StringBuilder sb = new StringBuilder();
        File parentFile = metadata.getFile().getParentFile();
        for (int i = 0; i < fileDepth; i++) {
            sb.insert(0, parentFile.getName() + File.separator);
            parentFile = parentFile.getParentFile();
        }

        new File(mirroredDirectoryRoot + File.separator + sb).mkdirs();

        return mirroredDirectoryRoot + File.separator + sb;

    }

    public String getGroupOutputPath(File inputFile) {
        String saveLocation = getParameterValue(GROUP_SAVE_LOCATION,null);
        String saveFilePath = getParameterValue(SAVE_FILE_PATH,null);

        // Determining the file path
        String path = "";
        switch (saveLocation) {
            case GroupSaveLocations.SAVE_WITH_INPUT:
                if (inputFile.isFile())
                    path = inputFile.getParent() + File.separator;
                else
                    path = inputFile.getAbsolutePath() + File.separator;
                break;

            case GroupSaveLocations.SPECIFIC_LOCATION:
                path = saveFilePath + File.separator;
                break;
        }

        String suffix = getOutputSuffix();
        String name = getOutputFilename(inputFile);

        return path + name + suffix;

    }

    String getOutputFilename(File inputFile) {
        String saveNameMode = getParameterValue(SAVE_NAME_MODE,null);
        String saveFileName = getParameterValue(SAVE_FILE_NAME,null);

        // Determining the file name
        String name = "";
        switch (saveNameMode) {
            case OutputControl.SaveNameModes.MATCH_INPUT:
                if (inputFile.isFile()) {
                    name = FilenameUtils.removeExtension(inputFile.getName());
                } else {
                    name = inputFile.getName();
                }
                break;

            case OutputControl.SaveNameModes.SPECIFIC_NAME:
                name = saveFileName;
                break;
        }

        return name;

    }

    String getOutputSuffix() {
        String seriesMode = modules.getInputControl().getParameterValue(InputControl.SERIES_MODE,null);
        String seriesList = modules.getInputControl().getParameterValue(InputControl.SERIES_LIST,null);

        // Determining the suffix
        String suffix = "";
        if (seriesMode.equals(InputControl.SeriesModes.SERIES_LIST)) {
            suffix = "_S" + seriesList.replace(" ", "");
        }

        return suffix;

    }

    public void runMacro(Workspace workspace) {
        if (!(boolean) parameters.getValue(RUN_MACRO,null)) {
            return;
        }

        MIA.log.writeStatus("Running post-processing macro");
        String macroMode = parameters.getValue(MACRO_MODE,null);
        String macroText = parameters.getValue(MACRO_TEXT,null);
        String macroFile = parameters.getValue(MACRO_FILE,null);

        // Setting the MacroHandler to the current workspace
        MacroHandler.setWorkspace(workspace);
        MacroHandler.setModules(modules);

        // If the macro is stored as a file, load this to the macroText string
        if (macroMode.equals(MacroModes.MACRO_FILE))
            macroText = IJ.openAsString(macroFile);

        // Appending variables to the front of the macro
        ParameterGroup variableGroup = parameters.getParameter(ADD_VARIABLE);
        String finalMacroText = AbstractMacroRunner.addVariables(macroText, variableGroup, workspace);

        // Running the macro
        Interpreter interpreter = new Interpreter();
        interpreter.setIgnoreErrors(true);
        try {
            interpreter.runBatchMacro(finalMacroText, null);
            if (interpreter.getErrorMessage() != null)
                throw new RuntimeException();
        } catch (RuntimeException e) {
            IJ.runMacro("setBatchMode(false)");
            MIA.log.writeError("Macro failed with error \"" + interpreter.getErrorMessage() + "\".  Skipping file.");
        }
    }


    @Override
    public Category getCategory() {
        return Categories.CORE;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Controls data export for each analysis run (job) as well as providing the option to run a final macro on all data.  If running a single file analysis, by default, the spreadsheet will be saved with the same name as the input file and stored at the same location.  Whereas, in batch mode (running multiple files from a folder) the spreadsheet will be saved in that folder with the same name as the input folder.  Using the \""
                + GROUP_SAVE_LOCATION
                + "\" parameter it's possible to redirect the output spreadsheet(s) to a specific location."

                + "<br><br>Data can be collated into a single Excel spreadsheet, or exported as one spreadsheet per analysis run (job).  Exported spreadsheets are separated into multiple sheets:<br><ul>"

                + "<li>\"Parameters\" An overview of the analysis setup (path to workflow file, date run, computer operating system) along with a list of all modules and parameters, their values and states.  The information here should be sufficient to reconstruct the analysis workflow in the absence of the original workflow file.</li>"

                + "<li>\"Log\" A list of any error messages presented to the user while the analysis was running.</li>"

                + "<li>\"Summary\" Each analysis run is summarised by a single line (or one line per timepoint or metadata value, if selected) containing metadata values, image measurements, the number of objects detected as well as statistics for object collections (e.g. mean of a particular measurement).  The summary sheet is intended to facilitate quick analysis; all data contained in this sheet (with the exception of image measurements) can be manually compiled from the individual object sheets.  This sheet can be enabled/disabled using the \""
                + EXPORT_SUMMARY + "\" parameter.</li>"

                + "<li>\"[Object-specific sheets]\" Each object collection can export to a separate sheet.  These sheets contain one row per object in that collection and include metadata values, along with all measurements and relationships for that object.  This sheet can be enabled/disabled using the \""
                + EXPORT_INDIVIDUAL_OBJECTS + "\" parameter.</li>"

                + "</ul><br>It's also possible to select the data to be exported for each sheet, including the metadata values (filename, series number, etc.), individual measurements and object collection statistics.";
    }

    @Override
    public Status process(Workspace workspace) {
        return Status.PASS;
    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(POSTPROCESSING_SEPARATOR, this));

        parameters.add(new BooleanP(RUN_MACRO, this, false));
        parameters.add(new ChoiceP(MACRO_MODE, this, MacroModes.MACRO_TEXT, MacroModes.ALL));
        parameters.add(new TextAreaP(MACRO_TEXT, this,
                "run(\"Enable MIA Extensions\");\n\n// Get a list of Workspace IDs with Ext.MIA_GetListOfWorkspaceIDs() and set active workspace with Ext.MIA_SetActiveWorkspace(ID).",
                true));
        parameters.add(new FilePathP(MACRO_FILE, this));
        parameters.add(new GenericButtonP(TEST_BUTTON, this, "Test macro", GenericButtonP.DefaultModes.TEST_MACRO));

        parameters.add(new SeparatorP(EXPORT_SEPARATOR, this));
        parameters.add(new ChoiceP(EXPORT_MODE, this, ExportModes.ALL_TOGETHER, ExportModes.ALL));
        parameters.add(new ChoiceP(INDIVIDUAL_SAVE_LOCATION, this, IndividualSaveLocations.SAVE_WITH_INPUT,
                IndividualSaveLocations.ALL));
        parameters.add(
                new ChoiceP(GROUP_SAVE_LOCATION, this, GroupSaveLocations.SAVE_WITH_INPUT, GroupSaveLocations.ALL));
        parameters.add(new FolderPathP(SAVE_FILE_PATH, this));
        parameters.add(new FolderPathP(MIRRORED_DIRECTORY_ROOT, this));
        parameters.add(new ChoiceP(SAVE_NAME_MODE, this, SaveNameModes.MATCH_INPUT, SaveNameModes.ALL));
        parameters.add(new StringP(SAVE_FILE_NAME, this));
        parameters.add(new MetadataItemP(METADATA_ITEM_FOR_GROUPING, this));
        parameters.add(new BooleanP(CONTINUOUS_DATA_EXPORT, this, false));
        parameters.add(new IntegerP(SAVE_EVERY_N, this, 10));
        parameters.add(new ChoiceP(APPEND_DATETIME_MODE, this, AppendDateTimeModes.NEVER, AppendDateTimeModes.ALL));

        parameters.add(new SeparatorP(SUMMARY_SEPARATOR, this));
        parameters.add(new BooleanP(EXPORT_SUMMARY, this, true));
        parameters.add(new ChoiceP(SUMMARY_MODE, this, SummaryModes.ONE_AVERAGE_PER_FILE, SummaryModes.ALL));
        parameters.add(new MetadataItemP(METADATA_ITEM_FOR_SUMMARY, this));
        parameters.add(new BooleanP(SHOW_OBJECT_COUNTS, this, true));
        parameters.add(new BooleanP(EXPORT_INDIVIDUAL_OBJECTS, this, true));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(POSTPROCESSING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RUN_MACRO));
        if ((boolean) parameters.getValue(RUN_MACRO,null)) {
            returnedParameters.addAll(super.updateAndGetParameters());
            returnedParameters.add(parameters.getParameter(MACRO_MODE));
            switch ((String) parameters.getValue(MACRO_MODE,null)) {
                case MacroModes.MACRO_FILE:
                    returnedParameters.add(parameters.getParameter(MACRO_FILE));
                    break;
                case MacroModes.MACRO_TEXT:
                    returnedParameters.add(parameters.getParameter(MACRO_TEXT));
                    break;
            }
            returnedParameters.add(parameters.getParameter(TEST_BUTTON));
        }

        returnedParameters.add(parameters.getParameter(EXPORT_SEPARATOR));
        ChoiceP exportMode = (ChoiceP) parameters.getParameter(EXPORT_MODE);
        returnedParameters.add(exportMode);
        switch (exportMode.getChoice()) {
            case ExportModes.GROUP_BY_METADATA:
                returnedParameters.add(parameters.getParameter(METADATA_ITEM_FOR_GROUPING));
                break;
            case ExportModes.NONE:
                return returnedParameters;
        }

        switch (exportMode.getChoice()) {
            case ExportModes.INDIVIDUAL_FILES:
                returnedParameters.add(parameters.getParameter(INDIVIDUAL_SAVE_LOCATION));
                switch ((String) parameters.getValue(INDIVIDUAL_SAVE_LOCATION,null)) {
                    case IndividualSaveLocations.SPECIFIC_LOCATION:
                        returnedParameters.add(parameters.getParameter(SAVE_FILE_PATH));
                        break;
                    case IndividualSaveLocations.MIRRORED_DIRECTORY:
                        returnedParameters.add(parameters.getParameter(MIRRORED_DIRECTORY_ROOT));
                        break;
                }
                break;
            case ExportModes.GROUP_BY_METADATA:
            case ExportModes.ALL_TOGETHER:
                returnedParameters.add(parameters.getParameter(GROUP_SAVE_LOCATION));
                switch ((String) parameters.getValue(GROUP_SAVE_LOCATION,null)) {
                    case GroupSaveLocations.SPECIFIC_LOCATION:
                        returnedParameters.add(parameters.getParameter(SAVE_FILE_PATH));
                        break;
                }
                break;
        }

        returnedParameters.add(parameters.getParameter(SAVE_NAME_MODE));
        switch ((String) parameters.getValue(SAVE_NAME_MODE,null)) {
            case SaveNameModes.SPECIFIC_NAME:
                returnedParameters.add(parameters.getParameter(SAVE_FILE_NAME));
                break;
        }

        if (!exportMode.getValue(null).equals(ExportModes.INDIVIDUAL_FILES)) {
            BooleanP continuousDataExport = (BooleanP) parameters.getParameter(CONTINUOUS_DATA_EXPORT);
            returnedParameters.add(continuousDataExport);
            if (continuousDataExport.isSelected()) {
                returnedParameters.add(parameters.getParameter(SAVE_EVERY_N));
            }
        }

        returnedParameters.add(parameters.getParameter(APPEND_DATETIME_MODE));

        returnedParameters.add(parameters.getParameter(SUMMARY_SEPARATOR));
        BooleanP exportSummary = (BooleanP) parameters.getParameter(EXPORT_SUMMARY);
        returnedParameters.add(exportSummary);
        if (exportSummary.isSelected()) {
            returnedParameters.add(parameters.getParameter(SUMMARY_MODE));
            switch ((String) parameters.getValue(SUMMARY_MODE,null)) {
                case SummaryModes.GROUP_BY_METADATA:
                    returnedParameters.add(parameters.getParameter(METADATA_ITEM_FOR_SUMMARY));
                    break;
            }

            returnedParameters.add(parameters.getParameter(SHOW_OBJECT_COUNTS));

        }

        returnedParameters.add(parameters.getParameter(EXPORT_INDIVIDUAL_OBJECTS));

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));

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

        parameters.get(RUN_MACRO).setDescription(
                "When selected, a final macro can be run once all the analysis runs (jobs) have been completed.  By using the workspace handling macros (\"MIA_GetListOfWorkspaceIDs\"and \"MIA_SetActiveWorkspace\") it's possible to switch between workspaces, thus facilitating dataset-wide analyses.  This macro will be executed only once as part of the final data exporting phase of the analysis.");

        parameters.get(MACRO_MODE)
                .setDescription("Select the source for the macro code:<br><ul>" + "<li>\"" + MacroModes.MACRO_FILE
                        + "\" Load the macro from the file specified by the \"" + MACRO_FILE + "\" parameter.</li>"

                        + "<li>\"" + MacroModes.MACRO_TEXT + "\" Macro code is written directly into the \""
                        + MACRO_TEXT + "\" box.</li></ul>");

        parameters.get(MACRO_TEXT).setDescription(
                "Macro code to be executed.  MIA macro commands are enabled using the \"run(\"Enable MIA Extensions\");\" command which is included by default.  This should always be the first line of a macro if these commands are needed.");

        parameters.get(MACRO_FILE)
                .setDescription("Select a macro file (.ijm) to run once, after all analysis runs have completed.");

        parameters.get(TEST_BUTTON).setDescription(
                "Runs the macro on the currently-active workspace.  This requires that the analysis has been run at least once already, either by clicking the \"Run\" button or by evaluating all modules (or some, if not all required for macro) using the arrow buttons.");

        parameters.get(EXPORT_MODE).setDescription(
                "Controls the number of results spreadsheets that are exported and what they contain:<br><ul>"

                        + "<li>\"" + ExportModes.ALL_TOGETHER
                        + "\" Results from all current analysis runs are grouped into a single spreadsheet.  The same sheets are used for all runs, so it's necessary to include a filename (and where necessary, a series name/number) metadata identifier.  Unless \""
                        + SAVE_NAME_MODE + "\" is set to \"" + SaveNameModes.SPECIFIC_NAME
                        + "\", this file is named after the input file/folder.</li>"

                        + "<li>\"" + ExportModes.GROUP_BY_METADATA
                        + "\" Results are grouped and exported by a specific metadata item associated with each analysis run.  Unless \""
                        + SAVE_NAME_MODE + "\" is set to \"" + SaveNameModes.SPECIFIC_NAME
                        + "\", the files are named in the format \"[input file/folder name]_[metadata name]-[metadata value]\".  The metadata item to group on is specified by the \""
                        + METADATA_ITEM_FOR_GROUPING + "\" parameter.</li>"

                        + "<li>\"" + ExportModes.INDIVIDUAL_FILES
                        + "\" A separate results spreadsheet is saved for each analysis run.</li>"

                        + "<li>\"" + ExportModes.NONE + "\" No results spreadsheets are exported.</li></ul>");

        parameters.get(INDIVIDUAL_SAVE_LOCATION).setDescription("If \"" + EXPORT_MODE + "\" is set to \""
                + ExportModes.INDIVIDUAL_FILES
                + "\" this parameter controls where the individual results files are saved:<br><ul>"

                + "<li>\"" + IndividualSaveLocations.MIRRORED_DIRECTORY
                + "\" The files are saved in a mirrored directory structure.  This structure has the same folder layout as all subfolders of the specified input folder.  The root location of the mirrored structure is specified by the \""
                + MIRRORED_DIRECTORY_ROOT + "\" parameter.</li>"

                + "<li>\"" + IndividualSaveLocations.SAVE_WITH_INPUT
                + "\" The files are saved in the same folder as the input file.  If a folder was selected as the input, the result files are saved directly inside that folder.</li>"

                + "<li>\"" + IndividualSaveLocations.SPECIFIC_LOCATION
                + "\" The files are all saved in a single folder specified by the \"" + SAVE_FILE_PATH
                + "\" parameter.</li></ul>");

        parameters.get(GROUP_SAVE_LOCATION).setDescription("If \"" + EXPORT_MODE + "\" is set to \""
                + ExportModes.GROUP_BY_METADATA
                + "\" this parameter controls where the grouped (by metadata) results files are saved:<br><ul>"

                + "<li>\"" + GroupSaveLocations.SAVE_WITH_INPUT
                + "\" The files are saved in the same folder as the input file.  If a folder was selected as the input, the result files are saved directly inside that folder.</li>"

                + "<li>\"" + GroupSaveLocations.SPECIFIC_LOCATION
                + "\" The files are all saved in a single folder specified by the \"" + SAVE_FILE_PATH
                + "\" parameter.</li></ul>");

        parameters.get(MIRRORED_DIRECTORY_ROOT).setDescription(
                "If using a mirrored directory structure for the results files, this parameter specifies the output structure root.  Subfolders will be created within this root folder that have identical structure to the subfolders of the input folder.");

                parameters.get(SAVE_FILE_PATH)
                .setDescription("The path to the folder where results will be saved if using a specific folder path.");
                        
        parameters.get(SAVE_NAME_MODE).setDescription("Controls how the output results filename is generated:<br><ul>"

                + "<li>\"" + SaveNameModes.MATCH_INPUT
                + "\" Results files are stored with the same name as the input file/folder (depending on the \""
                + EXPORT_MODE + "\" parameter).</li>"

                + "<li>\"" + SaveNameModes.SPECIFIC_NAME
                + "\" Results files are stored with a specific name, specified by \"" + SAVE_FILE_NAME
                + "\".</li></ul>");

        parameters.get(SAVE_FILE_NAME)
                .setDescription("Name to save the results file with if saving results files with a specific name (\""
                        + SAVE_NAME_MODE + "\" parameter).");

        parameters.get(METADATA_ITEM_FOR_GROUPING).setDescription("If \"" + EXPORT_MODE + "\" is set to \""
                + ExportModes.GROUP_BY_METADATA
                + "\", results will be grouped and saved by the value of this metadata item associated with each analysis run.  There will be one results spreadsheet for each unique value of this metadata item.");

        parameters.get(CONTINUOUS_DATA_EXPORT).setDescription(
                "When selected, the results spreadsheet(s) can be exported at intervals during a multi-analysis run.  They will be exported every N runs, where N is controlled by the \""
                        + SAVE_EVERY_N
                        + "\" parameter.  The spreadsheet(s) will still be stored when all analysis runs have completed.");

        parameters.get(SAVE_EVERY_N).setDescription("If \"" + CONTINUOUS_DATA_EXPORT
                + "\" is enabled, the current version of the spreadsheet will be exported after every N analysis runs.  This means if the analysis fails or the computer crashes, results collected so far are not lost.");

        parameters.get(APPEND_DATETIME_MODE).setDescription(
                "Controls under what conditions the time and date will be appended on to the end of the results file filename.  This can be used to prevent accidental over-writing of results files from previous runs:<br><ul>"

                        + "<li>\"" + AppendDateTimeModes.ALWAYS
                        + "\" Always append the time and date on to the end of the filename.</li>"

                        + "<li>\"" + AppendDateTimeModes.IF_FILE_EXISTS
                        + "\" Only append the time and date if the results file already exists.</li>"

                        + "<li>\"" + AppendDateTimeModes.NEVER
                        + "\" Never append time and date (unless the file is open and unwritable).</li></ul>");

        parameters.get(EXPORT_SUMMARY).setDescription(
                "When selected, a summary sheet will be added to the results spreadsheet.  The summary sheet contains either (1) one row per input image file, (2) one row per timepoint per image file or (3) one row per metadata item.  The export mode is controlled by the \""
                        + SUMMARY_MODE
                        + "\" parameter.  This sheet is given the name \"SUMMARY\" and contains statistics for measurements (mean, min, max, standard deviation and sum), object counts and metadata items.");

        parameters.get(SUMMARY_MODE).setDescription("Controls the form of the summary sheet (if \"" + EXPORT_SUMMARY
                + "\" is selected):<br><ul>"

                + "<li>\"" + SummaryModes.AVERAGE_PER_TIMEPOINT
                + "\" Each timepoint of each input image file is summarised in a separate row.</li>"

                + "<li>\"" + SummaryModes.GROUP_BY_METADATA
                + "\" All files matching a specific metadata value are averaged and summarised in a separate row (i.e. one row per unique metadata item).</li>"

                + "<li>\"" + SummaryModes.ONE_AVERAGE_PER_FILE
                + "\" Each input image file is summarised in a separate row.</li></ul>");

        parameters.get(METADATA_ITEM_FOR_SUMMARY)
                .setDescription("The metadata item to group the rows of the summary sheet by if \"" + SUMMARY_MODE
                        + "\" is set to \"" + SummaryModes.GROUP_BY_METADATA + "\".");

        parameters.get(SHOW_OBJECT_COUNTS).setDescription(
                "When selected, the \"Summary\" results sheet displays columns reporting the number of objects per object collection.");

        parameters.get(EXPORT_INDIVIDUAL_OBJECTS).setDescription(
                "When selected, individual results sheets will be created for each object collection.  In these sheets, each object in that collection is summarised per row.  The individual object sheets have the same name as their corresponding object collection.");

    }
}
