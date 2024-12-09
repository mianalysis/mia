package io.github.mianalysis.mia.module.images.measure;

import java.util.ArrayList;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.measure.abstrakt.MeasurementCalculator;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.MeasurementProvider;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by Stephen Cross on 19/03/2019.
 */

/**
 * Perform a mathematical operation on measurements associated with an image.
 * The calculation can replace either or both input image measurements with
 * fixed values. The resulting measurement is associated with the input image as
 * a new measurement.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ImageMeasurementCalculator extends MeasurementCalculator {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input";

    /**
     * Image from the workspace to perform the measurement calculation for.
     */
    public static final String INPUT_IMAGE = "Input image";

    public interface ValueModes extends MeasurementCalculator.ValueModes{};

    public interface CalculationModes extends MeasurementCalculator.CalculationModes{};

    public interface StatisticModes extends MeasurementCalculator.StatisticModes{};
    

    public ImageMeasurementCalculator(Modules modules) {
        super("Image measurement calculator", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_MEASURE;
    }

    @Override
    public String getVersionNumber() {
        return "1.1.0";
    }

    @Override
    public String getDescription() {
        return "Perform a mathematical operation on measurements associated with an image.  The calculation can replace either or both input image measurements with fixed values.  The resulting measurement is associated with the input image as a new measurement.";

    }

    @Override
    public Status process(Workspace workspace) {
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        Image inputImage = workspace.getImage(inputImageName);

        // For compatibility with the object-based calculator, the image has to be passed in as a list
        ArrayList<MeasurementProvider> inputImages = new ArrayList<>();
        inputImages.add(inputImage);

        Status status = process(workspace, ImageObjectMode.IMAGE, inputImages);

        if (status != Status.PASS)
            return status;

        // Showing results
        if (showOutput)
            inputImage.showMeasurements(this);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        initialiseParameters(ImageObjectMode.IMAGE);

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParams = new Parameters();

        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);

        returnedParams.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParams.add(parameters.getParameter(INPUT_IMAGE));

        returnedParams.addAll(updateAndGetParameters(ImageObjectMode.IMAGE, inputImageName));

        return returnedParams;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        // Creating new MeasurementRef
        String inputImageName = parameters.getValue(INPUT_IMAGE, null);
        String measurementName = parameters.getValue(OUTPUT_MEASUREMENT, null);

        returnedRefs.add(imageMeasurementRefs.getOrPut(measurementName).setImageName(inputImageName));

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
        parameters.get(INPUT_IMAGE)
                .setDescription("Image from the workspace to perform the measurement calculation for.");

    }
}
