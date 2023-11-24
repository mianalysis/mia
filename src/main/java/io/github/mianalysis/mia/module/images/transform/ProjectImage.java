package io.github.mianalysis.mia.module.images.transform;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImgPlusImage;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * Created by sc13967 on 04/05/2017.
 */

/**
* 
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ProjectImage<T extends RealType<T> & NativeType<T>> extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/output";

	/**
	* 
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* 
	*/
    public static final String OUTPUT_IMAGE = "Output image";

	/**
	* 
	*/
    public static final String PROJECTION_SEPARATOR = "Image projection";
    // public static final String AXIS_1 = "Axis 2";
    // public static final String AXIS_2 = "Axis 1";

	/**
	* 
	*/
    public static final String PROJECTION_AXIS = "Projection axis";

	/**
	* 
	*/
    public static final String PROJECTION_MODE = "Projection mode";

    public ProjectImage(Modules modules) {
        super("Project image", modules);
    }

    public interface AxisModes {
        // String X = "X";
        // String Y = "Y";
        String Z = "Z";
        String CHANNEL = "Channel";
        String TIME = "Time";

        // String[] ALL = new String[] { X, Y, Z, CHANNEL, TIME };
        String[] ALL = new String[] { Z, CHANNEL, TIME };

    }

    public interface ProjectionModes {
        String AVERAGE = "Average";
        String MIN = "Minimum";
        String MEDIAN = "Median";
        String MAX = "Maximum";
        String STDEV = "Standard deviation";
        String SUM = "Sum";

        String[] ALL = new String[] { AVERAGE, MIN, MEDIAN, MAX, STDEV, SUM };

    }

    static AxisType getAxis(String axisName) {
        switch (axisName) {
            // case AxisModes.X:
            // return Axes.X;
            // case AxisModes.Y:
            // return Axes.Y;
            case AxisModes.CHANNEL:
                return Axes.CHANNEL;
            case AxisModes.Z:
            default:
                return Axes.Z;
            case AxisModes.TIME:
                return Axes.TIME;
        }
    }

    static String getProjection(String projectionMode) {
        switch (projectionMode) {
            case ProjectionModes.AVERAGE:
                return Ops.Stats.Mean.NAME;
            case ProjectionModes.MIN:
                return Ops.Stats.Min.NAME;
            case ProjectionModes.MEDIAN:
                return Ops.Stats.Median.NAME;
            case ProjectionModes.MAX:
            default:
                return Ops.Stats.Max.NAME;
            case ProjectionModes.STDEV:
                return Ops.Stats.StdDev.NAME;
            case ProjectionModes.SUM:
                return Ops.Stats.Sum.NAME;
        }
    }

    public static <T extends RealType<T> & NativeType<T>> Image projectImageInZ(Image inputImage,
            String outputImageName, String projectionMode) {
        // return project(inputImage, outputImageName, AxisModes.X, AxisModes.Y,
        // AxisModes.Z, projectionMode);
        return project(inputImage, outputImageName, AxisModes.Z, projectionMode);
    }

    // public static <T extends RealType<T> & NativeType<T>> Image project(Image
    // inputImage, String outputImageName,
    // String outputXAxis, String outputYAxis, String projectionAxis, String
    // projectionMode) {
    public static <T extends RealType<T> & NativeType<T>> Image project(Image inputImage, String outputImageName,
            String projectionAxis, String projectionMode) {
        ImgPlus<T> img = inputImage.getImgPlus();
        
        // Getting key axis indices
        // AxisType xType = getAxis(outputXAxis);
        // AxisType yType = getAxis(outputYAxis);
        AxisType xType = Axes.X;
        AxisType yType = Axes.Y;
        AxisType projType = getAxis(projectionAxis);

        // Check specified axes exist, else return null
        if (!checkAxisExists(img, xType) | !checkAxisExists(img, yType))
            return null;

        // If the projection axis doesn't exist, permute to correct view, then output
        if (img.dimensionIndex(projType) == -1)
            return getNonProjectedImage(img, outputImageName, xType, yType, projectionMode);

        HashMap<Integer, AxisType> axisAssignments = getAxisAssignments(img);

        // Permute axes, so that display axes X and Y are at positions 0 and 1 and
        // projection axis is at position 2
        RandomAccessibleInterval<T> perm = permute(img, axisAssignments, xType, 0);
        perm = permute(perm, axisAssignments, yType, 1);
        perm = permute(perm, axisAssignments, projType, 2);
        perm = enforceCZT(perm, axisAssignments);
        perm = Views.addDimension(perm, 0, 0);

        // Determine output size
        int idx = 0;
        HashMap<Integer, CalibratedAxis> axes = new HashMap<>();
        int[] projected_dimensions = new int[perm.numDimensions() - 1];
        for (int d = 0; d < img.numDimensions(); ++d) {
            if (d != 2) {
                projected_dimensions[idx] = (int) perm.dimension(d);
                axes.put(idx, img.axis(img.dimensionIndex(axisAssignments.get(d))));
                idx++;
            }
        }
        projected_dimensions[idx] = 1;
        axes.put(idx, img.axis(img.dimensionIndex(axisAssignments.get(2))));

        OpService ops = MIA.getOpService();

        // Creating output image
        T type;
        switch (projectionMode) {
            case ProjectionModes.AVERAGE:
            case ProjectionModes.STDEV:
            case ProjectionModes.SUM:
                type = (T) new FloatType();
                break;
            default:
            case ProjectionModes.MAX:
            case ProjectionModes.MIN:
            case ProjectionModes.MEDIAN:
                type = (T) img.firstElement();
                break;
        }

        DiskCachedCellImgOptions options = ImgPlusImage.getCellImgOptions();
        ImgPlus<T> proj = new ImgPlus<>(new DiskCachedCellImgFactory(type, options).create(projected_dimensions));

        // Apply transformation
        UnaryComputerOp mean_op = (UnaryComputerOp) ops.op(getProjection(projectionMode), img);
        ops.transform().project(proj, perm, mean_op, 2);
        
        // Update axes
        int dOut = 0;
        for (int dIn = 0; dIn < img.numDimensions(); dIn++) {
            if (dIn != img.dimensionIndex(projType)) {
                CalibratedAxis axIn = img.axis(dIn);
                CalibratedAxis axOut = new DefaultLinearAxis(axIn.type(), axIn.unit(), axIn.calibratedValue(1));
                proj.setAxis(axOut, dOut++);
            }
        }

        CalibratedAxis axIn = img.axis(img.dimensionIndex(projType));
        CalibratedAxis axOut = new DefaultLinearAxis(axIn.type(), axIn.unit(), axIn.calibratedValue(1));
        proj.setAxis(axOut, proj.numDimensions() - 1);

        return ImageFactory.createImage(outputImageName, proj);

    }

    static <T extends RealType<T> & NativeType<T>, R extends RealType<R> & NativeType<R>> Image getNonProjectedImage(ImgPlus<T> img, String outputImageName,
            AxisType xType, AxisType yType, String projectionMode) {
        HashMap<Integer, AxisType> axisAssignments = getAxisAssignments(img);

        // Permute axes, so that display axes X and Y are at positions 0 and 1 and
        // projection axis is at position 2
        RandomAccessibleInterval<T> perm = permute(img, axisAssignments, xType, 0);
        perm = permute(perm, axisAssignments, yType, 1);

        int idx = 0;
        HashMap<Integer, CalibratedAxis> axes = new HashMap<>();
        int[] projected_dimensions = new int[perm.numDimensions()];
        for (int d = 0; d < img.numDimensions(); ++d) {
            projected_dimensions[idx] = (int) perm.dimension(d);
            axes.put(idx, img.axis(img.dimensionIndex(axisAssignments.get(d))));
            idx++;
        }

        T type;
        switch (projectionMode) {
            case ProjectionModes.AVERAGE:
            case ProjectionModes.STDEV:
            case ProjectionModes.SUM:
                type = (T) new FloatType();
                break;
            default:
            case ProjectionModes.MAX:
            case ProjectionModes.MIN:
            case ProjectionModes.MEDIAN:
                type = (T) img.firstElement();
                break;
        }

        CellImgFactory<T> factory = new CellImgFactory<T>(type);
        ImgPlus<T> outImg = new ImgPlus<>(factory.create(projected_dimensions));

        if (projectionMode.equals(ProjectionModes.STDEV))
            LoopBuilder.setImages(outImg).forEachPixel(pixel -> pixel.setReal(0));
        else
            LoopBuilder.setImages(perm,outImg).forEachPixel((s,t) -> t.setReal(s.getRealDouble()));

        // Update axes
        for (int cD = 0; cD < outImg.numDimensions(); cD++)
            outImg.setAxis(axes.get(cD), cD);
        // ImagePlus outputImagePlus = ImageJFunctions.wrap(outImg,outputImageName);
        // ImgPlusTools.applyAxes(outImg,outputImagePlus);

        return ImageFactory.createImage(outputImageName, outImg);

    }

    static <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> enforceCZT(
            RandomAccessibleInterval<T> imgPlus, HashMap<Integer, AxisType> axisAssignments) {
        // This problem should only arise with 5 dimensions, as the first 3 are fixed by
        // the projection configuration
        if (imgPlus.numDimensions() == 5) {
            if (axisAssignments.get(3) == Axes.Z && axisAssignments.get(4) == Axes.CHANNEL) {
                return permute(imgPlus, axisAssignments, Axes.Z, 4);
            } else if (axisAssignments.get(3) == Axes.TIME && axisAssignments.get(4) == Axes.CHANNEL) {
                return permute(imgPlus, axisAssignments, Axes.TIME, 4);
            } else if (axisAssignments.get(3) == Axes.TIME && axisAssignments.get(4) == Axes.Z) {
                return permute(imgPlus, axisAssignments, Axes.TIME, 4);
            }
        }

        return imgPlus;

    }

    static <T extends RealType<T> & NativeType<T>> HashMap<Integer, AxisType> getAxisAssignments(ImgPlus<T> img) {
        // Create HashMap containing axis assignments
        HashMap<Integer, AxisType> axisAssignments = new HashMap<>();
        for (int d = 0; d < img.numDimensions(); ++d)
            axisAssignments.put(d, img.axis(d).type());

        return axisAssignments;

    }

    static boolean checkAxisExists(ImgPlus imgPlus, AxisType type) {
        if (imgPlus.dimensionIndex(type) == -1) {
            MIA.log.writeWarning("Specified axis (" + type.getLabel() + ") not present in image");
            return false;
        }

        return true;

    }

    static <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> permute(
            RandomAccessibleInterval<T> interval, HashMap<Integer, AxisType> axisAssignments, AxisType fromType,
            int toIdx) {
        int fromIdx = getPosition(axisAssignments, fromType);
        AxisType toType = axisAssignments.get(toIdx);
        axisAssignments.put(toIdx, fromType);
        axisAssignments.put(fromIdx, toType);

        return Views.permute(interval, fromIdx, toIdx);

    }

    static int getPosition(HashMap<Integer, AxisType> axisAssignments, AxisType type) {
        for (int position : axisAssignments.keySet()) {
            if (axisAssignments.get(position) == type)
                return position;
        }

        // If this axis isn't in the list, return -1
        return -1;

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_TRANSFORM;
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
    public Status process(Workspace workspace) {
        // Loading image into workspace
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        // String xAxis = parameters.getValue(AXIS_1);
        // String yAxis = parameters.getValue(AXIS_2);
        String projectionAxis = parameters.getValue(PROJECTION_AXIS, workspace);
        String projectionMode = parameters.getValue(PROJECTION_MODE, workspace);

        // Create max projection image
        // Image outputImage = project(inputImage, outputImageName, xAxis, yAxis,
        // projectionAxis, projectionMode);
        Image outputImage = project(inputImage, outputImageName, projectionAxis, projectionMode);

        // Adding projected image to workspace
        workspace.addImage(outputImage);

        if (showOutput)
            outputImage.show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new SeparatorP(PROJECTION_SEPARATOR, this));
        // parameters.add(new ChoiceP(AXIS_1, this, AxisModes.X, AxisModes.ALL));
        // parameters.add(new ChoiceP(AXIS_2, this, AxisModes.Y, AxisModes.ALL));
        parameters.add(new ChoiceP(PROJECTION_AXIS, this, AxisModes.Z, AxisModes.ALL));
        parameters.add(new ChoiceP(PROJECTION_MODE, this, ProjectionModes.MAX, ProjectionModes.ALL));

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
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
