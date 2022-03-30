package io.github.mianalysis.mia.module.objects.measure.intensity;

import java.util.concurrent.atomic.AtomicInteger;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.SubHyperstackMaker;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageTypeConverter;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.common.analysis.TextureCalculator;

/**
 * Takes a set of objects and measures intensity texture values on a provided
 * image. Measurements are stored with the objects.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class MeasureObjectTexture extends Module {
    public static final String INPUT_SEPARATOR = "Object and image input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE = "Input image";

    public static final String TEXTURE_SEPARATOR = "Texture calculation";
    public static final String X_OFFSET = "X-offset";
    public static final String Y_OFFSET = "Y-offset";
    public static final String Z_OFFSET = "Z-offset";
    public static final String CALIBRATED_OFFSET = "Calibrated offset";

    public MeasureObjectTexture(Modules modules) {
        super("Measure object texture", modules);
    }

    public interface Measurements {
        String ASM = "ASM";
        String CONTRAST = "CONTRAST";
        String CORRELATION = "CORRELATION";
        String ENTROPY = "ENTROPY";

    }

    public static String getFullName(String imageName, String measurement, double[] offs, boolean calibrated) {
        if (calibrated) {
            return "TEXTURE // " + imageName + "_" + measurement + "_(" + offs[0] + "," + offs[1] + "," + offs[2]
                    + " ${SCAL})";
        } else {
            return "TEXTURE // " + imageName + "_" + measurement + "_(" + offs[0] + "," + offs[1] + "," + offs[2]
                    + " PX)";
        }
    }

    static void convertCalibratedOffsets(double[] offs, Obj referenceObject) {
        double dppXY = referenceObject.getDppXY();
        double dppZ = referenceObject.getDppZ();

        offs[0] = (int) Math.round(offs[0] / dppXY);
        offs[1] = (int) Math.round(offs[1] / dppXY);
        offs[2] = (int) Math.round(offs[2] / dppZ);

    }

    public static void processObject(Obj object, Image image, TextureCalculator textureCalculator, double[] offs,
            boolean calibratedOffset) {
        ImagePlus ipl = image.getImagePlus();

        // If the input stack is a single timepoint and channel, there's no need to
        // create a new ImageStack
        ImageStack timeStack = null;
        if (ipl.getNChannels() == 1 && ipl.getNFrames() == 1) {
            timeStack = ipl.getStack();
        } else {
            int t = object.getT() + 1;
            int nSlices = ipl.getNSlices();
            timeStack = SubHyperstackMaker.makeSubhyperstack(ipl, "1-1", "1-" + nSlices, t + "-" + t).getStack();
        }

        textureCalculator.calculate(timeStack, object, (int) offs[0], (int) offs[1], (int) offs[2]);

        // Acquiring measurements
        String name = getFullName(image.getName(), Measurements.ASM, offs, calibratedOffset);
        object.addMeasurement(new Measurement(name, textureCalculator.getASM()));

        name = getFullName(image.getName(), Measurements.CONTRAST, offs, calibratedOffset);
        object.addMeasurement(new Measurement(name, textureCalculator.getContrast()));

        name = getFullName(image.getName(), Measurements.CORRELATION, offs, calibratedOffset);
        object.addMeasurement(new Measurement(name, textureCalculator.getCorrelation()));

        name = getFullName(image.getName(), Measurements.ENTROPY, offs, calibratedOffset);
        object.addMeasurement(new Measurement(name, textureCalculator.getEntropy()));

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_INTENSITY;
    }

    @Override
    public String getDescription() {
        return "Calculates Haralick's texture features for each object in a collection for a specific image.  Each point in each object is compared to a corresponding point, a defined offset away (e.g. x-offset = 1, y-offset=0, z-offset=0 to compare to the pixel immediately right of each pixel).  The intensities of each point pair are added to a 2D gray-level co-occurrence matrix (GLCM) from which measures of angular second moment, contrast, correlation and entropy can be calculated."

        + "<br><br>Robert M Haralick; K Shanmugam; Its'hak Dinstein, \"Textural Features for Image Classification\" <i>IEEE Transactions on Systems, Man, and Cybernetics. SMC-3</i> (1973) <b>6</b> 610–621.";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // This requires an 8-bit image. If the provided image isn't 8-bit, convert it
        if (inputImagePlus.getBitDepth() != 8) {
            MIA.log.writeWarning(
                    "Texture analysis requires an 8-bit image.  Converting to 8-bit with scaling enabled.");
            inputImagePlus = inputImagePlus.duplicate();
            ImageTypeConverter.process(inputImagePlus, 8, ImageTypeConverter.ScalingModes.SCALE);
            inputImage = ImageFactory.createImage(inputImage.getName(), inputImagePlus);
        }

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // If no objects were detected, skipping this module
        if (inputObjects.size() == 0)
            return Status.PASS;

        // Getting parameters
        double xOffsIn = parameters.getValue(X_OFFSET);
        double yOffsIn = parameters.getValue(Y_OFFSET);
        double zOffsIn = parameters.getValue(Z_OFFSET);
        boolean calibratedOffset = parameters.getValue(CALIBRATED_OFFSET);

        double[] offs = new double[] { xOffsIn, yOffsIn, zOffsIn };

        // If using calibrated offset values, determining the closest pixel offset
        if (calibratedOffset)
            convertCalibratedOffsets(offs, inputObjects.getFirst());

        // Initialising the texture calculator
        TextureCalculator textureCalculator = new TextureCalculator();

        int nObjects = inputObjects.size();
        AtomicInteger iter = new AtomicInteger(0);
        for (Obj object : inputObjects.values()) {
            processObject(object, inputImage, textureCalculator, offs, calibratedOffset);
            writeProgressStatus(iter.incrementAndGet(), nObjects, "objects");
        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(TEXTURE_SEPARATOR, this));
        parameters.add(new DoubleP(X_OFFSET, this, 1d));
        parameters.add(new DoubleP(Y_OFFSET, this, 0d));
        parameters.add(new DoubleP(Z_OFFSET, this, 0d));
        parameters.add(new BooleanP(CALIBRATED_OFFSET, this, false));

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
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        double xOffsIn = parameters.getValue(X_OFFSET);
        double yOffsIn = parameters.getValue(Y_OFFSET);
        double zOffsIn = parameters.getValue(Z_OFFSET);
        boolean calibratedOffset = parameters.getValue(CALIBRATED_OFFSET);
        double[] offs = new double[] { xOffsIn, yOffsIn, zOffsIn };

        String name = getFullName(inputImageName, Measurements.ASM, offs, calibratedOffset);
        ObjMeasurementRef asm = objectMeasurementRefs.getOrPut(name);
        asm.setObjectsName(inputObjectsName);
        returnedRefs.add(asm);

        name = getFullName(inputImageName, Measurements.CONTRAST, offs, calibratedOffset);
        ObjMeasurementRef contrast = objectMeasurementRefs.getOrPut(name);
        contrast.setObjectsName(inputObjectsName);
        returnedRefs.add(contrast);

        name = getFullName(inputImageName, Measurements.CORRELATION, offs, calibratedOffset);
        ObjMeasurementRef correlation = objectMeasurementRefs.getOrPut(name);
        correlation.setObjectsName(inputObjectsName);
        returnedRefs.add(correlation);

        name = getFullName(inputImageName, Measurements.ENTROPY, offs, calibratedOffset);
        ObjMeasurementRef entropy = objectMeasurementRefs.getOrPut(name);
        entropy.setObjectsName(inputObjectsName);
        returnedRefs.add(entropy);

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
        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects from the workspace for which the corresponding texture of the image (specified by \""
                        + INPUT_IMAGE + "\") will be calculated.  Textures will be calculated for each coordinate of each object and will include instances where the corresponding point (the intensity at the specified offset) is outside the object.  Texture measurements will be assigned to the relevant objects.");

        parameters.get(INPUT_IMAGE).setDescription(
                "Image from the workspace from which texture metrics for each object will be calculated.");

        parameters.get(X_OFFSET).setDescription(
                "Each pixel in the input image will be compared to the pixel a defined offset-away.  This parameter controls the x-axis offset.  Offset specified in pixel units unless \""
                        + CALIBRATED_OFFSET
                        + "\" is selected.  If using calibrated units, the offset will be rounded to the closest integer value.");

        parameters.get(Y_OFFSET).setDescription(
                "Each pixel in the input image will be compared to the pixel a defined offset-away.  This parameter controls the y-axis offset.  Offset specified in pixel units unless \""
                        + CALIBRATED_OFFSET
                        + "\" is selected.  If using calibrated units, the offset will be rounded to the closest integer value.");

        parameters.get(Z_OFFSET).setDescription(
                "Each pixel in the input image will be compared to the pixel a defined offset-away.  This parameter controls the z-axis offset.  Offset specified in pixel units unless \""
                        + CALIBRATED_OFFSET
                        + "\" is selected.  If using calibrated units, the offset will be rounded to the closest integer value.");

        parameters.get(CALIBRATED_OFFSET).setDescription(
                "When selected, offsets are specified in calibrated units.  Otherwise, offsets are assumed to be in pixel units.");

    }
}