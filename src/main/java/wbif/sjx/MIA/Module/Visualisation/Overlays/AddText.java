package wbif.sjx.MIA.Module.Visualisation.Overlays;

import java.awt.Color;

import ij.ImagePlus;
import ij.gui.TextRoi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import wbif.sjx.MIA.GUI.Colours;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.Parameters.Text.MessageP;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.MIA.Process.CommaSeparatedStringInterpreter;

public class AddText extends Overlay {
    TextRoi textRoi = null;
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String RENDERING_SEPARATOR = "Overlay rendering";
    public static final String TEXT = "Text";
    public static final String DYNAMIC_VALUES = "Available dynamic values";
    public static final String X_POSITION = "X-position";
    public static final String Y_POSITION = "Y-position";
    public static final String Z_RANGE = "Z-range";
    public static final String FRAME_RANGE = "Frame-range";
    public static final String LABEL_SIZE = "Label size";
    public static final String LABEL_COLOUR = "Label colour";


    public AddText(ModuleCollection modules) {
        super("Add text",modules);
    }


    public static void addOverlay(ImagePlus ipl, String text, Color color, int labelSize, double opacity, int xPosition, int yPosition, int[] zRange, int[] frameRange) {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        for (int z:zRange) {
            for (int f : frameRange) {
                String finalText = replaceDynamicValues(text,f,z);
                double[] location = new double[]{xPosition,yPosition,z,f};
                AddLabels.addOverlay(ipl,finalText,location,color,labelSize,false);
            }
        }
    }

    public static String replaceDynamicValues(String text, int f, int z) {
        text = text.replace("D{FRAME}", String.valueOf(f));
        return text.replace("D{SLICE}", String.valueOf(z));

    }

    @Override
    public String getPackageName() {
        return PackageNames.VISUALISATION_OVERLAYS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting label settings
        double opacity = parameters.getValue(OPACITY);
        String text = parameters.getValue(TEXT);
        int xPosition = parameters.getValue(X_POSITION);
        int yPosition = parameters.getValue(Y_POSITION);
        String zRangeString = parameters.getValue(Z_RANGE);
        String frameRangeString = parameters.getValue(FRAME_RANGE);
        int labelSize = parameters.getValue(LABEL_SIZE);
        String labelColour = parameters.getValue(LABEL_COLOUR);
        Color color = ColourFactory.getColour(labelColour);

        // Only add output to workspace if not applying to input
        if (applyToInput) addOutputToWorkspace = false;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput) ipl = new Duplicator().run(ipl);

        // Converting slice and frame ranges to numbers
        int[] zRange = CommaSeparatedStringInterpreter.interpretIntegers(zRangeString,true);
        zRange = CommaSeparatedStringInterpreter.extendRangeToEnd(zRange,ipl.getNSlices());
        int[] frameRange = CommaSeparatedStringInterpreter.interpretIntegers(frameRangeString,true);
        frameRange = CommaSeparatedStringInterpreter.extendRangeToEnd(frameRange,ipl.getNFrames());

        addOverlay(ipl,text,color,labelSize,opacity,xPosition,yPosition,zRange,frameRange);

        Image outputImage = new Image(outputImageName,ipl);

        // If necessary, adding output image to workspace.  This also allows us to show it.
        if (addOutputToWorkspace) workspace.addImage(outputImage);
        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new BooleanP(APPLY_TO_INPUT,this,false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE,this,false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));

        parameters.add(new ParamSeparatorP(RENDERING_SEPARATOR,this));
        parameters.add(new StringP(TEXT,this));
        parameters.add(new MessageP(DYNAMIC_VALUES,this,"The current slice and/or frame can be inserted into the rendered text by including one of the following: D{FRAME}, D{SLICE}.", Colours.DARK_BLUE));
        parameters.add(new IntegerP(X_POSITION,this,0));
        parameters.add(new IntegerP(Y_POSITION,this,0));
        parameters.add(new StringP(Z_RANGE,this,"1-end"));
        parameters.add(new StringP(FRAME_RANGE,this,"1-end"));
        parameters.add(new IntegerP(LABEL_SIZE,this,8));
        parameters.add(new ChoiceP(LABEL_COLOUR,this, SingleColours.BLACK,SingleColours.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE)) {
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
        returnedParameters.add(parameters.getParameter(LABEL_SIZE));
        returnedParameters.add(parameters.getParameter(LABEL_COLOUR));

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
    public ParentChildRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}

