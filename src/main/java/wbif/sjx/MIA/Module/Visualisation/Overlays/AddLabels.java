package wbif.sjx.MIA.Module.Visualisation.Overlays;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.BinaryOperations2D;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.DistanceMap;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Deprecated.AddObjectsOverlay;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.MIA.Process.LabelFactory;
import wbif.sjx.common.Object.Point;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AddLabels extends Module {
    TextRoi textRoi = null;
    public static final String INPUT_SEPARATOR = "Image and object input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String OUTPUT_SEPARATOR = "Image output";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String RENDERING_SEPARATOR = "Overlay rendering";
    public static final String LABEL_MODE = "Label mode";
    public static final String DECIMAL_PLACES = "Decimal places";
    public static final String USE_SCIENTIFIC = "Use scientific notation";
    public static final String LABEL_SIZE = "Label size";
    public static final String PARENT_OBJECT_FOR_LABEL = "Parent object for label";
    public static final String MEASUREMENT_FOR_LABEL = "Measurement for label";
    public static final String LABEL_POSITION = "Label position";
    public static final String RENDER_IN_ALL_FRAMES = "Render in all frames";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    private ColourServer colourServer;

    public interface LabelModes extends LabelFactory.LabelModes {}

    public interface LabelPositions {
        String CENTRE = "Centre of object";
        String INSIDE = "Inside largest part of object";

        String[] ALL = new String[]{CENTRE,INSIDE};

    }

    public static double[] getMeanObjectLocation(Obj object, boolean renderInAllFrames) {
        double xMean = object.getXMean(true);
        double yMean = object.getYMean(true);
        double zMean = object.getZMean(true, false);
        int z = (int) Math.round(zMean + 1);
        int t = renderInAllFrames ? 0 : object.getT() + 1;

        return new double[]{xMean, yMean, z, t};

    }

    public static double[] getInsideObjectLocation(Obj obj, boolean renderInAllFrames) {
        // Binarise object and calculate its distance map
        Image binaryImage = obj.convertObjToImage("Binary");
        InvertIntensity.process(binaryImage);
        BinaryOperations2D.process(binaryImage,BinaryOperations2D.OperationModes.ERODE,1);
        ImagePlus distanceMap = DistanceMap.getDistanceMap(binaryImage.getImagePlus(),true);

        // Get location of largest value
        Point<Integer> bestPoint = null;
        double distance = Double.MIN_VALUE;
        for (Point<Integer> point:obj.getPoints()) {
            distanceMap.setPosition(1,point.getZ()+1,obj.getT()+1);
            double currDistance = distanceMap.getProcessor().getPixelValue(point.getX(),point.getY());

            if (currDistance > distance) {
                distance = currDistance;
                bestPoint = point;
            }
        }

        int t = renderInAllFrames ? 0 : obj.getT() + 1;

        // Returning this point
        return new double[]{bestPoint.getX(),bestPoint.getY(),bestPoint.getZ()+1,t};

    }

    public static void addLabelsOverlay(ImagePlus ipl, String label, double[] labelCoords, Color colour, int labelSize) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());

        // Adding text label
        TextRoi text = new TextRoi(labelCoords[0], labelCoords[1], label, new Font(Font.SANS_SERIF,Font.PLAIN,labelSize));
        text.setStrokeColor(colour);

        if (ipl.isHyperStack()) {
            text.setPosition(1, (int) labelCoords[2], (int) labelCoords[3]);
        } else {
            text.setPosition((int) Math.max(Math.max(1, labelCoords[2]), labelCoords[3]));
        }

        text.setLocation(text.getXBase()-text.getFloatWidth()/2+1,text.getYBase()-text.getFloatHeight()/2+1);
        ipl.getOverlay().addElement(text);

    }

    public HashMap<Integer,String> getLabels(ObjCollection inputObjects, String labelMode, DecimalFormat df, String parentObjectsForLabelName, String measurementForLabel) {
        switch (labelMode) {
            case AddObjectsOverlay.LabelModes.ID:
                return LabelFactory.getIDLabels(inputObjects,df);
            case AddObjectsOverlay.LabelModes.MEASUREMENT_VALUE:
                return LabelFactory.getMeasurementLabels(inputObjects,measurementForLabel,df);
            case AddObjectsOverlay.LabelModes.PARENT_ID:
                return LabelFactory.getParentIDLabels(inputObjects,parentObjectsForLabelName,df);
            case AddObjectsOverlay.LabelModes.PARENT_MEASUREMENT_VALUE:
                return LabelFactory.getParentMeasurementLabels(inputObjects,parentObjectsForLabelName,measurementForLabel,df);
        }

        return null;

    }


    @Override
    public String getTitle() {
        return "Add labels";
    }

    @Override
    public String getPackageName() {
        return PackageNames.VISUALISATION_OVERLAYS;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting label settings
        String labelMode = parameters.getValue(LABEL_MODE);
        int labelSize = parameters.getValue(LABEL_SIZE);
        int decimalPlaces = parameters.getValue(DECIMAL_PLACES);
        boolean useScientific = parameters.getValue(USE_SCIENTIFIC);
        String parentObjectsForLabelName = parameters.getValue(PARENT_OBJECT_FOR_LABEL);
        String measurementForLabel = parameters.getValue(MEASUREMENT_FOR_LABEL);
        String labelPosition = parameters.getValue(LABEL_POSITION);
        boolean renderInAllFrames = parameters.getValue(RENDER_IN_ALL_FRAMES);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // Duplicating the image, so the original isn't altered
        if (!applyToInput) ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer,Float> hues= colourServer.getHues(inputObjects);
        DecimalFormat df = LabelFactory.getDecimalFormat(decimalPlaces,useScientific);
        HashMap<Integer,String> labels = getLabels(inputObjects,labelMode,df,parentObjectsForLabelName,measurementForLabel);

        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        // Adding the overlay element
        try {
            int nThreads = multithread ? Prefs.getThreads() : 1;
            ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

            // Running through each object, adding it to the overlay along with an ID label
            AtomicInteger count = new AtomicInteger();
            for (Obj object:inputObjects.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    float hue = hues.get(object.getID());
                    Color colour = ColourFactory.getColour(hue);
                    String label = labels == null ? "" : labels.get(object.getID());

                    double[] location;
                    switch (labelPosition) {
                        case LabelPositions.CENTRE:
                        default:
                            location = getMeanObjectLocation(object,renderInAllFrames);
                            break;
                        case LabelPositions.INSIDE:
                            location = getInsideObjectLocation(object,renderInAllFrames);
                            break;
                    }

                    addLabelsOverlay(finalIpl, label, location, colour, labelSize);
                    writeMessage("Rendered " + (count.incrementAndGet()) + " objects of " + inputObjects.size());
                };
                pool.submit(task);
            }

            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        } catch (InterruptedException e) {
            return false;
        }

        Image outputImage = new Image(outputImageName,ipl);

        // If necessary, adding output image to workspace.  This also allows us to show it.
        if (addOutputToWorkspace) workspace.addImage(outputImage);
        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this,false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new ParamSeparatorP(RENDERING_SEPARATOR,this));
        parameters.add(new ChoiceP(LABEL_MODE, this, LabelModes.ID, LabelModes.ALL));
        parameters.add(new IntegerP(DECIMAL_PLACES, this,0));
        parameters.add(new BooleanP(USE_SCIENTIFIC,this,false));
        parameters.add(new IntegerP(LABEL_SIZE, this,8));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_LABEL, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_LABEL, this));
        parameters.add(new ChoiceP(LABEL_POSITION,this,LabelPositions.CENTRE,LabelPositions.ALL));
        parameters.add(new BooleanP(RENDER_IN_ALL_FRAMES,this,false));

        parameters.add(new ParamSeparatorP(EXECUTION_SEPARATOR,this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        colourServer = new ColourServer(parameters.getParameter(INPUT_OBJECTS),this);
        parameters.addAll(colourServer.getParameters());

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT_FOR_LABEL);

        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if (parameters.getValue(ADD_OUTPUT_TO_WORKSPACE)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

            }
        }

        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LABEL_MODE));
        switch ((String) parameters.getValue(LABEL_MODE)) {
            case LabelModes.MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_LABEL));
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_FOR_LABEL)).setObjectName(inputObjectsName);
                break;

            case LabelModes.PARENT_ID:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_LABEL));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_LABEL)).setChildObjectsName(inputObjectsName);
                break;

            case LabelModes.PARENT_MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_LABEL));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_LABEL)).setChildObjectsName(inputObjectsName);

                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_LABEL));
                if (parentObjectsName != null) {
                    ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_FOR_LABEL)).setObjectName(parentObjectsName);
                }
        }

        returnedParameters.add(parameters.getParameter(DECIMAL_PLACES));
        returnedParameters.add(parameters.getParameter(USE_SCIENTIFIC));
        returnedParameters.add(parameters.getParameter(LABEL_SIZE));
        returnedParameters.addAll(colourServer.updateAndGetParameters());
        returnedParameters.add(parameters.getParameter(LABEL_POSITION));
        returnedParameters.add(parameters.getParameter(RENDER_IN_ALL_FRAMES));

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

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
