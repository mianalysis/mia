package wbif.sjx.MIA.Module.InputOutput;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageConverter;
import org.apache.commons.io.FilenameUtils;
import wbif.sjx.MIA.GUI.InputOutput.OutputControl;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;
import wbif.sjx.common.Process.IntensityMinMax;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;

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
    public static final String SAVE_AS_RGB = "Save as RGB";
    public static final String FLATTEN_OVERLAY = "Flatten overlay";


    public interface SaveLocations {
        String MIRRORED_DIRECTORY = "Mirrored directory";
        String MATCH_OUTPUT_CONTROL = "Match Output Control";
        String SAVE_WITH_INPUT = "Save with input file";
        String SPECIFIC_LOCATION = "Specific location";

        String[] ALL = new String[]{MIRRORED_DIRECTORY, MATCH_OUTPUT_CONTROL, SAVE_WITH_INPUT, SPECIFIC_LOCATION};

    }

    public interface SaveNameModes {
        String MATCH_INPUT = "Match input file name";
        String SPECIFIC_NAME = "Specific name";

        String[] ALL = new String[]{MATCH_INPUT, SPECIFIC_NAME};

    }

    public interface AppendSeriesModes {
        String NONE = "None";
        String SERIES_NAME = "Series name";
        String SERIES_NUMBER = "Series number";

        String[] ALL = new String[]{NONE,SERIES_NAME,SERIES_NUMBER};

    }

    public interface AppendDateTimeModes {
        String ALWAYS = "Always";
        String IF_FILE_EXISTS = "If file exists";
        String NEVER = "Never";

        String[] ALL = new String[]{ALWAYS,IF_FILE_EXISTS, NEVER};

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
                if (!file.exists()) return inputName;
            case AppendDateTimeModes.ALWAYS:
                String nameWithoutExtension = FilenameUtils.removeExtension(inputName);
                String extension = FilenameUtils.getExtension(inputName);
                ZonedDateTime zonedDateTime = ZonedDateTime.now();
                String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                return  nameWithoutExtension+ "_("+ dateTime + ")."+extension;
            case AppendDateTimeModes.NEVER:
            default:
                return inputName;
        }
    }

    private static String getMirroredDirectory(Workspace workspace, String mirroredDirectoryRoot) {
        File rootFile = workspace.getMetadata().getFile();
        int fileDepth;
        if (workspace.getMetadata().get("FILE_DEPTH") == null) {
            fileDepth = 0;
        } else {
            fileDepth = (int) workspace.getMetadata().get("FILE_DEPTH");
        }

        StringBuilder sb = new StringBuilder();
        File parentFile = rootFile.getParentFile();
        for (int i=0;i<fileDepth;i++) {
            sb.insert(0,parentFile.getName()+MIA.getSlashes());
            parentFile = parentFile.getParentFile();
        }

        new File(mirroredDirectoryRoot + MIA.getSlashes() +sb).mkdirs();

        return mirroredDirectoryRoot + MIA.getSlashes() +sb;

    }

    @Override
    public String getTitle() {
        return "Save image";
    }

    @Override
    public String getPackageName() {
        return PackageNames.INPUT_OUTPUT;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
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
        boolean saveAsRGB = parameters.getValue(SAVE_AS_RGB);
        boolean flattenOverlay = parameters.getValue(FLATTEN_OVERLAY);

        // Loading the image to save
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // If the image is being altered make a copy
        if (saveAsRGB || flattenOverlay) inputImagePlus = inputImagePlus.duplicate();
        if (saveAsRGB) new ImageConverter(inputImagePlus).convertToRGB();

        if (flattenOverlay) {
            // Flattening overlay onto image for saving
            if (inputImagePlus.getNSlices() > 1) {
                IntensityMinMax.run(inputImagePlus,true);
                if (inputImagePlus.getOverlay() != null) inputImagePlus.flattenStack();
            } else {
                if (inputImagePlus.getOverlay() != null) inputImagePlus = inputImagePlus.flatten();
            }
        }

        // If using the same settings as OutputControl, update saveLocation and filePath (if necessary)
        if (saveLocation.equals(SaveLocations.MATCH_OUTPUT_CONTROL)) {
            Analysis analysis = workspace.getAnalysis();
            if (analysis == null) {
                System.err.println("No analysis found attached to workspace.  Can't get output path.");
                return false;
            }

            OutputControl outputControl = analysis.getModules().getOutputControl();
            String outputSaveLocation = outputControl.getParameterValue(OutputControl.SAVE_LOCATION);
            switch (outputSaveLocation) {
                case OutputControl.SaveLocations.SAVE_WITH_INPUT:
                    saveLocation = SaveLocations.SAVE_WITH_INPUT;
                    break;

                case OutputControl.SaveLocations.SPECIFIC_LOCATION:
                    saveLocation = SaveLocations.SPECIFIC_LOCATION;
                    filePath = outputControl.getParameterValue(SAVE_FILE_PATH);
                    break;
            }
        }

        String path;
        switch (saveLocation) {
            case SaveLocations.MIRRORED_DIRECTORY:
                path = getMirroredDirectory(workspace,mirroredDirectoryRoot);
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
        path = appendSeries(path,workspace,appendSeriesMode);
        path = path + suffix + ".tif";
        path = appendDateTime(path,appendDateTimeMode);
        IJ.save(inputImagePlus,path);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(LOADER_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new ChoiceP(SAVE_LOCATION, this,SaveLocations.SAVE_WITH_INPUT,SaveLocations.ALL));
        parameters.add(new FolderPathP(MIRROR_DIRECTORY_ROOT,this));
        parameters.add(new FolderPathP(SAVE_FILE_PATH,this));

        parameters.add(new ParamSeparatorP(NAME_SEPARATOR,this));
        parameters.add(new ChoiceP(SAVE_NAME_MODE, this,SaveNameModes.MATCH_INPUT,SaveNameModes.ALL));
        parameters.add(new StringP(SAVE_FILE_NAME,this));
        parameters.add(new ChoiceP(APPEND_SERIES_MODE, this, AppendSeriesModes.SERIES_NUMBER, AppendSeriesModes.ALL));
        parameters.add(new ChoiceP(APPEND_DATETIME_MODE, this, AppendDateTimeModes.NEVER, AppendDateTimeModes.ALL));
        parameters.add(new StringP(SAVE_SUFFIX, this));

        parameters.add(new ParamSeparatorP(FORMAT_SEPARATOR,this));
        parameters.add(new BooleanP(SAVE_AS_RGB, this,false));
        parameters.add(new BooleanP(FLATTEN_OVERLAY, this,false));

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
        returnedParameters.add(parameters.getParameter(SAVE_AS_RGB));
        if (parameters.getValue(SAVE_AS_RGB)) {
            returnedParameters.add(parameters.getParameter(FLATTEN_OVERLAY));
        }

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs(ModuleCollection modules) {
        return objectMeasurementRefs;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

}
