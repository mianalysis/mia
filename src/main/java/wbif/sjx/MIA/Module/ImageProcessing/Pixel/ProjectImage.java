// TODO: Seems to be a problem with hyperstack projection on all channels and timepoints

package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ZProjector;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.transform.project.DefaultProjectParallel;
import net.imagej.ops.transform.project.ProjectRAIToII;
import net.imglib2.FinalDimensions;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

import java.io.IOException;

//import ij.plugin.ZProjector;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class ProjectImage < T extends RealType< T > & NativeType< T >> extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String PROJECTION_SEPARATOR = "Image projection";
    public static final String PROJECTION_MODE = "Projection mode";


    public ProjectImage(ModuleCollection modules) {
        super("Project image",modules);
    }

    public interface ProjectionModes {
        String AVERAGE = "Average";
        String MIN = "Minimum";
        String MEDIAN = "Median";
        String MAX = "Maximum";
        String STDEV = "Standard deviation";
        String SUM = "Sum";

        String[] ALL = new String[]{AVERAGE, MIN, MEDIAN, MAX, STDEV, SUM};

    }

    public static void main(String[] args) {
        String path = "C:\\Users\\steph\\Documents\\Java Projects\\ModularImageAnalysis\\src\\test\\resources\\images\\BestFocusSubstack\\BestFocus5D_8bit.tif";
        try {
            Dataset dataset = new ImageJ().scifio().datasetIO().open(path);
            Image image = new Image("Im",dataset.getImgPlus());

            Image projected = ProjectImage.project(image,"Z",ProjectionModes.MAX);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> Image project(Image inputImage, String projectionDimension, String projectionMode) {
        ImageJ ij = new ImageJ();

        ImgPlus inputImg = inputImage.getImgPlus();

        int idx = 0;
        int[] projected_dimensions = new int[inputImg.numDimensions()];
        int dim = inputImg.dimensionIndex(Axes.Z);
        for (int d=0; d < inputImg.numDimensions();++d){
            if (d == dim) projected_dimensions[idx++] =  (int) inputImg.dimension(d);
        }

//        Img<T> proj = (Img<T>) ij.op().create().img(projected_dimensions);
//        Img<T> projection=(Img<T>)ij.op().transform().project(proj, inputImg, mean_op, dim);

//        Img<T> proj = (Img<T>) ij.op().create().img(projected_dimensions);
//
//        UnaryComputerOp maxOp = (UnaryComputerOp) ij.op().run("stats.max", inputImg);
//        Img<T> projection = (Img<T>) ij.op().run("project",proj,inputImg,maxOp,2);
////        UnaryComputerOp mean_op = (UnaryComputerOp) ij.op().op(Ops.Stats.Mean.NAME, inputImg);
////
////        Img<T> projection = (Img<T>) ij.op().transform().project(proj, inputImg, mean_op, dim);

        return null;

    }

    public static Image projectImageInZ(Image inputImage, String outputImageName, String projectionMode) {
        // If the input image is multi-channel, but with 1 slice it will try and project the channels
        if (inputImage.getImagePlus().getNChannels() > 1 && inputImage.getImagePlus().getNSlices() == 1) {
            return new Image(outputImageName,inputImage.getImagePlus().duplicate());
        }

        ImagePlus iplOut = null;
        switch (projectionMode) {
            case ProjectionModes.AVERAGE:
                iplOut = ZProjector.run(inputImage.getImagePlus(),"avg all");
                break;

            case ProjectionModes.MIN:
                iplOut = ZProjector.run(inputImage.getImagePlus(),"min all");
                break;

            case ProjectionModes.MEDIAN:
                iplOut = ZProjector.run(inputImage.getImagePlus(),"median all");
                break;

            case ProjectionModes.MAX:
                iplOut = ZProjector.run(inputImage.getImagePlus(),"max all");
                break;

            case ProjectionModes.STDEV:
                iplOut = ZProjector.run(inputImage.getImagePlus(),"sd all");
                break;

            case ProjectionModes.SUM:
                iplOut = ZProjector.run(inputImage.getImagePlus(),"sum all");
                break;
        }

        // Setting spatial calibration
        Calibration calibrationIn = inputImage.getImagePlus().getCalibration();
        Calibration calibrationOut = new Calibration();

        calibrationOut.pixelWidth= calibrationIn.pixelWidth;
        calibrationOut.pixelHeight = calibrationIn.pixelHeight;
        calibrationOut.pixelDepth = calibrationIn.pixelDepth;
        calibrationOut.setUnit(calibrationIn.getUnit());

        iplOut.setCalibration(calibrationOut);

        return new Image(outputImageName,iplOut);

    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Loading image into workspace
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String projectionMode = parameters.getValue(PROJECTION_MODE);

        // Create max projection image
        Image outputImage = projectImageInZ(inputImage,outputImageName,projectionMode);

        // Adding projected image to workspace
        workspace.addImage(outputImage);

        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new ParamSeparatorP(PROJECTION_SEPARATOR,this));
        parameters.add(new ChoiceP(PROJECTION_MODE,this,ProjectionModes.AVERAGE,ProjectionModes.ALL));

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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
