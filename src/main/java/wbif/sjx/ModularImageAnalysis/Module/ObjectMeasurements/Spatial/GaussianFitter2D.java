// TODO: Show original and fit PSFs - maybe as a mosaic - to demonstrate the process is working correctly

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification.GetLocalObjectRegion;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.Iterator;

import static wbif.sjx.common.MathFunc.GaussianFitter.fitGaussian2D;

/**
 * Created by sc13967 on 05/06/2017.
 */
public class GaussianFitter2D extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String RADIUS_MODE = "Method to estimate spot radius";
    public static final String RADIUS = "Radius";
    public static final String RADIUS_MEASUREMENT = "Radius measurement";
    public static final String MEASUREMENT_MULTIPLIER = "Measurement multiplier";
    public static final String MAX_EVALUATIONS = "Maximum number of evaluations";
    public static final String REMOVE_UNFIT = "Remove objects with failed fitting";
    public static final String APPLY_VOLUME = "Apply volume";

    public interface RadiusModes {
        String FIXED_VALUE = "Fixed value";
        String MEASUREMENT = "Measurement";

        String[] ALL = new String[]{FIXED_VALUE, MEASUREMENT};

    }
    
    public interface Measurements {
        String X0_PX = "GAUSSFIT2D//X0_PX";
        String Y0_PX = "GAUSSFIT2D//Y0_PX";
        String Z0_SLICE = "GAUSSFIT2D//Z0_SLICE_(CENTROID)";
        String SIGMA_X_PX = "GAUSSFIT2D//SIGMA_X_PX";
        String SIGMA_Y_PX = "GAUSSFIT2D//SIGMA_Y_PX";
        String SIGMA_MEAN_PX = "GAUSSFIT2D//SIGMA_MEAN_PX";
        String X0_CAL = "GAUSSFIT2D//X0_CAL";
        String Y0_CAL = "GAUSSFIT2D//Y0_CAL";
        String Z0_CAL = "GAUSSFIT2D//Z0_CAL_(CENTROID)";
        String SIGMA_X_CAL = "GAUSSFIT2D//SIGMA_X_CAL";
        String SIGMA_Y_CAL = "GAUSSFIT2D//SIGMA_Y_CAL";
        String SIGMA_MEAN_CAL = "GAUSSFIT2D//SIGMA_MEAN_CAL";
        String A_0 = "GAUSSFIT2D//A_0";
        String A_BG = "GAUSSFIT2D//A_BG";
        String THETA = "GAUSSFIT2D//THETA";
        String ELLIPTICITY = "GAUSSFIT2D//ELLIPTICITY";

    }
    

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
    public void run(Workspace workspace, boolean verbose) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting calibration
        double distPerPxXY = inputImagePlus.getCalibration().pixelWidth;
        double distPerPxZ = inputImagePlus.getCalibration().pixelDepth;

        // Getting input objects to refine (if selected by used)
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        String radiusMode = parameters.getValue(RADIUS_MODE);
        int maxEvaluations = parameters.getValue(MAX_EVALUATIONS);
        boolean removeUnfit = parameters.getValue(REMOVE_UNFIT);
        boolean applyVolume = parameters.getValue(APPLY_VOLUME);

        // Running through each object, doing the fitting
        int count = 0;
        int startingNumber = inputObjects.size();
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            if (verbose)
                System.out.println("[" + moduleName + "] Fitting object " + (count + 1) + " of " + startingNumber);
            count++;

            // Getting the centroid of the current object (should be single points anyway)
            int x = (int) Math.round(inputObject.getXMean(true));
            int y = (int) Math.round(inputObject.getYMean(true));
            int z = (int) Math.round(inputObject.getZMean(true, false));

            // Getting time and channel coordinates
            int t = inputObject.getT();

            // Getting the radius of the object
            int r;
            if (radiusMode.equals(RadiusModes.FIXED_VALUE)) {
                r = (int) Math.ceil(parameters.getValue(RADIUS));
            } else {
                double multiplier = parameters.getValue(MEASUREMENT_MULTIPLIER);
                r = (int) Math.ceil(inputObject.getMeasurement(parameters.getValue(RADIUS_MEASUREMENT)).getValue() * multiplier);

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
                    {0, 2 * r + 1},
                    {0, 2 * r + 1},
                    {1E-50, Double.MAX_VALUE}, // Sigma can't go to zero
                    {1E-50, Double.MAX_VALUE},
//                    {r*0.3, r*3}, // Sigma can't go to zero
//                    {r*0.3, r*3},
                    {Double.MIN_VALUE, Double.MAX_VALUE},
                    {Double.MIN_VALUE, Double.MAX_VALUE},
                    {0, 2 * Math.PI}
            };

            // Getting the local image region
            if (x - r > 0 & x + r + 1 < inputImagePlus.getWidth() & y - r > 0 & y + r + 1 < inputImagePlus.getHeight()) {
                inputImagePlus.setPosition(1, z + 1, t + 1);
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
                    z0 = inputObject.getZMean(true, false);
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

                // If the centroid has moved more than the width of the window, removing this localisation
                if (pOut != null) {
                    if (pOut[0] <= 1 || pOut[0] >= r * 2 || pOut[1] <= 1 || pOut[1] >= r * 2 || pOut[2] < 0.1 || pOut[3] < 0.1) {
                        pOut = null;
                    }
                }
            }

            double sm = (sx+sy)/2;

            // Storing the results as measurements
            inputObject.addMeasurement(new Measurement(Measurements.X0_PX, x0));
            inputObject.addMeasurement(new Measurement(Measurements.Y0_PX, y0));
            inputObject.addMeasurement(new Measurement(Measurements.Z0_SLICE, z0));
            inputObject.addMeasurement(new Measurement(Measurements.SIGMA_X_PX, sx));
            inputObject.addMeasurement(new Measurement(Measurements.SIGMA_Y_PX, sy));
            inputObject.addMeasurement(new Measurement(Measurements.SIGMA_MEAN_PX, sm));
            inputObject.addMeasurement(new Measurement(Measurements.X0_CAL, x0*distPerPxXY));
            inputObject.addMeasurement(new Measurement(Measurements.Y0_CAL, y0*distPerPxXY));
            inputObject.addMeasurement(new Measurement(Measurements.Z0_CAL, z0*distPerPxZ));
            inputObject.addMeasurement(new Measurement(Measurements.SIGMA_X_CAL, sx*distPerPxXY));
            inputObject.addMeasurement(new Measurement(Measurements.SIGMA_Y_CAL, sy*distPerPxXY));
            inputObject.addMeasurement(new Measurement(Measurements.SIGMA_MEAN_CAL, sm*distPerPxXY));
            inputObject.addMeasurement(new Measurement(Measurements.A_0, A0));
            inputObject.addMeasurement(new Measurement(Measurements.A_BG, ABG));
            inputObject.addMeasurement(new Measurement(Measurements.THETA, th));
            inputObject.addMeasurement(new Measurement(Measurements.ELLIPTICITY, ellipticity));

            // If selected, any objects that weren't fit are removed
            if (removeUnfit & pOut == null) {
                inputObject.removeRelationships();
                iterator.remove();
            }
        }

        // Adding explicit volume to spots
        if (applyVolume) {
            GetLocalObjectRegion.getLocalRegions(inputObjects,"SpotVolume",0,false,true, Measurements.SIGMA_X_PX);

            // Replacing spot volumes with explicit volume
            for (Obj spotObject:inputObjects.values()) {
                Obj spotVolumeObject = spotObject.getChildren("SpotVolume").values().iterator().next();

                spotObject.setPoints(spotVolumeObject.getPoints());
            }
        }

        if (verbose) System.out.println("["+moduleName+"] Fit "+inputObjects.size()+" objects");

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(RADIUS_MODE, Parameter.CHOICE_ARRAY,RadiusModes.FIXED_VALUE,RadiusModes.ALL));
        parameters.add(new Parameter(RADIUS, Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(RADIUS_MEASUREMENT, Parameter.OBJECT_MEASUREMENT,null));
        parameters.add(new Parameter(MEASUREMENT_MULTIPLIER, Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(MAX_EVALUATIONS, Parameter.INTEGER,1000));
        parameters.add(new Parameter(REMOVE_UNFIT, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(APPLY_VOLUME,Parameter.BOOLEAN,true));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.X0_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.Y0_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.Z0_SLICE));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.SIGMA_X_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.SIGMA_Y_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.SIGMA_MEAN_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.X0_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.Y0_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.Z0_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.SIGMA_X_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.SIGMA_Y_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.SIGMA_MEAN_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.A_0));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.A_BG));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.THETA));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.ELLIPTICITY));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(RADIUS_MODE));

        if (parameters.getValue(RADIUS_MODE).equals(RadiusModes.FIXED_VALUE)) {
            returnedParameters.add(parameters.getParameter(RADIUS));

        } else if (parameters.getValue(RADIUS_MODE).equals(RadiusModes.MEASUREMENT)) {
            returnedParameters.add(parameters.getParameter(RADIUS_MEASUREMENT));
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            parameters.updateValueSource(RADIUS_MEASUREMENT,inputObjectsName);
            returnedParameters.add(parameters.getParameter(MEASUREMENT_MULTIPLIER));

        }

        returnedParameters.add(parameters.getParameter(MAX_EVALUATIONS));
        returnedParameters.add(parameters.getParameter(REMOVE_UNFIT));
        returnedParameters.add(parameters.getParameter(APPLY_VOLUME));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        objectMeasurementReferences.updateImageObjectName(Measurements.X0_PX,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.Y0_PX,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.Z0_SLICE,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.SIGMA_X_PX,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.SIGMA_Y_PX,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.SIGMA_MEAN_PX,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.X0_CAL,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.Y0_CAL,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.Z0_CAL,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.SIGMA_X_CAL,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.SIGMA_Y_CAL,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.SIGMA_MEAN_CAL,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.A_0,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.A_BG,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.THETA,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.ELLIPTICITY,inputObjectsName);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}

//when signax_0:sigmay_o is >1.5, delete spot... i - 1 ;