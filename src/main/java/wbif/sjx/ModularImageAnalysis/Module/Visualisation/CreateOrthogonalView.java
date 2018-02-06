package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.View;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IterableRandomAccessibleInterval;
import net.imglib2.view.RandomAccessiblePair;
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
                RandomAccess randomAccess = inputImg.randomAccess();


                break;

            case PositionModes.LARGEST_OBJ_CENTROID:

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
