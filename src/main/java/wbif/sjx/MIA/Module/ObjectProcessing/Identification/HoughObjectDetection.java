package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Visualisation.Overlays.AddLabels;
import wbif.sjx.MIA.Module.Visualisation.Overlays.AddObjectOutline;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.MIA.Process.LabelFactory;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.MathFunc.Indexer;
import wbif.sjx.common.MathFunc.MidpointCircle;
import wbif.sjx.common.Process.HoughTransform.Transforms.CircleHoughTransform;
import wbif.sjx.common.Process.IntensityMinMax;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sc13967 on 15/01/2018.
 */
public class HoughObjectDetection extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String DETECTION_SEPARATOR = "Hough-based circle detection";
    public static final String MIN_RADIUS = "Minimum radius (px)";
    public static final String MAX_RADIUS = "Maximum radius (px)";
    public static final String SAMPLE_FRACTION = "Sample fraction (0 = none, 1 = all)";
    public static final String DETECTION_THRESHOLD = "Detection threshold";
    public static final String EXCLUSION_RADIUS = "Exclusion radius (px)";
    public static final String RANDOM_SAMPLING = "Random sampling (faster, but less accurate)";

    public static final String VISUALISATION_SEPARATOR = "Visualisation controls";
    public static final String SHOW_DETECTION_IMAGE = "Show detection image";
    public static final String SHOW_TRANSFORM_IMAGE = "Show transform image";
    public static final String SHOW_HOUGH_SCORE = "Show detection score";
    public static final String LABEL_SIZE = "Label size";

    public HoughObjectDetection(ModuleCollection modules) {
        super("Hough-based detection",modules);
    }


    private interface Measurements {
        String SCORE = "HOUGH_DETECTION//SCORE";

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting output image name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Getting parameters
        int minR = parameters.getValue(MIN_RADIUS);
        int maxR = parameters.getValue(MAX_RADIUS);
        boolean randomSampling = parameters.getValue(RANDOM_SAMPLING);
        double sampleFraction = parameters.getValue(SAMPLE_FRACTION);
        double detectionThreshold = parameters.getValue(DETECTION_THRESHOLD);
        int exclusionRadius = parameters.getValue(EXCLUSION_RADIUS);
        boolean showTransformImage = parameters.getValue(SHOW_TRANSFORM_IMAGE);
        boolean showDetectionImage = parameters.getValue(SHOW_DETECTION_IMAGE);
        boolean showHoughScore = parameters.getValue(SHOW_HOUGH_SCORE);
        int labelSize = parameters.getValue(LABEL_SIZE);

        // Storing the image calibration
        Calibration calibration = ipl.getCalibration();
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String calibrationUnits = calibration.getUnits();
        boolean twoD = ipl.getNSlices()==1;

        // Iterating over all images in the ImagePlus
        int count = 1;
        int total = ipl.getNChannels()*ipl.getNSlices()*ipl.getNFrames();

        for (int c=0;c<ipl.getNChannels();c++) {
            for (int z = 0; z < ipl.getNSlices(); z++) {
                for (int t = 0; t < ipl.getNFrames(); t++) {
                    ipl.setPosition(c + 1, z + 1, t + 1);

                    // Initialising the Hough transform
                    int[][] paramRanges = new int[][]{{0,ipl.getWidth() - 1}, {0,ipl.getHeight() - 1}, {minR,maxR}};
                    CircleHoughTransform circleHoughTransform = new CircleHoughTransform(ipl.getProcessor(),paramRanges);
                    circleHoughTransform.setRandomSampling(randomSampling);
                    circleHoughTransform.setSampleFraction(sampleFraction);

                    // Running the transforms
                    writeMessage("Running transform (image " + (count) + " of " + total+")");
                    circleHoughTransform.run();

                    // Normalising scores based on the number of points in that circle
                    writeMessage("Normalising scores (image " + (count) + " of " + total+")");
                    circleHoughTransform.normaliseScores();

                    // Getting the accumulator as an image
                    if (showOutput && showTransformImage) {
                        ImagePlus showIpl = new Duplicator().run(circleHoughTransform.getAccumulatorAsImage());
                        IntensityMinMax.run(showIpl,true);
                        showIpl.setTitle("Accumulator");
                        showIpl.show();
                    }

                    // Getting circle objects and adding to workspace
                    writeMessage("Detecting objects (image " + (count++) + " of " + total+")");
                    ArrayList<double[]> circles = circleHoughTransform.getObjects(detectionThreshold, exclusionRadius);
                    Indexer indexer = new Indexer(ipl.getWidth(), ipl.getHeight());
                    for (double[] circle : circles) {
                        // Initialising the object
                        Obj outputObject = new Obj(outputObjectsName, outputObjects.getAndIncrementID(), dppXY, dppZ,
                                calibrationUnits,twoD);

                        // Getting circle parameters
                        int x = (int) Math.round(circle[0]);
                        int y = (int) Math.round(circle[1]);
                        int r = (int) Math.round(circle[2]);
                        double score = circle[3];

                        // Getting coordinates corresponding to circle
                        MidpointCircle midpointCircle = new MidpointCircle(r);
                        int[] xx = midpointCircle.getXCircleFill();
                        int[] yy = midpointCircle.getYCircleFill();

                        for (int i = 0; i < xx.length; i++) {
                            int idx = indexer.getIndex(new int[]{xx[i] + x, yy[i] + y});
                            if (idx == -1) continue;

                            try {
                                outputObject.addCoord(xx[i] + x, yy[i] + y, z);
                            } catch (IntegerOverflowException e) {
                                return false;
                            }

                        }

                        // Adding measurements
                        outputObject.setT(t);
                        outputObject.addMeasurement(new Measurement(Measurements.SCORE, score));

                        // Adding object to object set
                        outputObjects.add(outputObject);

                    }

                    writeMessage(circles.size()+" circles detected in frame C="+c+", Z="+z+", T="+t);

                }
            }
        }

        ipl.setPosition(1,1,1);
        workspace.addObjects(outputObjects);

        if (showOutput && showDetectionImage) {
            ImagePlus dispIpl = new Duplicator().run(ipl);
            IntensityMinMax.run(dispIpl,true);

            String colourMode = ObjCollection.ColourModes.RANDOM_COLOUR;
            HashMap<Integer,Float> hues = ColourFactory.getRandomHues(outputObjects);

            HashMap<Integer, String> IDs = null;
            if (showHoughScore) {
                DecimalFormat df = LabelFactory.getDecimalFormat(0,true);
                IDs = LabelFactory.getMeasurementLabels(outputObjects,Measurements.SCORE,df);
                AddLabels.addOverlay(dispIpl,outputObjects,AddLabels.LabelPositions.CENTRE,IDs,labelSize,hues,false,true);
            }

            AddObjectOutline.addOverlay(dispIpl,outputObjects,0.3,hues,false,true);

            dispIpl.setPosition(1,1,1);
            dispIpl.updateChannelAndDraw();
            dispIpl.show();

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this));

        parameters.add(new ParamSeparatorP(DETECTION_SEPARATOR,this));
        parameters.add(new IntegerP(MIN_RADIUS,this,10));
        parameters.add(new IntegerP(MAX_RADIUS,this,20));
        parameters.add(new BooleanP(RANDOM_SAMPLING,this,false));
        parameters.add(new DoubleP(SAMPLE_FRACTION,this,0.5));
        parameters.add(new DoubleP(DETECTION_THRESHOLD,this,1.0));
        parameters.add(new IntegerP(EXCLUSION_RADIUS,this,10));

        parameters.add(new ParamSeparatorP(VISUALISATION_SEPARATOR,this));
        parameters.add(new BooleanP(SHOW_TRANSFORM_IMAGE,this,true));
        parameters.add(new BooleanP(SHOW_DETECTION_IMAGE,this,true));
        parameters.add(new BooleanP(SHOW_HOUGH_SCORE,this,false));
        parameters.add(new IntegerP(LABEL_SIZE,this,12));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(DETECTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MIN_RADIUS));
        returnedParameters.add(parameters.getParameter(MAX_RADIUS));
        returnedParameters.add(parameters.getParameter(DETECTION_THRESHOLD));
        returnedParameters.add(parameters.getParameter(EXCLUSION_RADIUS));

        returnedParameters.add(parameters.getParameter(RANDOM_SAMPLING));
        if (parameters.getValue(RANDOM_SAMPLING)) {
            returnedParameters.add(parameters.getParameter(SAMPLE_FRACTION));
        }

        returnedParameters.add(parameters.getParameter(VISUALISATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SHOW_TRANSFORM_IMAGE));

        returnedParameters.add(parameters.getParameter(SHOW_DETECTION_IMAGE));
        if (parameters.getValue(SHOW_DETECTION_IMAGE)) {
            returnedParameters.add(parameters.getParameter(SHOW_HOUGH_SCORE));
            if (parameters.getValue(SHOW_HOUGH_SCORE)) {
                returnedParameters.add(parameters.getParameter(LABEL_SIZE));
            }
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        ObjMeasurementRef score = objectMeasurementRefs.getOrPut(Measurements.SCORE);
        score.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
        returnedRefs.add(score);

        return returnedRefs;

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
