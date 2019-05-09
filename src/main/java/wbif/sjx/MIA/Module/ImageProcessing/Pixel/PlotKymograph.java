package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

public class PlotKymograph extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String MODE = "Mode";
    public static final String INPUT_TRACK_OBJECTS = "Input track objects";
    public static final String INPUT_SPOT_OBJECTS = "Input spot objects";
    public static final String HALF_WIDTH = "Half width (px)";

    public interface Modes {
        String LINE_AT_OBJECT_CENTROID = "Line at object centroid";

        String[] ALL = new String[]{LINE_AT_OBJECT_CENTROID};

    }


    public Image plotObjectLine(Image inputImage, String outputImageName, ObjCollection inputTrackObjects, String inputSpotObjectsName, int halfWidth) {
        ImagePlus inputIpl = inputImage.getImagePlus();

        // Getting properties of the output image
        int nFrames = inputIpl.getNFrames();
        int height = (int) Math.ceil(halfWidth*2+1);
        int nSlices = inputTrackObjects.size();
        int nChannels = inputIpl.getNChannels();
        int bitDepth = inputIpl.getBitDepth();

        // Initialising the output image
        ImagePlus outputIpl = IJ.createHyperStack(outputImageName,nFrames,height,nChannels,nSlices,1,bitDepth);

        // Iterating over each timepoint for input image, generating an average across all objects
        int count = 1;
            for (Obj inputTrackObject:inputTrackObjects.values()) {
                for (int c=1;c<=nChannels;c++) {
                    outputIpl.setPosition(c, count, 1);
                    ImageProcessor outputIpr = outputIpl.getProcessor();

                    for (Obj inputSpotObject : inputTrackObject.getChildren(inputSpotObjectsName).values()) {
                        // Getting object centroid
                        int x = (int) Math.round(inputSpotObject.getXMean(true));
                        int y = (int) Math.round(inputSpotObject.getYMean(true));
                        int z = (int) Math.round(inputSpotObject.getZMean(true, false));
                        int t = inputSpotObject.getT();

                        int x1 = x;
                        int x2 = x;
                        int y1 = y - halfWidth;
                        int y2 = y + halfWidth;

                        inputIpl.setPosition(c, z + 1, t + 1);

                        Line line = new Line(x1, y1, x2, y2);
                        line.setPosition(inputIpl);
                        inputIpl.setRoi(line);
                        double[] pixels = line.getPixels();

                        for (int i = 0; i < height; i++) outputIpr.set(t, i, (int) Math.round(pixels[i]));

                    }
                }

                count++;

        }

        return new Image(outputImageName,outputIpl);

    }


    @Override
    public String getTitle() {
        return "Plot kymograph";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting the input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String mode = parameters.getValue(MODE);
        String inputTrackObjectsName = parameters.getValue(INPUT_TRACK_OBJECTS);
        String inputSpotObjectsName = parameters.getValue(INPUT_SPOT_OBJECTS);
        int halfWidth = parameters.getValue(HALF_WIDTH);

        Image outputImage = null;
        switch (mode) {
            case Modes.LINE_AT_OBJECT_CENTROID:
                ObjCollection inputTrackObjects = workspace.getObjectSet(inputTrackObjectsName);
                outputImage = plotObjectLine(inputImage,outputImageName,inputTrackObjects,inputSpotObjectsName,halfWidth);
                break;
        }

        workspace.addImage(outputImage);

        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new ChoiceP(MODE,this,Modes.LINE_AT_OBJECT_CENTROID,Modes.ALL));
        parameters.add(new InputObjectsP(INPUT_TRACK_OBJECTS,this));
        parameters.add(new ChildObjectsP(INPUT_SPOT_OBJECTS,this));
        parameters.add(new IntegerP(HALF_WIDTH,this,10));
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(MODE));
        switch ((String) parameters.getValue(MODE)) {
            case Modes.LINE_AT_OBJECT_CENTROID:
                String inputTrackObjectsName = parameters.getValue(INPUT_TRACK_OBJECTS);
                ChildObjectsP spotObjects = parameters.getParameter(INPUT_SPOT_OBJECTS);
                spotObjects.setParentObjectsName(inputTrackObjectsName);

                returnedParameters.add(parameters.getParameter(INPUT_TRACK_OBJECTS));
                returnedParameters.add(parameters.getParameter(INPUT_SPOT_OBJECTS));
                returnedParameters.add(parameters.getParameter(HALF_WIDTH));

                break;
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
