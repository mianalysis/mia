package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import net.imglib2.*;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.*;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 05/02/2018.
 */
public class CreateOrthogonalView < T extends RealType< T > & NativeType< T >> extends HCModule {
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
                // Get centre of object
                long dimX = inputImg.dimension(0);
                long dimY = inputImg.dimension(1);
                long dimZ = inputImg.dimension(2);

                // Get orthogonal views through centre point
                IntervalView<T> viewXY = Views.hyperSlice(inputImg,2,(dimZ/2));
                IntervalView<T> viewXZ = Views.hyperSlice(inputImg,1,(dimY/2));
                IntervalView<T> viewYZ = Views.hyperSlice(inputImg,0,(dimX/2));
                viewYZ = Views.rotate(viewYZ,0,1);

                // Combine three views into a single image
                // Creating a single image, large enough to hold all images
                final ImgFactory< T > factory = new ArrayImgFactory< T >();
                final long[] dimensions = new long[] { viewXY.dimension(0)+viewYZ.dimension(0), viewXY.dimension(1)+viewXZ.dimension(1) };
                final Img< T > viewOrtho = factory.create( dimensions, viewXY.firstElement() );

                // Place XY plane pixels in orthogonal view
                Cursor<T> cursorXY = viewXY.cursor();
                Cursor<T> cursorXZ = viewXZ.cursor();
                Cursor<T> cursorYZ = viewYZ.cursor();

                IntervalView<T> orthoViewXY = Views.offsetInterval(viewOrtho,new long[]{0,0},new long[]{dimX,dimY});
                IntervalView<T> orthoViewXZ = Views.offsetInterval(viewOrtho,new long[]{dimX,0},new long[]{dimZ,dimY});
                IntervalView<T> orthoViewYZ = Views.offsetInterval(viewOrtho,new long[]{0,dimY},new long[]{dimX,dimZ});
                Cursor<T> cursorOrthoXY = orthoViewXY.cursor();

                while (cursorXY.hasNext()) {
                    cursorOrthoXY.next().set(cursorXY.next());
                }

                ImageJFunctions.show(viewOrtho);

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
