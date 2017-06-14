//TODO: Add measurement outputs

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;
import ij.ImagePlus;
import org.apache.commons.math3.fitting.leastsquares.*;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.Indexer;

import java.util.ArrayList;

/**
 * Created by sc13967 on 05/06/2017.
 */
public class FitGaussian2D extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String RADIUS_MODE = "Method to estimate spot radius";
    public static final String RADIUS = "Radius";
    public static final String RADIUS_MEASUREMENT = "Radius measurement";

    private static final String FIXED_VALUE = "Fixed value";
    private static final String MEASUREMENT = "Measurement";
    private static final String[] RADIUS_MODES = new String[]{FIXED_VALUE,MEASUREMENT};

    /**
     *
     * @param inputImagePlus
     * @param pIn double[] containing all estimated parameters
     * @return double[] containing all fit parameters (x,y,sigma,A_0,A_BG)
     */
    public static double[] fitGaussian(ImagePlus inputImagePlus, double[] pIn) {
//        int maxEvaluations = 1000;
//        int maxIterations = 1000;
//
//        int width = inputImagePlus.getWidth();
//        int height = inputImagePlus.getHeight();
//
//        // Populating the 3D vector matrix with the pixels from the ImagePlus.  We're treating intensity as a third
//        // dimension (i.e. the image is a surface we will fit the 2D Gaussian function to).
//        final Vector3D[] imagePoints = new Vector3D[width*height];
//        Indexer indexer =  new Indexer(width,height);
//
//        for (int x=0;x<width;x++) {
//            for (int y=0;y<height;y++) {
//                int idx = indexer.getIndex(new int[]{x,y});
//                imagePoints[idx] = new Vector3D(x,y,inputImagePlus.getProcessor().get(x,y));
//
//            }
//        }
//
//        MultivariateJacobianFunction distancesToCurrentCenter = new MultivariateJacobianFunction() {
//            public Pair<RealVector, RealMatrix> value(final RealVector p) {
//                RealVector value = new ArrayRealVector(imagePoints.length);
//                RealMatrix jacobian = new Array2DRowRealMatrix(imagePoints.length, 2);
//
//                for (int i=0;i<imagePoints.length;i++) {
//                    Vector3D point = imagePoints[i];
//                }
//
//                return new Pair<>(value, jacobian);
//
//            }
//        };
//
//
//        // Creating the inputs for the least squares problem
//        MultivariateJacobianFunction distancesToCurrentCenter = new MultivariateJacobianFunction() {
//            public Pair<RealVector, RealMatrix> value(final RealVector point) {
//
//                Vector2D center = new Vector2D(point.getEntry(0), point.getEntry(1));
//
////                RealVector value = new ArrayRealVector(observedPoints.length);
////                RealMatrix jacobian = new Array2DRowRealMatrix(observedPoints.length, 2);
//
//                for (int i = 0; i < observedPoints.length; ++i) {
//                    Vector2D o = observedPoints[i];
//                    double modelI = Vector2D.distance(o, center);
//                    value.setEntry(i, modelI);
//                    // derivative with respect to p0 = x center
//                    jacobian.setEntry(i, 0, (center.getX() - o.getX()) / modelI);
//                    // derivative with respect to p1 = y center
//                    jacobian.setEntry(i, 1, (center.getX() - o.getX()) / modelI);
//                }
//
//                return new Pair<>(value, jacobian);
//
//            }
//        };
//        LeastSquaresProblem lsqProblem = LeastSquaresFactory.create();
//
//        // Creating the optimiser
//        LeastSquaresOptimizer optimizer = new LevenbergMarquardtOptimizer().
//                withCostRelativeTolerance(1.0e-12).
//                withParameterRelativeTolerance(1.0e-12);
//
//        // Doing the fitting
//        LeastSquaresOptimizer.Optimum optimum = optimizer.optimize(lsqProblem);

        return null;

    }

    @Override
    public String getTitle() {
        return "Fit Gaussian 2D";
    }

    @Override
    public String getHelp() {
        return "INCOMPLETE" +
                "\n\nGaussian spot fitting.  Can take objects as estimated locations." +
                "\n***Only works in 2D***" +
                "\n***Only works for refinement of existing spots***";
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input image
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage = workspace.getImage(inputImageName);

        // Getting input objects to refine (if selected by used)
        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        HCObjectSet inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting output objects
        HCName outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // Getting parameters
        String radiusMode = parameters.getValue(RADIUS_MODE);

        // Running through each object, doing the fitting
        for (HCObject inputObject:inputObjects.values()) {
            // Getting the centroid of the current object.  These should be single point objects, but this is a
            // precautionary measure
            ArrayList<Integer> xArray = inputObject.getCoordinates(HCObject.X);
            ArrayList<Integer> yArray = inputObject.getCoordinates(HCObject.Y);
            double x = MeasureObjectCentroid.calculateCentroid(xArray,MeasureObjectCentroid.MEAN);
            double y = MeasureObjectCentroid.calculateCentroid(yArray,MeasureObjectCentroid.MEAN);

            // Estimating parameters
            if (radiusMode.equals(FIXED_VALUE)) {
                double r = parameters.getValue(RADIUS);
            } else {
                double r = inputObject.getMeasurement(parameters.getValue(RADIUS_MEASUREMENT)).getValue();
            }



        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(INPUT_OBJECTS,HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(OUTPUT_OBJECTS,HCParameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(RADIUS_MODE,HCParameter.CHOICE_ARRAY,FIXED_VALUE,RADIUS_MODES));
        parameters.addParameter(new HCParameter(RADIUS,HCParameter.DOUBLE,null));
        parameters.addParameter(new HCParameter(RADIUS_MEASUREMENT,HCParameter.MEASUREMENT,null));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(OUTPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(RADIUS_MODE));

        if (parameters.getValue(RADIUS_MODE) == FIXED_VALUE) {
            returnedParameters.addParameter(parameters.getParameter(RADIUS));

        } else if (parameters.getValue(RADIUS_MODE) == MEASUREMENT) {
            returnedParameters.addParameter(parameters.getParameter(RADIUS_MEASUREMENT));

        }


        return returnedParameters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {
        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        HCName outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        relationships.addRelationship(inputObjectsName,outputObjectsName);

    }
}
