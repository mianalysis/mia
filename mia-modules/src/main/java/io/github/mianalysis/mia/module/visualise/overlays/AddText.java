package io.github.mianalysis.mia.module.visualise.overlays;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.gui.TextRoi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
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
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.mianalysis.mia.process.string.CommaSeparatedStringInterpreter;
import ome.units.UNITS;


/**
* Adds an overlay to the specified input image showing a fixed text label.  Slice and frame indices can be dynamically inserted into the text using keywords.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class AddText extends AbstractOverlay {
    TextRoi textRoi = null;

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/output";

	/**
	* Image onto which overlay will be rendered.  Input image will only be updated if "Apply to input image" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by "Output image".
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* Image onto which overlay will be rendered.  Input image will only be updated if "Apply to input image" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by "Output image".
	*/
    public static final String APPLY_TO_INPUT = "Apply to input image";

	/**
	* If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.
	*/
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";

	/**
	* The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).
	*/
    public static final String OUTPUT_IMAGE = "Output image";


	/**
	* 
	*/
    public static final String RENDERING_SEPARATOR = "Overlay rendering";

	/**
	* Fixed text to be displayed.  The current slice and frame numbers can be inserted using "D{SLICE}" and "D{FRAME}".  Similarly, it's possible to insert the elapsed frame time in the form "T{HH:mm:ss.SSS}" (where this example would give hours:minutes:seconds.millis).  The full description of supported time values can be found <a href="https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html">here</a>.
	*/
    public static final String TEXT = "Text";

	/**
	* 
	*/
    public static final String DYNAMIC_VALUES = "Available dynamic values";

	/**
	* Horizontal location of the text to be displayed.  Specified in pixel units relative to the left of the image (x=0).
	*/
    public static final String X_POSITION = "X-position";

	/**
	* Vertical location of the text to be displayed.  Specified in pixel units relative to the top of the image (y=0).
	*/
    public static final String Y_POSITION = "Y-position";

	/**
	* Z-slices on which to display the text.  This is specified as a comma-separated list of slice indices.  The keyword "end" is used to denote the final slice in the stack and will be interpreted automatically.  Accepted formats are "[VALUE]" for a single index, "[RANGE START]-[RANGE END]" for a complete range of indices and "[RANGE START]-[RANGE-END]-[INTERVAL]" for indices evenly spaced at the specified interval.
	*/
    public static final String Z_RANGE = "Z-range";

	/**
	* Frames on which to display the text.  This is specified as a comma-separated list of frame indices.  The keyword "end" is used to denote the final frame in the stack and will be interpreted automatically.  Accepted formats are "[VALUE]" for a single index, "[RANGE START]-[RANGE END]" for a complete range of indices and "[RANGE START]-[RANGE-END]-[INTERVAL]" for indices evenly spaced at the specified interval.
	*/
    public static final String FRAME_RANGE = "Frame-range";

	/**
	* When selected, text will be centred on the specified XY coordinate.  Otherwise, text will be based with its top-left corner at the specified coordinate.
	*/
    public static final String CENTRE_TEXT = "Centre text";

	/**
	* Font size of the text label.
	*/
    public static final String LABEL_SIZE = "Label size";

	/**
	* Colour of the text label.  Choices are: White, Black, Red, Orange, Yellow, Green, Cyan, Blue, Violet, Magenta.
	*/
    public static final String LABEL_COLOUR = "Label colour";

    public AddText(Modules modules) {
        super("Add text", modules);
    }

    public static void addOverlay(ImagePlus ipl, String text, Color color, int labelSize, double opacity, int xPosition,
            int yPosition, int[] zRange, int[] frameRange, boolean centreText) {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
        // be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1))
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());

        // Getting frame interval in second units
        long frameIntervalMs = TemporalUnit.getOMEUnit()
                .convertValue(ipl.getCalibration().frameInterval, UNITS.MILLISECOND).longValue();
        
        for (int z : zRange) {
            for (int f : frameRange) {
                String replacedText = replaceDynamicValues(text, f, z);
                replacedText = replaceTimeValues(replacedText, frameIntervalMs, f);
                double[] location = new double[] { xPosition, yPosition, z };
                AddLabels.addOverlay(ipl, replacedText, location, f, color, labelSize, centreText);
            }
        }
    }

    public static String replaceDynamicValues(String text, int f, int z) {
        text = text.replace("D{FRAME}", String.valueOf(f));
        return text.replace("D{SLICE}", String.valueOf(z));

    }

    public static String replaceTimeValues(String text, long frameIntervalMs, int f) {
        Pattern pattern = Pattern.compile("T\\{([^\\{}]+)}");
        Matcher matcher = pattern.matcher(text);

        if (matcher.matches()) {
            String format = matcher.group(1);

            String formatted = DurationFormatUtils.formatDuration(frameIntervalMs * (f-1), format);
            text = text.replace("T{" + format + "}", formatted);
        }

        return text;

    }

    @Override
    public Category getCategory() {
        return Categories.VISUALISATION_OVERLAYS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Adds an overlay to the specified input image showing a fixed text label.  Slice and frame indices can be dynamically inserted into the text using keywords.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        ImageI inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting label settings
        double opacity = parameters.getValue(OPACITY, workspace);
        String text = parameters.getValue(TEXT, workspace);
        int xPosition = parameters.getValue(X_POSITION, workspace);
        int yPosition = parameters.getValue(Y_POSITION, workspace);
        String zRangeString = parameters.getValue(Z_RANGE, workspace);
        String frameRangeString = parameters.getValue(FRAME_RANGE, workspace);
        int labelSize = parameters.getValue(LABEL_SIZE, workspace);
        boolean centreText = parameters.getValue(CENTRE_TEXT, workspace);
        String labelColour = parameters.getValue(LABEL_COLOUR, workspace);
        Color color = ColourFactory.getColour(labelColour);

        // Only add output to workspace if not applying to input
        if (applyToInput)
            addOutputToWorkspace = false;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
            ipl = new Duplicator().run(ipl);

        // Converting slice and frame ranges to numbers
        int[] zRange = CommaSeparatedStringInterpreter.interpretIntegers(zRangeString, true, ipl.getNSlices());
        int[] frameRange = CommaSeparatedStringInterpreter.interpretIntegers(frameRangeString, true, ipl.getNFrames());

        addOverlay(ipl, text, color, labelSize, opacity, xPosition, yPosition, zRange, frameRange, centreText);

        ImageI outputImage = ImageFactory.createImage(outputImageName, ipl);

        // If necessary, adding output image to workspace. This also allows us to show
        // it.
        if (addOutputToWorkspace)
            workspace.addImage(outputImage);
        if (showOutput)
            outputImage.show();

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(RENDERING_SEPARATOR, this));
        parameters.add(new StringP(TEXT, this));
        parameters.add(new MessageP(DYNAMIC_VALUES, this,
                "The current slice and/or frame can be inserted into the rendered text by including one of the following: D{FRAME}, D{SLICE}.  Elapsed time can be inserted in the form T{HH:mm:ss.SSS}.",
                ParameterState.MESSAGE));
        parameters.add(new IntegerP(X_POSITION, this, 0));
        parameters.add(new IntegerP(Y_POSITION, this, 0));
        parameters.add(new StringP(Z_RANGE, this, "1-end"));
        parameters.add(new StringP(FRAME_RANGE, this, "1-end"));
        parameters.add(new BooleanP(CENTRE_TEXT, this, false));
        parameters.add(new IntegerP(LABEL_SIZE, this, 8));
        parameters.add(new ChoiceP(LABEL_COLOUR, this, SingleColours.BLACK, SingleColours.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE, workspace)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

            }
        }

        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(TEXT));
        returnedParameters.add(parameters.getParameter(DYNAMIC_VALUES));
        returnedParameters.add(parameters.getParameter(X_POSITION));
        returnedParameters.add(parameters.getParameter(Y_POSITION));
        returnedParameters.add(parameters.getParameter(Z_RANGE));
        returnedParameters.add(parameters.getParameter(FRAME_RANGE));
        returnedParameters.add(parameters.getParameter(CENTRE_TEXT));
        returnedParameters.add(parameters.getParameter(LABEL_SIZE));
        returnedParameters.add(parameters.getParameter(LABEL_COLOUR));

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

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(INPUT_IMAGE)
                .setDescription("Image onto which overlay will be rendered.  Input image will only be updated if \""
                        + APPLY_TO_INPUT
                        + "\" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by \""
                        + OUTPUT_IMAGE + "\".");

        parameters.get(APPLY_TO_INPUT)
                .setDescription("Image onto which overlay will be rendered.  Input image will only be updated if \""
                        + APPLY_TO_INPUT
                        + "\" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by \""
                        + OUTPUT_IMAGE + "\".");

        parameters.get(ADD_OUTPUT_TO_WORKSPACE).setDescription(
                "If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).");

        parameters.get(TEXT).setDescription("Fixed text to be displayed.  The current slice and frame numbers can be inserted using \"D{SLICE}\" and \"D{FRAME}\".  Similarly, it's possible to insert the elapsed frame time in the form \"T{HH:mm:ss.SSS}\" (where this example would give hours:minutes:seconds.millis).  The full description of supported time values can be found <a href=\"https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html\">here</a>.");

        parameters.get(X_POSITION).setDescription(
                "Horizontal location of the text to be displayed.  Specified in pixel units relative to the left of the image (x=0).");

        parameters.get(Y_POSITION).setDescription(
                "Vertical location of the text to be displayed.  Specified in pixel units relative to the top of the image (y=0).");

        parameters.get(Z_RANGE).setDescription(
                "Z-slices on which to display the text.  This is specified as a comma-separated list of slice indices.  The keyword \"end\" is used to denote the final slice in the stack and will be interpreted automatically.  Accepted formats are \"[VALUE]\" for a single index, \"[RANGE START]-[RANGE END]\" for a complete range of indices and \"[RANGE START]-[RANGE-END]-[INTERVAL]\" for indices evenly spaced at the specified interval.");

        parameters.get(FRAME_RANGE).setDescription(
                "Frames on which to display the text.  This is specified as a comma-separated list of frame indices.  The keyword \"end\" is used to denote the final frame in the stack and will be interpreted automatically.  Accepted formats are \"[VALUE]\" for a single index, \"[RANGE START]-[RANGE END]\" for a complete range of indices and \"[RANGE START]-[RANGE-END]-[INTERVAL]\" for indices evenly spaced at the specified interval.");

        parameters.get(CENTRE_TEXT).setDescription(
                "When selected, text will be centred on the specified XY coordinate.  Otherwise, text will be based with its top-left corner at the specified coordinate.");

        parameters.get(LABEL_SIZE).setDescription("Font size of the text label.");

        parameters.get(LABEL_COLOUR).setDescription(
                "Colour of the text label.  Choices are: " + String.join(", ", SingleColours.ALL) + ".");

    }
}
