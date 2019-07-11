// TODO: Check "frame" from RidgeDetection is 0-indexed
// TODO: Add junction linking (could be just for objects with a single shared junction)
// TODO: Add multitimepoint analysis (LineDetector only works on a single image in 2D)

package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import de.biomedical_imaging.ij.steger.*;
import ij.ImagePlus;
import ij.measure.Calibration;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Visualisation.Overlays.AddObjectOutline;
import wbif.sjx.MIA.Module.Visualisation.Overlays.Overlay;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Process.IntensityMinMax;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sc13967 on 30/05/2017.
 */
public class RidgeDetection extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String LOWER_THRESHOLD = "Lower threshold";
    public static final String UPPER_THRESHOLD = "Upper threshold";
    public static final String SIGMA = "Sigma";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String ESTIMATE_WIDTH = "Estimate width";
    public static final String MIN_LENGTH = "Minimum length";
    public static final String MAX_LENGTH = "Maximum length";
    public static final String CONTOUR_CONTRAST = "Contour contrast";
    public static final String LINK_CONTOURS = "Link contours";

    public RidgeDetection(ModuleCollection modules) {
        super("Ridge detection",modules);
    }

    private interface Measurements {
        String LENGTH_PX = "RIDGE_DETECT // LENGTH_(PX)";
        String LENGTH_CAL = "RIDGE_DETECT // LENGTH_(${CAL})";
        String MEAN_HALFWIDTH_PX = "RIDGE_DETECT // MEAN_HALFWIDTH_(PX)";
        String STDEV_HALFWIDTH_PX = "RIDGE_DETECT // STDEV_HALFWIDTH_(PX)";
        String MEAN_HALFWIDTH_CAL = "RIDGE_DETECT // MEAN_HALFWIDTH_(${CAL})";
        String STDEV_HALFWIDTH_CAL = "RIDGE_DETECT // STDEV_HALFWIDTH_(${CAL})";
    }

    private interface ContourContrast {
        String DARK_LINE = "Dark line";
        String LIGHT_LINE = "Light line";

        String[] ALL = new String[]{DARK_LINE,LIGHT_LINE};

    }

    public static void linkJunctions(HashMap<Line, HashSet<Line>> groups, Junctions junctions) {
        for (Junction junction : junctions) {
            // Getting the LineGroup associated with Line1.  If there isn't one, creating a new one
            Line line1 = junction.getLine1();
            HashSet<Line> group1 = groups.get(line1);

            // Getting the LineGroup associated with Line2.  If there isn't one, creating a new one
            Line line2 = junction.getLine2();
            HashSet<Line> group2 = groups.get(line2);

            // Adding all entries from the second LineGroup into the first
            group1.addAll(group2);

            // Removing the second Line from the HashMap, then re-adding it with the first LineGroup
            groups.remove(line2);
            groups.put(line2, group1);

        }
    }

    public ObjCollection process(Image inputImage, String outputObjectsName, String contourContrast, double sigma,
                                 double upperThreshold, double lowerThreshold, double minLength, double maxLength,
                                 boolean linkContours, boolean estimateWidth) throws IntegerOverflowException {
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Storing the image calibration
        ImagePlus inputImagePlus = inputImage.getImagePlus();
        Calibration calibration = inputImagePlus.getCalibration();
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String calibrationUnits = calibration.getUnits();
        boolean twoD = inputImagePlus.getNSlices()==1;
        int imWidth = inputImagePlus.getWidth();
        int imHeight = inputImagePlus.getHeight();
        int nChannels = inputImagePlus.getNChannels();
        int nSlices = inputImagePlus.getNSlices();

        LineDetector lineDetector = new LineDetector();
        boolean darkLine = contourContrast.equals(ContourContrast.DARK_LINE);

        // Iterating over each image in the stack
        int count = 1;
        int total = inputImagePlus.getNChannels()*inputImagePlus.getNSlices()*inputImagePlus.getNFrames();

        for (int c=0;c<nChannels;c++) {
            for (int z=0;z<nSlices;z++) {
                for (int t = 0; t < inputImagePlus.getNFrames(); t++) {
                    writeMessage("Processing image "+(count++)+" of "+total);
                    inputImagePlus.setPosition(c+1,z+1,t+1);

                    // Running the ridge detection
                    Lines lines;
                    try {
                        lines = lineDetector.detectLines(inputImagePlus.getProcessor(), sigma, upperThreshold,
                                lowerThreshold, minLength, maxLength, darkLine, true, true, false);
                    } catch (NegativeArraySizeException | ArrayIndexOutOfBoundsException e) {
                        continue;
                    }

                    Junctions junctions = lineDetector.getJunctions();

                    // If linking contours, adding all to a HashSet.  This prevents the same contours being added to
                    // the same set
                    HashMap<Line, HashSet<Line>> groups = new HashMap<>(); // Stored as <Line,LineGroup>
                    for (Line line : lines) {
                        HashSet<Line> lineGroup = new HashSet<>();
                        lineGroup.add(line);
                        groups.put(line, lineGroup);
                    }

                    // Iterating over each object, adding it to the nascent ObjCollection
                    if (linkContours) linkJunctions(groups,junctions);

                    // Getting the unique LineGroups
                    Set<HashSet<Line>> uniqueLineGroup = new HashSet<>(groups.values());
                    for (HashSet<Line> lineGroup : uniqueLineGroup) {
                        Obj outputObject = new Obj(outputObjectsName, outputObjects.getAndIncrementID(), dppXY, dppZ,
                                calibrationUnits,twoD);

                        double estimatedLength = 0;
                        CumStat width = new CumStat();
                        for (Line line : lineGroup) {
                            // Adding coordinates for the current line
                            float[] x = line.getXCoordinates();
                            float[] y = line.getYCoordinates();
                            float[] widthL = line.getLineWidthL();
                            float[] widthR = line.getLineWidthR();
                            for (int i = 0; i < x.length; i++) {
                                float halfWidth = (widthL[i] + widthR[i])/2;
                                width.addMeasure(halfWidth);

                                // Adding central point
                                if (x[i]>=0 && x[i]<imWidth && y[i]>=0 && y[i]<imHeight) {
                                    outputObject.addCoord(Math.round(x[i]), Math.round(y[i]), z);
                                }

                                // If selected, adding other points within the width of that point
                                if (estimateWidth) {
                                    int xMin = Math.round(x[i]-halfWidth);
                                    int xMax = Math.round(x[i]+halfWidth);
                                    int yMin = Math.round(y[i]-halfWidth);
                                    int yMax = Math.round(y[i]+halfWidth);

                                    for (int xx=xMin;xx<=xMax;xx++) {
                                        for (int yy=yMin;yy<=yMax;yy++) {
                                            if (Math.sqrt((xx-x[i])*(xx-x[i])+(yy-y[i])*(yy-y[i])) > halfWidth) continue;
                                            if (xx<0 || xx>=imWidth || yy<0 || yy>=imHeight) continue;
                                            outputObject.addCoord(xx, yy, z);
                                        }
                                    }
                                }
                            }

                            // Adding the estimated length to the current length
                            estimatedLength += line.estimateLength();

                        }

                        // Setting single values for the current contour
                        outputObject.setT(t);
                        outputObject.addMeasurement(new Measurement(Measurements.LENGTH_PX, estimatedLength));
                        outputObject.addMeasurement(new Measurement(Units.replace(Measurements.LENGTH_CAL), estimatedLength*dppXY));
                        if (estimateWidth) {
                            outputObject.addMeasurement(new Measurement(Measurements.MEAN_HALFWIDTH_PX, width.getMean()));
                            outputObject.addMeasurement(new Measurement(Measurements.STDEV_HALFWIDTH_PX, width.getStd(CumStat.SAMPLE)));
                            outputObject.addMeasurement(new Measurement(Units.replace(Measurements.MEAN_HALFWIDTH_CAL), width.getMean() * dppXY));
                            outputObject.addMeasurement(new Measurement(Units.replace(Measurements.STDEV_HALFWIDTH_CAL), width.getStd(CumStat.SAMPLE) * dppXY));
                        }

                        outputObjects.add(outputObject);

                    }
                }
            }
        }

        inputImagePlus.setPosition(1,1,1);

        return outputObjects;

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getDescription() {
        return "Uses the RidgeDetection Fiji plugin by Thorsten Wagner, which implements Carsten " +
                "\nSteger's paper \"An Unbiased Detector of Curvilinear Structures\"" +
                "\nINCOMPLETE";

    }

    @Override
    public boolean process(Workspace workspace) {
        Calendar calendar = Calendar.getInstance();

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters (RidgeDetection plugin wants to use pixel units only)
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        double lowerThreshold = parameters.getValue(LOWER_THRESHOLD);
        double upperThreshold = parameters.getValue(UPPER_THRESHOLD);
        double sigma = parameters.getValue(SIGMA);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean estimateWidth = parameters.getValue(ESTIMATE_WIDTH);
        String contourContrast = parameters.getValue(CONTOUR_CONTRAST);
        double minLength = parameters.getValue(MIN_LENGTH);
        double maxLength = parameters.getValue(MAX_LENGTH);
        boolean linkContours = parameters.getValue(LINK_CONTOURS);

        // Converting distances to calibrated units if necessary
        if (calibratedUnits) {
            Calibration calibration = inputImage.getImagePlus().getCalibration();

            sigma = calibration.getRawX(sigma);
            minLength = calibration.getRawX(minLength);
            maxLength = calibration.getRawX(maxLength);

        }

        // Running on the present image
        ObjCollection outputObjects = null;
        try {
            outputObjects = process(inputImage,outputObjectsName,contourContrast,sigma,upperThreshold,
                    lowerThreshold,minLength,maxLength,linkContours,estimateWidth);
        } catch (IntegerOverflowException e) {
            return false;
        }

        workspace.addObjects(outputObjects);

        if (showOutput) {
            // Adding image to workspace
            writeMessage("Adding objects (" + outputObjectsName + ") to workspace");

            // Creating a duplicate of the input image
            ImagePlus dispIpl = inputImage.getImagePlus().duplicate();
            IntensityMinMax.run(dispIpl, true);

            // Creating the overlay
            String colourMode = Overlay.ColourModes.RANDOM_COLOUR;
            HashMap<Integer,Float> hues = ColourFactory.getRandomHues(outputObjects);
            AddObjectOutline.addOverlay(dispIpl,outputObjects,0.2,hues,false,true);

            // Displaying the overlay
            dispIpl.setPosition(1,1,1);
            dispIpl.updateChannelAndDraw();
            dispIpl.show();

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this));
        parameters.add(new DoubleP(LOWER_THRESHOLD,this, 0.5));
        parameters.add(new DoubleP(UPPER_THRESHOLD,this, 0.85));
        parameters.add(new DoubleP(SIGMA,this, 3d));
        parameters.add(new BooleanP(CALIBRATED_UNITS,this,false));
        parameters.add(new BooleanP(ESTIMATE_WIDTH,this,false));
        parameters.add(new ChoiceP(CONTOUR_CONTRAST,this,ContourContrast.DARK_LINE,ContourContrast.ALL));
        parameters.add(new DoubleP(MIN_LENGTH,this, 0d));
        parameters.add(new DoubleP(MAX_LENGTH,this, 0d));
        parameters.add(new BooleanP(LINK_CONTOURS,this, false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.LENGTH_PX);
        reference.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
        reference.setDescription("Length of detected, \""+outputObjectsName+"\" ridge object.  Measured in pixel " +
                "units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.LENGTH_CAL));
        reference.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
        reference.setDescription("Length of detected, \""+outputObjectsName+"\" ridge object.  Measured in calibrated " +
                "("+Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        if (parameters.getValue(ESTIMATE_WIDTH)) {
            reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_HALFWIDTH_PX);
            reference.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
            reference.setDescription("Mean half width of detected, \""+outputObjectsName+"\" ridge object.  Half width" +
                    "is from the central (backbone) of the ridge to the edge.  Measured in pixel units.");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.STDEV_HALFWIDTH_PX);
            reference.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
            reference.setDescription("Standard deviation of the half width of detected, \""+outputObjectsName+"\" " +
                    "ridge object.  Half width is from the central (backbone) of the ridge to the edge.  Measured in " +
                    "pixel units.");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.MEAN_HALFWIDTH_CAL));
            reference.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
            reference.setDescription("Mean half width of detected, \""+outputObjectsName+"\" ridge object.  Half width" +
                    "is from the central (backbone) of the ridge to the edge.  Measured in calibrated " +
                    "("+Units.getOMEUnits().getSymbol()+") units.");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.STDEV_HALFWIDTH_CAL));
            reference.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
            reference.setDescription("Standard deviation of the half width of detected, \""+outputObjectsName+"\" " +
                    "ridge object.  Half width is from the central (backbone) of the ridge to the edge.  Measured in " +
                    "calibrated ("+Units.getOMEUnits().getSymbol()+") units.");
            returnedRefs.add(reference);

        }

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}