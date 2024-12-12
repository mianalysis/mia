package io.github.mianalysis.mia.module.images.measure;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.measure.Calibration;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.analysis.TextureCalculator;

/**
 * Created by Stephen on 09/05/2017.
 */

/**
* Calculates Haralick's texture features for an image.  Each pixel in the image is compared to a corresponding pixel, a defined offset away (e.g. x-offset = 1, y-offset=0, z-offset=0 to compare to the pixel immediately right of each pixel).  The intensities of the pixel pairs are added to a 2D gray-level co-occurrence matrix (GLCM) from which measures of angular second moment, contrast, correlation and entropy can be calculated.<br><br>Robert M Haralick; K Shanmugam; Its'hak Dinstein, "Textural Features for Image Classification" <i>IEEE Transactions on Systems, Man, and Cybernetics. SMC-3</i> (1973) <b>6</b> 610–621.
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class MeasureImageTexture extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input";

	/**
	* Image from the workspace for which texture metrics will be calculated.  Texture measurements will be assigned to this image.
	*/
    public static final String INPUT_IMAGE = "Input image";


	/**
	* 
	*/
    public static final String TEXTURE_SEPARATOR = "Texture calculation";

	/**
	* Each pixel in the input image will be compared to the pixel a defined offset-away.  This parameter controls the x-axis offset.  Offset specified in pixel units unless "Calibrated offset" is selected.  If using calibrated units, the offset will be rounded to the closest integer value.
	*/
    public static final String X_OFFSET = "X-offset";

	/**
	* Each pixel in the input image will be compared to the pixel a defined offset-away.  This parameter controls the y-axis offset.  Offset specified in pixel units unless "Calibrated offset" is selected.  If using calibrated units, the offset will be rounded to the closest integer value.
	*/
    public static final String Y_OFFSET = "Y-offset";

	/**
	* Each pixel in the input image will be compared to the pixel a defined offset-away.  This parameter controls the z-axis offset.  Offset specified in pixel units unless "Calibrated offset" is selected.  If using calibrated units, the offset will be rounded to the closest integer value.
	*/
    public static final String Z_OFFSET = "Z-offset";

	/**
	* When selected, offsets are specified in calibrated units.  Otherwise, offsets are assumed to be in pixel units.
	*/
    public static final String CALIBRATED_OFFSET = "Calibrated offset";

    public MeasureImageTexture(Modules modules) {
        super("Measure image texture", modules);
    }

    public interface Measurements {
        String ASM = "TEXTURE // ASM";
        String CONTRAST = "TEXTURE // CONTRAST";
        String CORRELATION = "TEXTURE // CORRELATION";
        String ENTROPY = "TEXTURE // ENTROPY";

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
        return "Calculates Haralick's texture features for an image.  Each pixel in the image is compared to a corresponding pixel, a defined offset away (e.g. x-offset = 1, y-offset=0, z-offset=0 to compare to the pixel immediately right of each pixel).  The intensities of the pixel pairs are added to a 2D gray-level co-occurrence matrix (GLCM) from which measures of angular second moment, contrast, correlation and entropy can be calculated."

                + "<br><br>Robert M Haralick; K Shanmugam; Its'hak Dinstein, \"Textural Features for Image Classification\" <i>IEEE Transactions on Systems, Man, and Cybernetics. SMC-3</i> (1973) <b>6</b> 610–621.";

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        ImageI inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        int xOffs = parameters.getValue(X_OFFSET,workspace);
        int yOffs = parameters.getValue(Y_OFFSET,workspace);
        int zOffs = parameters.getValue(Z_OFFSET,workspace);
        boolean calibratedOffset = parameters.getValue(CALIBRATED_OFFSET,workspace);

        // If using calibrated offset values, determining the closest pixel offset
        if (calibratedOffset) {
            Calibration cal = inputImagePlus.getCalibration();
            xOffs = (int) Math.round((double) xOffs / cal.pixelWidth);
            yOffs = (int) Math.round((double) yOffs / cal.pixelWidth);
            zOffs = (int) Math.round((double) zOffs / cal.pixelDepth);
        }

        // Running texture measurement
        TextureCalculator textureCalculator = new TextureCalculator();
        textureCalculator.calculate(inputImagePlus.getStack(), xOffs, yOffs, zOffs);

        // Acquiring measurements
        Measurement ASMMeasurement = new Measurement(Measurements.ASM, textureCalculator.getASM());
        inputImage.addMeasurement(ASMMeasurement);
        writeStatus("ASM = " + ASMMeasurement.getValue());

        Measurement contrastMeasurement = new Measurement(Measurements.CONTRAST, textureCalculator.getContrast());
        inputImage.addMeasurement(contrastMeasurement);
        writeStatus("Contrast = " + contrastMeasurement.getValue());

        Measurement correlationMeasurement = new Measurement(Measurements.CORRELATION,
                textureCalculator.getCorrelation());
        inputImage.addMeasurement(correlationMeasurement);
        writeStatus("Correlation = " + correlationMeasurement.getValue());

        Measurement entropyMeasurement = new Measurement(Measurements.ENTROPY, textureCalculator.getEntropy());
        inputImage.addMeasurement(entropyMeasurement);
        writeStatus("Entropy = " + entropyMeasurement.getValue());

        if (showOutput)
            inputImage.showMeasurements(this);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(TEXTURE_SEPARATOR, this));
        parameters.add(new IntegerP(X_OFFSET, this, 1));
        parameters.add(new IntegerP(Y_OFFSET, this, 0));
        parameters.add(new IntegerP(Z_OFFSET, this, 0));
        parameters.add(new BooleanP(CALIBRATED_OFFSET, this, false));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
WorkspaceI workspace = null;
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
WorkspaceI workspace = null;
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        String imageName = parameters.getValue(INPUT_IMAGE,workspace);

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
        parameters.get(INPUT_IMAGE).setDescription(
                "Image from the workspace for which texture metrics will be calculated.  Texture measurements will be assigned to this image.");

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
