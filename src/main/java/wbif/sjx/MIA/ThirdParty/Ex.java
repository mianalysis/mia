package wbif.sjx.MIA.ThirdParty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.*;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import wbif.sjx.MIA.MIA;

public class Ex {
    //    private static final String inputName = "C:\\Users\\steph\\Documents\\Java Projects\\ModularImageAnalysis\\src\\test\\resources\\images\\BestFocusSubstack\\BestFocus5D_8bit.tif";
    private static final String inputName = "F:\\Java Projects\\MIA\\src\\test\\resources\\images\\BestFocusSubstack\\BestFocus3D_8bit.tif";

    private final static ImageJ ij = new ImageJ();

    public static <T extends RealType<T> & NativeType<T>> void main(final String[] args) throws InterruptedException, IOException {
        ij.launch(args);

        // final Img<T> img = (Img<T>)
        final Dataset currentData = (Dataset) ij.io().open(inputName);
        ImgPlus img = currentData.getImgPlus();

        // Getting key axis indices
        AxisType xType = Axes.Y;
        AxisType yType = Axes.X;
        AxisType projType = Axes.Z;

        // Check specified axes exist, else return null
        if (!checkAxisExists(img,xType) |! checkAxisExists(img,yType) |! checkAxisExists(img,projType)) return;

        // Create HashMap containing axis assignments
        HashMap<Integer,AxisType> axisAssignments = new HashMap<>();
        for (int d = 0; d < currentData.numDimensions(); ++d) axisAssignments.put(d,currentData.axis(d).type());

        // Permute axes, so that display axes X and Y are at positions 0 and 1 and projection axis is at position 2
        RandomAccessibleInterval perm = permute(axisAssignments,img,xType,0);
        perm = permute(axisAssignments,perm,yType,1);
        perm = permute(axisAssignments,perm,projType,2);

        // Determine output size
        int idx = 0;
        HashMap<Integer,CalibratedAxis> axes = new HashMap<>();
        int[] projected_dimensions = new int[perm.numDimensions()-1];
        for (int d = 0; d < currentData.numDimensions(); ++d) {
            if (d != 2) {
                projected_dimensions[idx] = (int) perm.dimension(d);
                axes.put(idx,currentData.axis(currentData.dimensionIndex(axisAssignments.get(d))));
                idx++;
            }
        }

        // Apply transformation
        ImgPlus<UnsignedByteType> proj = ImgPlus.wrap((Img<UnsignedByteType>) ij.op().create().img(new FinalDimensions(projected_dimensions), new UnsignedByteType()));
        UnaryComputerOp mean_op = (UnaryComputerOp) ij.op().op(Ops.Stats.Mean.NAME,img);
        ij.op().transform().project(proj,perm,mean_op,2);

        // Update axes
        for (int cD=2;cD<proj.numDimensions();cD++) proj.setAxis(axes.get(cD),cD);

        ij.ui().show(proj);

    }

    static boolean checkAxisExists(ImgPlus imgPlus, AxisType type) {
        if (imgPlus.dimensionIndex(type) == -1) {
            MIA.log.writeWarning("Specified axis ("+type.getLabel()+") not present in image");
            return false;
        }

        return true;

    }

    static RandomAccessibleInterval permute(HashMap<Integer,AxisType> axisAssignments, RandomAccessibleInterval interval, AxisType fromType, int toIdx) {
        int fromIdx = getPosition(axisAssignments,fromType);
        AxisType toType = axisAssignments.get(toIdx);
        axisAssignments.put(toIdx,fromType);
        axisAssignments.put(fromIdx,toType);

        return Views.permute(interval,fromIdx,toIdx);

    }

    private static int getPosition(HashMap<Integer,AxisType> axisAssignments, AxisType type) {
        for (int position:axisAssignments.keySet()) {
            if (axisAssignments.get(position) == type) return position;
        }

        // If this axis isn't in the list, return -1
        return -1;

    }
}