package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

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
    public static final String INPUT_IMAGE = "Input image";

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

        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Converting object to image, then inverting, so we have a black object on a white background
//        ImagePlus objectIpl = inputObjects.values().iterator().next().getAsImage("Object").getImagePlus();
        ObjCollection tempObj = new ObjCollection("Backbone");
        ImagePlus objectIpl = ObjectImageConverter.convertObjectsToImage(tempObj,"Temp",inputImagePlus,ObjectImageConverter.ColourModes.SINGLE_COLOUR,"",false).getImagePlus();
        

        InvertIntensity.process(objectIpl);

        // Skeletonise fish to get single backbone
        BinaryOperations.applyBinaryTransform(objectIpl,BinaryOperations.OperationModes.SKELETONISE_2D,1,0);

        // Using the Common library's Skeleton tools to extract the longest branch.  This requires coordinates for the
        Skeleton skeleton = new Skeleton(objectIpl);
        LinkedHashSet<Vertex> longestPath = skeleton.getLongestPath();

        // Calculating local curvature along the path
        CurvatureCalculator curvatureCalculator = new CurvatureCalculator(longestPath);

        // Displaying the image (the image is duplicated, so it doesn't get deleted if the window is closed)
//        if (showImage && ipl != null) {
        inputImagePlus = new Duplicator().run(inputImagePlus);
        curvatureCalculator.showOverlay(inputImagePlus);
        inputImagePlus.show();
//        }

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
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
