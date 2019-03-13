package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;

/**
 * Created by sc13967 on 05/02/2018.
 */
public class CreateOrthogonalView < T extends RealType< T > & NativeType< T >> extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String POSITION_MODE = "Position mode";
    public static final String INPUT_OBJECTS = "Input objects";


    interface PositionModes{
        String IMAGE_CENTRE = "Image centre";
        String INTENSITY_PROJECTION = "Intensity projection";
        String LARGEST_OBJ_CENTROID = "Centroid of largest object";

        String[] ALL = new String[]{IMAGE_CENTRE, INTENSITY_PROJECTION,LARGEST_OBJ_CENTROID};

    }

    public Img<T> getCentreView(Img<T> inputImg, long[] centres, long[] dims, double ZXYRatio) {
        long border = 10;

        // Get orthogonal views through centre point
        IntervalView<T> viewXY = Views.hyperSlice(inputImg,2,centres[2]);
        IntervalView<T> viewXZ = Views.hyperSlice(inputImg,1,centres[1]);
        IntervalView<T> viewYZ = Views.hyperSlice(inputImg,0,centres[0]);

        // Scaling the XZ and YZ views
        AffineTransform2D affine2D = new AffineTransform2D();
        affine2D.set(new double[][]{{1,0,0},{0,ZXYRatio,0},{0,0,1}});
        NearestNeighborInterpolatorFactory<T> interpolator = new NearestNeighborInterpolatorFactory<T>();

        Interval intervalXZ = new FinalInterval(new long[]{0, 0}, new long[]{dims[0]-1, dims[2]});
        RealRandomAccessible<T> interpXZ = Views.interpolate(viewXZ, interpolator);
        viewXZ = Views.interval(RealViews.affine(interpXZ, affine2D),intervalXZ);

        Interval intervalYZ = new FinalInterval(new long[]{0, 0}, new long[]{dims[1]-1,dims[2]});
        RealRandomAccessible<T> interpYZ = Views.interpolate(viewYZ, interpolator);
        viewYZ = Views.interval(RealViews.affine(interpYZ, affine2D),intervalYZ);

        // Creating a single image, large enough to hold all images
        CellImgFactory<T> factory = new CellImgFactory<T>((T) inputImg.firstElement());
        final long[] orthoDims = new long[] { dims[0]+dims[2]+border, dims[1]+dims[2]+border};
        Img< T > orthoImg = factory.create( orthoDims, viewXY.firstElement() );
        Cursor<T> cursorOrtho = orthoImg.cursor();

        double white = orthoImg.firstElement().getMaxValue();
        while (cursorOrtho.hasNext()) cursorOrtho.next().setReal(white);

        // Adding XY view
        Cursor<T> cursorXY = viewXY.cursor();
        Cursor<T> cursorOrthoXY = Views.offsetInterval(orthoImg,new long[]{0,0},new long[]{dims[0],dims[1]}).cursor();
        while (cursorXY.hasNext()) cursorOrthoXY.next().set(cursorXY.next());

        // Adding XZ view
        Cursor<T> cursorXZ = viewXZ.cursor();
        Cursor<T> cursorOrthoXZ = Views.offsetInterval(orthoImg,new long[]{0,dims[1]+border},new long[]{dims[0],dims[2]}).cursor();
        while (cursorXZ.hasNext()) cursorOrthoXZ.next().set(cursorXZ.next());

        // Adding YZ view
        Cursor<T> cursorYZ = viewYZ.cursor();
        Cursor<T> cursorOrthoYZ = Views.permute(Views.offsetInterval(orthoImg,new long[]{dims[0]+border,0},new long[]{dims[2],dims[1]}),0,1).cursor();
        while (cursorYZ.hasNext()) cursorOrthoYZ.next().set(cursorYZ.next());

        return orthoImg;

    }


    @Override
    public String getTitle() {
        return "Create orthogonal view";
    }

    @Override
    public String getPackageName() {
        return PackageNames.VISUALISATION;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean run(Workspace workspace) {
        // Loading image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        Img<T> inputImg = inputImage.getImgPlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String positionMode = parameters.getValue(POSITION_MODE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        // Getting input image dimensions
        double xCal = ((ImgPlus<T>) inputImg).averageScale(0);
        double zCal = inputImg.numDimensions() > 2 ? ((ImgPlus<T>) inputImg).averageScale(2) : 1;
        double ZXYRatio = zCal/xCal;
        long dimX = inputImg.dimension(0);
        long dimY = inputImg.dimension(1);
        long dimZ = inputImg.dimension(2);
        long dimZScaled = (long) Math.floor((dimZ-1) * ZXYRatio);
        long[] dims = new long[]{dimX,dimY,dimZScaled};

        Img< T > orthoImg = null;

        switch (positionMode) {
            case PositionModes.INTENSITY_PROJECTION:

                break;

            case PositionModes.LARGEST_OBJ_CENTROID:
                // Getting the largest object
                ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
                Obj largestObject = null;
                int largestSize = 0;
                for (Obj inputObject:inputObjects.values()) {
                    int currentSize = inputObject.getPoints().size();
                    if (currentSize > largestSize) {
                        largestSize = currentSize;
                        largestObject = inputObject;
                    }
                }

                // Getting the object centroid
                long xCent = Math.round(largestObject.getXMean(true));
                long yCent = Math.round(largestObject.getYMean(true));
                long zCent = Math.round(largestObject.getZMean(true,false));

                long[] centres = new long[]{xCent,yCent,zCent};
                orthoImg = getCentreView(inputImg,centres,dims,ZXYRatio);

                break;

            case PositionModes.IMAGE_CENTRE:
                centres = new long[]{dimX/2, dimY/2, dimZ/2};
                orthoImg = getCentreView(inputImg,centres,dims,ZXYRatio);

                break;

        }

        // Adding image to workspace
        ImagePlus outputImagePlus = ImageJFunctions.wrap(orthoImg,outputImageName);
        Image outputImage = new Image(outputImageName,outputImagePlus);
        workspace.addImage(outputImage);

        // Displaying the image
        if (showOutput) {
            ImageJFunctions.show(orthoImg);
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new ChoiceP(POSITION_MODE,this,PositionModes.IMAGE_CENTRE,PositionModes.ALL));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(POSITION_MODE));

        switch ((String) parameters.getValue(POSITION_MODE)) {
            case PositionModes.LARGEST_OBJ_CENTROID:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                break;
        }

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
