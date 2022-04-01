package io.github.mianalysis.mia.module.visualise;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImgPlus;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * Created by sc13967 on 05/02/2018.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class CreateOrthogonalView<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String ORTHO_SEPARATOR = "Orthogonal view controls";
    public static final String POSITION_MODE = "Position mode";
    public static final String INPUT_OBJECTS = "Input objects";

    public CreateOrthogonalView(Modules modules) {
        super("Create orthogonal view", modules);
    }

    interface PositionModes {
        String IMAGE_CENTRE = "Image centre";
        // String INTENSITY_PROJECTION = "Intensity projection";
        String LARGEST_OBJ_CENTROID = "Centroid of largest object";

        // String[] ALL = new String[] { IMAGE_CENTRE, INTENSITY_PROJECTION,
        // LARGEST_OBJ_CENTROID };
        String[] ALL = new String[] { IMAGE_CENTRE, LARGEST_OBJ_CENTROID };

    }

    long[] getLargestObjectCentroid(Objs inputObjects) {
        if (inputObjects.size() == 0)
            return null;

        // Getting the largest object
        Obj largestObject = null;
        int largestSize = 0;
        for (Obj inputObject : inputObjects.values()) {
            int currentSize = inputObject.size();
            if (currentSize > largestSize) {
                largestSize = currentSize;
                largestObject = inputObject;
            }
        }

        // Getting the object centroid
        long xCent = Math.round(largestObject.getXMean(true));
        long yCent = Math.round(largestObject.getYMean(true));
        long zCent = Math.round(largestObject.getZMean(true, false));

        return new long[] { xCent, yCent, zCent };

    }

    public Img<T> getEmptyImage(Img<T> inputImg, long[] dims, double ZXYRatio) {
        long border = 10;

        // Creating a single image, large enough to hold all images
        CellImgFactory<T> factory = new CellImgFactory<T>((T) inputImg.firstElement());
        final long[] orthoDims = new long[] { dims[0] + dims[2] + border, dims[1] + dims[2] + border };
        Img<T> orthoImg = factory.create(orthoDims, inputImg.firstElement());
        Cursor<T> cursorOrtho = orthoImg.cursor();

        double white = orthoImg.firstElement().getMaxValue();
        while (cursorOrtho.hasNext())
            cursorOrtho.next().setReal(white);

        return orthoImg;

    }

    public Img<T> getCentreView(Img<T> inputImg, long[] centres, long[] dims, double ZXYRatio) {
        long border = 10;

        // Get orthogonal views through centre point
        IntervalView<T> viewXY = Views.hyperSlice(inputImg, 2, centres[2]);
        IntervalView<T> viewXZ = Views.hyperSlice(inputImg, 1, centres[1]);
        IntervalView<T> viewYZ = Views.hyperSlice(inputImg, 0, centres[0]);

        // Scaling the XZ and YZ views
        AffineTransform2D affine2D = new AffineTransform2D();
        affine2D.set(new double[][] { { 1, 0, 0 }, { 0, ZXYRatio, 0 }, { 0, 0, 1 } });
        NearestNeighborInterpolatorFactory<T> interpolator = new NearestNeighborInterpolatorFactory<T>();

        Interval intervalXZ = new FinalInterval(new long[] { 0, 0 }, new long[] { dims[0] - 1, dims[2] });
        RealRandomAccessible<T> interpXZ = Views.interpolate(viewXZ, interpolator);
        viewXZ = Views.interval(RealViews.affine(interpXZ, affine2D), intervalXZ);

        Interval intervalYZ = new FinalInterval(new long[] { 0, 0 }, new long[] { dims[1] - 1, dims[2] });
        RealRandomAccessible<T> interpYZ = Views.interpolate(viewYZ, interpolator);
        viewYZ = Views.interval(RealViews.affine(interpYZ, affine2D), intervalYZ);

        // Creating a single image, large enough to hold all images
        CellImgFactory<T> factory = new CellImgFactory<T>((T) inputImg.firstElement());
        final long[] orthoDims = new long[] { dims[0] + dims[2] + border, dims[1] + dims[2] + border };
        Img<T> orthoImg = factory.create(orthoDims, viewXY.firstElement());
        Cursor<T> cursorOrtho = orthoImg.cursor();

        double white = orthoImg.firstElement().getMaxValue();
        while (cursorOrtho.hasNext())
            cursorOrtho.next().setReal(white);

        // Adding XY view
        Cursor<T> cursorXY = viewXY.cursor();
        Cursor<T> cursorOrthoXY = Views.offsetInterval(orthoImg, new long[] { 0, 0 }, new long[] { dims[0], dims[1] })
                .cursor();
        while (cursorXY.hasNext())
            cursorOrthoXY.next().set(cursorXY.next());

        // Adding XZ view
        Cursor<T> cursorXZ = viewXZ.cursor();
        Cursor<T> cursorOrthoXZ = Views
                .offsetInterval(orthoImg, new long[] { 0, dims[1] + border }, new long[] { dims[0], dims[2] }).cursor();
        while (cursorXZ.hasNext())
            cursorOrthoXZ.next().set(cursorXZ.next());

        // Adding YZ view
        Cursor<T> cursorYZ = viewYZ.cursor();
        Cursor<T> cursorOrthoYZ = Views.permute(
                Views.offsetInterval(orthoImg, new long[] { dims[0] + border, 0 }, new long[] { dims[2], dims[1] }), 0,
                1).cursor();
        while (cursorYZ.hasNext())
            cursorOrthoYZ.next().set(cursorYZ.next());

        return orthoImg;

    }


    @Override
    public Category getCategory() {
        return Categories.VISUALISATION;
    }

    @Override
    public String getDescription() {
        return "Create a montage image showing orthogonal views of a specified input image from the workspace.  Orthogonal views are taken in the XY, XZ and YZ planes and all share a common coordinate.  This common coordinate can be the centre of the image or the centre of the largest object in a specified object collection.  The output orthogonal view is stored in the workspace as a separate image.";
    }

    @Override
    public Status process(Workspace workspace) {
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
        double ZXYRatio = zCal / xCal;
        long dimX = inputImg.dimension(0);
        long dimY = inputImg.dimension(1);
        long dimZ = inputImg.dimension(2);
        long dimZScaled = (long) Math.floor((dimZ - 1) * ZXYRatio);
        long[] dims = new long[] { dimX, dimY, dimZScaled };

        Img<T> orthoImg = null;

        switch (positionMode) {
            // case PositionModes.INTENSITY_PROJECTION:

            // break;

            case PositionModes.LARGEST_OBJ_CENTROID:
                Objs inputObjects = workspace.getObjectSet(inputObjectsName);
                long[] centres = getLargestObjectCentroid(inputObjects);

                // If no objects were present, create a blank image
                if (centres == null)
                    orthoImg = getEmptyImage(inputImg, dims, ZXYRatio);
                else
                    orthoImg = getCentreView(inputImg, centres, dims, ZXYRatio);

                break;

            case PositionModes.IMAGE_CENTRE:
                centres = new long[] { dimX / 2, dimY / 2, dimZ / 2 };
                orthoImg = getCentreView(inputImg, centres, dims, ZXYRatio);

                break;

        }

        // Adding image to workspace
        ImagePlus outputImagePlus = ImageJFunctions.wrap(orthoImg, outputImageName);
        Image outputImage = ImageFactory.createImage(outputImageName, outputImagePlus);
        workspace.addImage(outputImage);

        // Displaying the image
        if (showOutput) {
            ImageJFunctions.show(orthoImg);
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(ORTHO_SEPARATOR, this));
        parameters.add(new ChoiceP(POSITION_MODE, this, PositionModes.IMAGE_CENTRE, PositionModes.ALL));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(ORTHO_SEPARATOR));
        returnedParameters.add(parameters.getParameter(POSITION_MODE));
        switch ((String) parameters.getValue(POSITION_MODE)) {
            case PositionModes.LARGEST_OBJ_CENTROID:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                break;
        }

        return returnedParameters;

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
        String storageMode = MIA.preferences.getDataStorageMode();
        if (storageMode.equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE) & il2Support.equals(IL2Support.NONE))
            return false;

        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription(
                "Image from workspace for which orthogonal view will be generated.  This image will not be affected by the process.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "Output orthogonal image showing orthogonal views in XY, XZ and YZ planes.  The output image will be formed from three panels, showing the orthogonal views, with white pixels elsewhere.  This image will be stored in the workspace using this name.");

        parameters.get(POSITION_MODE).setDescription("Controls how the orthogonal views are generated:<br><ul>"

                + "<li>\"" + PositionModes.IMAGE_CENTRE
                + "\" Orthogonal views are extracted as single slices from the input image.  The views share a common coordinate, coincident with the centre of the image.</li>"

                + "<li>\"" + PositionModes.LARGEST_OBJ_CENTROID
                + "\" Orthogonal views are extracted as single slices from the input image.  The views share a common coordinate, coincident with the centre of the largest object from the collection specified by \""
                + INPUT_OBJECTS + "\".</li></ul>");

        parameters.get(INPUT_OBJECTS).setDescription("If \"" + POSITION_MODE + "\" is set to \""
                + PositionModes.LARGEST_OBJ_CENTROID
                + "\", the orthogonal views will be positioned coincident with the centroid of the largest object from this collection.");

    }
}
