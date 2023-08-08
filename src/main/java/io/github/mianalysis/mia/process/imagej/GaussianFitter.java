package io.github.mianalysis.mia.process.imagej;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;

import ij.process.ImageProcessor;
import io.github.mianalysis.mia.process.math.GaussianDistribution2D;
import io.github.mianalysis.mia.process.math.Indexer;
import io.github.mianalysis.mia.process.math.Validator;

/**
 * Created by sc13967 on 05/06/2017.
 */
public class GaussianFitter {
//    public static void main(String[] args) {
//        new ImageJ();
//
//        ImagePlus iplReal = IJ.openImage("C:\\Users\\sc13967\\Desktop\\Crop.tif");
//        ImagePlus iplGauss = IJ.createImage("Gauss",iplReal.getWidth(),iplReal.getHeight(),1,8);
//        ImageProcessor iprReal = iplReal.getProcessor();
//
////        double x0, double y0, double sx, double sy, double A0, double ABG, double th
//        double[] pIn = new double[]{5.0,5.0,5.0,5.0,20.0,7.0,1.0};
//        double[][] limits = new double[][]{{0.0,11.0},
//                {0.0,11.0},{1.0E-50,1.7976931348623157E308},
//                {1.0E-50,1.7976931348623157E308},
//                {-1.7976931348623157E308,1.7976931348623157E308},
//                {-1.7976931348623157E308,1.7976931348623157E308},
//                {0.0,6.283185307179586}};
//        int maxEvaluations = 1000;
//
//        double[] output = fitGaussian2D(iprReal,pIn,limits,maxEvaluations);
////        double[] output = pIn;
//
//        if (output == null) {
//            System.err.println("No fit");
//            return;
//        }
//
//        GaussianDistribution2D fitDistribution2D = new GaussianDistribution2D(output[0],output[1],output[2],output[3],output[4],output[5],output[6]);
//
//        for (int xPos=0;xPos<iprReal.getWidth();xPos++) {
//            for (int yPos=0;yPos<iprReal.getHeight();yPos++) {
//                double realVal = iprReal.get(xPos,yPos);
//                double fitVal = fitDistribution2D.getValues(xPos,yPos)[0];
//
//                iplGauss.getProcessor().setf(xPos,yPos,(float) fitVal);
//
//            }
//        }
//
//        iplReal.duplicate().show();
//        iplGauss.duplicate().show();
//        IJ.runMacro("waitForUser");
//
//    }

    public static double[] fitGaussian2D(ImageProcessor ipr, double[] pIn, double[][] limits, int maxEvaluations) {
        int width = ipr.getWidth();
        int height = ipr.getHeight();

        // Populating the 3D vector matrix with the pixels from the ImagePlus.  We're treating intensity as a third
        // dimension (i.e. the image is a surface we will fit the 2D Gaussian function to).
        final Vector3D[] imagePoints = new Vector3D[width*height];
        Indexer indexer =  new Indexer(width,height);

        for (int x=0;x<width;x++) {
            for (int y=0;y<height;y++) {
                int idx = indexer.getIndex(new int[]{x,y});
                imagePoints[idx] = new Vector3D(x,y,ipr.getPixelValue(x,y));

            }
        }

        // The only element of p is the height
        MultivariateJacobianFunction model = new Model(imagePoints);

        // Starting point
        RealVector start = new ArrayRealVector(pIn);

        // Target residual (i.e. zero)
        double[] target = new double[imagePoints.length];
        for (int i=0;i<imagePoints.length;i++) {
            target[i] = imagePoints[i].getZ();
        }

        LeastSquaresBuilder lsqBuilder = new LeastSquaresBuilder();
        lsqBuilder.start(start);
        lsqBuilder.model(model);
        lsqBuilder.target(target);
        lsqBuilder.maxIterations(maxEvaluations);
        lsqBuilder.maxEvaluations(maxEvaluations);
        if (limits != null) {
            ParameterValidator validator = new Validator(new Array2DRowRealMatrix(limits));
            lsqBuilder.parameterValidator(validator);
        }

        LeastSquaresProblem lsqProblem= lsqBuilder.build();

        // Creating the optimiser
        LeastSquaresOptimizer optimizer = new LevenbergMarquardtOptimizer();

        // Doing the fitting and returning null if no solution was reached in the maximum number of evaluations
        LeastSquaresOptimizer.Optimum optimum;
        try {
            optimum = optimizer.optimize(lsqProblem);
        } catch(TooManyEvaluationsException e) {
            return null;
        }

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
}

class Model implements MultivariateJacobianFunction {
    private Vector3D[] imagePoints;

    Model(Vector3D[] imagePoints) {
        this.imagePoints = imagePoints;

    }

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

        GaussianDistribution2D distribution2D = new GaussianDistribution2D(x0,y0,sx,sy,A0,ABG,th);

        for (int i=0;i<imagePoints.length;i++) {
            // The current point to evaluate
            Vector3D currPoint = imagePoints[i];
            double x = currPoint.getX();
            double y = currPoint.getY();

            // The value of the function at this location
            double[] values = distribution2D.getValues(x,y);
            value.setEntry(i, values[0]);

            // Partial derivatives of the Gaussian function with respect to each parameter
            double ori = Math.exp(-values[1]*((x-x0)*(x-x0))-values[3]*((y-y0)*(y-y0))
                    -(Math.sin(2*th)/(2*sx*sx)-Math.sin(2*th)/(2*sy*sy))*(x-x0)*(y-y0));
            double j0 = A0*ori*((Math.sin(2*th)/(2*sx*sx)-Math.sin(2*th)/(2*sy*sy))*(y-y0)+values[1]*(2*x-2*x0));
            double j1 = A0*ori*((Math.sin(2*th)/(2*sx*sx)-Math.sin(2*th)/(2*sy*sy))*(x-x0)+values[3]*(2*y-2*y0));
            double j2 = A0*ori*(((Math.cos(th)*Math.cos(th))*((x-x0)*(x-x0))/(sx*sx*sx))
                    + ((Math.sin(th)*Math.sin(th))*((y-y0)*(y-y0)))/(sx*sx*sx)
                    + (Math.sin(2*th)*(x-x0)*(y-y0))/(sx*sx*sx));
            double j3 = A0*ori*(((Math.cos(th)*Math.cos(th))*((y-y0)*(y-y0)))/(sy*sy*sy)
                    + ((Math.sin(th)*Math.sin(th))*((x-x0)*(x-x0)))/(sy*sy*sy)
                    - (Math.sin(2*th)*(x-x0)*(y-y0))/(sy*sy*sy));
            double j4 = ori;
            double j5 = 1;
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