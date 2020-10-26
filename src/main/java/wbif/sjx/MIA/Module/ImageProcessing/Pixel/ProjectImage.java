// TODO: Seems to be a problem with hyperstack projection on all channels and timepoints

package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ZProjector;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

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
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getDescription() {
        return "Project an image along the z-axis into the xy-plane.  Various statistics are available at each pixel " +
                "location (e.g. maximum, mean, etc.).";
    }

    @Override
    public Status process(Workspace workspace) {
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

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image to be projected."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "", "Name for the projected image to be stored in the workspace with."));
        parameters.add(new SeparatorP(PROJECTION_SEPARATOR,this));
        parameters.add(new ChoiceP(PROJECTION_MODE,this,ProjectionModes.MAX,ProjectionModes.ALL,"Statistic for calculation of the intensity projection."));

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

///// THE FOLLOWING IS POTENTIALLY READY TO USE; IT JUST NEEDS LOADS OF TESTS WRITING FIRST /////

//package wbif.sjx.MIA.Module.ImageProcessing.Pixel;
//
//import ij.IJ;
//import ij.ImagePlus;
//import net.imagej.ImageJ;
//import net.imagej.ImgPlus;
//import net.imagej.axis.Axes;
//import net.imagej.axis.AxisType;
//import net.imagej.axis.CalibratedAxis;
//import net.imagej.ops.Ops;
//import net.imagej.ops.special.computer.UnaryComputerOp;
//import net.imglib2.*;
//import net.imglib2.img.Img;
//import net.imglib2.img.array.ArrayImgFactory;
//import net.imglib2.img.cell.CellImgFactory;
//import net.imglib2.img.display.imagej.ImageJFunctions;
//import net.imglib2.type.NativeType;
//import net.imglib2.type.numeric.RealType;
//import net.imglib2.view.IntervalView;
//import net.imglib2.view.Views;
//import wbif.sjx.MIA.MIA;
//import wbif.sjx.MIA.Module.Module;
//import wbif.sjx.MIA.Module.ModuleCollection;
//import wbif.sjx.MIA.Module.PackageNames;
//import wbif.sjx.MIA.Object.*;
//import wbif.sjx.MIA.Object.Parameters.*;
//import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
//import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
//import wbif.sjx.MIA.Object.References.MetadataRefCollection;
//import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
//import wbif.sjx.common.Process.ImgPlusTools;
//
//import java.io.IOException;
//import java.util.HashMap;
//
///**
// * Created by sc13967 on 04/05/2017.
// */
//public class ProjectImage <T extends RealType<T> & NativeType<T>> extends Module {
//    public static final String INPUT_SEPARATOR = "Image input/output";
//    public static final String INPUT_IMAGE = "Input image";
//    public static final String OUTPUT_IMAGE = "Output image";
//    public static final String PROJECTION_SEPARATOR = "Image projection";
//    public static final String AXIS_1 = "Axis 2";
//    public static final String AXIS_2 = "Axis 1";
//    public static final String PROJECTION_AXIS  = "Projection axis";
//    public static final String PROJECTION_MODE = "Projection mode";
//
//
//    public ProjectImage(ModuleCollection modules) {
//        super("Project image",modules);
//    }
//
//    public interface AxisModes {
//        String X = "X";
//        String Y = "Y";
//        String Z = "Z";
//        String CHANNEL = "Channel";
//        String TIME = "Time";
//
//        String[] ALL = new String[]{X,Y,Z,CHANNEL,TIME};
//
//    }
//
//    public interface ProjectionModes {
//        String AVERAGE = "Average";
//        String MIN = "Minimum";
//        String MEDIAN = "Median";
//        String MAX = "Maximum";
//        String STDEV = "Standard deviation";
//        String SUM = "Sum";
//
//        String[] ALL = new String[]{AVERAGE, MIN, MEDIAN, MAX, STDEV, SUM};
//
//    }
//
//    static AxisType getAxis(String axisName) {
//        switch (axisName) {
//            case AxisModes.X:
//                return Axes.X;
//            case AxisModes.Y:
//                return Axes.Y;
//            case AxisModes.CHANNEL:
//                return Axes.CHANNEL;
//            case AxisModes.Z:
//            default:
//                return Axes.Z;
//            case AxisModes.TIME:
//                return Axes.TIME;
//        }
//    }
//
//    static String getProjection(String projectionMode) {
//        switch (projectionMode) {
//            case ProjectionModes.AVERAGE:
//                return Ops.Stats.Mean.NAME;
//            case ProjectionModes.MIN:
//                return Ops.Stats.Min.NAME;
//            case ProjectionModes.MEDIAN:
//                return Ops.Stats.Median.NAME;
//            case ProjectionModes.MAX:
//            default:
//                return Ops.Stats.Max.NAME;
//            case ProjectionModes.STDEV:
//                return Ops.Stats.StdDev.NAME;
//            case ProjectionModes.SUM:
//                return Ops.Stats.Sum.NAME;
//        }
//    }
//
//    public static <T extends RealType<T> & NativeType<T>> Image<T> projectImageInZ(Image<T> inputImage, String outputImageName, String projectionMode) {
//        return project(inputImage,outputImageName,AxisModes.X,AxisModes.Y,AxisModes.Z,projectionMode);
//    }
//
//    public static <T extends RealType<T> & NativeType<T>> Image<T> project(Image<T> inputImage, String outputImageName, String outputXAxis, String outputYAxis, String projectionAxis, String projectionMode) {
//        ImageJ ij = new ImageJ();
//
//        ImgPlus<T> img = inputImage.getImgPlus();
//
//        // Getting key axis indices
//        AxisType xType = getAxis(outputXAxis);
//        AxisType yType = getAxis(outputYAxis);
//        AxisType projType = getAxis(projectionAxis);
//
//        // Check specified axes exist, else return null
//        if (!checkAxisExists(img,xType) |! checkAxisExists(img,yType)) return null;
//
//        // If the projection axis doesn't exist, permute to correct view, then output
//        if (img.dimensionIndex(projType) == -1) return getNonProjectedImage(img,outputImageName,xType,yType);
//
//        HashMap<Integer,AxisType> axisAssignments = getAxisAssignments(img);
//
//        // Permute axes, so that display axes X and Y are at positions 0 and 1 and projection axis is at position 2
//        RandomAccessibleInterval<T> perm = permute(img,axisAssignments,xType,0);
//        perm = permute(perm,axisAssignments,yType,1);
//        perm = permute(perm,axisAssignments,projType,2);
//        perm = enforceCZT(perm,axisAssignments);
//
//        // Determine output size
//        int idx = 0;
//        HashMap<Integer, CalibratedAxis> axes = new HashMap<>();
//        int[] projected_dimensions = new int[perm.numDimensions()-1];
//        for (int d = 0; d < img.numDimensions(); ++d) {
//            if (d != 2) {
//                projected_dimensions[idx] = (int) perm.dimension(d);
//                axes.put(idx,img.axis(img.dimensionIndex(axisAssignments.get(d))));
//                idx++;
//
//            }
//        }
//
//        // Creating output image
//        ImgPlus<T> proj = ImgPlus.wrap((Img<T>) ij.op().create().img(new FinalDimensions(projected_dimensions), perm.randomAccess().get()));
//
//        // Apply transformation
//        UnaryComputerOp mean_op = (UnaryComputerOp) ij.op().op(getProjection(projectionMode),img);
//        ij.op().transform().project(proj,perm,mean_op,2);
//
//        // Update axes
//        for (int cD=0;cD<proj.numDimensions();cD++) proj.setAxis(axes.get(cD),cD);
//
//        ImagePlus outputImagePlus = ImageJFunctions.wrap(proj,outputImageName);
//        ImgPlusTools.applyAxes(proj,outputImagePlus);
//        outputImagePlus.setCalibration(inputImage.getImagePlus().getCal());
//
//        return new Image<T>(outputImageName,outputImagePlus);
//
//    }
//
//    static <T extends RealType<T> & NativeType<T>> Image<T> getNonProjectedImage(ImgPlus<T> img, String outputImageName, AxisType xType, AxisType yType) {
//        ImageJ ij = new ImageJ();
//
//        HashMap<Integer,AxisType> axisAssignments = getAxisAssignments(img);
//
//        // Permute axes, so that display axes X and Y are at positions 0 and 1 and projection axis is at position 2
//        RandomAccessibleInterval<T> perm = permute(img,axisAssignments,xType,0);
//        perm = permute(perm,axisAssignments,yType,1);
//
//        int idx = 0;
//        HashMap<Integer, CalibratedAxis> axes = new HashMap<>();
//        int[] projected_dimensions = new int[perm.numDimensions()];
//        for (int d = 0; d < img.numDimensions(); ++d) {
//            projected_dimensions[idx] = (int) perm.dimension(d);
//            axes.put(idx,img.axis(img.dimensionIndex(axisAssignments.get(d))));
//            idx++;
//        }
//
//        CellImgFactory<T> factory = new CellImgFactory<T>(img.firstElement());
//        ImgPlus<T> outImg = new ImgPlus<>(factory.create(projected_dimensions));
//        Cursor<T> inCursor = Views.iterable(perm).cursor();
//        RandomAccess<T> randomAccess = outImg.randomAccess();
//
//        while (inCursor.hasNext()) {
//            inCursor.fwd();
//            randomAccess.setPosition(inCursor);
//            randomAccess.get().set(inCursor.get());
//        }
//
//        // Update axes
//        for (int cD=0;cD<outImg.numDimensions();cD++) outImg.setAxis(axes.get(cD),cD);
//        ImagePlus outputImagePlus = ImageJFunctions.wrap(outImg,outputImageName);
//        ImgPlusTools.applyAxes(outImg,outputImagePlus);
//
//        return new Image<T>(outputImageName,outputImagePlus);
//
//    }
//
//    static <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> enforceCZT(RandomAccessibleInterval<T> imgPlus, HashMap<Integer,AxisType> axisAssignments) {
//        // This problem should only arise with 5 dimensions, as the first 3 are fixed by the projection configuration
//        if (imgPlus.numDimensions() == 5) {
//            if (axisAssignments.get(3) == Axes.Z && axisAssignments.get(4) == Axes.CHANNEL) {
//                return permute(imgPlus,axisAssignments,Axes.Z,4);
//            } else if (axisAssignments.get(3) == Axes.TIME && axisAssignments.get(4) == Axes.CHANNEL) {
//                return permute(imgPlus,axisAssignments,Axes.TIME,4);
//            } else if (axisAssignments.get(3) == Axes.TIME && axisAssignments.get(4) == Axes.Z) {
//                return permute(imgPlus,axisAssignments,Axes.TIME,4);
//            }
//        }
//
//        return imgPlus;
//
//    }
//
//    static <T extends RealType<T> & NativeType<T>> HashMap<Integer,AxisType> getAxisAssignments(ImgPlus<T> img) {
//        // Create HashMap containing axis assignments
//        HashMap<Integer,AxisType> axisAssignments = new HashMap<>();
//        for (int d = 0; d < img.numDimensions(); ++d) axisAssignments.put(d,img.axis(d).type());
//
//        return axisAssignments;
//
//    }
//
//    static boolean checkAxisExists(ImgPlus imgPlus, AxisType type) {
//        if (imgPlus.dimensionIndex(type) == -1) {
//            MIA.log.writeWarning("Specified axis ("+type.getLabel()+") not present in image");
//            return false;
//        }
//
//        return true;
//
//    }
//
//    static <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> permute(RandomAccessibleInterval<T> interval, HashMap<Integer,AxisType> axisAssignments, AxisType fromType, int toIdx) {
//        int fromIdx = getPosition(axisAssignments,fromType);
//        AxisType toType = axisAssignments.get(toIdx);
//        axisAssignments.put(toIdx,fromType);
//        axisAssignments.put(fromIdx,toType);
//
//        return Views.permute(interval,fromIdx,toIdx);
//
//    }
//
//    static int getPosition(HashMap<Integer,AxisType> axisAssignments, AxisType type) {
//        for (int position:axisAssignments.keySet()) {
//            if (axisAssignments.get(position) == type) return position;
//        }
//
//        // If this axis isn't in the list, return -1
//        return -1;
//
//    }
//
//    @Override
//    public String getPackageName() {
//        return PackageNames.IMAGE_PROCESSING_PIXEL;
//    }
//
//    @Override
//    public String getDescription() {
//        return "";
//    }
//
//    @Override
//    public Status process(Workspace workspace) {
//        // Loading image into workspace
//        String inputImageName = parameters.getValue(INPUT_IMAGE);
//        Image inputImage = workspace.getImages().get(inputImageName);
//
//        // Getting parameters
//        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
//        String xAxis = parameters.getValue(AXIS_1);
//        String yAxis = parameters.getValue(AXIS_2);
//        String projectionAxis = parameters.getValue(PROJECTION_AXIS);
//        String projectionMode = parameters.getValue(PROJECTION_MODE);
//
//        // Create max projection image
//        Image outputImage = project(inputImage,outputImageName,xAxis,yAxis,projectionAxis,projectionMode);
//
//        // Adding projected image to workspace
//        workspace.addImage(outputImage);
//
//        if (showOutput) outputImage.showImage();
//
//        return true;
//
//    }
//
//    @Override
//    protected void initialiseParameters() {
//        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
//        parameters.add(new InputImageP(INPUT_IMAGE, this));
//        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
//        parameters.add(new ParamSeparatorP(PROJECTION_SEPARATOR,this));
//        parameters.add(new ChoiceP(AXIS_1,this,AxisModes.X,AxisModes.ALL));
//        parameters.add(new ChoiceP(AXIS_2,this,AxisModes.Y,AxisModes.ALL));
//        parameters.add(new ChoiceP(PROJECTION_AXIS,this,AxisModes.Z,AxisModes.ALL));
//        parameters.add(new ChoiceP(PROJECTION_MODE,this,ProjectionModes.AVERAGE,ProjectionModes.ALL));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        return parameters;
//    }
//
//    @Override
//    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
//        return null;
//    }
//
//    @Override
//    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
//        return null;
//    }
//
//    @Override
//    public MetadataRefCollection updateAndGetMetadataReferences() {
//        return null;
//    }
//
//    @Override
//    public ParentChildRefCollection updateAndGetParentChildRefs() {
//        return null;
//    }
//
//    @Override
//    public boolean verify() {
//        return true;
//    }
//}
