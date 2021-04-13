package wbif.sjx.MIA.Module.ImageProcessing.Miscellaneous;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.Interval;
import net.imglib2.histogram.HistogramNd;
import net.imglib2.histogram.Integer1dBinMapper;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

public class Create2DIntensityHistogram <T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public final static String INPUT_IMAGE1 = "Input image 1";
    public final static String INPUT_IMAGE2 = "Input image 2";
    public final static String OUTPUT_IMAGE = "Output image";

    public Create2DIntensityHistogram(ModuleCollection modules) {
        super("Create 2D intensity histogram",modules);
    }


    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "";
    }



    @Override
    public Status process(Workspace workspace) {
        String inputImageName1 = parameters.getValue(INPUT_IMAGE1);
        Image inputImage1 = workspace.getImage(inputImageName1);
        String inputImageName2 = parameters.getValue(INPUT_IMAGE2);
        Image inputImage2 = workspace.getImage(inputImageName2);

        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        ImgPlus imgPlus1 = inputImage1.getImgPlus();
        ImgPlus imgPlus2 = inputImage2.getImgPlus();

        long[] minVals = new long[] { 0, 0 };
		long[] numBins = new long[] { 256, 256 };
		boolean[] tailBins = new boolean[] { false, false };
        HistogramNd<UnsignedByteType> hist = Integer1dBinMapper.histogramNd(minVals, numBins, tailBins);
                        
        List<Iterable<UnsignedByteType>> intervals = new ArrayList<>();
        intervals.add(imgPlus1);
        intervals.add(imgPlus2);
        
        hist.addData(intervals);

        Image outputImage = new Image(outputImageName, ImageJFunctions.wrap(hist, outputImageName));
        workspace.addImage(outputImage);
                
        if (showOutput) outputImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE1,this));
        parameters.add(new InputImageP(INPUT_IMAGE2,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
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
