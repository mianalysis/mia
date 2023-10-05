package io.github.mianalysis.mia.module.objects.measure.intensity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.analyze.directionality.Directionality_;
import fiji.analyze.directionality.Directionality_.AnalysisMethod;
import ij.ImagePlus;
import ij.gui.Roi;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.measure.MeasureImageIntensityOrientation;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
* Calculates the orientation of structures in each object for a specific image.  This module uses the <a href="https://imagej.net/plugins/directionality">Directionality_</a> plugin to calculate core measures.  Additional measurements, such as the Alignment Index [1] are also calculated.  All measurements are made for all slices within an object; that is, the individual slice histograms are merged and normalised prior to calculation of all measurements.<br><br>References:<br><ol><li>Sun, M., et al. "Rapid Quantification of 3D Collagen Fiber Alignment and Fiber Intersection Correlations with High Sensitivity" <i>PLOS ONE</i> (2015), doi: https://doi.org/10.1371/journal.pone.0131814</li></ol>
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureObjectIntensityOrientation extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input/output";

    /**
    * 
    */
    public static final String INPUT_IMAGE = "Input image";

    /**
    * 
    */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
    * 
    */
    public static final String ORIENTATION_SEPARATOR = "Orientation controls";

    /**
    * 
    */
    public static final String METHOD = "Method";

    /**
    * 
    */
    public static final String NUMBER_OF_BINS = "Number of bins";

    /**
    * 
    */
    public static final String HISTOGRAM_START = "Histogram start (°)";

    /**
    * 
    */
    public static final String HISTOGRAM_END = "Histogram end (°)";

    /**
    * 
    */
    public static final String MEASUREMENT_SEPARATOR = "Measurement controls";

    /**
    * 
    */
    public static final String INCLUDE_BIN_RANGE_IN_NAME = "Include bin range in name";

    /**
    * 
    */
    public static final String INCLUDE_BIN_NUMBER_IN_NAME = "Include number of bins in name";

    public MeasureObjectIntensityOrientation(Modules modules) {
        super("Measure object intensity orientation", modules);
    }

    public interface Methods {
        String FOURIER = "Fourier components";
        String LOCAL_GRADIENT = "Local gradient orientation";

        String[] ALL = new String[] { FOURIER, LOCAL_GRADIENT };

    }

    public interface Measurements {
        String DIRECTION = "DIRECTION_(°)";
        String DISPERSION = "DISPERSION_(°)";
        String AMOUNT = "AMOUNT";
        String GOODNESS = "GOODNESS";
        String ALIGNMENT_INDEX = "ALIGNMENT_INDEX";
        String ALIGNMENT_INDEX_BG_SUB = "ALIGNMENT_INDEX_(BG_SUB)";
        String BACKGROUND = "BACKGROUND_VALUE";

        String[] ALL = new String[] { DIRECTION, DISPERSION, AMOUNT, GOODNESS, ALIGNMENT_INDEX, ALIGNMENT_INDEX_BG_SUB,
                BACKGROUND };

    }

    public static String getFullName(double binStart, double binEnd, int nBins, String imageName, String measurement,
            boolean includeBinRange, boolean includeBinNumber) {
        DecimalFormat df = new DecimalFormat("#0.0");
        StringBuilder sb = new StringBuilder("INT_ORI // ");

        sb.append(imageName + " // ");

        if (includeBinRange)
            sb.append(df.format(binStart) + "° TO " + df.format(binEnd) + "° // ");

        if (includeBinNumber)
            sb.append(nBins + " BINS // ");

        sb.append(measurement);

        return sb.toString();

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_INTENSITY;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Calculates the orientation of structures in each object for a specific image.  This module uses the <a href=\"https://imagej.net/plugins/directionality\">Directionality_</a> plugin to calculate core measures.  Additional measurements, such as the Alignment Index [1] are also calculated.  All measurements are made for all slices within an object; that is, the individual slice histograms are merged and normalised prior to calculation of all measurements." +
        "<br><br>References:<br>" +
        "<ol><li>Sun, M., et al. \"Rapid Quantification of 3D Collagen Fiber Alignment and Fiber Intersection Correlations with High Sensitivity\" <i>PLOS ONE</i> (2015), doi: https://doi.org/10.1371/journal.pone.0131814</li></ol>";
    }

    public static void processObject(Obj obj, Image inputImage, int nBins, double binStart, double binEnd,
            AnalysisMethod method, boolean includeBinRange, boolean includeBinNumber) {
        ArrayList<double[]> tempHistograms = new ArrayList<>();
        HashMap<Integer, Roi> rois = obj.getRois();
        Directionality_ directionality = new Directionality_();

        for (int z:rois.keySet()) {
            // Getting current image slice
            Image sliceImage = ExtractSubstack.extractSubstack(inputImage,"Slice","1",String.valueOf(z+1),String.valueOf(obj.getT()+1));
            ImagePlus sliceIpl = sliceImage.getImagePlus();
            sliceIpl.setRoi(rois.get(z));

            // Configuring Directionality plugin
            directionality = new Directionality_();
            directionality.setImagePlus(sliceIpl.crop());
            directionality.setBinNumber(nBins);
            directionality.setBinRange(binStart, binEnd);
            directionality.setMethod(method);

            // Running analysis on this slice for this object
            directionality.computeHistograms();
            ArrayList<double[]> currHistograms = directionality.getHistograms();
            tempHistograms.add(currHistograms.get(0));

        }

        // Removing existing histograms (from the most recent run) and replacing with all histograms from each object slice
        ArrayList<double[]> histograms = directionality.getHistograms();
        histograms.clear();
        for (double[] histogram:tempHistograms)
            histograms.add(histogram);
        
        MeasureImageIntensityOrientation.mergeHistograms(directionality);
        directionality.fitHistograms();

        // Adding measurements
        ArrayList<double[]> fitParameters = directionality.getFitAnalysis();
        double[] results = fitParameters.iterator().next();

        String inputImageName = inputImage.getName();
        String name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.DIRECTION, includeBinRange,
                includeBinNumber);
        obj.addMeasurement(new Measurement(name, Math.toDegrees(results[0])));

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.DISPERSION, includeBinRange,
                includeBinNumber);
        obj.addMeasurement(new Measurement(name, Math.toDegrees(results[1])));

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.AMOUNT, includeBinRange,
                includeBinNumber);
        obj.addMeasurement(new Measurement(name, results[2]));

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.GOODNESS, includeBinRange,
                includeBinNumber);
        obj.addMeasurement(new Measurement(name, results[3]));

        histograms = directionality.getHistograms();
        double[] hist = histograms.iterator().next();
        double[] binsDegs = directionality.getBins();
        double[] binsRads = Arrays.stream(binsDegs).map((v) -> Math.toRadians(v)).toArray();
        double[] extra_results = MeasureImageIntensityOrientation.calculateSpreadMeasures(hist, binsRads);

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.ALIGNMENT_INDEX, includeBinRange,
                includeBinNumber);
        obj.addMeasurement(new Measurement(name, extra_results[0]));

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.ALIGNMENT_INDEX_BG_SUB,
                includeBinRange,
                includeBinNumber);
        obj.addMeasurement(new Measurement(name, extra_results[1]));

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.BACKGROUND, includeBinRange,
                includeBinNumber);
        obj.addMeasurement(new Measurement(name, extra_results[2]));
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String methodString = parameters.getValue(METHOD, workspace);
        int nBins = parameters.getValue(NUMBER_OF_BINS, workspace);
        double binStart = parameters.getValue(HISTOGRAM_START, workspace);
        double binEnd = parameters.getValue(HISTOGRAM_END, workspace);
        boolean includeBinRange = parameters.getValue(INCLUDE_BIN_RANGE_IN_NAME, workspace);
        boolean includeBinNumber = parameters.getValue(INCLUDE_BIN_NUMBER_IN_NAME, workspace);

        Image inputImage = workspace.getImage(inputImageName);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        AnalysisMethod method;
        switch (methodString) {
            case Methods.FOURIER:
            default:
                method = AnalysisMethod.FOURIER_COMPONENTS;
                break;
            case Methods.LOCAL_GRADIENT:
                method = AnalysisMethod.LOCAL_GRADIENT_ORIENTATION;
                break;
        }

        for (Obj obj:inputObjects.values())
            processObject(obj, inputImage, nBins, binStart, binEnd, method, includeBinRange, includeBinNumber);

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(ORIENTATION_SEPARATOR, this));
        parameters.add(new ChoiceP(METHOD, this, Methods.FOURIER, Methods.ALL));
        parameters.add(new IntegerP(NUMBER_OF_BINS, this, 90));
        parameters.add(new DoubleP(HISTOGRAM_START, this, -90));
        parameters.add(new DoubleP(HISTOGRAM_END, this, 90));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new BooleanP(INCLUDE_BIN_RANGE_IN_NAME, this, true));
        parameters.add(new BooleanP(INCLUDE_BIN_NUMBER_IN_NAME, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;

    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        int nBins = parameters.getValue(NUMBER_OF_BINS, workspace);
        double binStart = parameters.getValue(HISTOGRAM_START, workspace);
        double binEnd = parameters.getValue(HISTOGRAM_END, workspace);
        boolean includeBinRange = parameters.getValue(INCLUDE_BIN_RANGE_IN_NAME, workspace);
        boolean includeBinNumber = parameters.getValue(INCLUDE_BIN_NUMBER_IN_NAME, workspace);

        String name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.DIRECTION, includeBinRange,
                includeBinNumber);
        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(name);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.DISPERSION, includeBinRange,
                includeBinNumber);
        ref = objectMeasurementRefs.getOrPut(name);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.AMOUNT, includeBinRange,
                includeBinNumber);
        ref = objectMeasurementRefs.getOrPut(name);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.GOODNESS, includeBinRange,
                includeBinNumber);
        ref = objectMeasurementRefs.getOrPut(name);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.ALIGNMENT_INDEX, includeBinRange,
                includeBinNumber);
        ref = objectMeasurementRefs.getOrPut(name);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.ALIGNMENT_INDEX_BG_SUB,
                includeBinRange,
                includeBinNumber);
        ref = objectMeasurementRefs.getOrPut(name);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.BACKGROUND, includeBinRange,
                includeBinNumber);
        ref = objectMeasurementRefs.getOrPut(name);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("");

    }
}
