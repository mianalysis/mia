package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import ij.IJ;
import ij.ImagePlus;
import org.apache.commons.io.FilenameUtils;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.io.File;

/**
 * Created by sc13967 on 26/06/2017.
 */
public class ImageSaver extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String SAVE_LOCATION = "Save location";
    public static final String SAVE_FILE_PATH = "File path";
    public static final String SAVE_SUFFIX = "Add filename suffix";

    private static final String SAVE_WITH_INPUT = "Save with input file";
    private static final String SPECIFIC_LOCATION = "Specific location";
    private static final String[] SAVE_LOCATIONS = new String[]{SAVE_WITH_INPUT,SPECIFIC_LOCATION};

    @Override
    public String getTitle() {
        return "Save image";
    }

    @Override
    public String getHelp() {
        return "+++INCOMPLETE+++";
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input image
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        String saveLocation = parameters.getValue(SAVE_LOCATION);
        String filePath = parameters.getValue(SAVE_FILE_PATH);
        String suffix = parameters.getValue(SAVE_SUFFIX);

        if (saveLocation.equals(SAVE_WITH_INPUT)) {
            File rootFile = workspace.getMetadata().getFile();
            String path = rootFile.getParent()+ "\\"+FilenameUtils.removeExtension(rootFile.getName());
            path = path + suffix + ".tif";
            IJ.save(inputImagePlus,path);

        } else if (saveLocation.equals(SPECIFIC_LOCATION)) {
            String path = FilenameUtils.removeExtension(filePath);
            path = path + suffix + ".tif";
            IJ.save(inputImagePlus,path);

        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(SAVE_LOCATION,HCParameter.CHOICE_ARRAY,SAVE_LOCATIONS[0],SAVE_LOCATIONS));
        parameters.addParameter(new HCParameter(SAVE_FILE_PATH,HCParameter.FILE_PATH,null));
        parameters.addParameter(new HCParameter(SAVE_SUFFIX,HCParameter.STRING,""));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParamters = new HCParameterCollection();

        returnedParamters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParamters.addParameter(parameters.getParameter(SAVE_LOCATION));

        if (parameters.getValue(SAVE_LOCATION).equals(SPECIFIC_LOCATION)) {
            returnedParamters.addParameter(parameters.getParameter(SAVE_FILE_PATH));
        }

        returnedParamters.addParameter(parameters.getParameter(SAVE_SUFFIX));

        return returnedParamters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
