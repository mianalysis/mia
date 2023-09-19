package io.github.mianalysis.mia.module.images.measure;

import java.util.ArrayList;

import javax.swing.JFrame;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.analyze.directionality.Directionality_;
import fiji.analyze.directionality.Directionality_.AnalysisMethod;
import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;


@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureImageIntensityOrientation extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input";

	/**
	* 
	*/
    public static final String INPUT_IMAGE = "Input image";

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



    public MeasureImageIntensityOrientation(Modules modules) {
        super("Measure image intensity orientation", modules);
    }

    public interface Methods {
        String FOURIER = "Fourier components";
        String LOCAL_GRADIENT = "Local gradient orientation";
        
        String[] ALL = new String[]{FOURIER, LOCAL_GRADIENT};

    }

    public interface Measurements {

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
        return "Note: This currently only calculates the histogram for the first image in a stack (i.e. first channel, slice and timepoint).  To measure for different images in a stack, please use the MeasureObjectIntensityOrientation module.";
    }

    @Override
    public Status process(Workspace workspace) {
                // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String methodString = parameters.getValue(METHOD, workspace);
        int nBins = parameters.getValue(NUMBER_OF_BINS, workspace);
        double binStart = parameters.getValue(HISTOGRAM_START, workspace);
        double binEnd = parameters.getValue(HISTOGRAM_END, workspace);

        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

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

        Directionality_ directionality = new Directionality_();

        // Configuring Directionality plugin
        directionality.setImagePlus(inputImagePlus);
        directionality.setBinNumber(nBins);
        directionality.setBinRange(binStart, binEnd);
        directionality.setMethod(method);

        directionality.computeHistograms();
        directionality.fitHistograms();

        ArrayList<double[]> fitParameters = directionality.getFitAnalysis();
        double[] results = fitParameters.iterator().next();

        MIA.log.writeDebug(results[0]);
        MIA.log.writeDebug(results[1]);
        MIA.log.writeDebug(results[2]);
        MIA.log.writeDebug(results[3]);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(ORIENTATION_SEPARATOR, this));
        parameters.add(new ChoiceP(METHOD, this, Methods.FOURIER, Methods.ALL));
        parameters.add(new IntegerP(NUMBER_OF_BINS, this, 90));
        parameters.add(new DoubleP(HISTOGRAM_START, this, -90));
        parameters.add(new DoubleP(HISTOGRAM_END, this, 90));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        Workspace workspace = null;
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);

        // ImageMeasurementRef mean = imageMeasurementRefs.getOrPut(Measurements.MEAN);
        // mean.setImageName(inputImageName);
        // mean.setDescription("Mean intensity of all pixels in the image \"" + inputImageName + "\".");
        // returnedRefs.add(mean);

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

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("");

    }
}
