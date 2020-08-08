package wbif.sjx.MIA.Module.Hidden;

import java.io.File;
import java.util.LinkedHashMap;

import org.apache.commons.io.FilenameUtils;

import ij.IJ;
import ij.macro.CustomInterpreter;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Macro.MacroHandler;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Miscellaneous.Macros.CoreMacroRunner;
import wbif.sjx.MIA.Module.Miscellaneous.Macros.RunMacroOnImage.MacroModes;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.FilePathP;
import wbif.sjx.MIA.Object.Parameters.FolderPathP;
import wbif.sjx.MIA.Object.Parameters.GenericButtonP;
import wbif.sjx.MIA.Object.Parameters.MetadataItemP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.Parameters.Text.TextAreaP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;
import wbif.sjx.common.Object.Metadata;

/**
* Created by Stephen on 29/07/2017.
*/
public class OutputControl extends Module {
    public static final String POSTPROCESSING_SEPARATOR = "Post-processing";
    public static final String RUN_MACRO = "Run macro";
    public static final String VARIABLE_NAME = "Variable name";
    public static final String VARIABLE_VALUE = "Variable value";
    public static final String ADD_VARIABLE = "Add variable";
    public static final String MACRO_MODE = "Macro mode";
    public static final String MACRO_TEXT = "Macro text";
    public static final String MACRO_FILE = "Macro file";
    public static final String REFRESH_BUTTON = "Refresh parameters";
    public static final String TEST_BUTTON = "Test macro";
    
    public static final String EXPORT_SEPARATOR = "Core export controls";
    public static final String EXPORT_MODE = "Export mode";
    public static final String GROUP_SAVE_LOCATION = "Group save location";
    public static final String INDIVIDUAL_SAVE_LOCATION = "Individual save location";
    public static final String MIRRORED_DIRECTORY_ROOT = "Mirrored directory root";
    public static final String SAVE_FILE_PATH = "File path";
    public static final String SAVE_NAME_MODE = "Save name mode";
    public static final String SAVE_FILE_NAME = "File name";
    public static final String METADATA_ITEM_FOR_GROUPING = "Metadata item for grouping";
    public static final String CONTINUOUS_DATA_EXPORT = "Continuous data export";
    public static final String SAVE_EVERY_N = "Save every n files";
    public static final String APPEND_DATETIME_MODE = "Append date/time mode";
    
    public static final String SUMMARY_SEPARATOR = "Summary sheets";
    public static final String EXPORT_SUMMARY = "Export summary";
    public static final String SUMMARY_MODE = "Summary mode";
    public static final String METADATA_ITEM_FOR_SUMMARY = "Metadata item for summary";
    public static final String SHOW_OBJECT_COUNTS = "Show object counts";
    public static final String EXPORT_INDIVIDUAL_OBJECTS = "Export individual objects";
    
    public static final String MEASUREMENT_SEPARATOR = "Measurement selection";
    
    public OutputControl(ModuleCollection modules) {
        super("Output control",modules);
    }
    
    
    public interface IndividualSaveLocations {
        String MIRRORED_DIRECTORY = "Mirrored directory";
        String SAVE_WITH_INPUT = "Save with input file";
        String SPECIFIC_LOCATION = "Specific location";
        
        String[] ALL = new String[]{MIRRORED_DIRECTORY,SAVE_WITH_INPUT,SPECIFIC_LOCATION};
        
    }
    
    public interface GroupSaveLocations {
        String SAVE_WITH_INPUT = "Save with input file";
        String SPECIFIC_LOCATION = "Specific location";
        
        String[] ALL = new String[]{SAVE_WITH_INPUT,SPECIFIC_LOCATION};
        
    }
    
    public interface SaveNameModes {
        String MATCH_INPUT = "Match input file/folder name";
        String SPECIFIC_NAME = "Specific name";
        
        String[] ALL = new String[]{MATCH_INPUT, SPECIFIC_NAME};
        
    }
    
    public interface ExportModes {
        String ALL_TOGETHER = "All together";
        String GROUP_BY_METADATA = "Group by metadata";
        String INDIVIDUAL_FILES = "Individual files";
        String NONE = "None";
        
        String[] ALL = new String[]{ALL_TOGETHER,GROUP_BY_METADATA,INDIVIDUAL_FILES,NONE};
        
    }
    
    public interface SummaryModes {
        String ONE_AVERAGE_PER_FILE = "Per input file";
        String AVERAGE_PER_TIMEPOINT = "Per timepoint per input file";
        String GROUP_BY_METADATA = "Group by metadata";
        
        String[] ALL = new String[]{ONE_AVERAGE_PER_FILE,AVERAGE_PER_TIMEPOINT,GROUP_BY_METADATA};
        
    }
    
    public interface AppendDateTimeModes {
        String ALWAYS = "Always";
        String IF_FILE_EXISTS = "If file exists";
        String NEVER = "Never";
        
        String[] ALL = new String[]{ALWAYS,IF_FILE_EXISTS, NEVER};
        
    }
    
