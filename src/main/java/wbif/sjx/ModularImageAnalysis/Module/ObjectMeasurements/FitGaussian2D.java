// TODO: Show original and fit PSFs - maybe as a mosaic - to demonstrate the process is working correctly

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;
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
    public static final String RADIUS_MODE = "Method to estimate spot radius";
    public static final String RADIUS = "Radius";
    public static final String RADIUS_MEASUREMENT = "Radius measurement";
    public static final String UPDATE_LOCATION = "Update location to fit centroid";

    private static final String FIXED_VALUE = "Fixed value";
    private static final String MEASUREMENT = "Measurement";
    private static final String[] RADIUS_MODES = new String[]{FIXED_VALUE,MEASUREMENT};

    private static final String X_0 = "X_0";
    private static final String Y_0 = "Y_0";
    private static final String SIGMA_X = "SIGMA_X";
    private static final String SIGMA_Y = "SIGMA_Y";
    private static final String A_0 = "A_0";
    private static final String A_BG = "A_BG";
    private static final String THETA = "THETA";

    public static void main(String[] args) {
//        System.out.println("x0 = "+x0+", y0 = "+y0+", sx = "+sx+", sy = "+sy+", A0 = "+A0+", ABG = "+ABG+", th = "+(th*180/Math.PI));
//
//        ImagePlus iplOut = IJ.createImage("Out",width,height,1,8);
//        for (int i=0;i<imagePoints.length;i++) {
//            double x = imagePoints[i].getX();
//            double y = imagePoints[i].getY();
//            double a = (Math.cos(th)*Math.cos(th))/(2*sx*sx) + (Math.sin(th)*Math.sin(th))/(2*sy*sy);
//            double b = Math.sin(2*th)/(4*sy*sy) - Math.sin(2*th)/(4*sx*sx);
//            double c = (Math.cos(th)*Math.cos(th))/(2*sy*sy) + (Math.sin(th)*Math.sin(th))/(2*sx*sx);
//            double val = ABG + A0*Math.exp(-(a*((x-x0)*(x-x0))-2*b*(x-x0)*(y-y0)+c*((y-y0)*(y-y0))));
//
//            iplOut.getProcessor().putPixelValue((int) x,(int) y,(int) val);
//        }
//
//        inputImagePlus.show();
//        iplOut.show();

    }

    /**
     *
     * @param ipr
     * @param pIn double[] containing all estimated parameters
     * @return double[] containing all fit parameters (x0,y0,sigmaX,sigmaY,A0,ABG,th)
     */
    public static double[] fitGaussian(ImageProcessor ipr, double[] pIn) {
        int maxEvaluations = Integer.MAX_VALUE;
        int maxIterations = Integer.MAX_VALUE;

        int width = ipr.getWidth();
        int height = ipr.getHeight();

        // Populating the 3D vector matrix with the pixels from the ImagePlus.  We're treating intensity as a third
        // dimension (i.e. the image is a surface we will fit the 2D Gaussian function to).
        final Vector3D[] imagePoints = new Vector3D[width*height];
        Indexer indexer =  new Indexer(width,height);

        for (int x=0;x<width;x++) {
            for (int y=0;y<height;y++) {
                int idx = indexer.getIndex(new int[]{x,y});
                imagePoints[idx] = new Vector3D(x,y,ipr.get(x,y));

            }
        }

        // The only element of p is the height
        MultivariateJacobianFunction model = new MultivariateJacobianFunction() {
            public Pair<RealVector, RealMatrix> value(final RealVector p) {
                RealVector value = new ArrayRealVector(imagePoints.length);
                RealMatrix jacobian = new Array2DRowRealMatrix(imagePoints.length, 7);

                double x0 = p.getEntry(0); // centroid x
                double y0 = p.getEntry(1); // centroid y
                double sx = p.getEntry(2); // sigma x
                double sy = p.getEntry(3); // sigma y
                double A0 = p.getEntry(4); // peak amplitude
                double ABG = p.getEntry(5); // background amplitude
                double th = p.getEntry(6); // theta

                for (int i=0;i<imagePoints.length;i++) {
                    // The current point to evaluate
                    Vector3D currPoint = imagePoints[i];
                    double x = currPoint.getX();
                    double y = currPoint.getY();

                    // The value of the function at this location
                    double a = (Math.cos(th)*Math.cos(th))/(2*sx*sx) + (Math.sin(th)*Math.sin(th))/(2*sy*sy);
                    double b = Math.sin(2*th)/(4*sy*sy) - Math.sin(2*th)/(4*sx*sx);
                    double c = (Math.cos(th)*Math.cos(th))/(2*sy*sy) + (Math.sin(th)*Math.sin(th))/(2*sx*sx);
                    double val = ABG + A0*Math.exp(-(a*((x-x0)*(x-x0))-2*b*(x-x0)*(y-y0)+c*((y-y0)*(y-y0))));
                    value.setEntry(i, val);

                    // Partial derivatives of the Gaussian function with respect to each parameter
                    double ori = Math.exp(-a*((x-x0)*(x-x0))-c*((y-y0)*(y-y0))
                            -(Math.sin(2*th)/(2*sx*sx)-Math.sin(2*th)/(2*sy*sy))*(x-x0)*(y-y0));
                    double j0 = A0*ori*((Math.sin(2*th)/(2*sx*sx)-Math.sin(2*th)/(2*sy*sy))*(y-y0)+a*(2*x-2*x0));
                    double j1 = A0*ori*((Math.sin(2*th)/(2*sx*sx)-Math.sin(2*th)/(2*sy*sy))*(x-x0)+c*(2*y-2*y0));
                    double j2 = A0*ori*(((Math.cos(th)*Math.cos(th))*((x-x0)*(x-x0))/(sx*sx*sx))
                            + ((Math.sin(th)*Math.sin(th))*((y-y0)*(y-y0)))/(sx*sx*sx)
                            + (Math.sin(2*th)*(x-x0)*(y-y0))/(sx*sx*sx));
                    double j3 = A0*ori*(((Math.cos(th)*Math.cos(th))*((y-y0)*(y-y0)))/(sy*sy*sy)
                            + ((Math.sin(th)*Math.sin(th))*((x-x0)*(x-x0)))/(sy*sy*sy)
                            - (Math.sin(2*th)*(x-x0)*(y-y0))/(sy*sy*sy));
                    double j4 = ori;
                    double j5 = Math.abs(1/(currPoint.getZ()-ABG));
                    double j6 = -A0*ori*(((
                            Math.cos(th)*Math.sin(th))/(sx*sx) - (Math.cos(th)*Math.sin(th))/(sy*sy))*((y-y0)*(y-y0))
                            - ((Math.cos(th)*Math.sin(th))/(sx*sx) - (Math.cos(th)*Math.sin(th))/(sy*sy))*((x-x0)*(x-x0))
                            + (Math.cos(2*th)/(sx*sx)-Math.cos(2*th)/(sy*sy))*(x-x0)*(y-y0));

                    // j5 hits infinity if the current value matches the real value
                    j5 = Double.isInfinite(j5) ? 0 : j5;

                    jacobian.setEntry(i, 0, j0);
                    jacobian.setEntry(i, 1, j1);
                    jacobian.setEntry(i, 2, j2);
                    jacobian.setEntry(i, 3, j3);
                    jacobian.setEntry(i, 4, j4);
                    jacobian.setEntry(i, 5, j5);
                    jacobian.setEntry(i, 6, j6);

                }

                return new Pair<>(value, jacobian);

            }
        };

        // Starting point
        RealVector start = new ArrayRealVector(pIn);

        // Target residual (i.e. zero)
        double[] target = new double[imagePoints.length];
        for (int i=0;i<imagePoints.length;i++) {
            target[i] = imagePoints[i].getZ();
        }

        LeastSquaresProblem lsqProblem = new LeastSquaresBuilder()
                .start(start)
                .model(model)
                .target(target)
                .maxEvaluations(maxEvaluations)
                .maxIterations(maxIterations)
                .build();

        // Creating the optimiser
        LeastSquaresOptimizer optimizer = new LevenbergMarquardtOptimizer();

        // Doing the fitting
        LeastSquaresOptimizer.Optimum optimum = optimizer.optimize(lsqProblem);
        RealVector optimP = optimum.getPoint();

        // Storing the output fits
        double[] pOut = new double[7];
        pOut[0] = optimP.getEntry(0); // centroid x
        pOut[1] = optimP.getEntry(1); // centroid y
        pOut[2] = optimP.getEntry(2); // sigma x
        pOut[3] = optimP.getEntry(3); // sigma y
        pOut[4] = optimP.getEntry(4); // peak amplitude
        pOut[5] = optimP.getEntry(5); // background amplitude
        pOut[6] = optimP.getEntry(6); // theta

        return pOut;

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
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting input objects to refine (if selected by used)
        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        HCObjectSet inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        String radiusMode = parameters.getValue(RADIUS_MODE);

        // Running through each object, doing the fitting
        int count = 0;
        for (HCObject inputObject:inputObjects.values()) {
            if (verbose) System.out.println("["+moduleName+"] Fitting object "+(count+1)+" of "+inputObjects.size());
            // Getting the centroid of the current object.  These should be single point objects, but this is a
            // precautionary measure
            ArrayList<Integer> xArray = inputObject.getCoordinates(HCObject.X);
            ArrayList<Integer> yArray = inputObject.getCoordinates(HCObject.Y);
            ArrayList<Integer> zArray = inputObject.getCoordinates(HCObject.Z);
            int x = (int) MeasureObjectCentroid.calculateCentroid(xArray,MeasureObjectCentroid.MEAN);
            int y = (int) MeasureObjectCentroid.calculateCentroid(yArray,MeasureObjectCentroid.MEAN);
            int z = (int) MeasureObjectCentroid.calculateCentroid(zArray,MeasureObjectCentroid.MEAN);

            // Getting time and channel coordinates
            int c = inputObject.getPosition(HCObject.C);
            int t = inputObject.getPosition(HCObject.T);

            // Getting the radius of the object
            int r;
            if (radiusMode.equals(FIXED_VALUE)) {
                r = (int) Math.ceil(parameters.getValue(RADIUS));
            } else {
                r = (int) Math.ceil(inputObject.getMeasurement(parameters.getValue(RADIUS_MEASUREMENT)).getValue());
            }

            // Getting the local image region
            if (x-2*r > 0 & x+2*r < inputImagePlus.getWidth() & y-2*r>0 & y+2*r<inputImagePlus.getHeight()) {
                inputImagePlus.setPosition(c+1, z+1, t+1);
                ImageProcessor ipr = inputImagePlus.getProcessor();
                int[] xx = new int[]{x - 2 * r, x - 2 * r, x + 2 * r, x + 2 * r, x - 2 * r};
                int[] yy = new int[]{y - 2 * r, y + 2 * r, y + 2 * r, y - 2 * r, y - 2 * r};
                Roi roi = new PolygonRoi(xx, yy, 5, Roi.POLYGON);
                ipr.setRoi(roi);
                ImageProcessor iprCrop = ipr.crop();

                // Estimating parameters
                double x0 = iprCrop.getWidth() / 2; // centroid x
                double y0 = iprCrop.getHeight() / 2; // centroid y
                double sx = r; // sigma x
                double sy = r; // sigma y
                double A0 = iprCrop.getStatistics().max; // peak amplitude
                double ABG = iprCrop.getStatistics().min; // background amplitude
                double th = 0; // theta

                double[] pIn = new double[]{x0, y0, sx, sy, A0, ABG, th};

                // Fitting the Gaussian
                double[] pOut = fitGaussian(iprCrop, pIn);

                x0 = pOut[0] + x-2*r;
                y0 = pOut[1] + y-2*r;
                sx = pOut[2];
                sy = pOut[3];
                A0 = pOut[4];
                ABG = pOut[5];
                th = pOut[6];
//                System.out.println("x0 = "+x0+", y0 = "+y0+", sx = "+sx+", sy = "+sy+", A0 = "+A0+", ABG = "+ABG+", th = "+(th*180/Math.PI));

                inputObject.addMeasurement(new HCMeasurement(X_0,x0,this));
                inputObject.addMeasurement(new HCMeasurement(Y_0,y0,this));
                inputObject.addMeasurement(new HCMeasurement(SIGMA_X,sx,this));
                inputObject.addMeasurement(new HCMeasurement(SIGMA_Y,sy,this));
                inputObject.addMeasurement(new HCMeasurement(A_0,A0,this));
                inputObject.addMeasurement(new HCMeasurement(A_BG,ABG,this));
                inputObject.addMeasurement(new HCMeasurement(THETA,th,this));

                System.out.println(x+", "+y+"_"+x0+", "+y0);

                if (parameters.getValue(UPDATE_LOCATION)) {
                    inputObject.setCoordinates(HCObject.X,(int) x0);
                    inputObject.setCoordinates(HCObject.Y,(int) y0);

                }

            } else {
                inputObject.addMeasurement(new HCMeasurement(X_0,Double.NaN,this));
                inputObject.addMeasurement(new HCMeasurement(Y_0,Double.NaN,this));
                inputObject.addMeasurement(new HCMeasurement(SIGMA_X,Double.NaN,this));
                inputObject.addMeasurement(new HCMeasurement(SIGMA_Y,Double.NaN,this));
                inputObject.addMeasurement(new HCMeasurement(A_0,Double.NaN,this));
                inputObject.addMeasurement(new HCMeasurement(A_BG,Double.NaN,this));
                inputObject.addMeasurement(new HCMeasurement(THETA,Double.NaN,this));

            }

            count++;

        }

        // Displaying fits (could be as either Gaussians or dots)

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(INPUT_OBJECTS,HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(RADIUS_MODE,HCParameter.CHOICE_ARRAY,FIXED_VALUE,RADIUS_MODES));
        parameters.addParameter(new HCParameter(RADIUS,HCParameter.DOUBLE,null));
        parameters.addParameter(new HCParameter(RADIUS_MEASUREMENT,HCParameter.MEASUREMENT,null));
        parameters.addParameter(new HCParameter(UPDATE_LOCATION,HCParameter.BOOLEAN,true));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(RADIUS_MODE));

        if (parameters.getValue(RADIUS_MODE) == FIXED_VALUE) {
            returnedParameters.addParameter(parameters.getParameter(RADIUS));

        } else if (parameters.getValue(RADIUS_MODE) == MEASUREMENT) {
            returnedParameters.addParameter(parameters.getParameter(RADIUS_MEASUREMENT));

        }

        returnedParameters.addParameter(parameters.getParameter(UPDATE_LOCATION));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {
        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        measurements.addMeasurement(inputObjectsName,X_0);
        measurements.addMeasurement(inputObjectsName,Y_0);
        measurements.addMeasurement(inputObjectsName,SIGMA_X);
        measurements.addMeasurement(inputObjectsName,SIGMA_Y);
        measurements.addMeasurement(inputObjectsName,A_0);
        measurements.addMeasurement(inputObjectsName,A_BG);
        measurements.addMeasurement(inputObjectsName,THETA);

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
