// package wbif.sjx.MIA.Module.ImageMeasurements;

// import java.util.HashMap;

// import ij.ImagePlus;
// import wbif.sjx.MIA.Module.Module;
// import wbif.sjx.MIA.Module.ModuleCollection;
// import wbif.sjx.MIA.Module.Category;
// import wbif.sjx.MIA.Module.Categories;
// import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
// import wbif.sjx.MIA.Object.Status;
// import wbif.sjx.MIA.Object.Image;
// import wbif.sjx.MIA.Object.Measurement;
// import wbif.sjx.MIA.Object.ObjCollection;
// import wbif.sjx.MIA.Object.Workspace;
// import wbif.sjx.MIA.Object.Parameters.ChoiceP;
// import wbif.sjx.MIA.Object.Parameters.InputImageP;
// import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
// import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
// import wbif.sjx.MIA.Object.References.ImageMeasurementRef;
// import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
// import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
// import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
// import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
// import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
// import wbif.sjx.MIA.Process.ColourFactory;
// import wbif.sjx.common.Analysis.ColocalisationCalculator;
// import wbif.sjx.common.MathFunc.CumStat;

// public class MeasureImageColocalisation extends Module {
//     public static final String INPUT_IMAGE_1 = "Input image 1";
//     public static final String INPUT_IMAGE_2 = "Input image 2";
//     public static final String MASKING_MODE = "Masking mode";
//     public static final String INPUT_OBJECTS = "Input objects";

//     public MeasureImageColocalisation(ModuleCollection modules) {
//         super("Measure image colocalisation",modules);
//     }

//     public interface MaskingModes {
//         String NONE = "None";
//         String MEASURE_INSIDE_OBJECTS = "Measure inside objects";
//         String MEASURE_OUTSIDE_OBJECTS = "Measure outside objects";

//         String[] ALL = new String[]{NONE,MEASURE_INSIDE_OBJECTS,MEASURE_OUTSIDE_OBJECTS};

//     }

//     public interface Measurements {
//         String MEAN_PCC = "MEAN_PCC";
//     }

//     public static Image getMaskImage(ObjCollection objects, String maskingMode) {
//         switch (maskingMode) {
//             case MaskingModes.MEASURE_INSIDE_OBJECTS:
//                 // Creating the new Obj
//                 HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(objects,ColourFactory.SingleColours.WHITE);
//                 Image image = objects.convertToImage("Mask",hues,8,false);
//                 InvertIntensity.process(image.getImagePlus());
//                 return image;

//             case MaskingModes.MEASURE_OUTSIDE_OBJECTS:
//                 // Creating the new Obj
//                 hues = ColourFactory.getSingleColourHues(objects,ColourFactory.SingleColours.WHITE);
//                 return objects.convertToImage("Mask",hues,8,false);

//             case MaskingModes.NONE:
//                 return null;

//             default:
//                 return null;
//         }
//     }

//     public static String getFullName(String imageName2, String measurement) {
//         return "COLOCALISATION // "+imageName2+"_"+measurement;
//     }

//     public static void measurePCC(Image image1, Image image2, Image mask) {
//         CumStat cs = new CumStat();
//         ImagePlus ipl1 = image1.getImagePlus();
//         ImagePlus ipl2 = image2.getImagePlus();
//         ImagePlus maskIpl = mask == null ? null : mask.getImagePlus();

//         // Iterating over all stacks
//         for (int c=0;c<ipl1.getNChannels();c++) {
//             for (int t=0;t<ipl1.getNFrames();t++) {
//                 ipl1.setPosition(c+1,1,t+1);
//                 ipl2.setPosition(c+1,1,t+1);

//                 if (mask == null) {
//                     cs.addMeasure(ColocalisationCalculator.calculatePCC(ipl1.getStack(),ipl2.getStack()));
//                 } else {
//                     maskIpl.setPosition(c+1,1,t+1);
//                     cs.addMeasure(ColocalisationCalculator.calculatePCC(ipl1.getStack(),ipl2.getStack(),maskIpl.getStack()));
//                 }
//             }
//         }

//         // Adding the measurement to the image
//         String name = getFullName(image2.getName(),Measurements.MEAN_PCC);
//         image1.addMeasurement(new Measurement(name,cs.getMean()));

//     }

//     @Override
//     public Category getCategory() {
//         return Categories.IMAGE_MEASUREMENTS;
//     }

//     @Override
//     public String getDescription() {
//         return "Calculates PCC, averaged across all timepoints and channels.";
//     }

//     @Override
//     public Status process(Workspace workspace) {
//         // Getting input images
//         String imageName1 = parameters.getValue(INPUT_IMAGE_1);
//         Image image1 = workspace.getImages().get(imageName1);