    public boolean isExportIndividual() {
        return parameters.getValue(EXPORT_MODE).equals(ExportModes.INDIVIDUAL_FILES);
    }
    
    public boolean isExportAllTogether() {
        return parameters.getValue(EXPORT_MODE).equals(ExportModes.ALL_TOGETHER);
    }
    
    public boolean isExportGroupedByMetadata() {
        return parameters.getValue(EXPORT_MODE).equals(ExportModes.GROUP_BY_METADATA);
    }
    
    public String getIndividualOutputPath(Metadata metadata) {
        String mirroredRoot = getParameterValue(MIRRORED_DIRECTORY_ROOT);
        String saveLocation = getParameterValue(INDIVIDUAL_SAVE_LOCATION);
        String saveFilePath = getParameterValue(SAVE_FILE_PATH);
        
        File inputFile = metadata.getFile();
        
        // Determining the file path
        String path = "";
        switch (saveLocation) {
            case IndividualSaveLocations.MIRRORED_DIRECTORY:
            path = getMirroredDirectory(modules.getInputControl().getRootFile(),metadata,mirroredRoot);
            break;
            
            case IndividualSaveLocations.SAVE_WITH_INPUT:
            if (inputFile.isFile()) path = inputFile.getParent() + MIA.getSlashes();
            else path = inputFile.getAbsolutePath() + MIA.getSlashes();
            break;
            
            case IndividualSaveLocations.SPECIFIC_LOCATION:
            path = saveFilePath + MIA.getSlashes();
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
        for (int i=0;i<fileDepth;i++) {
            sb.insert(0,parentFile.getName()+MIA.getSlashes());
            parentFile = parentFile.getParentFile();
        }
        
        new File(mirroredDirectoryRoot + MIA.getSlashes() +sb).mkdirs();
        
        return mirroredDirectoryRoot + MIA.getSlashes() +sb;
        
    }
    
    public String getGroupOutputPath(File inputFile) {
        String saveLocation = getParameterValue(GROUP_SAVE_LOCATION);
        String saveFilePath = getParameterValue(SAVE_FILE_PATH);
        
        // Determining the file path
        String path = "";
        switch (saveLocation) {
            case GroupSaveLocations.SAVE_WITH_INPUT:
            if (inputFile.isFile()) path = inputFile.getParent() + MIA.getSlashes();
            else path = inputFile.getAbsolutePath() + MIA.getSlashes();
            break;
            
            case GroupSaveLocations.SPECIFIC_LOCATION:
            path = saveFilePath + MIA.getSlashes();
            break;
        }
        
        String suffix = getOutputSuffix();
        String name = getOutputFilename(inputFile);
        
        return path + name + suffix;
        
    }
    
    String getOutputFilename(File inputFile) {
        String saveNameMode = getParameterValue(SAVE_NAME_MODE);
        String saveFileName = getParameterValue(SAVE_FILE_NAME);
        
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
        String seriesMode = modules.getInputControl().getParameterValue(InputControl.SERIES_MODE);
        String seriesList = modules.getInputControl().getParameterValue(InputControl.SERIES_LIST);
        
        // Determining the suffix
        String suffix = "";
        if (seriesMode.equals(InputControl.SeriesModes.SERIES_LIST)) {
            suffix = "_S" + seriesList.replace(" ", "");
        }
        
        return suffix;
        
    }
    
    public void runMacro(Workspace workspace) {
        if (!(boolean) parameters.getValue(RUN_MACRO)) {
            return;
        }
        
        MIA.log.writeStatus("Running post-processing macro");
        String macroMode = parameters.getValue(MACRO_MODE);
        String macroText = parameters.getValue(MACRO_TEXT);
        String macroFile = parameters.getValue(MACRO_FILE);
        
        // Getting a Map of input variable names and their values
        ParameterGroup variableGroup = parameters.getParameter(ADD_VARIABLE);
        LinkedHashMap<String, String> inputVariables = CoreMacroRunner.inputVariables(variableGroup, VARIABLE_NAME,
        VARIABLE_VALUE);
        
        // Setting the MacroHandler to the current workspace
        MacroHandler.setWorkspace(workspace);
        MacroHandler.setModules(modules);
        
        // If the macro is stored as a file, load this to the macroText string
        if (macroMode.equals(MacroModes.MACRO_FILE)) macroText = IJ.openAsString(macroFile);
        
        // Appending variables to the front of the macro
        String finalMacroText = CoreMacroRunner.addVariables(macroText, inputVariables);
        
        // Running the macro
        CustomInterpreter interpreter = new CustomInterpreter();
        try {
            interpreter.runBatchMacro(finalMacroText, null);
            if (interpreter.wasError())
            throw new RuntimeException();
        } catch (RuntimeException e) {
            IJ.runMacro("setBatchMode(false)");
            MIA.log.writeError(
            "Macro failed with error \"" + interpreter.getErrorMessage() + "\".  Skipping file.");
        }
    }
    
    @Override
    public String getPackageName() {
        return "Hidden";
    }
    
    @Override
    public String getDescription() {
        return "";
    }
    
    @Override
    public Status process(Workspace workspace) {
        return Status.PASS;
    }
    
    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(POSTPROCESSING_SEPARATOR, this));
        
