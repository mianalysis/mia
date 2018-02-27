package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.BinaryOperations;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.InvertIntensity;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.ObjectImageConverter;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.Analysis.CurvatureCalculator;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.Vertex;
import wbif.sjx.common.Process.SkeletonTools.Skeleton;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.TreeMap;

/**
 * Created by sc13967 on 24/01/2018.
 */
public class SplineAnalysis extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String SPLINE_FITTING_METHOD = "Spline fitting method";
    public static final String N_NEIGHBOURS = "Number of neighbours (smoothing)";
    public static final String ITERATIONS = "Iterations";
    public static final String ACCURACY = "Accuracy";
    public static final String SHOW_SPLINE = "Show spline";
    public static final String MAX_CURVATURE = "Maximum curvature (for colour)";
    public static final String APPLY_TO_IMAGE = "Apply to image";


    interface SplineFittingMethods {
        String LOESS = "LOESS (smooth fitting)";
        String STANDARD = "Standard (fits all points)";

        String[] ALL = new String[]{LOESS,STANDARD};

    }

    public interface Measurements {
        String MEAN_CURVATURE = "SPLINE//MEAN_CURVATURE";
        String MIN_CURVATURE = "SPLINE//MIN_CURVATURE";
        String MAX_CURVATURE = "SPLINE//MAX_CURVATURE";
        String STD_CURVATURE = "SPLINE//STD_CURVATURE";

    }

    @Override
    public String getTitle() {
        return "Spline analysis";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);

        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
        Image referenceImage = workspace.getImage(referenceImageName);
        ImagePlus referenceImageImagePlus = referenceImage.getImagePlus();

        // Getting parameters
        String splineFittingMethod = parameters.getValue(SPLINE_FITTING_METHOD);
        int nNeighbours = parameters.getValue(N_NEIGHBOURS);
        int iterations = parameters.getValue(ITERATIONS);
        double accuracy = parameters.getValue(ACCURACY);
        boolean showSplines = parameters.getValue(SHOW_SPLINE);
        boolean applyToImage = parameters.getValue(APPLY_TO_IMAGE);
        double maxCurvature = parameters.getValue(MAX_CURVATURE);

        if (showSplines &! applyToImage) {
            referenceImageImagePlus = new Duplicator().run(referenceImageImagePlus);
        }

        ImagePlus templateImage = IJ.createImage("Template",referenceImageImagePlus.getWidth(),referenceImageImagePlus.getHeight(),1,8);
        int count = 1;
        int total = inputObjects.size();
        for (Obj inputObject:inputObjects.values()) {
            if (verbose)
                System.out.println("[" + moduleName + "] Processing object " + (count++) + " of " + total);

            // Converting object to image, then inverting, so we have a black object on a white background
            ObjCollection tempObjects = new ObjCollection("Backbone");
            tempObjects.add(inputObject);

            HashMap<Integer,Float> hues = tempObjects.getHue(ObjCollection.ColourModes.SINGLE_COLOUR,"","",false);
            ImagePlus objectIpl = tempObjects.convertObjectsToImage("Objects", templateImage, ObjectImageConverter.ColourModes.SINGLE_COLOUR, hues, false).getImagePlus();
            InvertIntensity.process(objectIpl);

            // Skeletonise fish to get single backbone
            BinaryOperations.applyBinaryTransform(objectIpl, BinaryOperations.OperationModes.SKELETONISE_2D, 1, 0);

            // Using the Common library's Skeleton tools to extract the longest branch.  This requires coordinates for the
            Skeleton skeleton = new Skeleton(objectIpl);
            LinkedHashSet<Vertex> longestPath = skeleton.getLongestPath();

            // Calculating local curvature along the path
            CurvatureCalculator curvatureCalculator = new CurvatureCalculator(longestPath);
            switch (splineFittingMethod) {
                case SplineFittingMethods.LOESS:
                    curvatureCalculator.setLoessNNeighbours(nNeighbours);
                    curvatureCalculator.setLoessIterations(iterations);
                    curvatureCalculator.setLoessAccuracy(accuracy);
                    curvatureCalculator.setFittingMethod(CurvatureCalculator.FittingMethod.LOESS);
                    break;

                case SplineFittingMethods.STANDARD:
                    curvatureCalculator.setFittingMethod(CurvatureCalculator.FittingMethod.STANDARD);
                    break;
            }

            TreeMap<Double,Double> curvature = curvatureCalculator.getCurvature();
            CumStat cumStat = new CumStat();
            for (double value:curvature.values()) {
                cumStat.addMeasure(value);
            }

            // Adding measurements
            inputObject.addMeasurement(new Measurement(Measurements.MEAN_CURVATURE,cumStat.getMean()));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_CURVATURE,cumStat.getMin()));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_CURVATURE,cumStat.getMax()));
            inputObject.addMeasurement(new Measurement(Measurements.STD_CURVATURE,cumStat.getStd()));

            // Displaying the image (the image is duplicated, so it doesn't get deleted if the window is closed)
            if (showSplines) {
                int[] position = new int[]{1,(int) (inputObject.getZ(false,false)[0]+1),(inputObject.getT()+1)};
                referenceImageImagePlus.setPosition(1,(int) (inputObject.getZ(false,false)[0]+1),inputObject.getT()+1);
                curvatureCalculator.showOverlay(referenceImageImagePlus, maxCurvature, position);
            }
        }

        if (showSplines) {
            new Duplicator().run(referenceImageImagePlus).show();
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(REFERENCE_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(SPLINE_FITTING_METHOD, Parameter.CHOICE_ARRAY,SplineFittingMethods.LOESS,SplineFittingMethods.ALL));
        parameters.add(new Parameter(N_NEIGHBOURS, Parameter.INTEGER,20));
        parameters.add(new Parameter(ITERATIONS, Parameter.INTEGER,10));
        parameters.add(new Parameter(ACCURACY, Parameter.DOUBLE,1d));
        parameters.add(new Parameter(SHOW_SPLINE, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(APPLY_TO_IMAGE, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(MAX_CURVATURE,Parameter.DOUBLE,1d));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_CURVATURE));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MIN_CURVATURE));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MAX_CURVATURE));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.STD_CURVATURE));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
        returnedParameters.add(parameters.getParameter(SPLINE_FITTING_METHOD));

        switch ((String) parameters.getValue(SPLINE_FITTING_METHOD)) {
            case SplineFittingMethods.LOESS:
                returnedParameters.add(parameters.getParameter(N_NEIGHBOURS));
                returnedParameters.add(parameters.getParameter(ITERATIONS));
                returnedParameters.add(parameters.getParameter(ACCURACY));
                break;
        }

        returnedParameters.add(parameters.getParameter(SHOW_SPLINE));
        if (parameters.getValue(SHOW_SPLINE)) {
            returnedParameters.add(parameters.getParameter(APPLY_TO_IMAGE));
            returnedParameters.add(parameters.getParameter(MAX_CURVATURE));
        }

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        objectMeasurementReferences.get(Measurements.MEAN_CURVATURE).setImageObjName(inputObjectsName);
        objectMeasurementReferences.get(Measurements.MIN_CURVATURE).setImageObjName(inputObjectsName);
        objectMeasurementReferences.get(Measurements.MAX_CURVATURE).setImageObjName(inputObjectsName);
        objectMeasurementReferences.get(Measurements.STD_CURVATURE).setImageObjName(inputObjectsName);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