//         String imageName2 = parameters.getValue(INPUT_IMAGE_2);
//         Image image2 = workspace.getImages().get(imageName2);

//         // Getting input objects
//         String objectName = parameters.getValue(INPUT_OBJECTS);
//         ObjCollection objects = workspace.getObjects().get(objectName);

//         // Getting parameters
//         String maskingMode = parameters.getValue(MASKING_MODE);

//         // If objects are to be used as a mask a binary image is created.  Otherwise, null is returned
//         Image mask = getMaskImage(objects,maskingMode);

//         // Running measurements
//         measurePCC(image1,image2,mask);

//         if (showOutput) image1.showMeasurements(this);
//         if (showOutput) image2.showMeasurements(this);

//         return Status.PASS;

//     }

//     @Override
//     protected void initialiseParameters() {
//         parameters.add(new InputImageP(INPUT_IMAGE_1,this));
//         parameters.add(new InputImageP(INPUT_IMAGE_2,this));
//         parameters.add(new ChoiceP(MASKING_MODE,this,MaskingModes.NONE,MaskingModes.ALL));
//         parameters.add(new InputObjectsP(INPUT_OBJECTS,this));

//     }

//     @Override
//     public ParameterCollection updateAndGetParameters() {
//         ParameterCollection returnedParameters = new ParameterCollection();

//         returnedParameters.add(parameters.getParameter(INPUT_IMAGE_1));
//         returnedParameters.add(parameters.getParameter(INPUT_IMAGE_2));

//         returnedParameters.add(parameters.getParameter(MASKING_MODE));
//         switch ((String) parameters.getValue(MASKING_MODE)) {
//             case MaskingModes.MEASURE_INSIDE_OBJECTS:
//             case MaskingModes.MEASURE_OUTSIDE_OBJECTS:
//                 returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
//                 break;
//         }

//         return returnedParameters;
//     }

//     @Override
//     public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
//         ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

//         String inputImage1Name = parameters.getValue(INPUT_IMAGE_1);
//         String inputImage2Name = parameters.getValue(INPUT_IMAGE_2);

//         String name = getFullName(inputImage2Name,Measurements.MEAN_PCC);
//         ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
//         reference.setImageName(inputImage1Name);
//         returnedRefs.add(reference);

//         return returnedRefs;

//     }

//     @Override
//     public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public MetadataRefCollection updateAndGetMetadataReferences() {
//         return null;
//     }

//     @Override
//     public ParentChildRefCollection updateAndGetParentChildRefs() {
//         return null;
//     }

//     @Override
//     public PartnerRefCollection updateAndGetPartnerRefs() {
//         return null;
//     }

//     @Override
//     public boolean verify() {
//         return true;
//     }
// }

package wbif.sjx.MIA.Module.ImageMeasurements;

import java.util.HashMap;
import java.util.HashSet;

