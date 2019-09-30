// TODO: Seems to be a problem with hyperstack projection on all channels and timepoints

package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ZProjector;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.transform.project.DefaultProjectParallel;
import net.imagej.ops.transform.project.ProjectRAIToII;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.InputOutput.ImageLoader;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.Process.ImgPlusTools;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

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

    public static void main(String[] args) throws IOException, DependencyException, FormatException, ServiceException {
        ImageJ ij = new ImageJ();
//        String path = "C:\\Users\\steph\\Documents\\Java Projects\\ModularImageAnalysis\\src\\test\\resources\\images\\BestFocusSubstack\\BestFocus5D_8bit.tif";
        String inputName = "F:\\Java Projects\\MIA\\src\\test\\resources\\images\\NoisyGradient\\NoisyGradient5D_32bit.tif";
        ImagePlus ipl = IJ.openImage(inputName);
        Image inputIm = new Image("",ipl);
        inputIm.showImage();
        Image projected = new ProjectImage(null).project(inputIm,"Z",ProjectionModes.MAX,"ProjectedIm");
        projected.showImage();
    }

    public Image<T> project(Image<T> inputImage, String projectionDimension, String projectionMode, String outputImageName) {
        ImageJ ij = new ImageJ();

        ImgPlus<T> img = inputImage.getImgPlus();

        // Getting key axis indices
        AxisType xType = Axes.X;
        AxisType yType = Axes.Y;
        AxisType projType = Axes.TIME;

        // Check specified axes exist, else return null
        if (!checkAxisExists(img,xType) |! checkAxisExists(img,yType) |! checkAxisExists(img,projType)) return null;

        // Create HashMap containing axis assignments
        HashMap<Integer,AxisType> axisAssignments = new HashMap<>();
        for (int d = 0; d < img.numDimensions(); ++d) axisAssignments.put(d,img.axis(d).type());

        // Permute axes, so that display axes X and Y are at positions 0 and 1 and projection axis is at position 2
        RandomAccessibleInterval<T> perm = permute(axisAssignments,img,xType,0);
        perm = permute(axisAssignments,perm,yType,1);
        perm = permute(axisAssignments,perm,projType,2);

        // Determine output size
        int idx = 0;
        HashMap<Integer, CalibratedAxis> axes = new HashMap<>();
        int[] projected_dimensions = new int[perm.numDimensions()-1];
        for (int d = 0; d < img.numDimensions(); ++d) {
            if (d != 2) {
                projected_dimensions[idx] = (int) perm.dimension(d);
                axes.put(idx,img.axis(img.dimensionIndex(axisAssignments.get(d))));
                idx++;
            }
        }

        // Creating output image
        ImgPlus<T> proj = ImgPlus.wrap((Img<T>) ij.op().create().img(new FinalDimensions(projected_dimensions), perm.randomAccess().get()));

        // Apply transformation
        UnaryComputerOp mean_op = (UnaryComputerOp) ij.op().op(Ops.Stats.Max.NAME,img);
        ij.op().transform().project(proj,perm,mean_op,2);

        // Update axes
        for (int cD=2;cD<proj.numDimensions();cD++) proj.setAxis(axes.get(cD),cD);

        ij.ui().show(proj);

        ImagePlus outputImagePlus = ImageJFunctions.wrap(proj,outputImageName);
        ImgPlusTools.applyAxes(proj,outputImagePlus);

        return new Image(outputImageName,outputImagePlus);

    }

    boolean checkAxisExists(ImgPlus imgPlus, AxisType type) {
        if (imgPlus.dimensionIndex(type) == -1) {
            MIA.log.writeWarning("Specified axis ("+type.getLabel()+") not present in image");
            return false;
        }

        return true;

    }

    RandomAccessibleInterval<T> permute(HashMap<Integer,AxisType> axisAssignments, RandomAccessibleInterval<T> interval, AxisType fromType, int toIdx) {
        int fromIdx = getPosition(axisAssignments,fromType);
        AxisType toType = axisAssignments.get(toIdx);
        axisAssignments.put(toIdx,fromType);
        axisAssignments.put(fromIdx,toType);

        return Views.permute(interval,fromIdx,toIdx);

    }

    int getPosition(HashMap<Integer,AxisType> axisAssignments, AxisType type) {
        for (int position:axisAssignments.keySet()) {
            if (axisAssignments.get(position) == type) return position;
        }

        // If this axis isn't in the list, return -1
        return -1;

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
