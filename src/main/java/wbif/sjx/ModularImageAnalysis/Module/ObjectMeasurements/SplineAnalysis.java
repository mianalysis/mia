package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.BinaryOperations;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.InvertIntensity;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.ObjectImageConverter;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Analysis.CurvatureCalculator;
import wbif.sjx.common.Object.Vertex;
import wbif.sjx.common.Process.SkeletonTools.Skeleton;
import wbif.sjx.common.Process.SkeletonTools.SkeletonVisualiser;

import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 24/01/2018.
 */
public class SplineAnalysis extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String SPLINE_FITTING_METHOD = "Spline fitting method";
    public static final String N_NEIGHBOURS = "Number of neighbours (smoothing)";
    public static final String ITERATIONS = "Iterations";
    public static final String ACCURACY = "Accuracy";
    public static final String SHOW_SPLINE = "Show spline";
    public static final String MAX_CURVATURE = "Maximum curvature (for colour)";


    interface SplineFittingMethods {
        String LOESS = "LOESS (smooth fitting)";
        String STANDARD = "Standard (fits all points)";

        String[] ALL = new String[]{LOESS,STANDARD};

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
        double maxCurvature = parameters.getValue(MAX_CURVATURE);

        if (showSplines) {
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

            System.out.println(inputObject.getPoints().size());

            ImagePlus objectIpl = ObjectImageConverter.convertObjectsToImage(tempObjects, "Temp", templateImage, ObjectImageConverter.ColourModes.SINGLE_COLOUR, "", false).getImagePlus();
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

            // Displaying the image (the image is duplicated, so it doesn't get deleted if the window is closed)
            if (showSplines) {
                int[] position = new int[]{1,(int) (inputObject.getZ(false,false)[0]+1),(inputObject.getT()+1)};
                referenceImageImagePlus.setPosition(1,(int) (inputObject.getZ(false,false)[0]+1),inputObject.getT()+1);
                curvatureCalculator.showOverlay(referenceImageImagePlus, maxCurvature, position);
            }
        }

        if (showSplines) referenceImageImagePlus.show();

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(REFERENCE_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(SPLINE_FITTING_METHOD, Parameter.CHOICE_ARRAY,SplineFittingMethods.LOESS,SplineFittingMethods.ALL));
        parameters.add(new Parameter(N_NEIGHBOURS, Parameter.INTEGER,10));
        parameters.add(new Parameter(ITERATIONS, Parameter.INTEGER,10));
        parameters.add(new Parameter(ACCURACY, Parameter.DOUBLE,1d));
        parameters.add(new Parameter(SHOW_SPLINE, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(MAX_CURVATURE,Parameter.DOUBLE,1d));

    }

    @Override
    protected void initialiseMeasurementReferences() {

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
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