import com.drew.lang.annotations.Nullable;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.TwinCursor;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import sc.fiji.coloc.algorithms.AutoThresholdRegression;
import sc.fiji.coloc.algorithms.MandersColocalization;
import sc.fiji.coloc.algorithms.MandersColocalization.MandersResults;
import sc.fiji.coloc.algorithms.MissingPreconditionException;
import sc.fiji.coloc.algorithms.PearsonsCorrelation;
import sc.fiji.coloc.gadgets.DataContainer;
import sc.fiji.coloc.gadgets.ThresholdMode;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
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

    public static final String MEASUREMENT_SEPARATOR = "Measurement controls";
    public static final String THRESHOLDING_MODE = "Thresholding mode";
    public static final String FIXED_THRESHOLD_1 = "Threshold (C1)";
    public static final String FIXED_THRESHOLD_2 = "Threshold (C2)";
    public static final String MEASURE_PCC = "Measure PCC";
    public static final String PCC_IMPLEMENTATION = "PCC implementation";
    public static final String MEASURE_MANDERS = "Measure Manders";

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
        String M1_ABOVE_ZERO = "M1_ABOVE_ZERO";
        String M2_ABOVE_ZERO = "M2_ABOVE_ZERO";
        String M1_ABOVE_THRESHOLD = "M1_ABOVE_THRESHOLD";
        String M2_ABOVE_THRESHOLD = "M2_ABOVE_THRESHOLD";

        String PCC = "PCC";
        String PCC_BELOW_THRESHOLD = "PCC_BELOW_THRESHOLD";
        String PCC_ABOVE_THRESHOLD = "PCC_ABOVE_THRESHOLD";

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

    public static <T extends RealType<T> & NativeType<T>> Image<T> getObjectMask(ObjCollection objects, String maskLogic) {
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(objects, ColourFactory.SingleColours.WHITE);
        Image<T> mask = objects.convertToImage("Mask", hues, 8, false);

        if (maskLogic.equals(ObjectMaskLogic.MEASURE_OUTSIDE_OBJECTS))
            InvertIntensity.process(mask);

        return mask;

    }

    public static <T extends RealType<T> & NativeType<T>> DataContainer<T> prepareDataContainer(Image<T> image1, Image<T> image2, @Nullable Image<T> mask) {
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

    public void setImageMeasurements(Image<T> image, HashSet<Measurement> measurements) {
        // Duplicating each measurement before applying, so different objects are
        // applied to each Image or Obj.
        for (Measurement measurement : measurements)
            image.addMeasurement(new Measurement(measurement.getName(), measurement.getValue()));
    }

    public static <T extends RealType<T> & NativeType<T>>HashSet<Measurement> setAutoThresholds(DataContainer<T> data, Image<T> image1, Image<T> image2,
            String thresholdingMode, String pccImplementationName) {
        HashSet<Measurement> measurements = new HashSet<>();

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

            String measurementName = getFullName(image1.getName(), image2.getName(), Measurements.THRESHOLD_1);
            measurements.add(new Measurement(measurementName, atr.getCh1MaxThreshold().getRealDouble()));
            measurementName = getFullName(image1.getName(), image2.getName(), Measurements.THRESHOLD_2);
            measurements.add(new Measurement(measurementName, atr.getCh2MaxThreshold().getRealDouble()));
            measurementName = getFullName(image1.getName(), image2.getName(), Measurements.THRESHOLD_SLOPE);
            measurements.add(new Measurement(measurementName, atr.getAutoThresholdSlope()));
            measurementName = getFullName(image1.getName(), image2.getName(), Measurements.THRESHOLD_Y_INTERCEPT);
            measurements.add(new Measurement(measurementName, atr.getAutoThresholdIntercept()));

        } catch (MissingPreconditionException e) {
            String measurementName = getFullName(image1.getName(), image2.getName(), Measurements.THRESHOLD_1);
            measurements.add(new Measurement(measurementName, Double.NaN));
            measurementName = getFullName(image1.getName(), image2.getName(), Measurements.THRESHOLD_1);
            measurements.add(new Measurement(measurementName, Double.NaN));
            measurementName = getFullName(image1.getName(), image2.getName(), Measurements.THRESHOLD_SLOPE);
            measurements.add(new Measurement(measurementName, Double.NaN));
            measurementName = getFullName(image1.getName(), image2.getName(), Measurements.THRESHOLD_Y_INTERCEPT);
            measurements.add(new Measurement(measurementName, Double.NaN));

            MIA.log.writeError(e.getMessage());
        }

        return measurements;

    }

    public static <T extends RealType<T> & NativeType<T>> void setManualThresholds(DataContainer<T> data, Image<T> image1, double fixedThreshold1,
            double fixedThreshold2) {
        ManualThreshold<T> manualThreshold = new ManualThreshold<T>(data, image1, fixedThreshold1, fixedThreshold2);
        data.setAutoThreshold(manualThreshold);
    }

    public static <T extends RealType<T> & NativeType<T>> HashSet<Measurement> measurePCC(DataContainer<T> data, Image<T> image1, Image<T> image2,
            String pccImplementationName) {
        HashSet<Measurement> measurements = new HashSet<>();

        PearsonsCorrelation.Implementation implementation = getPCCImplementation(pccImplementationName);
        PearsonsCorrelation<T> pc = new PearsonsCorrelation<T>(implementation);
        try {
            pc.execute(data);

            String measurementName = getFullName(image1.getName(), image2.getName(), Measurements.PCC);
            measurements.add(new Measurement(measurementName, pc.getPearsonsCorrelationValue()));

            if (data.getAutoThreshold() != null) {
                measurementName = getFullName(image1.getName(), image2.getName(), Measurements.PCC_BELOW_THRESHOLD);
                measurements.add(new Measurement(measurementName, pc.getPearsonsCorrelationBelowThreshold()));
                measurementName = getFullName(image1.getName(), image2.getName(), Measurements.PCC_ABOVE_THRESHOLD);
                measurements.add(new Measurement(measurementName, pc.getPearsonsCorrelationAboveThreshold()));
            }

        } catch (MissingPreconditionException e) {
            String measurementName = getFullName(image1.getName(), image2.getName(), Measurements.PCC);
            measurements.add(new Measurement(measurementName, Double.NaN));

            if (data.getAutoThreshold() != null) {
                measurementName = getFullName(image1.getName(), image2.getName(), Measurements.PCC_BELOW_THRESHOLD);
                measurements.add(new Measurement(measurementName, Double.NaN));
                measurementName = getFullName(image1.getName(), image2.getName(), Measurements.PCC_ABOVE_THRESHOLD);
                measurements.add(new Measurement(measurementName, Double.NaN));
            }

            MIA.log.writeError(e.getMessage());

        }

        return measurements;

    }

    public static <T extends RealType<T> & NativeType<T>> HashSet<Measurement> measureManders(DataContainer<T> data, Image<T> image1, Image<T> image2) {
        // Based on code from
        // https://github.com/fiji/Colocalisation_Analysis/blob/master/src/main/java/sc/fiji/coloc/algorithms/MandersColocalization.java
        // (Accessed 2021-08-10)
        HashSet<Measurement> measurements = new HashSet<>();

        MandersColocalization<T> mc = new MandersColocalization<>();

        // get the two images for the calculation of Manders' split coefficients
        RandomAccessible<T> img1 = data.getSourceImage1();
        RandomAccessible<T> img2 = data.getSourceImage2();
        RandomAccessibleInterval<BitType> mask = data.getMask();

        TwinCursor<T> cursor = new TwinCursor<T>(img1.randomAccess(), img2.randomAccess(),
                Views.iterable(mask).localizingCursor());

        // calculate Manders' split coefficients without threshold, M1 and M2.
        MandersResults results = mc.calculateMandersCorrelation(cursor, img1.randomAccess().get().createVariable());

        String measurementName = getFullName(image1.getName(), image2.getName(), Measurements.M1_ABOVE_ZERO);
        measurements.add(new Measurement(measurementName, results.m1));
        measurementName = getFullName(image1.getName(), image2.getName(), Measurements.M2_ABOVE_ZERO);
        measurements.add(new Measurement(measurementName, results.m2));

        // Calculate the thresholded Manders' split coefficients, tM1 and tM2, if
        // possible
        AutoThresholdRegression<T> autoThreshold = data.getAutoThreshold();
        if (autoThreshold != null) {
            // thresholded Manders' split coefficients, tM1 and tM2
            cursor.reset();
            results = mc.calculateMandersCorrelation(cursor, autoThreshold.getCh1MaxThreshold(),
                    autoThreshold.getCh2MaxThreshold(), ThresholdMode.Above);

            measurementName = getFullName(image1.getName(), image2.getName(), Measurements.M1_ABOVE_THRESHOLD);
            measurements.add(new Measurement(measurementName, results.m1));
            measurementName = getFullName(image1.getName(), image2.getName(), Measurements.M2_ABOVE_THRESHOLD);
            measurements.add(new Measurement(measurementName, results.m2));

        }

        return measurements;

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getDescription() {
        return "";
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
        boolean measurePCC = parameters.getValue(MEASURE_PCC);
        String pccImplementationName = parameters.getValue(PCC_IMPLEMENTATION);
        boolean measureManders = parameters.getValue(MEASURE_MANDERS);

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
                HashSet<Measurement> measurements = setAutoThresholds(data, image1, image2, thresholdingMode,
                        pccImplementationName);
                setImageMeasurements(image1, measurements);
                break;
            case ThresholdingModes.MANUAL:
                setManualThresholds(data, image1, fixedThreshold1, fixedThreshold2);
                break;
        }

        // Making colocalisation measurements
        if (measurePCC) {
            HashSet<Measurement> measurements = measurePCC(data, image1, image2, pccImplementationName);
            setImageMeasurements(image1, measurements);
        }

        if (measureManders) {
            HashSet<Measurement> measurements = measureManders(data, image1, image2);
            setImageMeasurements(image1, measurements);
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

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new ChoiceP(THRESHOLDING_MODE, this, ThresholdingModes.BISECTION, ThresholdingModes.ALL));
        parameters.add(new DoubleP(FIXED_THRESHOLD_1, this, 1.0));
        parameters.add(new DoubleP(FIXED_THRESHOLD_2, this, 1.0));
        parameters.add(new BooleanP(MEASURE_PCC, this, true));
        parameters.add(new ChoiceP(PCC_IMPLEMENTATION, this, PCCImplementations.FAST, PCCImplementations.ALL));
        parameters.add(new BooleanP(MEASURE_MANDERS, this, true));

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

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THRESHOLDING_MODE));
        switch ((String) parameters.getValue(THRESHOLDING_MODE)) {
            case ThresholdingModes.MANUAL:
                returnedParameters.add(parameters.getParameter(FIXED_THRESHOLD_1));
                returnedParameters.add(parameters.getParameter(FIXED_THRESHOLD_2));
                break;
        }

        returnedParameters.add(parameters.getParameter(MEASURE_PCC));
        if ((boolean) parameters.getValue(MEASURE_PCC)) {
            returnedParameters.add(parameters.getParameter(PCC_IMPLEMENTATION));
        }

        returnedParameters.add(parameters.getParameter(MEASURE_MANDERS));

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

    public ManualThreshold(DataContainer<T> data, Image image, double fixedThreshold1, double fixedThreshold2) {
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