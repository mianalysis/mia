// TODO: Show original and fit PSFs - maybe as a mosaic - to demonstrate the process is working correctly

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;
import java.util.Iterator;

import static wbif.sjx.common.MathFunc.GaussianFitter.fitGaussian2D;

/**
 * Created by sc13967 on 05/06/2017.
 */
public class GaussianFitter2D extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String RADIUS_MODE = "Method to estimate spot radius";
    public static final String RADIUS = "Radius";
    public static final String RADIUS_MEASUREMENT = "Radius measurement";
    public static final String MEASUREMENT_MULTIPLIER = "Measurement multiplier";
    public static final String MAX_EVALUATIONS = "Maximum number of evaluations";
    public static final String REMOVE_UNFIT = "Remove objects with failed fitting";

    private static final String FIXED_VALUE = "Fixed value";
    private static final String MEASUREMENT = "Measurement";
    private static final String[] RADIUS_MODES = new String[]{FIXED_VALUE,MEASUREMENT};

    private static final String X_0 = "X_0";
    private static final String Y_0 = "Y_0";
    private static final String Z_0 = "Z_0_(CENTROID)";
    private static final String SIGMA_X = "SIGMA_X";
    private static final String SIGMA_Y = "SIGMA_Y";
    private static final String A_0 = "A_0";
    private static final String A_BG = "A_BG";
    private static final String THETA = "THETA";
    private static final String ELLIPTICITY = "ELLIPTICITY";



    @Override
    public String getTitle() {
        return "Fit Gaussian 2D";
    }

    @Override
    public String getHelp() {
        return "Gaussian spot fitting.  Can take objects as estimated locations." +
                "\n***Only works in 2D***" +
                "\n***Only works for refinement of existing spots***";
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting input objects to refine (if selected by used)
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjSet inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        String radiusMode = parameters.getValue(RADIUS_MODE);
        int maxEvaluations = parameters.getValue(MAX_EVALUATIONS);
        boolean removeUnfit = parameters.getValue(REMOVE_UNFIT);

        // Running through each object, doing the fitting
        int count = 0;
        int startingNumber = inputObjects.size();
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            if (verbose) System.out.println("["+moduleName+"] Fitting object "+(count+1)+" of "+startingNumber);
            count++;

            // Getting the centroid of the current object (should be single points anyway)
            ArrayList<Integer> xArray = inputObject.getCoordinates(Obj.X);
            ArrayList<Integer> yArray = inputObject.getCoordinates(Obj.Y);
            ArrayList<Integer> zArray = inputObject.getCoordinates(Obj.Z);
            int x = (int) MeasureObjectCentroid.calculateCentroid(xArray,MeasureObjectCentroid.MEAN);
            int y = (int) MeasureObjectCentroid.calculateCentroid(yArray,MeasureObjectCentroid.MEAN);
            int z = (int) MeasureObjectCentroid.calculateCentroid(zArray,MeasureObjectCentroid.MEAN);

            // Getting time and channel coordinates
            int c = inputObject.getPosition(Obj.C);
            int t = inputObject.getPosition(Obj.T);

            // Getting the radius of the object
            int r;
            if (radiusMode.equals(FIXED_VALUE)) {
                r = (int) Math.ceil(parameters.getValue(RADIUS));
            } else {
                double multiplier = parameters.getValue(MEASUREMENT_MULTIPLIER);
                r = (int) Math.ceil(inputObject.getMeasurement(parameters.getValue(RADIUS_MEASUREMENT)).getValue()*multiplier);

            }

            double x0 = Double.NaN;
            double y0 = Double.NaN;
            double z0 = Double.NaN;
            double sx = Double.NaN;
            double sy = Double.NaN;
            double A0 = Double.NaN;
            double ABG = Double.NaN;
            double th = Double.NaN;
            double ellipticity = Double.NaN;
            double[] pOut = null;

            // Setting limits
            double[][] limits = new double[][]{
                    {0, 2*r+1},
                    {0, 2*r+1},
                    {1E-50, Double.MAX_VALUE}, // Sigma can't go to zero
                    {1E-50, Double.MAX_VALUE},
                    {Double.MIN_VALUE, Double.MAX_VALUE},
                    {Double.MIN_VALUE, Double.MAX_VALUE},
                    {0, 2*Math.PI}
            };

            // Getting the local image region
            if (x-r > 0 & x+r+1 < inputImagePlus.getWidth() & y-r>0 & y+r+1 < inputImagePlus.getHeight()) {
                inputImagePlus.setPosition(c+1, z+1, t+1);
                ImageProcessor ipr = inputImagePlus.getProcessor();
                int[] xx = new int[]{x - r, x - r, x + r + 1, x + r + 1, x - r};
                int[] yy = new int[]{y - r, y + r + 1, y + r + 1, y - r, y - r};
                Roi roi = new PolygonRoi(xx, yy, 5, Roi.POLYGON);
                ipr.setRoi(roi);
                ImageProcessor iprCrop = ipr.crop();

                // Estimating parameters
                x0 = iprCrop.getWidth() / 2; // centroid x
                y0 = iprCrop.getHeight() / 2; // centroid y
                sx = r; // sigma x
                sy = r; // sigma y
                A0 = iprCrop.getStatistics().max; // peak amplitude
                ABG = iprCrop.getStatistics().min; // background amplitude
                th = 0; // theta

                double[] pIn = new double[]{x0, y0, sx, sy, A0, ABG, th};

                // Fitting the Gaussian and checking it reached convergence
                pOut = fitGaussian2D(iprCrop, pIn, limits, maxEvaluations);
                if (pOut != null) {
                    x0 = pOut[0] + x - r;
                    y0 = pOut[1] + y - r;
                    z0 = MeasureObjectCentroid.calculateCentroid(inputObject.getCoordinates(Obj.Z), MeasureObjectCentroid.MEAN);
                    sx = pOut[2];
                    sy = pOut[3];
                    A0 = pOut[4];
                    ABG = pOut[5];
                    th = pOut[6];
                    ellipticity = sx > sy ? (sx - sy) / sx : (sy - sx) / sy;

//                    iprCrop = iprCrop.convertToFloatProcessor();
//                    ImageProcessor iprOut = iprCrop.duplicate();
//                    for (int xPx=0;xPx<iprOut.getWidth();xPx++) {
//                        for (int yPx = 0; yPx < iprOut.getHeight(); yPx++) {
//                            double aa = (Math.cos(th) * Math.cos(th)) / (2 * sx * sx) + (Math.sin(th) * Math.sin(th)) / (2 * sy * sy);
//                            double bb = Math.sin(2 * th) / (4 * sy * sy) - Math.sin(2 * th) / (4 * sx * sx);
//                            double cc = (Math.cos(th) * Math.cos(th)) / (2 * sy * sy) + (Math.sin(th) * Math.sin(th)) / (2 * sx * sx);
//                            double val = ABG + A0 * Math.exp(-(aa * ((xPx - pOut[0]) * (xPx - pOut[0])) - 2 * bb * (xPx - pOut[0]) * (yPx - pOut[1]) + cc * ((yPx - pOut[1]) * (yPx - pOut[1]))));
//                            iprOut.putPixelValue(xPx, yPx, val);
//                        }
//                    }
//                    ImagePlus iplStack = IJ.createImage("Stack",iprOut.getWidth(),iprOut.getHeight(),2,32);
//                    iplStack.setPosition(1);
//                    iplStack.setProcessor(iprCrop);
//                    iplStack.setPosition(2);
//                    iplStack.setProcessor(iprOut);
//                    iplStack.setPosition(1);
//                    iplStack.show();
//                    IJ.runMacro("waitForUser");

                }
            }

            // Storing the results as measurements
            inputObject.addMeasurement(new MIAMeasurement(X_0,x0,this));
            inputObject.addMeasurement(new MIAMeasurement(Y_0,y0,this));
            inputObject.addMeasurement(new MIAMeasurement(Z_0,z0,this));
            inputObject.addMeasurement(new MIAMeasurement(SIGMA_X,sx,this));
            inputObject.addMeasurement(new MIAMeasurement(SIGMA_Y,sy,this));
            inputObject.addMeasurement(new MIAMeasurement(A_0,A0,this));
            inputObject.addMeasurement(new MIAMeasurement(A_BG,ABG,this));
            inputObject.addMeasurement(new MIAMeasurement(THETA,th,this));
            inputObject.addMeasurement(new MIAMeasurement(ELLIPTICITY,ellipticity,this));

            // If selected, any objects that weren't fit are removed
            if (removeUnfit & pOut == null) {
                inputObject.removeRelationships();
                iterator.remove();
            }
        }

        if (verbose) System.out.println("["+moduleName+"] Fit "+inputObjects.size()+" objects");

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(RADIUS_MODE, Parameter.CHOICE_ARRAY,FIXED_VALUE,RADIUS_MODES));
        parameters.addParameter(new Parameter(RADIUS, Parameter.DOUBLE,null));
        parameters.addParameter(new Parameter(RADIUS_MEASUREMENT, Parameter.MEASUREMENT,null));
        parameters.addParameter(new Parameter(MEASUREMENT_MULTIPLIER, Parameter.DOUBLE,1.0));
        parameters.addParameter(new Parameter(MAX_EVALUATIONS, Parameter.INTEGER,1000));
        parameters.addParameter(new Parameter(REMOVE_UNFIT, Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(RADIUS_MODE));

        if (parameters.getValue(RADIUS_MODE).equals(FIXED_VALUE)) {
            returnedParameters.addParameter(parameters.getParameter(RADIUS));

        } else if (parameters.getValue(RADIUS_MODE).equals(MEASUREMENT)) {
            returnedParameters.addParameter(parameters.getParameter(RADIUS_MEASUREMENT));
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            parameters.updateValueSource(RADIUS_MEASUREMENT,inputObjectsName);
            returnedParameters.addParameter(parameters.getParameter(MEASUREMENT_MULTIPLIER));

        }

        returnedParameters.addParameter(parameters.getParameter(MAX_EVALUATIONS));
        returnedParameters.addParameter(parameters.getParameter(REMOVE_UNFIT));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        measurements.addMeasurement(inputObjectsName,X_0);
        measurements.addMeasurement(inputObjectsName,Y_0);
        measurements.addMeasurement(inputObjectsName,Z_0);
        measurements.addMeasurement(inputObjectsName,SIGMA_X);
        measurements.addMeasurement(inputObjectsName,SIGMA_Y);
        measurements.addMeasurement(inputObjectsName,A_0);
        measurements.addMeasurement(inputObjectsName,A_BG);
        measurements.addMeasurement(inputObjectsName,THETA);
        measurements.addMeasurement(inputObjectsName,ELLIPTICITY);

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}