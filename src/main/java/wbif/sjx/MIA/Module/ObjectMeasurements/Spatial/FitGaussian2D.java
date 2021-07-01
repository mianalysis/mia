// TODO: Show original and fit PSFs - maybe as a mosaic - to demonstrate the processAutomatic is working correctly

package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import static wbif.sjx.common.MathFunc.GaussianFitter.fitGaussian2D;

import java.util.Iterator;

import org.eclipse.sisu.Nullable;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ImageMath;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.CropImage;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.GetLocalObjectRegion;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.MathFunc.GaussianDistribution2D;
import wbif.sjx.common.Object.Point;

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
    public static final String CREATE_GAUSSIAN_IMAGE = "Create Gaussian image";
    public static final String GAUSSIAN_IMAGE = "Gaussian image name";


    public FitGaussian2D(ModuleCollection modules) {
        super("Fit Gaussian 2D",modules);
    }

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
        String X0_CAL = "GAUSSFIT2D // X0_(${SCAL})";
        String Y0_CAL = "GAUSSFIT2D // Y0_(${SCAL})";
        String Z0_CAL = "GAUSSFIT2D // Z0_(${SCAL})_(CENTROID)";
        String SIGMA_X_CAL = "GAUSSFIT2D // SIGMA_X_(${SCAL})";
        String SIGMA_Y_CAL = "GAUSSFIT2D // SIGMA_Y_(${SCAL})";
        String SIGMA_MEAN_CAL = "GAUSSFIT2D // SIGMA_MEAN_(${SCAL})";
        String A_0 = "GAUSSFIT2D // A_0";
        String A_BG = "GAUSSFIT2D // A_BG";
        String THETA = "GAUSSFIT2D // THETA";
        String ELLIPTICITY = "GAUSSFIT2D // ELLIPTICITY";
        String RESIDUAL = "GAUSSFIT2D // RESIDUAL_(NORM)";

    }


    static double[][] getLimits(int r, double minSigma, double maxSigma) {
        return new double[][]{
                {0, 2 * r + 1},
                {0, 2 * r + 1},
                {minSigma, maxSigma}, // Sigma can't go to zero
                {minSigma, maxSigma},
                {-Double.MAX_VALUE, Double.MAX_VALUE},
                {-Double.MAX_VALUE, Double.MAX_VALUE},
                {0, 2 * Math.PI}
        };
    }

    static double[] estimateParameters(ImageProcessor iprCrop, int r) {
        double x0 = iprCrop.getWidth() / 2; // centroid x
        double y0 = iprCrop.getHeight() / 2; // centroid y
        double A0 = iprCrop.getStatistics().max; // peak amplitude
        double ABG = iprCrop.getStatistics().min; // background amplitude
        double th = Math.PI; // theta

        return new double[]{x0, y0, r, r, A0, ABG, th};

    }

    static double calculateResidual(ImageProcessor iprCrop, double[] p) {
        GaussianDistribution2D fitDistribution2D = new GaussianDistribution2D(p[0],p[1],p[2],p[3],p[4],p[5],p[6]);
        GaussianDistribution2D offsetDistribution2D = new GaussianDistribution2D(p[0],p[1],p[2],p[3],p[4]-p[5],0,p[6]);

        double residual = 0;
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

        return residual/totalReal;

    }

    static void assignVolume(ObjCollection objects) {
        // Replacing spot volumes with explicit volume
        ObjCollection tempObjects = new ObjCollection("SpotVolume",objects);
        for (Obj spotObject:objects.values()) {
            int radius = (int) Math.round(spotObject.getMeasurement(Measurements.SIGMA_X_PX).getValue());
            Point<Double> cent = spotObject.getMeanCentroid(true,false);
            int[] centroid = new int[] {(int) Math.round(cent.getX()),(int) Math.round(cent.getY()),(int) Math.round(cent.getZ())};
            Obj volumeObject = GetLocalObjectRegion.getLocalRegion(spotObject, tempObjects, centroid, radius, false);
            spotObject.getCoordinateSet().clear();
            spotObject.getCoordinateSet().addAll(volumeObject.getCoordinateSet());
            spotObject.clearSurface();
            spotObject.clearCentroid();
            spotObject.clearProjected();
            spotObject.clearROIs();

        }
    }

    static void assignMissingMeasurements(Obj obj) {
        obj.addMeasurement(new Measurement(Measurements.X0_PX, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.Y0_PX, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.Z0_SLICE, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.SIGMA_X_PX, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.SIGMA_Y_PX, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.SIGMA_MEAN_PX, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.X0_CAL, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.Y0_CAL, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.Z0_CAL, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.SIGMA_X_CAL, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.SIGMA_Y_CAL, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.SIGMA_MEAN_CAL, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.A_0, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.A_BG, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.THETA, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.ELLIPTICITY, Double.NaN));
        obj.addMeasurement(new Measurement(Measurements.RESIDUAL, Double.NaN));

    }

    static void assignMeasurements(Obj obj, ImageProcessor iprCrop, double[] p, int x, int y, int r) {
        double distPerPxXY = obj.getDppXY();
        double distPerPxZ = obj.getDppZ();

        double x0 = p[0] + x - r;
        double y0 = p[1] + y - r;
        double z0 = obj.getZMean(true, false);
        double sx = p[2];
        double sy = p[3];
        double A0 = p[4];
        double ABG = p[5];
        double th = p[6];
        double residual = calculateResidual(iprCrop,p);
        double ellipticity = sx > sy ? (sx - sy) / sx : (sy - sx) / sy;
        double sm = (sx+sy)/2;

        // Storing the results as measurements
        obj.addMeasurement(new Measurement(Measurements.X0_PX, x0));
        obj.addMeasurement(new Measurement(Measurements.Y0_PX, y0));
        obj.addMeasurement(new Measurement(Measurements.Z0_SLICE, z0));
        obj.addMeasurement(new Measurement(Measurements.SIGMA_X_PX, sx));
        obj.addMeasurement(new Measurement(Measurements.SIGMA_Y_PX, sy));
        obj.addMeasurement(new Measurement(Measurements.SIGMA_MEAN_PX, sm));
        obj.addMeasurement(new Measurement(Measurements.X0_CAL, x0*distPerPxXY));
        obj.addMeasurement(new Measurement(Measurements.Y0_CAL, y0*distPerPxXY));
        obj.addMeasurement(new Measurement(Measurements.Z0_CAL, z0*distPerPxZ));
        obj.addMeasurement(new Measurement(Measurements.SIGMA_X_CAL, sx*distPerPxXY));
        obj.addMeasurement(new Measurement(Measurements.SIGMA_Y_CAL, sy*distPerPxXY));
        obj.addMeasurement(new Measurement(Measurements.SIGMA_MEAN_CAL, sm*distPerPxXY));
        obj.addMeasurement(new Measurement(Measurements.A_0, A0));
        obj.addMeasurement(new Measurement(Measurements.A_BG, ABG));
        obj.addMeasurement(new Measurement(Measurements.THETA, th));
        obj.addMeasurement(new Measurement(Measurements.ELLIPTICITY, ellipticity));
        obj.addMeasurement(new Measurement(Measurements.RESIDUAL, residual));

    }

    static Image createGaussianImage(ObjCollection objects, String outputImageName, @Nullable Module module) {
        if (objects.size() == 0) return null;

        // Create blank image
        Obj firstObject = objects.getFirst();
        ImagePlus ipl = IJ.createImage(outputImageName,firstObject.getWidth(),firstObject.getHeight(),1,32);
        Image image = new Image(outputImageName,ipl);

        // Get the image for the first object.  Not adding the background to any object, as the average background will
        // be added later on.
        Iterator<Obj> iterator = objects.values().iterator();
        addGaussianProfile(iterator.next(),image,true);

        int count = 0;
        int total = objects.size();
        while (iterator.hasNext()) {
            // Adding current object's Gaussian
            addGaussianProfile(iterator.next(),image,true);

            if (module != null)
                module.writeProgressStatus(++count, total, "objects");

        }

        // Determining the average background and adding this on to the image
        double BG = getMeanBackground(objects);
        ImageMath.process(image.getImagePlus(),ImageMath.CalculationTypes.ADD,BG);

        return image;

    }

    static double getMeanBackground(ObjCollection objects) {
        CumStat cs = new CumStat();

        for (Obj obj:objects.values()) cs.addMeasure(obj.getMeasurement(Measurements.A_BG).getValue());

        return cs.getMean();

    }

    static void addGaussianProfile(Obj obj, Image inputImage, boolean zeroBackground) {
        // Create the 2D Gaussian distribution
        double x0 = obj.getMeasurement(Measurements.X0_PX).getValue();
        double y0 = obj.getMeasurement(Measurements.Y0_PX).getValue();
        double sx = obj.getMeasurement(Measurements.SIGMA_X_PX).getValue();
        double sy = obj.getMeasurement(Measurements.SIGMA_Y_PX).getValue();
        double A0 = obj.getMeasurement(Measurements.A_0).getValue();
        double ABG = zeroBackground? 0 : obj.getMeasurement(Measurements.A_BG).getValue();
        double th = obj.getMeasurement(Measurements.THETA).getValue();
        GaussianDistribution2D g2D = new GaussianDistribution2D(x0,y0,sx,sy,A0,ABG,th);

        // Create the blank image
        int minX = (int) Math.round(Math.max(0,x0-4*sx));
        int maxX = (int) Math.round(Math.min(x0+4*sx,obj.getWidth()));
        int minY = (int) Math.round(Math.max(0,y0-4*sy));
        int maxY = (int) Math.round(Math.min(y0+4*sy,obj.getHeight()));

        // Iterate over all pixels, evaluating the Gaussian
        ImageProcessor ipr = inputImage.getImagePlus().getProcessor();
        for (int x=minX;x<=maxX;x++) {
            for (int y=minY;y<=maxY;y++) {
                float inputVal = ipr.getf(x,y);
                float newVal = (float) g2D.getValues(x,y)[0];
                ipr.setf(x,y,inputVal+newVal);
            }
        }
    }


    @Override
    public Category getCategory() {
        return Categories.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getDescription() {
        return "Gaussian spot fitting.  Can take objects as estimated locations." +
                "\n***Only works in 2D***" +
                "\n***Only works for refinement of existing spots***";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();
        inputImagePlus = new Duplicator().run(inputImagePlus);

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
        boolean createGaussianImage = parameters.getValue(CREATE_GAUSSIAN_IMAGE);
        String gaussianImageName = parameters.getValue(GAUSSIAN_IMAGE);

        // Setting the desired values to limit sigma
        if (!limitSigma) {
            minSigma = -Double.MAX_VALUE;
            maxSigma = Double.MAX_VALUE;
        }

        // Running through each object, doing the fitting
        int count = 0;
        int startingNumber = inputObjects.size();
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            
            // Getting the centroid of the current object (should be single points anyway)
            int x = (int) Math.round(inputObject.getXMean(true));
            int y = (int) Math.round(inputObject.getYMean(true));
            int z = (int) Math.round(inputObject.getZMean(true, false));
            int t = inputObject.getT();

            // Getting the radius of the object
            int r;
            if (radiusMode.equals(RadiusModes.FIXED_VALUE)) {
                r = (int) Math.ceil(parameters.getValue(RADIUS));
            } else {
                double multiplier = parameters.getValue(MEASUREMENT_MULTIPLIER);
                r = (int) Math.ceil(inputObject.getMeasurement(parameters.getValue(RADIUS_MEASUREMENT)).getValue() * multiplier);
            }

            // Setting limits
            double[][] limits = getLimits(r,minSigma,maxSigma);

            // Ensuring the window width is odd, then getting the half width
            if (windowWidth%2!=0) windowWidth--;
            int halfW = fixedFittingWindow ? windowWidth/2 : r;

            // Getting the local image region
            if (x-halfW < 0 || x+halfW+1 > inputImagePlus.getWidth() || y-halfW < 0 || y+halfW + 1 > inputImagePlus.getHeight()) {
                assignMissingMeasurements(inputObject);
                continue;
            }

            // Cropping image
            inputImagePlus.setPosition(1, z + 1, t + 1);
            Image preCropImage = new Image("PreCrop",new ImagePlus("Slice",inputImagePlus.getProcessor()));
            ImageProcessor iprCrop = CropImage.cropImage(preCropImage,"Crop",y-halfW,x-halfW,halfW*2+1,halfW*2+1).getImagePlus().getProcessor();

            // Estimating parameters
            double[] pIn = estimateParameters(iprCrop,r);

            // Fitting the Gaussian and checking it reached convergence
            double[] pOut = fitGaussian2D(iprCrop,pIn,limits,maxEvaluations);

            // If the centroid has moved more than the width of the window, removing this localisation
            if (pOut != null && (pOut[0] <= 1 || pOut[0] >= r * 2 || pOut[1] <= 1 || pOut[1] >= r * 2)) {
                pOut = null;
            }

            // If the width is outside the permitted range
            if ((pOut != null && limitSigma) && ((pOut[2]+pOut[3])/2 < r*minSigma || (pOut[2]+pOut[3])/2 > r*maxSigma)) {
                pOut = null;
            }

            // Calculating residual
            if (pOut == null) assignMissingMeasurements(inputObject);
            else assignMeasurements(inputObject,iprCrop,pOut,x,y,z);

            // If selected, any objects that weren't fit are removed
            if (removeUnfit & pOut == null) {
                inputObject.removeRelationships();
                iterator.remove();
            }

            writeProgressStatus(++count, startingNumber, "objects");
            
        }

        // Adding explicit volume to spots
        if (applyVolume) assignVolume(inputObjects);

        // Creating a Gaussian image
        Image gaussianImage = null;
        if (createGaussianImage) {
            gaussianImage = createGaussianImage(inputObjects,gaussianImageName,this);
            workspace.addImage(gaussianImage);
        }

        if (showOutput) {
            inputObjects.showMeasurements(this,modules);

            if (createGaussianImage) gaussianImage.showImage();

            // Also show output objects, if storing coordinates is somehow enabled

        }

        return Status.PASS;

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
        parameters.add(new BooleanP(CREATE_GAUSSIAN_IMAGE,this,false));
        parameters.add(new OutputImageP(GAUSSIAN_IMAGE,this));


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
        if ((boolean) parameters.getValue(LIMIT_SIGMA_RANGE)) {
            returnedParameters.add(parameters.getParameter(MIN_SIGMA));
            returnedParameters.add(parameters.getParameter(MAX_SIGMA));
        }

        returnedParameters.add(parameters.getParameter(FIXED_FITTING_WINDOW));
        if ((boolean) parameters.getValue(FIXED_FITTING_WINDOW)) {
            returnedParameters.add(parameters.getParameter(WINDOW_SIZE));
        }

        returnedParameters.add(parameters.getParameter(MAX_EVALUATIONS));
        returnedParameters.add(parameters.getParameter(REMOVE_UNFIT));
        returnedParameters.add(parameters.getParameter(APPLY_VOLUME));

        returnedParameters.add(parameters.getParameter(CREATE_GAUSSIAN_IMAGE));
        if ((boolean) parameters.getValue(CREATE_GAUSSIAN_IMAGE)) {
            returnedParameters.add(parameters.getParameter(GAUSSIAN_IMAGE));
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
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.X0_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Y0_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Z0_SLICE);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.SIGMA_X_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.SIGMA_Y_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.SIGMA_MEAN_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.X0_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Y0_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Z0_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.SIGMA_X_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.SIGMA_Y_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.SIGMA_MEAN_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.A_0);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.A_BG);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.THETA);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.ELLIPTICITY);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.RESIDUAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}

//when signax_0:sigmay_o is >1.5, delete spot... i - 1 ;