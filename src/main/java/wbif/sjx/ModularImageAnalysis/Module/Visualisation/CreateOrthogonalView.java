package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import net.imagej.ImgPlus;
import net.imglib2.*;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.*;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.*;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 05/02/2018.
 */
public class CreateOrthogonalView < T extends RealType< T > & NativeType< T >> extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String POSITION_MODE = "Position mode";
    public static final String INPUT_OBJECTS = "Input objects";


    interface PositionModes{
        String INTENSITY_PROJECTION = "Intensity projection";
        String LARGEST_OBJ_CENTROID = "Centroid of largest object";

        String[] ALL = new String[]{INTENSITY_PROJECTION,LARGEST_OBJ_CENTROID};

    }


    @Override
    public String getTitle() {
        return "Create orthogonal view";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Loading image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image<T> inputImage = workspace.getImage(inputImageName);
        Img<T> inputImg = inputImage.getImg();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String positionMode = parameters.getValue(POSITION_MODE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        switch (positionMode) {
            case PositionModes.INTENSITY_PROJECTION:

                break;

            case PositionModes.LARGEST_OBJ_CENTROID:
                long border = 10;

                // Getting input image dimensions
                double xCal = ((ImgPlus<T>) inputImg).averageScale(0);
                double zCal = ((ImgPlus<T>) inputImg).averageScale(2);
                double ZXYRatio = zCal/xCal;

                long dimX = inputImg.dimension(0);
                long dimY = inputImg.dimension(1);
                long dimZ = inputImg.dimension(2);
                long dimZScaled = (long) Math.floor((dimZ-1) * ZXYRatio);

                // Get centre of object
                long centX = dimX/2;
                long centY = dimY/2;
                long centZ = dimZ/2;

                // Get orthogonal views through centre point
                IntervalView<T> viewXY = Views.hyperSlice(inputImg,2,centZ);
                IntervalView<T> viewXZ = Views.hyperSlice(inputImg,1,centY);
                IntervalView<T> viewYZ = Views.hyperSlice(inputImg,0,centX);

                // Scaling the XZ and YZ views
                AffineTransform2D affine2D = new AffineTransform2D();
                affine2D.set(new double[][]{{1,0,0},{0,ZXYRatio,0},{0,0,1}});
                NearestNeighborInterpolatorFactory<T> interpolator = new NearestNeighborInterpolatorFactory<T>();

                Interval intervalXZ = new FinalInterval(new long[]{0, 0}, new long[]{dimX-1, dimZScaled});
                RealRandomAccessible<T> interpXZ = Views.interpolate(viewXZ, interpolator);
                viewXZ = Views.interval(RealViews.affine(interpXZ, affine2D),intervalXZ);

                Interval intervalYZ = new FinalInterval(new long[]{0, 0}, new long[]{dimY-1,dimZScaled});
                RealRandomAccessible<T> interpYZ = Views.interpolate(viewYZ, interpolator);
                viewYZ = Views.interval(RealViews.affine(interpYZ, affine2D),intervalYZ);

                // Creating a single image, large enough to hold all images
                final ImgFactory< T > factory = new ArrayImgFactory< T >();
                final long[] dimensions = new long[] { dimX+dimZScaled+border, dimY+dimZScaled+border};
                final Img< T > orthoImg = factory.create( dimensions, viewXY.firstElement() );
                Cursor<T> cursorOrtho = orthoImg.cursor();
                double white = orthoImg.firstElement().getMaxValue();
                while (cursorOrtho.hasNext()) cursorOrtho.next().setReal(white);

                // Adding XY view
                Cursor<T> cursorXY = viewXY.cursor();
                Cursor<T> cursorOrthoXY = Views.offsetInterval(orthoImg,new long[]{0,0},new long[]{dimX,dimY}).cursor();
                while (cursorXY.hasNext()) cursorOrthoXY.next().set(cursorXY.next());

                // Adding XZ view
                Cursor<T> cursorXZ = viewXZ.cursor();
                Cursor<T> cursorOrthoXZ = Views.offsetInterval(orthoImg,new long[]{0,dimY+border},new long[]{dimX,dimZScaled}).cursor();
                while (cursorXZ.hasNext()) cursorOrthoXZ.next().set(cursorXZ.next());

                // Adding YZ view
                Cursor<T> cursorYZ = viewYZ.cursor();
//                Cursor<T> cursorOrthoYZ = Views.invertAxis(Views.rotate(Views.offsetInterval(orthoImg,new long[]{dimX+border,0},new long[]{dimZScaled,dimY}),0,1),0).cursor();
                Cursor<T> cursorOrthoYZ = Views.permute(Views.offsetInterval(orthoImg,new long[]{dimX+border,0},new long[]{dimZScaled,dimY}),0,1).cursor();
                while (cursorYZ.hasNext()) cursorOrthoYZ.next().set(cursorYZ.next());
                ImageJFunctions.show(orthoImg);

                break;

        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE,Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(POSITION_MODE,Parameter.CHOICE_ARRAY,PositionModes.INTENSITY_PROJECTION,PositionModes.ALL));
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {

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
    public void addRelationships(RelationshipCollection relationships) {

    }
}
