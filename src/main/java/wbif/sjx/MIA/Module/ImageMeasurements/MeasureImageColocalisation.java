package wbif.sjx.MIA.Module.ImageMeasurements;

import java.util.HashMap;

import com.drew.lang.annotations.Nullable;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.TwinCursor;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import sc.fiji.coloc.algorithms.AutoThresholdRegression;
import sc.fiji.coloc.algorithms.KendallTauRankCorrelation;
import sc.fiji.coloc.algorithms.LiICQ;
import sc.fiji.coloc.algorithms.MandersColocalization;
import sc.fiji.coloc.algorithms.MandersColocalization.MandersResults;
import sc.fiji.coloc.algorithms.MissingPreconditionException;
import sc.fiji.coloc.algorithms.PearsonsCorrelation;
import sc.fiji.coloc.algorithms.SpearmanRankCorrelation;
import sc.fiji.coloc.gadgets.DataContainer;
import sc.fiji.coloc.gadgets.ThresholdMode;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ObjectMeasurements.Intensity.MeasureObjectColocalisation;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;

public class MeasureImageColocalisation<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Input separator";
    public static final String INPUT_IMAGE_1 = "Input image 1";
    public static final String INPUT_IMAGE_2 = "Input image 2";

    public static final String MASKING_SEPARATOR = "Image masking";
    public static final String MASKING_MODE = "Masking mode";
    public static final String MASK_IMAGE = "Mask image";
    public static final String IMAGE_MASK_LOGIC = "Image mask logic";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OBJECT_MASK_LOGIC = "Object mask logic";

    public static final String THRESHOLD_SEPARATOR = "Threshold controls";
    public static final String THRESHOLDING_MODE = "Thresholding mode";
    public static final String FIXED_THRESHOLD_1 = "Threshold (C1)";
    public static final String FIXED_THRESHOLD_2 = "Threshold (C2)";
    
    public static final String MEASUREMENT_SEPARATOR = "Measurement controls";
    public static final String PCC_IMPLEMENTATION = "PCC implementation";
    public static final String MEASURE_KENDALLS_RANK = "Measure Kendall's Rank Correlation";
    public static final String MEASURE_LI_ICQ = "Measure Li's ICQ";
    public static final String MEASURE_MANDERS = "Measure Manders' Correlation";
    public static final String MEASURE_PCC = "Measure PCC";
    public static final String MEASURE_SPEARMANS_RANK = "Measure Spearman's Rank Correlation";


    public MeasureImageColocalisation(ModuleCollection modules) {
        super("Measure image colocalisation", modules);
    }

    public interface MaskingModes {
        String NONE = "None";
        String MASK_IMAGE = "Mask using image";
        String MASK_OBJECTS = "Mask using objects";

        String[] ALL = new String[] { NONE, MASK_IMAGE, MASK_OBJECTS };

    }

    public interface ImageMaskLogic {
        String MEASURE_ON_BLACK = "Measure on black (0 intensity)";
        String MEASURE_ON_WHITE = "Measure on white (255 intensity)";

        String[] ALL = new String[] { MEASURE_ON_BLACK, MEASURE_ON_WHITE };

    }

    public interface ObjectMaskLogic {
        String MEASURE_INSIDE_OBJECTS = "Measure inside objects";
        String MEASURE_OUTSIDE_OBJECTS = "Measure outside objects";

        String[] ALL = new String[] { MEASURE_INSIDE_OBJECTS, MEASURE_OUTSIDE_OBJECTS };

    }

    public interface PCCImplementations {
        String CLASSIC = "Classic";
        String FAST = "Fast";

        String[] ALL = new String[] { CLASSIC, FAST };

    }

    public interface ThresholdingModes {
        String BISECTION = "Bisection (correlation)";
        String COSTES = "Costes (correlation)";
        String MANUAL = "Manual";
        String NONE = "None";

        String[] ALL = new String[] { BISECTION, COSTES, MANUAL, NONE };

    }

    public interface Measurements {
        String KENDALLS_TAU = "KENDALL_TAU";

        String LI_ICQ = "LI_ICQ";

        String M1_ABOVE_ZERO = "M1_ABOVE_ZERO";
        String M2_ABOVE_ZERO = "M2_ABOVE_ZERO";
        String M1_ABOVE_THRESHOLD = "M1_ABOVE_THRESHOLD";
        String M2_ABOVE_THRESHOLD = "M2_ABOVE_THRESHOLD";

        String PCC = "PCC";
        String PCC_BELOW_THRESHOLD = "PCC_BELOW_THRESHOLD";
        String PCC_ABOVE_THRESHOLD = "PCC_ABOVE_THRESHOLD";

        String SPEARMAN_RHO = "SPEARMAN_RHO";
        String SPEARMAN_T_STATISTIC = "SPEARMAN_T_STATISTIC";
        String SPEARMAN_DF = "SPEARMAN_DEGREES_OF_FREEDOM";

        String THRESHOLD_1 = "THRESHOLD_1";
        String THRESHOLD_2 = "THRESHOLD_2";
        String THRESHOLD_SLOPE = "THRESHOLD_SLOPE";
        String THRESHOLD_Y_INTERCEPT = "THRESHOLD_Y_INTERCEPT";

    }

    public static String getFullName(String imageName1, String imageName2, String measurement) {
        return "COLOCALISATION // " + imageName1 + "-" + imageName2 + "_" + measurement;
    }

    public static PearsonsCorrelation.Implementation getPCCImplementation(String pccImplementationName) {
        switch (pccImplementationName) {
            case PCCImplementations.CLASSIC:
            default:
                return PearsonsCorrelation.Implementation.Classic;
            case PCCImplementations.FAST:
                return PearsonsCorrelation.Implementation.Fast;
        }
    }

    public static <T extends RealType<T> & NativeType<T>> Image<T> getObjectMask(ObjCollection objects,
            String maskLogic) {
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(objects, ColourFactory.SingleColours.WHITE);
        Image<T> mask = objects.convertToImage("Mask", hues, 8, false);

        if (maskLogic.equals(ObjectMaskLogic.MEASURE_OUTSIDE_OBJECTS))
            InvertIntensity.process(mask);

        return mask;

    }

    public static <T extends RealType<T> & NativeType<T>> DataContainer<T> prepareDataContainer(Image<T> image1,
            Image<T> image2, @Nullable Image<T> mask) {
        DataContainer<T> data;

        try {
            if (mask == null)
                data = new DataContainer<T>(image1.getImgPlus(), image2.getImgPlus(), 0, 0, "Channel 1", "Channel 2");
            else
                data = new DataContainer<T>(image1.getImgPlus(), image2.getImgPlus(), 0, 0, "Channel 1", "Channel 2",
                        mask.getImgPlus(), new long[2], new long[2]);

            return data;

        } catch (MissingPreconditionException e) {
            MIA.log.writeError(e.getMessage());
            return null;
        }
    }

    public void setImageMeasurements(Image<T> image, HashMap<String, Double> measurements, String imageName1,
            String imageName2) {
        for (String measurementName : measurements.keySet()) {
            String fullName = getFullName(imageName1, imageName2, measurementName);
            image.addMeasurement(new Measurement(fullName, measurements.get(measurementName)));
        }
    }

    public static <T extends RealType<T> & NativeType<T>> HashMap<String, Double> setAutoThresholds(
            DataContainer<T> data, String thresholdingMode, String pccImplementationName) {
        HashMap<String, Double> measurements = new HashMap<>();

        PearsonsCorrelation.Implementation implementation = getPCCImplementation(pccImplementationName);
        PearsonsCorrelation<T> pc = new PearsonsCorrelation<T>(implementation);

        AutoThresholdRegression.Implementation atrImplementation;
        switch (thresholdingMode) {
            default:
            case ThresholdingModes.BISECTION:
                atrImplementation = AutoThresholdRegression.Implementation.Bisection;
                break;
            case ThresholdingModes.COSTES:
                atrImplementation = AutoThresholdRegression.Implementation.Costes;
                break;
        }

        AutoThresholdRegression<T> atr = new AutoThresholdRegression<T>(pc, atrImplementation);
        try {
            atr.execute(data);
            data.setAutoThreshold(atr);

            measurements.put(Measurements.THRESHOLD_1, atr.getCh1MaxThreshold().getRealDouble());
            measurements.put(Measurements.THRESHOLD_2, atr.getCh2MaxThreshold().getRealDouble());
            measurements.put(Measurements.THRESHOLD_SLOPE, atr.getAutoThresholdSlope());
            measurements.put(Measurements.THRESHOLD_Y_INTERCEPT, atr.getAutoThresholdIntercept());

        } catch (MissingPreconditionException e) {
            measurements.put(Measurements.THRESHOLD_1, Double.NaN);
            measurements.put(Measurements.THRESHOLD_2, Double.NaN);
            measurements.put(Measurements.THRESHOLD_SLOPE, Double.NaN);
            measurements.put(Measurements.THRESHOLD_Y_INTERCEPT, Double.NaN);

            MIA.log.writeError(e.getMessage());

        }

        return measurements;

    }

    public static <T extends RealType<T> & NativeType<T>> void setManualThresholds(DataContainer<T> data,
            Image<T> image1, double fixedThreshold1, double fixedThreshold2) {
        ManualThreshold<T> manualThreshold = new ManualThreshold<T>(image1, fixedThreshold1, fixedThreshold2);
        data.setAutoThreshold(manualThreshold);
    }

    public static <T extends RealType<T> & NativeType<T>> HashMap<String, Double> measureKendalls(DataContainer<T> data) {
        // Based on code from
        // https://github.com/fiji/Colocalisation_Analysis/blob/master/src/main/java/sc/fiji/coloc/algorithms/KendallTauRankCorrelation.java
        // (Accessed 2021-08-11)
        HashMap<String, Double> measurements = new HashMap<>();

        RandomAccessible<T> img1 = data.getSourceImage1();
		RandomAccessible<T> img2 = data.getSourceImage2();
		RandomAccessibleInterval<BitType> mask = data.getMask();

		TwinCursor<T> cursor = new TwinCursor<T>(img1.randomAccess(),
				img2.randomAccess(), Views.iterable(mask).localizingCursor());

		double tau = KendallTauRankCorrelation.calculateMergeSort(cursor);
        measurements.put(Measurements.KENDALLS_TAU, tau);

        return measurements;

    }
    
    public static <T extends RealType<T> & NativeType<T>> HashMap<String, Double> measureLiICQ(DataContainer<T> data) {
        // Based on code from
        // https://github.com/fiji/Colocalisation_Analysis/blob/master/src/main/java/sc/fiji/coloc/algorithms/LiICQ.java
        // (Accessed 2021-08-11)
        HashMap<String, Double> measurements = new HashMap<>();

        double mean1 = data.getMeanCh1();
        double mean2 = data.getMeanCh2();

        // get the 2 images for the calculation of Li's ICQ
        RandomAccessible<T> img1 = data.getSourceImage1();
        RandomAccessible<T> img2 = data.getSourceImage2();
        RandomAccessibleInterval<BitType> mask = data.getMask();

        TwinCursor<T> cursor = new TwinCursor<T>(img1.randomAccess(), img2.randomAccess(),
                Views.iterable(mask).localizingCursor());

        double icqValue = LiICQ.calculateLisICQ(cursor, mean1, mean2);

        measurements.put(Measurements.LI_ICQ, icqValue);

        return measurements;

    }

    public static <T extends RealType<T> & NativeType<T>> HashMap<String, Double> measureManders(
            DataContainer<T> data) {
        // Based on code from
        // https://github.com/fiji/Colocalisation_Analysis/blob/master/src/main/java/sc/fiji/coloc/algorithms/MandersColocalization.java
        // (Accessed 2021-08-10)
        HashMap<String, Double> measurements = new HashMap<>();

        MandersColocalization<T> mc = new MandersColocalization<>();

        // get the two images for the calculation of Manders' split coefficients
        RandomAccessible<T> img1 = data.getSourceImage1();
        RandomAccessible<T> img2 = data.getSourceImage2();
        RandomAccessibleInterval<BitType> mask = data.getMask();

        TwinCursor<T> cursor = new TwinCursor<T>(img1.randomAccess(), img2.randomAccess(),
                Views.iterable(mask).localizingCursor());

        // calculate Manders' split coefficients without threshold, M1 and M2.
        MandersResults results = mc.calculateMandersCorrelation(cursor, img1.randomAccess().get().createVariable());

        measurements.put(Measurements.M1_ABOVE_ZERO, results.m1);
        measurements.put(Measurements.M2_ABOVE_ZERO, results.m2);

        // Calculate the thresholded Manders' split coefficients, tM1 and tM2, if
        // possible
        AutoThresholdRegression<T> autoThreshold = data.getAutoThreshold();
        if (autoThreshold != null) {
            // thresholded Manders' split coefficients, tM1 and tM2
            cursor.reset();
            results = mc.calculateMandersCorrelation(cursor, autoThreshold.getCh1MaxThreshold(),
                    autoThreshold.getCh2MaxThreshold(), ThresholdMode.Above);

            measurements.put(Measurements.M1_ABOVE_THRESHOLD, results.m1);
            measurements.put(Measurements.M2_ABOVE_THRESHOLD, results.m2);

        }

        return measurements;

    }

    public static <T extends RealType<T> & NativeType<T>> HashMap<String, Double> measurePCC(DataContainer<T> data,
            String pccImplementationName) {
        HashMap<String, Double> measurements = new HashMap<>();

        PearsonsCorrelation.Implementation implementation = getPCCImplementation(pccImplementationName);
        PearsonsCorrelation<T> pc = new PearsonsCorrelation<T>(implementation);
        try {
            pc.execute(data);

            measurements.put(Measurements.PCC, pc.getPearsonsCorrelationValue());
            if (data.getAutoThreshold() != null) {
                measurements.put(Measurements.PCC_BELOW_THRESHOLD, pc.getPearsonsCorrelationBelowThreshold());
                measurements.put(Measurements.PCC_ABOVE_THRESHOLD, pc.getPearsonsCorrelationAboveThreshold());
            }

        } catch (MissingPreconditionException e) {
            measurements.put(Measurements.PCC, Double.NaN);
            if (data.getAutoThreshold() != null) {
                measurements.put(Measurements.PCC_BELOW_THRESHOLD, Double.NaN);
                measurements.put(Measurements.PCC_ABOVE_THRESHOLD, Double.NaN);
            }

            MIA.log.writeError(e.getMessage());

        }

        return measurements;

    }

    public static <T extends RealType<T> & NativeType<T>> HashMap<String, Double> measureSpearman(
            DataContainer<T> data) {
        // Based on code from
        // https://github.com/fiji/Colocalisation_Analysis/blob/master/src/main/java/sc/fiji/coloc/algorithms/SpearmanRankCorrelation.java
        // (Accessed 2021-08-11)
        HashMap<String, Double> measurements = new HashMap<>();

        // get the 2 images for the calculation of Spearman's rho
        RandomAccessibleInterval<T> img1 = data.getSourceImage1();
        RandomAccessibleInterval<T> img2 = data.getSourceImage2();
        RandomAccessibleInterval<BitType> mask = data.getMask();

        TwinCursor<T> cursor = new TwinCursor<T>(img1.randomAccess(), img2.randomAccess(),
                Views.iterable(mask).localizingCursor());

        // Count the pixels first.
        int n = 0;
        while (cursor.hasNext()) {
            n++;
            cursor.fwd();
        }
        cursor.reset();

        // calculate Spearman's rho value
        SpearmanRankCorrelation src = new SpearmanRankCorrelation<T>();
        double rhoValue = src.calculateSpearmanRank(cursor);

        measurements.put(Measurements.SPEARMAN_RHO, rhoValue);
        measurements.put(Measurements.SPEARMAN_DF, new Double(src.getSpearmanDF(n)).doubleValue());
        measurements.put(Measurements.SPEARMAN_T_STATISTIC, src.getTStatistic(rhoValue, n));

        return measurements;

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getDescription() {
        return "Calculates colocalisation of two input images.  All measurements are associated with the first input image.  Measurements can be restricted to specific region using image or object-based masking.  To measure colocalisation on an object-by-object basis please use the \""+new MeasureObjectColocalisation<>(null).getName()+"\" module.<br><br>"
        
        +"All calculations are performed using the <a href=\"https://imagej.net/plugins/coloc-2\">Coloc2 plugin</a>.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input images
        String imageName1 = parameters.getValue(INPUT_IMAGE_1);
        Image<T> image1 = (Image<T>) workspace.getImage(imageName1);

        String imageName2 = parameters.getValue(INPUT_IMAGE_2);
        Image<T> image2 = (Image<T>) workspace.getImages().get(imageName2);

        // Getting parameters
        String maskingMode = parameters.getValue(MASKING_MODE);
        String maskImageName = parameters.getValue(MASK_IMAGE);
        Image<T> maskImage = (Image<T>) workspace.getImage(maskImageName);
        String imageMaskLogic = parameters.getValue(IMAGE_MASK_LOGIC);
        String objectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection objects = workspace.getObjects().get(objectName);
        String objectMaskLogic = parameters.getValue(OBJECT_MASK_LOGIC);
        String thresholdingMode = parameters.getValue(THRESHOLDING_MODE);
        double fixedThreshold1 = parameters.getValue(FIXED_THRESHOLD_1);
        double fixedThreshold2 = parameters.getValue(FIXED_THRESHOLD_2);        
        String pccImplementationName = parameters.getValue(PCC_IMPLEMENTATION);
        boolean measureKendalls = parameters.getValue(MEASURE_KENDALLS_RANK);
        boolean measureLiICQ = parameters.getValue(MEASURE_LI_ICQ);
        boolean measureManders = parameters.getValue(MEASURE_MANDERS);
        boolean measurePCC = parameters.getValue(MEASURE_PCC);
        boolean measureSpearman = parameters.getValue(MEASURE_SPEARMANS_RANK);

        // If objects are to be used as a mask a binary image is created. Otherwise,
        // null is returned
        switch (maskingMode) {
            case MaskingModes.MASK_IMAGE:
                maskImage = new Image("Mask", maskImage.getImagePlus().duplicate());
                if (imageMaskLogic.equals(ImageMaskLogic.MEASURE_ON_BLACK))
                    InvertIntensity.process(maskImage);
                break;
            case MaskingModes.MASK_OBJECTS:
                maskImage = getObjectMask(objects, objectMaskLogic);
                break;
            default:
            case MaskingModes.NONE:
                maskImage = null;
                break;
        }

        // Creating data container against which all algorithms will be run
        DataContainer<T> data = prepareDataContainer(image1, image2, maskImage);

        switch (thresholdingMode) {
            case ThresholdingModes.BISECTION:
            case ThresholdingModes.COSTES:
                HashMap<String, Double> measurements = setAutoThresholds(data, thresholdingMode, pccImplementationName);
                setImageMeasurements(image1, measurements, imageName1, imageName2);
                break;
            case ThresholdingModes.MANUAL:
                setManualThresholds(data, image1, fixedThreshold1, fixedThreshold2);
                break;
        }

        // Making colocalisation measurements
        if (measureKendalls) {
            HashMap<String, Double> measurements = measureKendalls(data);
            setImageMeasurements(image1, measurements, imageName1, imageName2);
        }

        if (measureLiICQ) {
            HashMap<String, Double> measurements = measureLiICQ(data);
            setImageMeasurements(image1, measurements, imageName1, imageName2);
        }

        if (measureManders) {
            HashMap<String, Double> measurements = measureManders(data);
            setImageMeasurements(image1, measurements, imageName1, imageName2);
        }

        if (measurePCC) {
            HashMap<String, Double> measurements = measurePCC(data, pccImplementationName);
            setImageMeasurements(image1, measurements, imageName1, imageName2);
        }

        if (measureSpearman) {
            HashMap<String, Double> measurements = measureSpearman(data);
            setImageMeasurements(image1, measurements, imageName1, imageName2);
        }

        if (showOutput)
            image1.showMeasurements(this);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE_1, this));
        parameters.add(new InputImageP(INPUT_IMAGE_2, this));

        parameters.add(new SeparatorP(MASKING_SEPARATOR, this));
        parameters.add(new ChoiceP(MASKING_MODE, this, MaskingModes.NONE, MaskingModes.ALL));
        parameters.add(new InputImageP(MASK_IMAGE, this));
        parameters.add(new ChoiceP(IMAGE_MASK_LOGIC, this, ImageMaskLogic.MEASURE_ON_WHITE, ImageMaskLogic.ALL));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters
                .add(new ChoiceP(OBJECT_MASK_LOGIC, this, ObjectMaskLogic.MEASURE_INSIDE_OBJECTS, ObjectMaskLogic.ALL));

        parameters.add(new SeparatorP(THRESHOLD_SEPARATOR, this));
        parameters.add(new ChoiceP(THRESHOLDING_MODE, this, ThresholdingModes.BISECTION, ThresholdingModes.ALL));
        parameters.add(new DoubleP(FIXED_THRESHOLD_1, this, 1.0));
        parameters.add(new DoubleP(FIXED_THRESHOLD_2, this, 1.0));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new ChoiceP(PCC_IMPLEMENTATION, this, PCCImplementations.FAST, PCCImplementations.ALL));                
        parameters.add(new BooleanP(MEASURE_KENDALLS_RANK, this, true));
        parameters.add(new BooleanP(MEASURE_LI_ICQ, this, true));
        parameters.add(new BooleanP(MEASURE_MANDERS, this, true));
        parameters.add(new BooleanP(MEASURE_PCC, this, true));
        parameters.add(new BooleanP(MEASURE_SPEARMANS_RANK, this, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE_1));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE_2));

        returnedParameters.add(parameters.getParameter(MASKING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MASKING_MODE));
        switch ((String) parameters.getValue(MASKING_MODE)) {
            case MaskingModes.MASK_IMAGE:
                returnedParameters.add(parameters.getParameter(MASK_IMAGE));
                returnedParameters.add(parameters.getParameter(IMAGE_MASK_LOGIC));
                break;
            case MaskingModes.MASK_OBJECTS:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(OBJECT_MASK_LOGIC));
                break;
        }

        returnedParameters.add(parameters.getParameter(THRESHOLD_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THRESHOLDING_MODE));
        switch ((String) parameters.getValue(THRESHOLDING_MODE)) {
            case ThresholdingModes.MANUAL:
                returnedParameters.add(parameters.getParameter(FIXED_THRESHOLD_1));
                returnedParameters.add(parameters.getParameter(FIXED_THRESHOLD_2));
                break;
        }

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        if ((boolean) parameters.getValue(MEASURE_PCC)
                || ((String) parameters.getValue(THRESHOLDING_MODE)).equals(ThresholdingModes.BISECTION)
                || ((String) parameters.getValue(THRESHOLDING_MODE)).equals(ThresholdingModes.COSTES)) {
            returnedParameters.add(parameters.getParameter(PCC_IMPLEMENTATION));
        }
        returnedParameters.add(parameters.getParameter(MEASURE_KENDALLS_RANK));
        returnedParameters.add(parameters.getParameter(MEASURE_LI_ICQ));
        returnedParameters.add(parameters.getParameter(MEASURE_MANDERS));
        returnedParameters.add(parameters.getParameter(MEASURE_PCC));
        returnedParameters.add(parameters.getParameter(MEASURE_SPEARMANS_RANK));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

        String inputImage1Name = parameters.getValue(INPUT_IMAGE_1);
        String inputImage2Name = parameters.getValue(INPUT_IMAGE_2);

        switch ((String) parameters.getValue(THRESHOLDING_MODE)) {
            case ThresholdingModes.BISECTION:
            case ThresholdingModes.COSTES:
                String name = getFullName(inputImage1Name, inputImage2Name, Measurements.THRESHOLD_1);
                ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImage1Name);
                returnedRefs.add(reference);

                name = getFullName(inputImage1Name, inputImage2Name, Measurements.THRESHOLD_2);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImage1Name);
                returnedRefs.add(reference);

                name = getFullName(inputImage1Name, inputImage2Name, Measurements.THRESHOLD_SLOPE);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImage1Name);
                returnedRefs.add(reference);

                name = getFullName(inputImage1Name, inputImage2Name, Measurements.THRESHOLD_Y_INTERCEPT);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImage1Name);
                returnedRefs.add(reference);

                break;
        }

        if ((boolean) parameters.getValue(MEASURE_KENDALLS_RANK)) {
            String name = getFullName(inputImage1Name, inputImage2Name, Measurements.KENDALLS_TAU);
            ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
            reference.setImageName(inputImage1Name);
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(MEASURE_LI_ICQ)) {
            String name = getFullName(inputImage1Name, inputImage2Name, Measurements.LI_ICQ);
            ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
            reference.setImageName(inputImage1Name);
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(MEASURE_MANDERS)) {
            String name = getFullName(inputImage1Name, inputImage2Name, Measurements.M1_ABOVE_ZERO);
            ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
            reference.setImageName(inputImage1Name);
            returnedRefs.add(reference);

            name = getFullName(inputImage1Name, inputImage2Name, Measurements.M2_ABOVE_ZERO);
            reference = imageMeasurementRefs.getOrPut(name);
            reference.setImageName(inputImage1Name);
            returnedRefs.add(reference);

            if (!((String) parameters.getValue(THRESHOLDING_MODE)).equals(ThresholdingModes.NONE)) {
                name = getFullName(inputImage1Name, inputImage2Name, Measurements.M1_ABOVE_THRESHOLD);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImage1Name);
                returnedRefs.add(reference);

                name = getFullName(inputImage1Name, inputImage2Name, Measurements.M2_ABOVE_THRESHOLD);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImage1Name);
                returnedRefs.add(reference);
            }
        }

        if ((boolean) parameters.getValue(MEASURE_PCC)) {
            String name = getFullName(inputImage1Name, inputImage2Name, Measurements.PCC);
            ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
            reference.setImageName(inputImage1Name);
            returnedRefs.add(reference);

            if (!((String) parameters.getValue(THRESHOLDING_MODE)).equals(ThresholdingModes.NONE)) {
                name = getFullName(inputImage1Name, inputImage2Name, Measurements.PCC_BELOW_THRESHOLD);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImage1Name);
                returnedRefs.add(reference);

                name = getFullName(inputImage1Name, inputImage2Name, Measurements.PCC_ABOVE_THRESHOLD);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImage1Name);
                returnedRefs.add(reference);
            }
        }

        if ((boolean) parameters.getValue(MEASURE_SPEARMANS_RANK)) {
            String name = getFullName(inputImage1Name, inputImage2Name, Measurements.SPEARMAN_RHO);
            ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
            reference.setImageName(inputImage1Name);
            returnedRefs.add(reference);

            name = getFullName(inputImage1Name, inputImage2Name, Measurements.SPEARMAN_DF);
            reference = imageMeasurementRefs.getOrPut(name);
            reference.setImageName(inputImage1Name);
            returnedRefs.add(reference);

            name = getFullName(inputImage1Name, inputImage2Name, Measurements.SPEARMAN_T_STATISTIC);
            reference = imageMeasurementRefs.getOrPut(name);
            reference.setImageName(inputImage1Name);
            returnedRefs.add(reference);
        }

        return returnedRefs;

    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
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

class ManualThreshold<T extends net.imglib2.type.numeric.RealType<T>> extends AutoThresholdRegression {
    private T fixedThreshold1;
    private T fixedThreshold2;

    public ManualThreshold(Image image, double fixedThreshold1, double fixedThreshold2) {
        super(null);

        T type = (T) image.getImgPlus().getImg().firstElement();
        this.fixedThreshold1 = type.createVariable();
        this.fixedThreshold1.setReal(fixedThreshold1);
        this.fixedThreshold2 = type.createVariable();
        this.fixedThreshold2.setReal(fixedThreshold2);

    }

    @Override
    public T getCh1MaxThreshold() {
        return this.fixedThreshold1;
    }

    @Override
    public T getCh2MaxThreshold() {
        return this.fixedThreshold2;
    }
}