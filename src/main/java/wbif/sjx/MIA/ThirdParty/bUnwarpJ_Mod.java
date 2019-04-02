package wbif.sjx.MIA.ThirdParty;

import bunwarpj.*;
import ij.IJ;
import ij.process.ImageProcessor;

import java.awt.*;
import java.util.Stack;

public class bUnwarpJ_Mod {
    public static Transformation computeTransformationBatch(ImageProcessor sourceIpr, ImageProcessor targetIpr, Stack<Point> sourcePoints, Stack<Point> targetPoints, Param parameter) {
        // Produce side information
        final int imagePyramidDepth = parameter.max_scale_deformation - parameter.min_scale_deformation + 1;

        // Create target image model
        final BSplineModel target = new BSplineModel(targetIpr,false,(int) Math.pow(2, parameter.img_subsamp_fact));
        target.setPyramidDepth(imagePyramidDepth);
        target.startPyramids();

        // Create target mask
        final Mask targetMsk = new Mask(targetIpr.getWidth(),targetIpr.getHeight());

        // Create source image model
        final BSplineModel source = new BSplineModel(sourceIpr, false, (int) Math.pow(2, parameter.img_subsamp_fact));
        source.setPyramidDepth(imagePyramidDepth);
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

        // Join threads
        try {
            source.getThread().join();
            target.getThread().join();
        } catch (InterruptedException e) {
            IJ.error("Unexpected interruption exception " + e);
        }

        // Set transformation parameters
        final Transformation warp = new Transformation(
                null, null, source, target, sourcePh, targetPh,
                sourceMsk, targetMsk, null, null,
                parameter.min_scale_deformation, parameter.max_scale_deformation,
                0, parameter.divWeight,
                parameter.curlWeight, 1, 0, parameter.consistencyWeight, parameter.stopThreshold,
                -1, false, parameter.mode, null, null, null, null, null, null, null);

        // Register
        if(parameter.mode == MainDialog.MONO_MODE) {
            warp.doUnidirectionalRegistration();
        } else {
            warp.doBidirectionalRegistration();
        }

        return warp;

    }
}
