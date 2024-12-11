package io.github.mianalysis.mia.module.inputoutput;

import java.io.File;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.process.ImageConverter;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.inputoutput.ImageSaver.ChannelModes;
import io.github.mianalysis.mia.module.inputoutput.ImageSaver.CompressionModes;
import io.github.mianalysis.mia.module.inputoutput.ImageSaver.FileFormats;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver.AppendDateTimeModes;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.metadata.Metadata;
import io.github.mianalysis.mia.object.metadata.MetadataI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.imagej.IntensityMinMax;

/**
 * Created by sc13967 on 26/06/2017.
 */

/**
* Save an image/stack from the workspace to file.  Output file locations and filenames are generated from metadata items and fixed values.  This gives greater flexibility to output image locations than the standard "Save image" module.  To prevent overwriting of previously-saved files, the current date and time can be appended to the end of each filename.  Images can be saved in a variety of formats (AVI, TIF and Zipped TIF).
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class GenericImageSaver extends Module {

	/**
	* 
	*/
    public static final String LOADER_SEPARATOR = "Image saving";

	/**
	* Image to be saved to file.
	*/
    public static final String INPUT_IMAGE = "Input image";


	/**
	* 
	*/
    public static final String APPEND_DATETIME_MODE = "Append date/time mode";


	/**
	* Format for a generic filename.  Plain text can be mixed with global variables or metadata values currently stored in the workspace.  Global variables are specified using the "V{name}" notation, where "name" is the name of the variable to insert.  Similarly, metadata values are specified with the "M{name}" notation.
	*/
    public static final String GENERIC_FORMAT = "Generic format";

	/**
	* List of the currently-available metadata values for this workspace.  These can be used when compiling a generic filename.
	*/
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";


	/**
	* 
	*/
    public static final String FORMAT_SEPARATOR = "Output image format";

	/**
	* The format the output image will be saved as:<br><ul><li>"AVI" Video written using the stock ImageJ "<a href="https://github.com/imagej/imagej1/blob/master/ij/plugin/filter/AVI_Writer.java">AVI Writer</a>".  Videos can use different compression algorithms specified using "Compression mode".  Framerate specified by "Frame rate (fps)" parameter.</li><li>"TIF" Standard multidimensional (multi-page) TIF image saving.</li><li>"ZIP" TIF images stored using ZIP compression.  For images with large homogeneous regions of pixel intensity this can greatly reduce file size in a lossless manner.  Zipped images can be read directly back into ImageJ/Fiji without the need for prior decompression.</li></ul>
	*/
    public static final String FILE_FORMAT = "File format";

	/**
	* Control whether saved images should be in ImageJ "Composite" (display all channels simultaneously) or "Color" (display one channel at a time) mode.
	*/
    public static final String CHANNEL_MODE = "Channel mode";

	/**
	* Convert images to RGB prior to saving.  This is useful for displaying multi-channel images to a format that can be easily viewed outside ImageJ.
	*/
    public static final String SAVE_AS_RGB = "Save as RGB";

	/**
	* Compression mode used when saving AVI videos ("File format" parameter):<br><ul><li>"JPEG" Lossy video compression with quality specified by "Quality (0-100)" parameter.  This option is good when reducing video size is more important than retaining perfect image quality.</li><li>"None" Frames are stored in their raw format (uncompressed).  This gives the highest quality, but also the largest file size.</li><li>"PNG" PNG video compression.</li></ul>
	*/
    public static final String COMPRESSION_MODE = "Compression mode";
    public static final String QUALITY = "Quality (0-100)";
    public static final String FRAME_RATE = "Frame rate (fps)";

	/**
	* Flatten any overlay elements onto the image prior to saving.
	*/
    public static final String FLATTEN_OVERLAY = "Flatten overlay";

    public GenericImageSaver(Modules modules) {
        super("Save image (generic)", modules);
        deprecated = true;
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
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Save an image/stack from the workspace to file.  Output file locations and filenames are generated from metadata items and fixed values.  This gives greater flexibility to output image locations than the standard \""
                + new ImageSaver(null).getName()
                + "\" module.  To prevent overwriting of previously-saved files, the current date and time can be appended to the end of each filename.  Images can be saved in a variety of formats (AVI, TIF and Zipped TIF).";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String genericFormat = parameters.getValue(GENERIC_FORMAT, workspace);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
        String fileFormat = parameters.getValue(FILE_FORMAT, workspace);
        String channelMode = parameters.getValue(CHANNEL_MODE, workspace);
        boolean flattenOverlay = parameters.getValue(FLATTEN_OVERLAY, workspace);
        String compressionMode = parameters.getValue(COMPRESSION_MODE, workspace);
        int quality = parameters.getValue(QUALITY, workspace);
        int frameRate = parameters.getValue(FRAME_RATE, workspace);
        boolean saveAsRGB = parameters.getValue(SAVE_AS_RGB, workspace);

        // Loading the image to save
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        MetadataI metadata = workspace.getMetadata();
        String path = metadata.insertMetadataValues(genericFormat);
        path = AbstractSaver.appendDateTime(path, appendDateTimeMode);

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
                ImageSaver.saveVideo(inputImagePlus, compressionMode, frameRate, quality, path);
                break;
            case FileFormats.TIF:
            case FileFormats.ZIP:
                ImageSaver.saveImage(inputImagePlus, fileFormat, path);
                break;
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new StringP(GENERIC_FORMAT, this));
        parameters.add(new MessageP(AVAILABLE_METADATA_FIELDS, this, ParameterState.MESSAGE, 170));
        parameters.add(new ChoiceP(APPEND_DATETIME_MODE, this, AppendDateTimeModes.NEVER, AppendDateTimeModes.ALL));

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
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(GENERIC_FORMAT));
        returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
        metadataRefs = modules.getMetadataRefs(this);
        parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());
        returnedParameters.add(parameters.getParameter(APPEND_DATETIME_MODE));

        returnedParameters.add(parameters.getParameter(FORMAT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILE_FORMAT));

        switch ((String) parameters.getValue(FILE_FORMAT, workspace)) {
            case FileFormats.TIF:
            case FileFormats.ZIP:
                returnedParameters.add(parameters.getParameter(CHANNEL_MODE));
                returnedParameters.add(parameters.getParameter(FLATTEN_OVERLAY));
                if (!((boolean) parameters.getValue(FLATTEN_OVERLAY, workspace))) {
                    returnedParameters.add(parameters.getParameter(SAVE_AS_RGB));
                }

                break;

            case FileFormats.AVI:
                returnedParameters.add(parameters.getParameter(COMPRESSION_MODE));
                if ((boolean) parameters.getValue(COMPRESSION_MODE, workspace).equals(CompressionModes.JPEG)) {
                    returnedParameters.add(parameters.getParameter(QUALITY));
                }
                returnedParameters.add(parameters.getParameter(FRAME_RATE));
                returnedParameters.add(parameters.getParameter(FLATTEN_OVERLAY));
                break;
        }
        
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
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
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
        parameters.get(INPUT_IMAGE).setDescription("Image to be saved to file.");

        parameters.get(FILE_FORMAT).setDescription("The format the output image will be saved as:<br><ul>"

                + "<li>\"" + FileFormats.AVI
                + "\" Video written using the stock ImageJ \"<a href=\"https://github.com/imagej/imagej1/blob/master/ij/plugin/filter/AVI_Writer.java\">AVI Writer</a>\".  Videos can use different compression algorithms specified using \""
                + COMPRESSION_MODE + "\".  Framerate specified by \"" + FRAME_RATE + "\" parameter.</li>"

                + "<li>\"" + FileFormats.TIF + "\" Standard multidimensional (multi-page) TIF image saving.</li>"

                + "<li>\"" + FileFormats.ZIP
                + "\" TIF images stored using ZIP compression.  For images with large homogeneous regions of pixel intensity this can greatly reduce file size in a lossless manner.  Zipped images can be read directly back into ImageJ/Fiji without the need for prior decompression.</li></ul>");

        parameters.get(CHANNEL_MODE).setDescription(
                "Control whether saved images should be in ImageJ \"Composite\" (display all channels simultaneously) or \"Color\" (display one channel at a time) mode.");

        parameters.get(SAVE_AS_RGB).setDescription(
                "Convert images to RGB prior to saving.  This is useful for displaying multi-channel images to a format that can be easily viewed outside ImageJ.");

        parameters.get(COMPRESSION_MODE).setDescription("Compression mode used when saving AVI videos (\"" + FILE_FORMAT
                + "\" parameter):<br><ul>"

                + "<li>\"" + CompressionModes.JPEG + "\" Lossy video compression with quality specified by \"" + QUALITY
                + "\" parameter.  This option is good when reducing video size is more important than retaining perfect image quality.</li>"

                + "<li>\"" + CompressionModes.NONE
                + "\" Frames are stored in their raw format (uncompressed).  This gives the highest quality, but also the largest file size.</li>"

                + "<li>\"" + CompressionModes.PNG + "\" PNG video compression.</li></ul>");

        parameters.get(QUALITY).setDescription(
                "Quality of output JPEG-compressed video (values in range 0-100).  For reference, saving AVIs via ImageJ's \"File > Save As...\" menu uses a quality of 90.");

        parameters.get(FRAME_RATE).setDescription("Output video framerate (frames per second).");

        parameters.get(FLATTEN_OVERLAY).setDescription("Flatten any overlay elements onto the image prior to saving.");

        parameters.get(GENERIC_FORMAT).setDescription(
                "Format for a generic filename.  Plain text can be mixed with global variables or metadata values currently stored in the workspace.  Global variables are specified using the \"V{name}\" notation, where \"name\" is the name of the variable to insert.  Similarly, metadata values are specified with the \"M{name}\" notation.");

        parameters.get(AVAILABLE_METADATA_FIELDS).setDescription(
                "List of the currently-available metadata values for this workspace.  These can be used when compiling a generic filename.");

    }
}
