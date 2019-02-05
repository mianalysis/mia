package wbif.sjx.ModularImageAnalysis.ThirdParty;

import bunwarpj.*;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.*;
import java.util.Stack;

public class bUnwarpJ_Mod {
    public static Transformation computeTransformationBatch(ImageProcessor sourceIpr, ImageProcessor targetIpr, Stack<Point> sourcePoints, Stack<Point> targetPoints, Param parameter) {
        if(sourcePoints == null || targetPoints == null || parameter == null)
        {
            IJ.error("Missing parameters to compute transformation!");
            return null;
        }

        //IJ.log("Registration parameters:\n" + parameter.toString());

        // Produce side information
        final int imagePyramidDepth = parameter.max_scale_deformation - parameter.min_scale_deformation + 1;
        final int min_scale_image = 0;

        // output level to -1 so nothing is displayed
        final int outputLevel = -1;

        final boolean showMarquardtOptim = false;

        // Create target image model
        final BSplineModel target = new BSplineModel(targetIpr,false,(int) Math.pow(2, parameter.img_subsamp_fact));

        target.setPyramidDepth(imagePyramidDepth+min_scale_image);
        target.startPyramids();

        // Create target mask
        final Mask targetMsk = new Mask(targetIpr.getWidth(),targetIpr.getHeight());

        // Create source image model
        final BSplineModel source = new BSplineModel(sourceIpr, false, (int) Math.pow(2, parameter.img_subsamp_fact));

        source.setPyramidDepth(imagePyramidDepth + min_scale_image);
        source.startPyramids();

        // Create source mask
        final Mask sourceMsk = new Mask(sourceIpr.getWidth(),sourceIpr.getHeight());


        // Set landmarks
        PointHandler sourcePh  = new PointHandler(sourceIpr.getWidth(), sourceIpr.getHeight());
        PointHandler targetPh  = new PointHandler(targetIpr.getWidth(),targetIpr.getHeight());

        while ((!sourcePoints.empty()) && (!targetPoints.empty()))
        {
            Point sourcePoint = (Point)sourcePoints.pop();
            Point targetPoint = (Point)targetPoints.pop();
            sourcePh.addPoint(sourcePoint.x, sourcePoint.y);
            targetPh.addPoint(targetPoint.x, targetPoint.y);
        }


        // Set no initial affine matrices
        final double[][] sourceAffineMatrix = null;
        final double[][] targetAffineMatrix = null;

        // Join threads
        try
        {
            source.getThread().join();
            target.getThread().join();
        }
        catch (InterruptedException e)
        {
            IJ.error("Unexpected interruption exception " + e);
        }

        // Perform registration
        ImagePlus[] output_ip = new ImagePlus[2];
        output_ip[0] = null;
        output_ip[1] = null;

        // The dialog is set to null to work in batch mode
        final MainDialog dialog = null;

        final ImageProcessor originalSourceIP = null;
        final ImageProcessor originalTargetIP = null;

        // Set transformation parameters
        final Transformation warp = new Transformation(
                null, null, source, target, sourcePh, targetPh,
                sourceMsk, targetMsk, sourceAffineMatrix, targetAffineMatrix,
                parameter.min_scale_deformation, parameter.max_scale_deformation,
                min_scale_image, parameter.divWeight,
                parameter.curlWeight, parameter.landmarkWeight, parameter.imageWeight,
                parameter.consistencyWeight, parameter.stopThreshold,
                outputLevel, showMarquardtOptim, parameter.mode, null, null, output_ip[0], output_ip[1], dialog,
                originalSourceIP, originalTargetIP);

        IJ.log("\nRegistering...\n");

        long start = System.currentTimeMillis(); // start timing

        // Register
        if(parameter.mode == MainDialog.MONO_MODE)
            warp.doUnidirectionalRegistration();
        else
            warp.doBidirectionalRegistration();

        long stop = System.currentTimeMillis(); // stop timing
        IJ.log("bUnwarpJ is done! Registration time: " + (stop - start) + "ms"); // print execution time

        return warp;

    }
}
