package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.ImagePlus;
import ij.plugin.SubHyperstackMaker;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by sc13967 on 18/01/2018.
 */
public class ExtractSubstack extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String USE_ALL_C = "Use all channels";
    public static final String STARTING_C = "Starting channel";
    public static final String ENDING_C = "Ending channel";
    public static final String USE_ALL_Z = "Use all Z-slices";
    public static final String STARTING_Z = "Starting Z-slice";
    public static final String ENDING_Z = "Ending Z-slice";
    public static final String USE_ALL_T = "Use all timepoints";
    public static final String STARTING_T = "Starting timepoint";
    public static final String ENDING_T = "Ending timepoint";
    public static final String SHOW_IMAGE = "Show image";

    @Override
    public String getTitle() {
        return "Extract subtack";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean useAllC = parameters.getValue(USE_ALL_C);
        int startingC = parameters.getValue(STARTING_C);
        int endingC = parameters.getValue(ENDING_C);
        boolean useAllZ = parameters.getValue(USE_ALL_Z);
        int startingZ = parameters.getValue(STARTING_Z);
        int endingZ = parameters.getValue(ENDING_Z);
        boolean useAllT = parameters.getValue(USE_ALL_T);
        int startingT = parameters.getValue(STARTING_T);
        int endingT = parameters.getValue(ENDING_T);
        boolean showImage = parameters.getValue(SHOW_IMAGE);

        // Updating ranges
        if (useAllC) {
            startingC = 1;
            endingC = inputImagePlus.getNChannels()+1;
        }
        if (useAllZ) {
            startingZ = 1;
            endingZ = inputImagePlus.getNSlices()+1;
        }
        if (useAllT) {
            startingT = 1;
            endingT = inputImagePlus.getNFrames()+1;
        }

        List<Integer> cList = IntStream.range(startingC, endingC).boxed().collect(Collectors.toList());
        List<Integer> zList = IntStream.range(startingZ, endingZ).boxed().collect(Collectors.toList());
        List<Integer> tList = IntStream.range(startingT, endingT).boxed().collect(Collectors.toList());

        // Generating the substack and adding to the workspace
        ImagePlus outputImagePlus =  SubHyperstackMaker.makeSubhyperstack(inputImagePlus,cList,zList,tList);
        Image outputImage = new Image(outputImageName,outputImagePlus);
        workspace.addImage(outputImage);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(USE_ALL_C, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(STARTING_C, Parameter.INTEGER,1));
        parameters.add(new Parameter(ENDING_C, Parameter.INTEGER,1));
        parameters.add(new Parameter(USE_ALL_Z, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(STARTING_Z, Parameter.INTEGER,1));
        parameters.add(new Parameter(ENDING_Z, Parameter.INTEGER,1));
        parameters.add(new Parameter(USE_ALL_T, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(STARTING_T, Parameter.INTEGER,1));
        parameters.add(new Parameter(ENDING_T, Parameter.INTEGER,1));
        parameters.add(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(USE_ALL_C));
        if (!(boolean) parameters.getValue(USE_ALL_C)) {
            returnedParameters.add(parameters.getParameter(STARTING_C));
            returnedParameters.add(parameters.getParameter(ENDING_C));
        }

        returnedParameters.add(parameters.getParameter(USE_ALL_Z));
        if (!(boolean) parameters.getValue(USE_ALL_Z)) {
            returnedParameters.add(parameters.getParameter(STARTING_Z));
            returnedParameters.add(parameters.getParameter(ENDING_Z));
        }

        returnedParameters.add(parameters.getParameter(USE_ALL_T));
        if (!(boolean) parameters.getValue(USE_ALL_T)) {
            returnedParameters.add(parameters.getParameter(STARTING_T));
            returnedParameters.add(parameters.getParameter(ENDING_T));
        }

        returnedParameters.add(parameters.getParameter(SHOW_IMAGE));

        return returnedParameters;

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
