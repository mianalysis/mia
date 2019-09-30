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
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.View;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Ex {
    private static final String inputName = "C:\\Users\\steph\\Documents\\Java Projects\\ModularImageAnalysis\\src\\test\\resources\\images\\BestFocusSubstack\\BestFocus5D_8bit.tif";

    private final static ImageJ ij = new ImageJ();

//    public static <T extends RealType<T> & NativeType<T>> void main(final String[] args) throws InterruptedException, IOException {
//        ij.launch(args);
//
//        // final Img<T> img = (Img<T>)
//        final Dataset currentData = (Dataset) ij.io().open(inputName);
//
//        int projDim = currentData.dimensionIndex(Axes.X);
//        int viewXDim = currentData.dimensionIndex(Axes.Y);
//        int viewYDim = currentData.dimensionIndex(Axes.Z);
//
//        int idx = 2;
//        HashMap<Integer,CalibratedAxis> axes = new HashMap<>();
//        int[] projected_dimensions = new int[currentData.numDimensions()-1];
//        for (int d = 0; d < currentData.numDimensions(); ++d) {
//            if (d == viewXDim) {
//                projected_dimensions[0] = (int) currentData.dimension(d);
////                axes.put(idx++,currentData.axis(d));
//            } else if (d == viewYDim) {
//                projected_dimensions[1] = (int) currentData.dimension(d);
//            } else {
//                projected_dimensions[idx++] = (int) currentData.dimension(d);
//            }
//        }
//
//        Img<FloatType> proj = (Img<FloatType>) ij.op().create().img(new FinalDimensions(projected_dimensions), new FloatType());
//        UnaryComputerOp mean_op =Computers.unary(ij.op(), Ops.Stats.Mean.class, RealType.class, Iterable.class);
//        ImgPlus<T> projection = ImgPlus.wrap((Img<T>) ij.op().transform().project(proj,currentData.getImgPlus(),mean_op,projDim));
//
//        for (int cD=0;cD<projection.numDimensions();cD++) {
//            System.out.println(cD+"_"+projection.axis(cD).unit()+"_"+projection.axis(cD).type()+"_"+axes.get(cD).type());
//            projection.setAxis(axes.get(cD),cD);
//        }
//
//        ij.ui().show(currentData);
//        ij.ui().show(projection);
//
//    }

    public static <T extends RealType<T> & NativeType<T>> void main(final String[] args) throws InterruptedException, IOException {
        ij.launch(args);

        // final Img<T> img = (Img<T>)
        final Dataset currentData = (Dataset) ij.io().open(inputName);
        ImgPlus img = currentData.getImgPlus();

        // Check specified axes exist, else return null

        // Permute axes, so that display axes X and Y are at positions 0 and 1 and projection axis is at position 2
        IntervalView perm = Views.permute(img, 0, 2);
        perm = Views.permute(perm,3,0);

        int[] projected_dimensions = new int[perm.numDimensions()-1];
        projected_dimensions[0] = (int) currentData.dimension(3);
        projected_dimensions[1] = (int) currentData.dimension(1);
        projected_dimensions[2] = (int) currentData.dimension(2);
        projected_dimensions[3] = (int) currentData.dimension(4);

//        int idx = 0;
//        HashMap<Integer,CalibratedAxis> axes = new HashMap<>();
//        int[] projected_dimensions = new int[perm.numDimensions()-1];
//        int dim = currentData.dimensionIndex(Axes.X);
//        for (int d = 0; d < currentData.numDimensions(); ++d) {
//            if (d != dim) {
//                projected_dimensions[idx] = (int) currentData.dimension(d);
//                axes.put(idx++,currentData.axis(d));
//            }
//        }

        Img<FloatType> proj = (Img<FloatType>) ij.op().create().img(
                new FinalDimensions(projected_dimensions), new FloatType());

        // 1. Use Computers.unary to get op
        UnaryComputerOp mean_op =Computers.unary(ij.op(), Ops.Stats.Mean.class, RealType.class, Iterable.class);

        // or 2. Cast it
//        UnaryComputerOp mean_op = (UnaryComputerOp) ij.op().op(Ops.Stats.Mean.NAME,
//                currentData.getImgPlus());

        ImgPlus<T> projection = ImgPlus.wrap((Img<T>) ij.op().transform().project(proj,currentData.getImgPlus(),mean_op,2));

//        for (int cD=2;cD<projection.numDimensions();cD++) {
//            System.out.println(cD+"_"+projection.axis(cD).unit()+"_"+projection.axis(cD).type()+"_"+axes.get(cD).type());
//            projection.setAxis(axes.get(cD),cD);
//        }

        ij.ui().show(img);
        ij.ui().show(projection);

    }

//    public static <T extends RealType<T> & NativeType<T>> void main(final String[] args) throws InterruptedException, IOException {
//        ij.launch(args);
//
//        // final Img<T> img = (Img<T>)
//        final Dataset currentData = (Dataset) ij.io().open(inputName);
//
//        int idx = 0;
//        HashMap<Integer,CalibratedAxis> axes = new HashMap<>();
//        int[] projected_dimensions = new int[currentData.numDimensions()-1];
//        int dim = currentData.dimensionIndex(Axes.X);
//        for (int d = 0; d < currentData.numDimensions(); ++d) {
//            if (d != dim) {
//                projected_dimensions[idx] = (int) currentData.dimension(d);
//                axes.put(idx++,currentData.axis(d));
//            }
//        }
//
//        Img<FloatType> proj = (Img<FloatType>) ij.op().create().img(
//                new FinalDimensions(projected_dimensions), new FloatType());
//
//        // 1. Use Computers.unary to get op
//        UnaryComputerOp mean_op =Computers.unary(ij.op(), Ops.Stats.Mean.class, RealType.class, Iterable.class);
//
//        // or 2. Cast it
////        UnaryComputerOp mean_op = (UnaryComputerOp) ij.op().op(Ops.Stats.Mean.NAME,
////                currentData.getImgPlus());
//
//        ImgPlus<T> projection = ImgPlus.wrap((Img<T>) ij.op().transform().project(proj,currentData.getImgPlus(),mean_op,dim));
//
//        for (int cD=2;cD<projection.numDimensions();cD++) {
//            System.out.println(cD+"_"+projection.axis(cD).unit()+"_"+projection.axis(cD).type()+"_"+axes.get(cD).type());
//            projection.setAxis(axes.get(cD),cD);
//        }
//
//        ij.ui().show(currentData);
//        ij.ui().show(projection);
//
//    }
}