        parameters.add(new BooleanP(RUN_MACRO,this,false));
        ParameterCollection variableCollection = new ParameterCollection();
        variableCollection.add(new StringP(VARIABLE_NAME,this));
        variableCollection.add(new StringP(VARIABLE_VALUE,this));
        parameters.add(new ParameterGroup(ADD_VARIABLE,this,variableCollection));
        parameters.add(new ChoiceP(MACRO_MODE,this,MacroModes.MACRO_TEXT,MacroModes.ALL));
        parameters.add(new TextAreaP(MACRO_TEXT,this,"run(\"Enable MIA Extensions\");\n\n// Get a list of Workspace IDs with Ext.MIA_GetListOfWorkspaceIDs() and set active workspace with Ext.MIA_SetActiveWorkspace(ID).",true));
        parameters.add(new FilePathP(MACRO_FILE, this));
        parameters.add(new GenericButtonP(REFRESH_BUTTON, this, "Refresh", GenericButtonP.DefaultModes.REFRESH));
        parameters.add(new GenericButtonP(TEST_BUTTON, this, "Test macro", GenericButtonP.DefaultModes.TEST_MACRO));
        
        parameters.add(new ParamSeparatorP(EXPORT_SEPARATOR,this));
        parameters.add(new ChoiceP(EXPORT_MODE,this,ExportModes.ALL_TOGETHER,ExportModes.ALL));
        parameters.add(new ChoiceP(INDIVIDUAL_SAVE_LOCATION, this,IndividualSaveLocations.SAVE_WITH_INPUT,IndividualSaveLocations.ALL));
        parameters.add(new ChoiceP(GROUP_SAVE_LOCATION, this,GroupSaveLocations.SAVE_WITH_INPUT,GroupSaveLocations.ALL));
        parameters.add(new FolderPathP(SAVE_FILE_PATH,this));
        parameters.add(new FolderPathP(MIRRORED_DIRECTORY_ROOT,this));
        parameters.add(new ChoiceP(SAVE_NAME_MODE, this,SaveNameModes.MATCH_INPUT,SaveNameModes.ALL));
        parameters.add(new StringP(SAVE_FILE_NAME,this));
        parameters.add(new MetadataItemP(METADATA_ITEM_FOR_GROUPING,this));
        parameters.add(new BooleanP(CONTINUOUS_DATA_EXPORT,this,false));
        parameters.add(new IntegerP(SAVE_EVERY_N,this,10));
        parameters.add(new ChoiceP(APPEND_DATETIME_MODE,this,AppendDateTimeModes.NEVER, AppendDateTimeModes.ALL));
        
        parameters.add(new ParamSeparatorP(SUMMARY_SEPARATOR,this));
        parameters.add(new BooleanP(EXPORT_SUMMARY,this,true));
        parameters.add(new ChoiceP(SUMMARY_MODE,this,SummaryModes.ONE_AVERAGE_PER_FILE,SummaryModes.ALL));
        parameters.add(new MetadataItemP(METADATA_ITEM_FOR_SUMMARY,this));
        parameters.add(new BooleanP(SHOW_OBJECT_COUNTS,this,true));
        parameters.add(new BooleanP(EXPORT_INDIVIDUAL_OBJECTS,this,true));
        
        parameters.add(new ParamSeparatorP(MEASUREMENT_SEPARATOR,this));
        
    }
    
    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        
        returnedParameters.add(parameters.getParameter(POSTPROCESSING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RUN_MACRO));
        if ((boolean) parameters.getValue(RUN_MACRO)) {
            returnedParameters.add(parameters.getParameter(ADD_VARIABLE));
            returnedParameters.add(parameters.getParameter(MACRO_MODE));
            switch ((String) parameters.getValue(MACRO_MODE)) {
                case MacroModes.MACRO_FILE:
                returnedParameters.add(parameters.getParameter(MACRO_FILE));
                break;
                case MacroModes.MACRO_TEXT:
                returnedParameters.add(parameters.getParameter(MACRO_TEXT));
                returnedParameters.add(parameters.getParameter(REFRESH_BUTTON));
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
            switch ((String) parameters.getValue(INDIVIDUAL_SAVE_LOCATION)) {
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
            switch ((String) parameters.getValue(GROUP_SAVE_LOCATION)) {
                case GroupSaveLocations.SPECIFIC_LOCATION:
                returnedParameters.add(parameters.getParameter(SAVE_FILE_PATH));
                break;
            }
            break;
        }
        
        returnedParameters.add(parameters.getParameter(SAVE_NAME_MODE));
        switch ((String) parameters.getValue(SAVE_NAME_MODE)) {
            case SaveNameModes.SPECIFIC_NAME:
            returnedParameters.add(parameters.getParameter(SAVE_FILE_NAME));
            break;
        }
        
        if (!exportMode.getValue().equals(ExportModes.INDIVIDUAL_FILES)) {
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
            switch ((String) parameters.getValue(SUMMARY_MODE)) {
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
}

