package io.github.mianalysis.mia.module.testmodules;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.process.StackStatistics;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import io.github.mianalysis.mia.object.measurements.MeasurementFactories;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

public class MeasureImageIntensity extends Module {

    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";

    public MeasureImageIntensity(ModulesI modules) {
        super("Measure image intensity", modules);
    }

    public interface Measurements {
        String MEAN = "INTENSITY // MEAN";
        String MEDIAN = "INTENSITY // MEDIAN";
        String MODE = "INTENSITY // MODE";
        String MIN = "INTENSITY // MIN";
        String MAX = "INTENSITY // MAX";
        String SUM = "INTENSITY // SUM";
        String STDEV = "INTENSITY // STDEV";

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
        return "";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        WorkspaceI workspace = null;
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);

        ImageMeasurementRef mean = imageMeasurementRefs.getOrPut(Measurements.MEAN);
        mean.setImageName(inputImageName);
        mean.setDescription("Mean intensity of all pixels in the image \"" + inputImageName + "\".");
        returnedRefs.add(mean);

        ImageMeasurementRef median = imageMeasurementRefs.getOrPut(Measurements.MEDIAN);
        median.setImageName(inputImageName);
        median.setDescription("Median intensity of all pixels in the image \"" + inputImageName + "\".");
        returnedRefs.add(median);

        ImageMeasurementRef mode = imageMeasurementRefs.getOrPut(Measurements.MODE);
        mode.setImageName(inputImageName);
        mode.setDescription("Mode intensity of all pixels in the image \"" + inputImageName + "\".");
        returnedRefs.add(mode);

        ImageMeasurementRef min = imageMeasurementRefs.getOrPut(Measurements.MIN);
        min.setImageName(inputImageName);
        min.setDescription("Minimum intensity of all pixels in the image \"" + inputImageName + "\".");
        returnedRefs.add(min);

        ImageMeasurementRef max = imageMeasurementRefs.getOrPut(Measurements.MAX);
        max.setImageName(inputImageName);
        max.setDescription("Maximum intensity of all pixels in the image \"" + inputImageName + "\".");
        returnedRefs.add(max);

        ImageMeasurementRef stdev = imageMeasurementRefs.getOrPut(Measurements.STDEV);
        stdev.setImageName(inputImageName);
        stdev.setDescription("Standard deviation of intensity of all pixels in the image \"" + inputImageName + "\".");
        returnedRefs.add(stdev);

        ImageMeasurementRef sum = imageMeasurementRefs.getOrPut(Measurements.SUM);
        sum.setImageName(inputImageName);
        sum.setDescription("Summed intensity of all pixels in the image \"" + inputImageName + "\".");
        returnedRefs.add(sum);

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
}
