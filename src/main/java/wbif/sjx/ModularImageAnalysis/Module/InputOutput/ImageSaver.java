package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.LUT;
import org.apache.commons.io.FilenameUtils;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.Visualisation.ShowImage;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.LUTs;
import wbif.sjx.common.Process.IntensityMinMax;

import java.io.File;

/**
 * Created by sc13967 on 26/06/2017.
 */
public class ImageSaver extends HCModule {
    public static final String SAVE_IMAGE = "Save image";
    public static final String INPUT_IMAGE = "Input image";
    public static final String SAVE_LOCATION = "Save location";
    public static final String MIRROR_DIRECTORY_ROOT = "Mirrored directory root";
    public static final String SAVE_FILE_PATH = "File path";
    public static final String SAVE_SUFFIX = "Add filename suffix";
    public static final String FLATTEN_OVERLAY = "Flatten overlay";
    public static final String SHOW_IMAGE = "Show image";

    public interface SaveLocations {
        String MIRRORED_DIRECTORY = "Mirrored directory";
        String SAVE_WITH_INPUT = "Save with input file";
        String SPECIFIC_LOCATION = "Specific location";

        String[] ALL = new String[]{MIRRORED_DIRECTORY, SAVE_WITH_INPUT, SPECIFIC_LOCATION};

    }


    @Override
    public String getTitle() {
        return "Save image";
    }

    @Override
    public String getHelp() {
        return "+++INCOMPLETE+++" +
                "\n'Mirrored location' is an equivalent directory structure to the input, but based at a different root";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        boolean saveImage = parameters.getValue(SAVE_IMAGE);
        String saveLocation = parameters.getValue(SAVE_LOCATION);
        String mirroredDirectoryRoot = parameters.getValue(MIRROR_DIRECTORY_ROOT);
        String filePath = parameters.getValue(SAVE_FILE_PATH);
        String suffix = parameters.getValue(SAVE_SUFFIX);
        boolean flattenOverlay = parameters.getValue(FLATTEN_OVERLAY);

        // The save image option is there so users can toggle it
        if (!saveImage) {
            // It's still possible to display the image
            if (parameters.getValue(SHOW_IMAGE)) {
                // Loading the image to save
                Image inputImage = workspace.getImages().get(inputImageName);
                ImagePlus dispIpl = new Duplicator().run(inputImage.getImagePlus());
                IntensityMinMax.run(dispIpl,true);
                dispIpl.setLut(LUTs.Random(true));
                dispIpl.show();
            }

            return;
        }

        // Loading the image to save
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        if (flattenOverlay) {
            // Flattening overlay onto image for saving
            if (inputImagePlus.getNSlices() > 1) {
                IntensityMinMax.run(inputImagePlus,true);
                if (inputImagePlus.getOverlay() != null) inputImagePlus.flattenStack();

            } else {
                IntensityMinMax.run(inputImagePlus,false);
                if (inputImagePlus.getOverlay() != null) inputImagePlus.flatten();
            }
        }

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
                    sb.insert(0,parentFile.getName()+"\\");
                    parentFile = parentFile.getParentFile();

                }

                new File(mirroredDirectoryRoot+"\\"+sb).mkdirs();

                String path = mirroredDirectoryRoot+"\\"+sb+FilenameUtils.removeExtension(rootFile.getName());
                path = path + suffix + ".tif";
                IJ.save(inputImagePlus,path);
                break;

            case SaveLocations.SAVE_WITH_INPUT:
                rootFile = workspace.getMetadata().getFile();
                path = rootFile.getParent()+ "\\"+FilenameUtils.removeExtension(rootFile.getName());
                path = path + suffix + ".tif";
                IJ.save(inputImagePlus,path);
                break;

            case SaveLocations.SPECIFIC_LOCATION:
                path = FilenameUtils.removeExtension(filePath);
                path = path + suffix + ".tif";
                IJ.save(inputImagePlus,path);
                break;

        }

        if (parameters.getValue(SHOW_IMAGE)) {
            ImagePlus dispIpl = new Duplicator().run(inputImagePlus);
            IntensityMinMax.run(dispIpl,true);
            dispIpl.setLut(LUTs.Random(true));
            dispIpl.show();
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(SAVE_IMAGE,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(SAVE_LOCATION, Parameter.CHOICE_ARRAY,SaveLocations.MIRRORED_DIRECTORY,SaveLocations.ALL));
        parameters.add(new Parameter(MIRROR_DIRECTORY_ROOT, Parameter.FOLDER_PATH,""));
        parameters.add(new Parameter(SAVE_FILE_PATH, Parameter.FOLDER_PATH,""));
        parameters.add(new Parameter(SAVE_SUFFIX, Parameter.STRING,""));
        parameters.add(new Parameter(FLATTEN_OVERLAY, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(SHOW_IMAGE,Parameter.BOOLEAN,false));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParamters = new ParameterCollection();

        returnedParamters.add(parameters.getParameter(SAVE_IMAGE));

        if (parameters.getValue(SAVE_IMAGE)) {
            returnedParamters.add(parameters.getParameter(INPUT_IMAGE));
            returnedParamters.add(parameters.getParameter(SAVE_LOCATION));

            switch ((String) parameters.getValue(SAVE_LOCATION)) {
                case SaveLocations.SPECIFIC_LOCATION:
                    returnedParamters.add(parameters.getParameter(SAVE_FILE_PATH));
                    break;

                case SaveLocations.MIRRORED_DIRECTORY:
                    returnedParamters.add(parameters.getParameter(MIRROR_DIRECTORY_ROOT));
                    break;

            }

            returnedParamters.add(parameters.getParameter(SAVE_SUFFIX));
            returnedParamters.add(parameters.getParameter(FLATTEN_OVERLAY));

        }

        returnedParamters.add(parameters.getParameter(SHOW_IMAGE));

        return returnedParamters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
