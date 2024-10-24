package io.github.mianalysis.mia.module.images.measure;

import java.util.HashMap;

import com.drew.lang.annotations.Nullable;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.module.objects.measure.intensity.MeasureObjectColocalisation;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImgPlusTools;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ImageMeasurementP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
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


/**
* Calculates colocalisation of two input images.  All measurements are associated with the first input image.  Measurements can be restricted to specific region using image or object-based masking.  To measure colocalisation on an object-by-object basis please use the "Measure object colocalisation" module.<br><br>All calculations are performed using the <a href="https://imagej.net/plugins/coloc-2">Coloc2 plugin</a>.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureImageColocalisation<T extends RealType<T> & NativeType<T>> extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Input separator";

	/**
	* First image for which colocalisation will be calculated.  Measurements will be associated with this image.
	*/
    public static final String INPUT_IMAGE_1 = "Input image 1";

	/**
	* Second image for which colocalisation will be calculated.
	*/
    public static final String INPUT_IMAGE_2 = "Input image 2";


	/**
	* 
	*/
    public static final String MASKING_SEPARATOR = "Image masking";

	/**
	* Controls which regions of the image will be evaluated for colocalisation:<br><ul><li>"Mask using image" A binary image (specified using "Mask image") determines which pixels are evaluated for colocalisation.  The "Image mask logic" parameter controls whether the pixels to be evaluated are black (0 intensity) or white (255 intensity).</li><li>"Mask using objects" An object collection (specified using "Input objects") determines which pixels are evaluated for colocalisation.  The "Object mask logic" parameter controls whether the pixels to be evaluated are inside or outside the objects.</li><li>"None" No mask will be applied.  All pixels in the image will be evaluated for colocalisation.</li></ul>
	*/
    public static final String MASKING_MODE = "Masking mode";

	/**
	* If "Masking mode" is set to "Mask using image", this is the binary image which will control the pixels to be evaluated for colocalisation.  The "Image mask logic" parameter controls whether the pixels to be evaluated are black (0 intensity) or white (255 intensity).
	*/
    public static final String MASK_IMAGE = "Mask image";

	/**
	* Controls whether colocalisation is measured for pixels coincident with black (0 intensity) or white (255 intensity) pixels in the mask image.
	*/
    public static final String IMAGE_MASK_LOGIC = "Image mask logic";

	/**
	* If "Masking mode" is set to "Mask using objects", this is the object collection which will control the pixels to be evaluated for colocalisation.  The "Object mask logic" parameter controls whether the pixels to be evaluated are inside or outside the objects.
	*/
    public static final String INPUT_OBJECTS = "Input objects";

	/**
	* Controls whether colocalisation is measured for pixels inside or outside objects in the masking object collection.
	*/
    public static final String OBJECT_MASK_LOGIC = "Object mask logic";


	/**
	* 
	*/
    public static final String THRESHOLD_SEPARATOR = "Threshold controls";

	/**
	* Controls how the thresholds for measurements such as Manders' are set:<br><ul><li>"Bisection (correlation)" A faster method to calculate thresholds than the Costes approach.</li><li>"Costes (correlation)" The "standard" method to calculate thresholds for Manders' colocalisation measures.  This approach sets the thresholds for the two input images such that the pixels with intensities lower than their respective thresholds don't have any statistical correlation (i.e. have PCC values less than or equal to 0).  This is based on Costes' 2004 paper (Costes et al., <i>Biophys. J.</i> <b>86</b> (2004) 3993–4003.</li><li>"Image measurements" Thresholds for each image will be set equal to measurements associated with each object.</li><li>"Manual" Threshold values are manually set from user-defined values ("Threshold (C1)" and "Threshold (C2)" parameters).</li><li>"None" No threshold is set.  In this instance, Manders' metrics will only be calculated above zero intensity rather than both above zero and above the thresholds.  Similarly, Pearson's correlation coefficients will only be calculated for the entire region (after masking) rather than also for above and below the thresholds.</li></ul>
	*/
    public static final String THRESHOLDING_MODE = "Thresholding mode";
    public static final String IMAGE_MEASUREMENT_1 = "Image measurement (C1)";
    public static final String IMAGE_MEASUREMENT_2 = "Image measurement (C2)";
    public static final String FIXED_THRESHOLD_1 = "Threshold (C1)";
    public static final String FIXED_THRESHOLD_2 = "Threshold (C2)";


	/**
	* 
	*/
    public static final String MEASUREMENT_SEPARATOR = "Measurement controls";

	/**
	* Controls whether PCC should be calculated using the classic algorithm or using the Coloc2-default "fast" method.
	*/
    public static final String PCC_IMPLEMENTATION = "PCC implementation";

	/**
	* When selected, Kendall's rank correlation will be calculated.  This works in a similar manner to Pearson's PCC, except it's calculated on ranked data rather than raw pixel intensities.
	*/
    public static final String MEASURE_KENDALLS_RANK = "Measure Kendall's Rank Correlation";

	/**
	* When selected, Li's ICQ (intensity correlation quotient) will be calculated.  This measure reports the frequency with which both corresponding pixels for both channels are either both above or both below their respective means.  Values are scaled into the range -0.5 to +0.5, with values below 0 corresponding to anti-correlation and values above 0 indicating correlation.
	*/
    public static final String MEASURE_LI_ICQ = "Measure Li's ICQ";

	/**
	* When selected, Manders' M1 and M2 coefficients will be calculated.  "Proportional to the amount of fluorescence of the colocalizing pixels or voxels in each colour channel. You can get more details in Manders et al. Values range from 0 to 1, expressing the fraction of intensity in a channel that is located in pixels where there is above zero (or threshold) intensity in the other colour channel." Description taken from <a href="https://imagej.net/imaging/colocalization-analysis">https://imagej.net/imaging/colocalization-analysis</a>
	*/
    public static final String MEASURE_MANDERS = "Measure Manders' Correlation";

	/**
	* When selected, Pearson's Correlation Coefficient (PCC) will be calculated.  "It is not sensitive to differences in mean signal intensities or range, or a zero offset between the two components. The result is +1 for perfect correlation, 0 for no correlation, and -1 for perfect anti-correlation. Noise makes the value closer to 0 than it should be." Description taken from <a href="https://imagej.net/imaging/colocalization-analysis">https://imagej.net/imaging/colocalization-analysis</a>
	*/
    public static final String MEASURE_PCC = "Measure PCC";

	/**
	* When selected, Spearman's rank correlation will be calculated.  Spearman's rho is calculated in a similar manner to Pearson's PCC, except the image intensities are replaced by their respective rank.  Spearman's correlation works with monotonic relationships.  As with PCC, values are in the range -1 to +1.
	*/
    public static final String MEASURE_SPEARMANS_RANK = "Measure Spearman's Rank Correlation";

    public MeasureImageColocalisation(Modules modules) {
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
        String IMAGE_MEASUREMENTS = "Image measurements";
        String MANUAL = "Manual";
        String NONE = "None";

        String[] ALL = new String[] { BISECTION, COSTES, IMAGE_MEASUREMENTS, MANUAL, NONE };

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

    public static Image getObjectMask(Objs objects, String maskLogic) {
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourValues(objects, ColourFactory.SingleColours.WHITE);
        Image mask = objects.convertToImage("Mask", hues, 8, false);

        if (maskLogic.equals(ObjectMaskLogic.MEASURE_OUTSIDE_OBJECTS))
            InvertIntensity.process(mask);

        return mask;

    }

    public static <T extends RealType<T> & NativeType<T>> DataContainer<T> prepareDataContainer(Image image1,
            Image image2, @Nullable Image mask) {
        DataContainer<T> data;

        try {
            if (mask == null)
                data = new DataContainer<T>(image1.getImgPlus(), image2.getImgPlus(), 0, 0, "Channel 1", "Channel 2");
            else
                data = new DataContainer<T>(image1.getImgPlus(), image2.getImgPlus(), 0, 0, "Channel 1", "Channel 2",
                        mask.getImgPlus(), new long[2], new long[2]);

            return data;

        } catch (MissingPreconditionException e) {
            MIA.log.writeError(e);
            return null;
        }
    }

    public void setImageMeasurements(Image image, HashMap<String, Double> measurements, String imageName1,
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

            MIA.log.writeError(e);

        }

        return measurements;

    }

    public static <T extends RealType<T> & NativeType<T>> void setManualThresholds(DataContainer<T> data, Image image1,
            double fixedThreshold1, double fixedThreshold2) {
        ManualThreshold<T> manualThreshold = new ManualThreshold<T>(image1, fixedThreshold1, fixedThreshold2);
        data.setAutoThreshold(manualThreshold);
    }

    public static <T extends RealType<T> & NativeType<T>> HashMap<String, Double> measureKendalls(
            DataContainer<T> data) {
        // Based on code from
        // https://github.com/fiji/Colocalisation_Analysis/blob/master/src/main/java/sc/fiji/coloc/algorithms/KendallTauRankCorrelation.java
        // (Accessed 2021-08-11)
        HashMap<String, Double> measurements = new HashMap<>();

        RandomAccessible<T> img1 = data.getSourceImage1();
        RandomAccessible<T> img2 = data.getSourceImage2();
        RandomAccessibleInterval<BitType> mask = data.getMask();

        TwinCursor<T> cursor = new TwinCursor<T>(img1.randomAccess(), img2.randomAccess(),
                Views.iterable(mask).localizingCursor());

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
            String pccImplementationName) throws MissingPreconditionException {
        HashMap<String, Double> measurements = new HashMap<>();

        PearsonsCorrelation.Implementation implementation = getPCCImplementation(pccImplementationName);
        PearsonsCorrelation<T> pc = new PearsonsCorrelation<T>(implementation);
        try {
            if (data.getMask() == null)
                measurements.put(Measurements.PCC, pc.calculatePearsons(data.getSourceImage1(), data.getSourceImage2()));
            else
                measurements.put(Measurements.PCC, pc.calculatePearsons(data.getSourceImage1(), data.getSourceImage2(), data.getMask()));
            
            if (data.getAutoThreshold() != null) {
                pc.execute(data);            
                measurements.put(Measurements.PCC_BELOW_THRESHOLD, pc.getPearsonsCorrelationBelowThreshold());
                measurements.put(Measurements.PCC_ABOVE_THRESHOLD, pc.getPearsonsCorrelationAboveThreshold());
            }

        } catch (MissingPreconditionException e) {
            measurements.put(Measurements.PCC, Double.NaN);
            if (data.getAutoThreshold() != null) {
                measurements.put(Measurements.PCC_BELOW_THRESHOLD, Double.NaN);
                measurements.put(Measurements.PCC_ABOVE_THRESHOLD, Double.NaN);
            }

            // Throw the exception, so the calling class can deal with writing an
            // appropriate warning (i.e. images and objects will want a different message).
            throw e;

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
        return Categories.IMAGES_MEASURE;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Calculates colocalisation of two input images.  All measurements are associated with the first input image.  Measurements can be restricted to specific region using image or object-based masking.  To measure colocalisation on an object-by-object basis please use the \""
                + new MeasureObjectColocalisation<>(null).getName() + "\" module.<br><br>"

                + "All calculations are performed using the <a href=\"https://imagej.net/plugins/coloc-2\">Coloc2 plugin</a>.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input images
        String imageName1 = parameters.getValue(INPUT_IMAGE_1,workspace);
        Image image1 = (Image) workspace.getImage(imageName1);

        String imageName2 = parameters.getValue(INPUT_IMAGE_2,workspace);
        Image image2 = (Image) workspace.getImages().get(imageName2);

        // Getting parameters
        String maskingMode = parameters.getValue(MASKING_MODE,workspace);
        String maskImageName = parameters.getValue(MASK_IMAGE,workspace);
        Image maskImage = (Image) workspace.getImage(maskImageName);
        String imageMaskLogic = parameters.getValue(IMAGE_MASK_LOGIC,workspace);
        String objectName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs objects = workspace.getObjects(objectName);
        String objectMaskLogic = parameters.getValue(OBJECT_MASK_LOGIC,workspace);
        String thresholdingMode = parameters.getValue(THRESHOLDING_MODE,workspace);
        String imageMeasurementName1 = parameters.getValue(IMAGE_MEASUREMENT_1,workspace);
        String imageMeasurementName2 = parameters.getValue(IMAGE_MEASUREMENT_2,workspace);
        double fixedThreshold1 = parameters.getValue(FIXED_THRESHOLD_1,workspace);
        double fixedThreshold2 = parameters.getValue(FIXED_THRESHOLD_2,workspace);
        String pccImplementationName = parameters.getValue(PCC_IMPLEMENTATION,workspace);
        boolean measureKendalls = parameters.getValue(MEASURE_KENDALLS_RANK,workspace);
        boolean measureLiICQ = parameters.getValue(MEASURE_LI_ICQ,workspace);
        boolean measureManders = parameters.getValue(MEASURE_MANDERS,workspace);
        boolean measurePCC = parameters.getValue(MEASURE_PCC,workspace);
        boolean measureSpearman = parameters.getValue(MEASURE_SPEARMANS_RANK,workspace);

        // If objects are to be used as a mask a binary image is created. Otherwise,
        // null is returned
        switch (maskingMode) {
            case MaskingModes.MASK_IMAGE:
                maskImage = ImageFactory.createImage("Mask", maskImage.getImagePlus().duplicate());
                if (imageMaskLogic.equals(ImageMaskLogic.MEASURE_ON_BLACK))
                    InvertIntensity.process(maskImage);
                break;
            case MaskingModes.MASK_OBJECTS:
                maskImage = getObjectMask(objects, objectMaskLogic);
                if (objects == null || objects.size() == 0)
                    return Status.PASS;
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
            case ThresholdingModes.IMAGE_MEASUREMENTS:
                double threshold1 = image1.getMeasurement(imageMeasurementName1).getValue();
                double threshold2 = image1.getMeasurement(imageMeasurementName2).getValue();
                setManualThresholds(data, image1, threshold1, threshold2);
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
            try {
                HashMap<String, Double> measurements = measurePCC(data, pccImplementationName);
                setImageMeasurements(image1, measurements, imageName1, imageName2);
            } catch (MissingPreconditionException e) {
                MIA.log.writeWarning("PCC can't be calculated for image");
            }
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
        parameters.add(new ImageMeasurementP(IMAGE_MEASUREMENT_1, this));
        parameters.add(new ImageMeasurementP(IMAGE_MEASUREMENT_2, this));
        parameters.add(new DoubleP(FIXED_THRESHOLD_1, this, 1.0));
        parameters.add(new DoubleP(FIXED_THRESHOLD_2, this, 1.0));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new ChoiceP(PCC_IMPLEMENTATION, this, PCCImplementations.FAST, PCCImplementations.ALL));
        parameters.add(new BooleanP(MEASURE_KENDALLS_RANK, this, true));
        parameters.add(new BooleanP(MEASURE_LI_ICQ, this, true));
        parameters.add(new BooleanP(MEASURE_MANDERS, this, true));
        parameters.add(new BooleanP(MEASURE_PCC, this, true));
        parameters.add(new BooleanP(MEASURE_SPEARMANS_RANK, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE_1));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE_2));

        returnedParameters.add(parameters.getParameter(MASKING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MASKING_MODE));
        switch ((String) parameters.getValue(MASKING_MODE,null)) {
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
        switch ((String) parameters.getValue(THRESHOLDING_MODE,null)) {
            case ThresholdingModes.IMAGE_MEASUREMENTS:
                returnedParameters.add(parameters.getParameter(IMAGE_MEASUREMENT_1));
                String imageName1 = parameters.getValue(INPUT_IMAGE_1,null);
                ((ImageMeasurementP) parameters.getParameter(IMAGE_MEASUREMENT_1)).setImageName(imageName1);
                returnedParameters.add(parameters.getParameter(IMAGE_MEASUREMENT_2));
                String imageName2 = parameters.getValue(INPUT_IMAGE_2,null);
                ((ImageMeasurementP) parameters.getParameter(IMAGE_MEASUREMENT_2)).setImageName(imageName2);
                break;
            case ThresholdingModes.MANUAL:
                returnedParameters.add(parameters.getParameter(FIXED_THRESHOLD_1));
                returnedParameters.add(parameters.getParameter(FIXED_THRESHOLD_2));
                break;
        }

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));        
        returnedParameters.add(parameters.getParameter(MEASURE_KENDALLS_RANK));
        returnedParameters.add(parameters.getParameter(MEASURE_LI_ICQ));
        returnedParameters.add(parameters.getParameter(MEASURE_MANDERS));
        returnedParameters.add(parameters.getParameter(MEASURE_PCC));
        returnedParameters.add(parameters.getParameter(MEASURE_SPEARMANS_RANK));
        if ((boolean) parameters.getValue(MEASURE_PCC,null)
                || ((String) parameters.getValue(THRESHOLDING_MODE,null)).equals(ThresholdingModes.BISECTION)
                || ((String) parameters.getValue(THRESHOLDING_MODE,null)).equals(ThresholdingModes.COSTES)) {
            returnedParameters.add(parameters.getParameter(PCC_IMPLEMENTATION));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
Workspace workspace = null;
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        String inputImage1Name = parameters.getValue(INPUT_IMAGE_1,workspace);
        String inputImage2Name = parameters.getValue(INPUT_IMAGE_2,workspace);

        switch ((String) parameters.getValue(THRESHOLDING_MODE,workspace)) {
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

        if ((boolean) parameters.getValue(MEASURE_KENDALLS_RANK,workspace)) {
            String name = getFullName(inputImage1Name, inputImage2Name, Measurements.KENDALLS_TAU);
            ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
            reference.setImageName(inputImage1Name);
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(MEASURE_LI_ICQ,workspace)) {
            String name = getFullName(inputImage1Name, inputImage2Name, Measurements.LI_ICQ);
            ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
            reference.setImageName(inputImage1Name);
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(MEASURE_MANDERS,workspace)) {
            String name = getFullName(inputImage1Name, inputImage2Name, Measurements.M1_ABOVE_ZERO);
            ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
            reference.setImageName(inputImage1Name);
            returnedRefs.add(reference);

            name = getFullName(inputImage1Name, inputImage2Name, Measurements.M2_ABOVE_ZERO);
            reference = imageMeasurementRefs.getOrPut(name);
            reference.setImageName(inputImage1Name);
            returnedRefs.add(reference);

            if (!((String) parameters.getValue(THRESHOLDING_MODE,workspace)).equals(ThresholdingModes.NONE)) {
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

        if ((boolean) parameters.getValue(MEASURE_PCC,workspace)) {
            String name = getFullName(inputImage1Name, inputImage2Name, Measurements.PCC);
            ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
            reference.setImageName(inputImage1Name);
            returnedRefs.add(reference);

            if (!((String) parameters.getValue(THRESHOLDING_MODE,workspace)).equals(ThresholdingModes.NONE)) {
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

        if ((boolean) parameters.getValue(MEASURE_SPEARMANS_RANK,workspace)) {
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
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
return null;
    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
	return null; 
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
        String siteRef = "Description taken from <a href=\"https://imagej.net/imaging/colocalization-analysis\">https://imagej.net/imaging/colocalization-analysis</a>";

        parameters.get(INPUT_IMAGE_1).setDescription(
                "First image for which colocalisation will be calculated.  Measurements will be associated with this image.");

        parameters.get(INPUT_IMAGE_2).setDescription("Second image for which colocalisation will be calculated.");

        parameters.get(MASKING_MODE)
                .setDescription("Controls which regions of the image will be evaluated for colocalisation:<br><ul>"

                        + "<li>\"" + MaskingModes.MASK_IMAGE + "\" A binary image (specified using \"" + MASK_IMAGE
                        + "\") determines which pixels are evaluated for colocalisation.  The \"" + IMAGE_MASK_LOGIC
                        + "\" parameter controls whether the pixels to be evaluated are black (0 intensity) or white (255 intensity).</li>"

                        + "<li>\"" + MaskingModes.MASK_OBJECTS + "\" An object collection (specified using \""
                        + INPUT_OBJECTS + "\") determines which pixels are evaluated for colocalisation.  The \""
                        + OBJECT_MASK_LOGIC
                        + "\" parameter controls whether the pixels to be evaluated are inside or outside the objects.</li>"

                        + "<li>\"" + MaskingModes.NONE
                        + "\" No mask will be applied.  All pixels in the image will be evaluated for colocalisation.</li></ul>");

        parameters.get(MASK_IMAGE).setDescription("If \"" + MASKING_MODE + "\" is set to \"" + MaskingModes.MASK_IMAGE
                + "\", this is the binary image which will control the pixels to be evaluated for colocalisation.  The \""
                + IMAGE_MASK_LOGIC
                + "\" parameter controls whether the pixels to be evaluated are black (0 intensity) or white (255 intensity).");

        parameters.get(IMAGE_MASK_LOGIC).setDescription(
                "Controls whether colocalisation is measured for pixels coincident with black (0 intensity) or white (255 intensity) pixels in the mask image.");

        parameters.get(INPUT_OBJECTS).setDescription("If \"" + MASKING_MODE + "\" is set to \""
                + MaskingModes.MASK_OBJECTS
                + "\", this is the object collection which will control the pixels to be evaluated for colocalisation.  The \""
                + OBJECT_MASK_LOGIC
                + "\" parameter controls whether the pixels to be evaluated are inside or outside the objects.");

        parameters.get(OBJECT_MASK_LOGIC).setDescription(
                "Controls whether colocalisation is measured for pixels inside or outside objects in the masking object collection.");

        parameters.get(THRESHOLDING_MODE)
                .setDescription("Controls how the thresholds for measurements such as Manders' are set:<br><ul>"

                        + "<li>\"" + ThresholdingModes.BISECTION
                        + "\" A faster method to calculate thresholds than the Costes approach.</li>"

                        + "<li>\"" + ThresholdingModes.COSTES
                        + "\" The \"standard\" method to calculate thresholds for Manders' colocalisation measures.  This approach sets the thresholds for the two input images such that the pixels with intensities lower than their respective thresholds don't have any statistical correlation (i.e. have PCC values less than or equal to 0).  This is based on Costes' 2004 paper (Costes et al., <i>Biophys. J.</i> <b>86</b> (2004) 3993–4003.</li>"

                        + "<li>\"" + ThresholdingModes.IMAGE_MEASUREMENTS
                        + "\" Thresholds for each image will be set equal to measurements associated with each object.</li>"

                        + "<li>\"" + ThresholdingModes.MANUAL
                        + "\" Threshold values are manually set from user-defined values (\"" + FIXED_THRESHOLD_1
                        + "\" and \"" + FIXED_THRESHOLD_2 + "\" parameters).</li>"

                        + "<li>\"" + ThresholdingModes.NONE
                        + "\" No threshold is set.  In this instance, Manders' metrics will only be calculated above zero intensity rather than both above zero and above the thresholds.  Similarly, Pearson's correlation coefficients will only be calculated for the entire region (after masking) rather than also for above and below the thresholds.</li></ul>");

        parameters.get(IMAGE_MEASUREMENT_1)
                .setDescription("If \"" + THRESHOLDING_MODE + "\" is set to \"" + ThresholdingModes.IMAGE_MEASUREMENTS
                        + "\", this is the measurement associated with \"" + INPUT_IMAGE_1
                        + "\" that will be applied to the first image.");

        parameters.get(IMAGE_MEASUREMENT_2)
                .setDescription("If \"" + THRESHOLDING_MODE + "\" is set to \"" + ThresholdingModes.IMAGE_MEASUREMENTS
                        + "\", this is the measurement associated with \"" + INPUT_IMAGE_2
                        + "\" that will be applied to the second image.");

        parameters.get(FIXED_THRESHOLD_1).setDescription("If \"" + THRESHOLDING_MODE + "\" is set to \""
                + ThresholdingModes.MANUAL + "\", this is the threshold that will be applied to the first image.");

        parameters.get(FIXED_THRESHOLD_2).setDescription("If \"" + THRESHOLDING_MODE + "\" is set to \""
                + ThresholdingModes.MANUAL + "\", this is the threshold that will be applied to the second image.");

        parameters.get(PCC_IMPLEMENTATION).setDescription(
                "Controls whether PCC should be calculated using the classic algorithm or using the Coloc2-default \"fast\" method.");

        parameters.get(MEASURE_KENDALLS_RANK).setDescription(
                "When selected, Kendall's rank correlation will be calculated.  This works in a similar manner to Pearson's PCC, except it's calculated on ranked data rather than raw pixel intensities.");

        parameters.get(MEASURE_LI_ICQ).setDescription(
                "When selected, Li's ICQ (intensity correlation quotient) will be calculated.  This measure reports the frequency with which both corresponding pixels for both channels are either both above or both below their respective means.  Values are scaled into the range -0.5 to +0.5, with values below 0 corresponding to anti-correlation and values above 0 indicating correlation.");

        parameters.get(MEASURE_MANDERS).setDescription(
                "When selected, Manders' M1 and M2 coefficients will be calculated.  \"Proportional to the amount of fluorescence of the colocalizing pixels or voxels in each colour channel. You can get more details in Manders et al. Values range from 0 to 1, expressing the fraction of intensity in a channel that is located in pixels where there is above zero (or threshold) intensity in the other colour channel.\" "
                        + siteRef);

        parameters.get(MEASURE_PCC).setDescription(
                "When selected, Pearson's Correlation Coefficient (PCC) will be calculated.  \"It is not sensitive to differences in mean signal intensities or range, or a zero offset between the two components. The result is +1 for perfect correlation, 0 for no correlation, and -1 for perfect anti-correlation. Noise makes the value closer to 0 than it should be.\" "
                        + siteRef);

        parameters.get(MEASURE_SPEARMANS_RANK).setDescription(
                "When selected, Spearman's rank correlation will be calculated.  Spearman's rho is calculated in a similar manner to Pearson's PCC, except the image intensities are replaced by their respective rank.  Spearman's correlation works with monotonic relationships.  As with PCC, values are in the range -1 to +1.");

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
