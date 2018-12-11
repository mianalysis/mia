// TODO: Seems to be a problem with hyperstack projection on all channels and timepoints

package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.ImageJ;
import ij.ImagePlus;
import ij.measure.Calibration;
//import ij.plugin.ZProjector;
import ij.plugin.ZProjector;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.display.projector.sampler.ProjectedSampler;
import net.imglib2.display.projector.sampler.SamplingProjector2D;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.ParameterCollection;

import com.drew.lang.annotations.Nullable;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class ProjectImage < T extends RealType< T > & NativeType< T >> extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String PROJECTION_MODE = "Projection mode";

    public interface ProjectionModes {
        String AVERAGE = "Average";
        String MIN = "Minimum";
        String MEDIAN = "Median";
        String MAX = "Maximum";
        String STDEV = "Standard deviation";
        String SUM = "Sum";

        String[] ALL = new String[]{AVERAGE, MIN, MEDIAN, MAX, STDEV, SUM};

    }

//    public Image project(Image inputImage, String projectionDimension, String projectionMode) {
//        final ImageJ ij = new ImageJ();
//
//        ImgPlus inputImg = inputImage.getImgPlus();
//
//        int d;
//        int[] projected_dimensions = new int[inputImg.numDimensions()-1];
//        int dim = inputImg.dimensionIndex(Axes.Z);
//        for (d=0; d < inputImg.numDimensions();++d){
//            if(d != dim) projected_dimensions[d]= (int) inputImg.dimension(d);
//        }
//
//        Img<T> proj = (Img<T>) ij.op().create().img(projected_dimensions);
//
//        // 1.  Use Computers.unary to get op
//        //UnaryComputerOp mean_op =Computers.unary(ij.op(), Ops.Stats.Mean.class, RealType.class, Iterable.class);
//
//        // or 2. Cast it
//        UnaryComputerOp mean_op =(UnaryComputerOp) ij.op().op(Ops.Stats.Mean.NAME, inputImg);
//
//        Img<T> projection=(Img<T>)ij.op().transform().project(proj, inputImg, mean_op, dim);
//
//        ij.ui().show(projection);
//
//        return null;
//
//    }

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
    public String getTitle() {
        return "Project image";

    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public boolean run(Workspace workspace) {
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

        if (showOutput) showImage(outputImage);

        return true;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(PROJECTION_MODE,Parameter.CHOICE_ARRAY,ProjectionModes.AVERAGE,ProjectionModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
