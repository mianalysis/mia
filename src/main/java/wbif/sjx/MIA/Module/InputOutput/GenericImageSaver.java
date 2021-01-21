package wbif.sjx.MIA.Module.InputOutput;

import java.io.File;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.process.ImageConverter;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.Parameters.Text.TextAreaP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Object.Metadata;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 26/06/2017.
 */
public class GenericImageSaver extends AbstractImageSaver {
    public static final String GENERIC_FORMAT = "Generic format";
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";

    public static final String FORMAT_SEPARATOR = "Output image format";
    public static final String FILE_FORMAT = "File format";
    public static final String CHANNEL_MODE = "Channel mode";
    public static final String SAVE_AS_RGB = "Save as RGB";
    public static final String COMPRESSION_MODE = "Compression mode";
    public static final String QUALITY = "Quality (0-100)";
    public static final String FRAME_RATE = "Frame rate (fps)";
    public static final String FLATTEN_OVERLAY = "Flatten overlay";

    public GenericImageSaver(ModuleCollection modules) {
        super("Save image (generic)", modules);
    }


    public static void makeDirectories(String path) {
        File file = new File(path);
        File directory = file.getParentFile();

        if (!directory.exists()) 
            directory.mkdirs();

    }
    

    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
    }

    @Override
    public String getDescription() {
        return "Save an image/stack from the workspace to file.  Output file locations and filenames are generated from metadata items and fixed values.  This gives greater flexibility to output image locations than the standard \""+ new ImageSaver(null).getName() +"\" module.  To prevent overwriting of previously-saved files, the current date and time can be appended to the end of each filename.  Images can be saved in a variety of formats (AVI, TIF and Zipped TIF).";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String genericFormat = parameters.getValue(GENERIC_FORMAT);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE);
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

        Metadata metadata = workspace.getMetadata();
        String path = metadata.insertMetadataValues(genericFormat);
        path = appendDateTime(path, appendDateTimeMode);

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

        // Ensuring the output folder exists
        makeDirectories(path);

        switch (fileFormat) {
            case FileFormats.AVI:
                saveVideo(inputImagePlus, compressionMode, frameRate, quality, path);
                break;
            case FileFormats.TIF:
            case FileFormats.ZIP:
                saveImage(inputImagePlus, fileFormat, path);
                break;
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();
        
        parameters.add(new StringP(GENERIC_FORMAT, this));
        parameters.add(new TextAreaP(AVAILABLE_METADATA_FIELDS, this, false));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(GENERIC_FORMAT));
        returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
        metadataRefs = modules.getMetadataRefs(this);
        parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());
        returnedParameters.add(parameters.getParameter(APPEND_DATETIME_MODE));

        returnedParameters.addAll(super.updateAndGetParameters());

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
        super.addParameterDescriptions();

        parameters.get(GENERIC_FORMAT).setDescription(
                "Format for a generic filename.  Plain text can be mixed with global variables or metadata values currently stored in the workspace.  Global variables are specified using the \"V{name}\" notation, where \"name\" is the name of the variable to insert.  Similarly, metadata values are specified with the \"M{name}\" notation.");

        parameters.get(AVAILABLE_METADATA_FIELDS).setDescription(
                "List of the currently-available metadata values for this workspace.  These can be used when compiling a generic filename.");
        
    }
}
