package io.github.mianalysis.mia.module.imageprocessing.pixel;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.objects.InputTrackObjectsP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;

public class PlotKymograph extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String MODE = "Mode";
    public static final String INPUT_TRACK_OBJECTS = "Input track objects";
    public static final String INPUT_SPOT_OBJECTS = "Input spot objects";
    public static final String HALF_WIDTH = "Half width (px)";

    public PlotKymograph(Modules modules) {
        super("Plot kymograph",modules);
    }

    public interface Modes {
        String LINE_AT_OBJECT_CENTROID = "Line at object centroid";

        String[] ALL = new String[]{LINE_AT_OBJECT_CENTROID};

    }


    public Image plotObjectLine(Image inputImage, String outputImageName, Objs inputTrackObjects, String inputSpotObjectsName, int halfWidth) {
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
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
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
                Objs inputTrackObjects = workspace.getObjectSet(inputTrackObjectsName);
                outputImage = plotObjectLine(inputImage,outputImageName,inputTrackObjects,inputSpotObjectsName,halfWidth);
                break;
        }

        workspace.addImage(outputImage);

        if (showOutput) outputImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new ChoiceP(MODE,this,Modes.LINE_AT_OBJECT_CENTROID,Modes.ALL));
        parameters.add(new InputTrackObjectsP(INPUT_TRACK_OBJECTS,this));
        parameters.add(new ChildObjectsP(INPUT_SPOT_OBJECTS,this));
        parameters.add(new IntegerP(HALF_WIDTH,this,10));
    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

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
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
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
}
