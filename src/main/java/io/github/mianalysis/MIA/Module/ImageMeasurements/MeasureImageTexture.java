package io.github.mianalysis.MIA.Module.ImageMeasurements;

import ij.ImagePlus;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Object.Image;
import io.github.mianalysis.MIA.Object.Measurement;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.InputImageP;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.Parameters;
import io.github.mianalysis.MIA.Object.Parameters.Text.IntegerP;
import io.github.mianalysis.MIA.Object.Refs.ImageMeasurementRef;
import io.github.mianalysis.MIA.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.PartnerRefs;
import io.github.sjcross.common.Analysis.TextureCalculator;

/**
 * Created by Stephen on 09/05/2017.
 */
public class MeasureImageTexture extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String X_OFFSET = "X-offset";
    public static final String Y_OFFSET = "Y-offset";
    public static final String Z_OFFSET = "Z-offset";

    public MeasureImageTexture(Modules modules) {
        super("Measure image texture",modules);
    }

    public interface Measurements {
        String ASM = "TEXTURE // ASM";
        String CONTRAST = "TEXTURE // CONTRAST";
        String CORRELATION = "TEXTURE // CORRELATION";
        String ENTROPY = "TEXTURE // ENTROPY";

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
        // Getting parameters
        int xOffs = parameters.getValue(X_OFFSET);
        int yOffs = parameters.getValue(Y_OFFSET);
        int zOffs = parameters.getValue(Z_OFFSET);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Running texture measurement
        TextureCalculator textureCalculator = new TextureCalculator();
        textureCalculator.calculate(inputImagePlus.getStack(),xOffs,yOffs,zOffs);

        // Acquiring measurements
        Measurement ASMMeasurement = new Measurement(Measurements.ASM, textureCalculator.getASM());
        inputImage.addMeasurement(ASMMeasurement);
        writeStatus("ASM = " + ASMMeasurement.getValue());

        Measurement contrastMeasurement = new Measurement(Measurements.CONTRAST, textureCalculator.getContrast());
        inputImage.addMeasurement(contrastMeasurement);
        writeStatus("Contrast = " + contrastMeasurement.getValue());

        Measurement correlationMeasurement = new Measurement(Measurements.CORRELATION, textureCalculator.getCorrelation());
        inputImage.addMeasurement(correlationMeasurement);
        writeStatus("Correlation = " + correlationMeasurement.getValue());

        Measurement entropyMeasurement = new Measurement(Measurements.ENTROPY, textureCalculator.getEntropy());
        inputImage.addMeasurement(entropyMeasurement);
        writeStatus("Entropy = " + entropyMeasurement.getValue());

        if (showOutput) inputImage.showMeasurements(this);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new IntegerP(X_OFFSET, this,1));
        parameters.add(new IntegerP(Y_OFFSET, this,0));
        parameters.add(new IntegerP(Z_OFFSET, this,0));

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        String imageName = parameters.getValue(INPUT_IMAGE);

        ImageMeasurementRef asm = imageMeasurementRefs.getOrPut(Measurements.ASM);
        asm.setImageName(imageName);
        returnedRefs.add(asm);

        ImageMeasurementRef contrast = imageMeasurementRefs.getOrPut(Measurements.CONTRAST);
        contrast.setImageName(imageName);
        returnedRefs.add(contrast);

        ImageMeasurementRef correlation = imageMeasurementRefs.getOrPut(Measurements.CORRELATION);
        correlation.setImageName(imageName);
        returnedRefs.add(correlation);

        ImageMeasurementRef entropy = imageMeasurementRefs.getOrPut(Measurements.ENTROPY);
        entropy.setImageName(imageName);
        returnedRefs.add(entropy);

        return returnedRefs;

    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
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
}
