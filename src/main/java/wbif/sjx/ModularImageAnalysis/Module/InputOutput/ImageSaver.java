package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageConverter;
import org.apache.commons.io.FilenameUtils;
import wbif.sjx.ModularImageAnalysis.MIA;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.common.Process.IntensityMinMax;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Created by sc13967 on 26/06/2017.
 */
public class ImageSaver extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String SAVE_LOCATION = "Save location";
    public static final String MIRROR_DIRECTORY_ROOT = "Mirrored directory root";
    public static final String SAVE_FILE_PATH = "File path";
    public static final String APPEND_DATETIME_MODE = "Append date/time mode";
    public static final String SAVE_SUFFIX = "Add filename suffix";
    public static final String FLATTEN_OVERLAY = "Flatten overlay";

    public interface SaveLocations {
        String MIRRORED_DIRECTORY = "Mirrored directory";
        String SAVE_WITH_INPUT = "Save with input file";
        String SPECIFIC_LOCATION = "Specific location";

        String[] ALL = new String[]{MIRRORED_DIRECTORY, SAVE_WITH_INPUT, SPECIFIC_LOCATION};

    }

    public interface AppendDateTimeModes {
        String ALWAYS = "Always";
        String IF_FILE_EXISTS = "If file exists";
        String NEVER = "Never";

        String[] ALL = new String[]{ALWAYS,IF_FILE_EXISTS, NEVER};

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
    public boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String saveLocation = parameters.getValue(SAVE_LOCATION);
        String mirroredDirectoryRoot = parameters.getValue(MIRROR_DIRECTORY_ROOT);
        String filePath = parameters.getValue(SAVE_FILE_PATH);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE);
        String suffix = parameters.getValue(SAVE_SUFFIX);
        boolean flattenOverlay = parameters.getValue(FLATTEN_OVERLAY);

        // Loading the image to save
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        if (flattenOverlay) {
            inputImagePlus = inputImagePlus.duplicate();
            new ImageConverter(inputImagePlus).convertToRGB();

            // Flattening overlay onto image for saving
            if (inputImagePlus.getNSlices() > 1) {
                IntensityMinMax.run(inputImagePlus,true);
                if (inputImagePlus.getOverlay() != null) inputImagePlus.flattenStack();
            } else {
                IntensityMinMax.run(inputImagePlus,false);
                if (inputImagePlus.getOverlay() != null) inputImagePlus = inputImagePlus.flatten();
            }
        }

        String path;
        switch (saveLocation) {
            case SaveLocations.MIRRORED_DIRECTORY:
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

                new File(mirroredDirectoryRoot+ MIA.getSlashes() +sb).mkdirs();

                path = mirroredDirectoryRoot+ MIA.getSlashes() +sb+FilenameUtils.removeExtension(rootFile.getName());
                break;

            case SaveLocations.SAVE_WITH_INPUT:
            default:
                rootFile = workspace.getMetadata().getFile();
                path = rootFile.getParent()+ MIA.getSlashes() +FilenameUtils.removeExtension(rootFile.getName());
                break;

            case SaveLocations.SPECIFIC_LOCATION:
                path = FilenameUtils.removeExtension(filePath);
                break;
        }

        // Adding last bits to name
        path = path + "_S" + workspace.getMetadata().getSeriesNumber();
        path = path + suffix + ".tif";
        path = appendDateTime(path,appendDateTimeMode);
        IJ.save(inputImagePlus,path);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new ChoiceP(SAVE_LOCATION, this,SaveLocations.SAVE_WITH_INPUT,SaveLocations.ALL));
        parameters.add(new FolderPathP(MIRROR_DIRECTORY_ROOT,this));
        parameters.add(new FolderPathP(SAVE_FILE_PATH,this));
        parameters.add(new ChoiceP(APPEND_DATETIME_MODE, this, AppendDateTimeModes.NEVER, AppendDateTimeModes.ALL));
        parameters.add(new StringP(SAVE_SUFFIX, this));
        parameters.add(new BooleanP(FLATTEN_OVERLAY, this,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

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

        returnedParameters.add(parameters.getParameter(APPEND_DATETIME_MODE));
        returnedParameters.add(parameters.getParameter(SAVE_SUFFIX));
        returnedParameters.add(parameters.getParameter(FLATTEN_OVERLAY));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}
