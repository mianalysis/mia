// TODO: Show original and fit PSFs - maybe as a mosaic - to demonstrate the processAutomatic is working correctly

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification.GetLocalObjectRegion;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.MathFunc.GaussianDistribution2D;

import java.util.Iterator;

import static wbif.sjx.common.MathFunc.GaussianFitter.fitGaussian2D;

/**
 * Created by sc13967 on 05/06/2017.
 */
public class FitGaussian2D extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String RADIUS_MODE = "Method to estimate spot radius";
    public static final String RADIUS = "Radius";
    public static final String RADIUS_MEASUREMENT = "Radius measurement";
    public static final String MEASUREMENT_MULTIPLIER = "Measurement multiplier";
    public static final String LIMIT_SIGMA_RANGE = "Limit sigma range";
    public static final String MIN_SIGMA = "Minimum sigma (x Radius)";
    public static final String MAX_SIGMA = "Maximum sigma (x Radius)";
    public static final String FIXED_FITTING_WINDOW = "Fixed fitting window";
    public static final String WINDOW_SIZE = "Window size";
    public static final String MAX_EVALUATIONS = "Maximum number of evaluations";
    public static final String REMOVE_UNFIT = "Remove objects with failed fitting";
    public static final String APPLY_VOLUME = "Apply volume";

    public interface RadiusModes {
        String FIXED_VALUE = "Fixed value";
        String MEASUREMENT = "Measurement";

        String[] ALL = new String[]{FIXED_VALUE, MEASUREMENT};

    }

    public interface Measurements {
        String X0_PX = "GAUSSFIT2D // X0_(PX)";
        String Y0_PX = "GAUSSFIT2D // Y0_(PX)";
        String Z0_SLICE = "GAUSSFIT2D // Z0_(SLICE)_(CENTROID)";
        String SIGMA_X_PX = "GAUSSFIT2D // SIGMA_X_(PX)";
        String SIGMA_Y_PX = "GAUSSFIT2D // SIGMA_Y_(PX)";
        String SIGMA_MEAN_PX = "GAUSSFIT2D // SIGMA_MEAN_(PX)";
        String X0_CAL = "GAUSSFIT2D // X0_(${CAL})";
        String Y0_CAL = "GAUSSFIT2D // Y0_(${CAL})";
        String Z0_CAL = "GAUSSFIT2D // Z0_(${CAL})_(CENTROID)";
        String SIGMA_X_CAL = "GAUSSFIT2D // SIGMA_X_(${CAL})";
        String SIGMA_Y_CAL = "GAUSSFIT2D // SIGMA_Y_(${CAL})";
        String SIGMA_MEAN_CAL = "GAUSSFIT2D // SIGMA_MEAN_(${CAL})";
        String A_0 = "GAUSSFIT2D // A_0";
        String A_BG = "GAUSSFIT2D // A_BG";
        String THETA = "GAUSSFIT2D // THETA";
        String ELLIPTICITY = "GAUSSFIT2D // ELLIPTICITY";
        String RESIDUAL = "GAUSSFIT2D // RESIDUAL_(NORM)";

    }


    @Override
    public String getTitle() {
        return "Fit Gaussian 2D";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getHelp() {
        return "Gaussian spot fitting.  Can take objects as estimated locations." +
                "\n***Only works in 2D***" +
                "\n***Only works for refinement of existing spots***";
    }

    @Override
    public boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();
        inputImagePlus = new Duplicator().run(inputImagePlus);

        // Getting calibration
        double distPerPxXY = inputImagePlus.getCalibration().pixelWidth;
        double distPerPxZ = inputImagePlus.getCalibration().pixelDepth;

        // Getting input objects to refine (if selected by used)
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        String radiusMode = parameters.getValue(RADIUS_MODE);
        boolean limitSigma = parameters.getValue(LIMIT_SIGMA_RANGE);
        double minSigma = parameters.getValue(MIN_SIGMA);
        double maxSigma = parameters.getValue(MAX_SIGMA);
        boolean fixedFittingWindow = parameters.getValue(FIXED_FITTING_WINDOW);
        int windowWidth = parameters.getValue(WINDOW_SIZE);
        int maxEvaluations = parameters.getValue(MAX_EVALUATIONS);
        boolean removeUnfit = parameters.getValue(REMOVE_UNFIT);
        boolean applyVolume = parameters.getValue(APPLY_VOLUME);

        // Setting the desired values to limit sigma
        if (!limitSigma) {
            minSigma = 1E-50;
            maxSigma = Double.MAX_VALUE;
        }

        // Running through each object, doing the fitting
        int count = 0;
        int startingNumber = inputObjects.size();
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            writeMessage("Fitting object " + (count + 1) + " of " + startingNumber);
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
            double residual = Double.NaN;
            double[] pOut = null;

            // Setting limits
            double[][] limits = new double[][]{
                    {0, 2 * r + 1},
                    {0, 2 * r + 1},
                    {minSigma, maxSigma}, // Sigma can't go to zero
                    {minSigma, maxSigma},
                    {-Double.MAX_VALUE, Double.MAX_VALUE},
                    {-Double.MAX_VALUE, Double.MAX_VALUE},
                    {0, 2 * Math.PI}
            };

            // Ensuring the window width is odd, then getting the half width
            if (windowWidth%2!=0) windowWidth--;
            int halfW = fixedFittingWindow ? windowWidth/2 : r;

            // Getting the local image region
            if (x - halfW > 0 & x + halfW + 1 < inputImagePlus.getWidth() & y - halfW > 0 & y + halfW + 1 < inputImagePlus.getHeight()) {
                inputImagePlus.setPosition(1, z + 1, t + 1);
                ImageProcessor ipr = inputImagePlus.getProcessor();

                int[] xx = new int[]{x - halfW, x - halfW, x + halfW + 1, x + halfW + 1, x - halfW};
                int[] yy = new int[]{y - halfW, y + halfW + 1, y + halfW + 1, y - halfW, y - halfW};

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

                    GaussianDistribution2D fitDistribution2D = new GaussianDistribution2D(pOut[0],pOut[1],sx,sy,A0,ABG,th);
                    GaussianDistribution2D offsetDistribution2D = new GaussianDistribution2D(pOut[0],pOut[1],sx,sy,A0-ABG,0,th);
                    residual = 0;
                    double totalReal = 0;
                    for (int xPos=0;xPos<iprCrop.getWidth();xPos++) {
                        for (int yPos=0;yPos<iprCrop.getHeight();yPos++) {
                            double realVal = iprCrop.get(xPos,yPos);
                            double fitVal = fitDistribution2D.getValues(xPos,yPos)[0];
                            double offsetVal = offsetDistribution2D.getValues(xPos,yPos)[0];

                            residual = residual + Math.abs(realVal-fitVal);
                            totalReal = totalReal + offsetVal;

                        }
                    }

                    residual = residual/totalReal;

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

                if (pOut != null) {
                    // If the centroid has moved more than the width of the window, removing this localisation
                    if (pOut[0] <= 1 || pOut[0] >= r * 2 || pOut[1] <= 1 || pOut[1] >= r * 2 || pOut[2] < 0.1 || pOut[3] < 0.1) {
                        pOut = null;
                    }

                    // If the width is outside the permitted range
                    if (limitSigma && ((sx+sy)/2 < r*minSigma || (sx+sy)/2 > r*maxSigma)) {
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
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.X0_CAL), x0*distPerPxXY));
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.Y0_CAL), y0*distPerPxXY));
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.Z0_CAL), z0*distPerPxZ));
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.SIGMA_X_CAL), sx*distPerPxXY));
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.SIGMA_Y_CAL), sy*distPerPxXY));
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.SIGMA_MEAN_CAL), sm*distPerPxXY));
            inputObject.addMeasurement(new Measurement(Measurements.A_0, A0));
            inputObject.addMeasurement(new Measurement(Measurements.A_BG, ABG));
            inputObject.addMeasurement(new Measurement(Measurements.THETA, th));
            inputObject.addMeasurement(new Measurement(Measurements.ELLIPTICITY, ellipticity));
            inputObject.addMeasurement(new Measurement(Measurements.RESIDUAL, residual));

            // If selected, any objects that weren't fit are removed
            if (removeUnfit & pOut == null) {
                inputObject.removeRelationships();
                iterator.remove();
            }
        }

        // Adding explicit volume to spots
        count = 0;
        startingNumber = inputObjects.size();
        if (applyVolume) {
            try {
                new GetLocalObjectRegion().getLocalRegions(inputObjects,"SpotVolume",inputImagePlus,true,Measurements.SIGMA_X_PX,0,false);
            } catch (IntegerOverflowException e) {
                return false;
            }

            // Replacing spot volumes with explicit volume
            for (Obj spotObject:inputObjects.values()) {
                Obj spotVolumeObject = spotObject.getChildren("SpotVolume").values().iterator().next();

                spotObject.setPoints(spotVolumeObject.getPoints());
            }
        }

        writeMessage("Fit "+inputObjects.size()+" objects");

        inputImagePlus.setPosition(1,1,1);

        if (showOutput) inputObjects.showMeasurements(this);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(RADIUS_MODE, this,RadiusModes.FIXED_VALUE,RadiusModes.ALL));
        parameters.add(new DoubleP(RADIUS, this,1.0));
        parameters.add(new ObjectMeasurementP(RADIUS_MEASUREMENT,this));
        parameters.add(new DoubleP(MEASUREMENT_MULTIPLIER, this,1.0));
        parameters.add(new BooleanP(LIMIT_SIGMA_RANGE, this,true));
        parameters.add(new DoubleP(MIN_SIGMA, this,0.25));
        parameters.add(new DoubleP(MAX_SIGMA, this,4d));
        parameters.add(new BooleanP(FIXED_FITTING_WINDOW,this,false));
        parameters.add(new IntegerP(WINDOW_SIZE,this,15));
        parameters.add(new IntegerP(MAX_EVALUATIONS, this,1000));
        parameters.add(new BooleanP(REMOVE_UNFIT, this,false));
        parameters.add(new BooleanP(APPLY_VOLUME,this,true));

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
            ((ObjectMeasurementP) parameters.getParameter(RADIUS_MEASUREMENT)).setObjectName(inputObjectsName);
            returnedParameters.add(parameters.getParameter(MEASUREMENT_MULTIPLIER));

        }

        returnedParameters.add(parameters.getParameter(LIMIT_SIGMA_RANGE));
        if (parameters.getValue(LIMIT_SIGMA_RANGE)) {
            returnedParameters.add(parameters.getParameter(MIN_SIGMA));
            returnedParameters.add(parameters.getParameter(MAX_SIGMA));
        }

        returnedParameters.add(parameters.getParameter(FIXED_FITTING_WINDOW));
        if (parameters.getValue(FIXED_FITTING_WINDOW)) {
            returnedParameters.add(parameters.getParameter(WINDOW_SIZE));
        }

        returnedParameters.add(parameters.getParameter(MAX_EVALUATIONS));
        returnedParameters.add(parameters.getParameter(REMOVE_UNFIT));
        returnedParameters.add(parameters.getParameter(APPLY_VOLUME));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        objectMeasurementRefs.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        MeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.X0_PX);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Measurements.Y0_PX);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Measurements.Z0_SLICE);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Measurements.SIGMA_X_PX);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Measurements.SIGMA_Y_PX);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Measurements.SIGMA_MEAN_PX);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.X0_CAL));
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.Y0_CAL));
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.Z0_CAL));
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.SIGMA_X_CAL));
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.SIGMA_Y_CAL));
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.SIGMA_MEAN_CAL));
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Measurements.A_0);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Measurements.A_BG);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Measurements.THETA);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Measurements.ELLIPTICITY);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Measurements.RESIDUAL);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        return objectMeasurementRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}

//when signax_0:sigmay_o is >1.5, delete spot... i - 1 ;