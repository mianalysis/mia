package io.github.mianalysis.mia.module.objects.measure.intensity;
// NOTE: Only works in 2D.  Re-enable once working fully in 3D

import java.util.ArrayList;
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
import io.github.mianalysis.mia.module.objects.measure.spatial.MeasureSkeleton;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.common.analysis.TextureCalculator;
import io.github.sjcross.common.object.Point;


@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureTextureAlongPath extends Module {
    public static final String INPUT_SEPARATOR = "Object and image input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE = "Input image";

    public static final String MEASUREMENT_SEPARATOR = "Measurement selection";
    public static final String OFFSET = "Offset (px)";

    public MeasureTextureAlongPath(Modules modules) {
        super("Measure texture along path", modules);
    }

    public interface Measurements {
        String ASM = "ASM";
        String CONTRAST = "CONTRAST";
        String CORRELATION = "CORRELATION";
        String ENTROPY = "ENTROPY";

    }

    public static String getFullName(String imageName, String measurement, int offs) {
        return "LONGEST_PATH_TEXTURE // " + imageName + "_" + measurement + "_(" + offs + " PX)";
    }

    static void convertCalibratedOffsets(double[] offs, Obj referenceObject) {
        double dppXY = referenceObject.getDppXY();
        double dppZ = referenceObject.getDppZ();

        offs[0] = (int) Math.round(offs[0] / dppXY);
        offs[1] = (int) Math.round(offs[1] / dppXY);
        offs[2] = (int) Math.round(offs[2] / dppZ);

    }

    public static void processObject(Obj object, Image image, TextureCalculator textureCalculator,
            int offs) {
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

        // Getting longest path
        ArrayList<Point<Integer>> longestPath = MeasureSkeleton.getLargestShortestPath(object);
        for (int i = 0; i < longestPath.size() - offs; i++) {
            int x1 = longestPath.get(i).getX();
            int y1 = longestPath.get(i).getY();
            int z1 = longestPath.get(i).getZ();
            int x2 = longestPath.get(i + offs).getX();
            int y2 = longestPath.get(i + offs).getY();
            int z2 = longestPath.get(i + offs).getZ();
            textureCalculator.addValueToConfusionMatrix(timeStack, x1, y1, z1, x2, y2, z2);
        }

        // Acquiring measurements
        String name = getFullName(image.getName(), Measurements.ASM, offs);
        object.addMeasurement(new Measurement(name, textureCalculator.getASM()));

        name = getFullName(image.getName(), Measurements.CONTRAST, offs);
        object.addMeasurement(new Measurement(name, textureCalculator.getContrast()));

        name = getFullName(image.getName(), Measurements.CORRELATION, offs);
        object.addMeasurement(new Measurement(name, textureCalculator.getCorrelation()));

        name = getFullName(image.getName(), Measurements.ENTROPY, offs);
        object.addMeasurement(new Measurement(name, textureCalculator.getEntropy()));

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_INTENSITY;
    }

    @Override
    public String getDescription() {
        return "Texture measures, largely from  Robert M. Haralick, K. Shanmugam, and Its'hak Dinstein, "
                + "\"Textural Features for Image Classification\", IEEE Transactions on Systems, Man, and Cybernetics, 1973, SMC-3 (6): 610–621";
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
        int offs = parameters.getValue(OFFSET);

        // Initialising the texture calculator
        TextureCalculator textureCalculator = new TextureCalculator();

        int nObjects = inputObjects.size();
        AtomicInteger iter = new AtomicInteger(0);
        for (Obj object : inputObjects.values()) {
            textureCalculator.resetConfusionMatrix();
            processObject(object, inputImage, textureCalculator, offs);
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

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new IntegerP(OFFSET, this, 1));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OFFSET));

        return returnedParameters;

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

        int offs = parameters.getValue(OFFSET);

        String name = getFullName(inputImageName, Measurements.ASM, offs);
        ObjMeasurementRef asm = objectMeasurementRefs.getOrPut(name);
        asm.setObjectsName(inputObjectsName);
        returnedRefs.add(asm);

        name = getFullName(inputImageName, Measurements.CONTRAST, offs);
        ObjMeasurementRef contrast = objectMeasurementRefs.getOrPut(name);
        contrast.setObjectsName(inputObjectsName);
        returnedRefs.add(contrast);

        name = getFullName(inputImageName, Measurements.CORRELATION, offs);
        ObjMeasurementRef correlation = objectMeasurementRefs.getOrPut(name);
        correlation.setObjectsName(inputObjectsName);
        returnedRefs.add(correlation);

        name = getFullName(inputImageName, Measurements.ENTROPY, offs);
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
        parameters.get(INPUT_OBJECTS).setDescription("Objects for which image texture along will be generated along the object backbone.");

        parameters.get(INPUT_IMAGE).setDescription("Image for which the texture will be measured.");

        parameters.get(OFFSET).setDescription("Each point along the object backbone will have its intensity compared to another point, this offset away along the backbone.");

    }
}
