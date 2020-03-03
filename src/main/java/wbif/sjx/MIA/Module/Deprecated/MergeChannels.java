package wbif.sjx.MIA.Module.Deprecated;

import java.util.Arrays;
import java.util.LinkedHashSet;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.IdentityAxis;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.IntegerP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 22/02/2018.
 */
public class MergeChannels <T extends RealType<T> & NativeType<T>> extends Module {
    public static final String ADD_INPUT_IMAGE = "Add image";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OVERWRITE_MODE = "Overwrite mode";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String IMAGE_INDEX_TO_OVERWRITE = "Image index to overwrite (>= 1)";

    public MergeChannels(ModuleCollection modules) {
        super("Merge channels",modules);
    }


    public interface OverwriteModes {
        String CREATE_NEW = "Create new image";
        String OVERWRITE_IMAGE = "Overwrite image";

        String[] ALL = new String[]{CREATE_NEW,OVERWRITE_IMAGE};

    }


//    public void forceSameType(Image inputImage1, Image inputImage2) {
//        ImgPlus<T> img1 = inputImage1.getImgPlus();
//        ImgPlus<T> img2 = inputImage2.getImgPlus();
//
//    }

    public Image combineImages(Image[] inputImages, String outputImageName) {
        // Processing first two images
        Image outputImage = combineImages(inputImages[0],inputImages[1],outputImageName);

        // Appending any additional images
        for (int i=2;i<inputImages.length;i++) {
            outputImage = combineImages(outputImage,inputImages[i],outputImageName);
        }

        return outputImage;

    }

    public Image combineImages(Image inputImage1, Image inputImage2, String outputImageName) {
        ImgPlus<T> img1 = inputImage1.getImgPlus();
        ImgPlus<T> img2 = inputImage2.getImgPlus();

        int xDim1 = img1.dimensionIndex(Axes.X);
        int yDim1 = img1.dimensionIndex(Axes.Y);
        int cDim1 = img1.dimensionIndex(Axes.CHANNEL);
        int zDim1 = img1.dimensionIndex(Axes.Z);
        int tDim1 = img1.dimensionIndex(Axes.TIME);
        int xDim2 = img2.dimensionIndex(Axes.X);
        int yDim2 = img2.dimensionIndex(Axes.Y);
        int cDim2 = img2.dimensionIndex(Axes.CHANNEL);
        int zDim2 = img2.dimensionIndex(Axes.Z);
        int tDim2 = img2.dimensionIndex(Axes.TIME);

        long[] dimsIn1 = new long[5];
        long[] dimsIn2 = new long[5];
        long[] dimsOut = new long[5];
        long[] offsetOut1 = new long[5];
        long[] offsetOut2 = new long[5];

        dimsIn1[0] = xDim1 == -1 ? 1 : img1.dimension(xDim1);
        dimsIn1[1] = yDim1 == -1 ? 1 : img1.dimension(yDim1);
        dimsIn1[2] = cDim1 == -1 ? 1 : img1.dimension(cDim1);
        dimsIn1[3] = zDim1 == -1 ? 1 : img1.dimension(zDim1);
        dimsIn1[4] = tDim1 == -1 ? 1 : img1.dimension(tDim1);

        dimsIn2[0] = xDim2 == -1 ? 1 : img2.dimension(xDim2);
        dimsIn2[1] = yDim2 == -1 ? 1 : img2.dimension(yDim2);
        dimsIn2[2] = cDim2 == -1 ? 1 : img2.dimension(cDim2);
        dimsIn2[3] = zDim2 == -1 ? 1 : img2.dimension(zDim2);
        dimsIn2[4] = tDim2 == -1 ? 1 : img2.dimension(tDim2);

        dimsOut[0] = xDim1 == -1 ? 1 : img1.dimension(xDim1);
        dimsOut[1] = yDim1 == -1 ? 1 : img1.dimension(yDim1);
        dimsOut[2] = (cDim1 == -1 ? 1 : img1.dimension(cDim1)) + (cDim2 == -1 ? 1 : img2.dimension(cDim2));
        dimsOut[3] = zDim1 == -1 ? 1 : img1.dimension(zDim1);
        dimsOut[4] = tDim1 == -1 ? 1 : img1.dimension(tDim1);

        Arrays.fill(offsetOut1,0);
        Arrays.fill(offsetOut2,0);
        offsetOut2[2] = dimsIn1[2];

        // Creating the composite image
        T type = img1.firstElement();
        CellImgFactory<T> factory = new CellImgFactory<T>(type);
        ImgPlus<T> mergedImg = new ImgPlus<>(factory.create(dimsOut));

        // Assigning the relevant dimensions
        CalibratedAxis xAxis = xDim1 == -1 ? new IdentityAxis(Axes.X) : img1.axis(xDim1);
        mergedImg.setAxis(xAxis,0);
        CalibratedAxis yAxis = yDim1 == -1 ? new IdentityAxis(Axes.Y) : img1.axis(yDim1);
        mergedImg.setAxis(yAxis,1);
        CalibratedAxis cAxis = cDim1 == -1 ? new IdentityAxis(Axes.CHANNEL) : img1.axis(cDim1);
        mergedImg.setAxis(cAxis,2);
        CalibratedAxis zAxis = zDim1 == -1 ? new IdentityAxis(Axes.Z) : img1.axis(zDim1);
        mergedImg.setAxis(zAxis,3);
        CalibratedAxis tAxis = tDim1 == -1 ? new IdentityAxis(Axes.TIME) : img1.axis(tDim1);
        mergedImg.setAxis(tAxis,4);

        Cursor<T> cursorIn = img1.cursor();
        Cursor<T> cursorOut = Views.offsetInterval(mergedImg, offsetOut1, dimsIn1).cursor();
        while (cursorIn.hasNext()) cursorOut.next().set(cursorIn.next());

        cursorIn = img2.cursor();
        cursorOut = Views.offsetInterval(mergedImg, offsetOut2, dimsIn2).cursor();
        while (cursorIn.hasNext()) cursorOut.next().set(cursorIn.next());

//        ImagePlus ipl;
//        if (mergedImg.firstElement().getClass().isInstance(new UnsignedByteType())) {
//            ipl = ImageJFunctions.wrapUnsignedByte(mergedImg,outputImageName);
//        } else if (mergedImg.firstElement().getClass().isInstance(new UnsignedShortType())) {
//            ipl = ImageJFunctions.wrapUnsignedShort(mergedImg,outputImageName);
//        } else {
//            ipl = ImageJFunctions.wrapFloat(mergedImg,outputImageName);
//        }

        ImagePlus ipl = ImageJFunctions.wrap(mergedImg,outputImageName);
        ipl = new Duplicator().run(HyperStackConverter.toHyperStack(ipl,ipl.getNChannels(),ipl.getNSlices(),ipl.getNFrames(),"xyczt","Composite"));

        // Updating the display range to help show all the colours
        IntensityMinMax.run(ipl,true,0.001,0.001,IntensityMinMax.PROCESS_FAST);

        // Spatial calibration has to be reapplied, as it's lost in the translation between ImagePlus and ImgPlus
        ipl.setCalibration(inputImage1.getImagePlus().getCalibration());

        ipl.setPosition(1,1,1);
        ipl.updateChannelAndDraw();

        return new Image(outputImageName,ipl);

    }

    private Img<T> createComposite(Image inputImageRed, Image inputImageGreen, Image inputImageBlue) {
        long dimX = 0;
        long dimY = 0;
        long dimZ = 0;
        T type = null;

        Img<T> redImg = null;
        if (inputImageRed != null) {
            redImg = inputImageRed.getImgPlus();
            dimX = redImg.dimension(0);
            dimY = redImg.dimension(1);
            dimZ = redImg.dimension(2);
            type = redImg.firstElement();
        }

        Img<T> greenImg = null;
        if (inputImageGreen != null) {
            greenImg = inputImageGreen.getImgPlus();
            dimX = greenImg.dimension(0);
            dimY = greenImg.dimension(1);
            dimZ = greenImg.dimension(2);
            type = greenImg.firstElement();
        }

        Img<T> blueImg = null;
        if (inputImageBlue != null) {
            blueImg = inputImageBlue.getImgPlus();
            dimX = blueImg.dimension(0);
            dimY = blueImg.dimension(1);
            dimZ = blueImg.dimension(2);
            type = blueImg.firstElement();
        }

        // Creating the composite image
        long[] dimensions = new long[]{dimX,dimY,3, dimZ,1};
        CellImgFactory<T> factory = new CellImgFactory<T>(type);
        Img<T> rgbImg = factory.create(dimensions);

        // Adding values view
        if (inputImageRed != null) {
            Cursor<T> cursorSingle = redImg.cursor();
            Cursor<T> cursorRGB = Views.offsetInterval(rgbImg, new long[]{0, 0, 0, 0,0}, new long[]{dimX,dimY,1, dimZ,1}).cursor();
            while (cursorSingle.hasNext()) cursorRGB.next().set(cursorSingle.next());
        }

        if (inputImageGreen != null) {
            Cursor<T> cursorSingle = greenImg.cursor();
            Cursor<T> cursorRGB = Views.offsetInterval(rgbImg, new long[]{0, 0, 1, 0,0}, new long[]{dimX, dimY, 1, dimZ,1}).cursor();
            while (cursorSingle.hasNext()) cursorRGB.next().set(cursorSingle.next());
        }

        if (inputImageBlue != null) {
            Cursor<T> cursorSingle = blueImg.cursor();
            Cursor<T> cursorRGB = Views.offsetInterval(rgbImg, new long[]{0, 0, 2, 0,0}, new long[]{dimX, dimY, 1, dimZ,1}).cursor();
            while (cursorSingle.hasNext()) cursorRGB.next().set(cursorSingle.next());
        }

        return rgbImg;

    }

    @Override
    public String getPackageName() {
        return PackageNames.DEPRECATED;
    }

    @Override
    public String getDescription() {
        return "NOTE: This Module has been superseeded by the more generalised \"Concatenate stacks\" Module.  It will " +
                "be removed in a future release.\r\n" +
                "Combines image stacks as different channels.  Output is automatically converted to a composite image.";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting parameters
        String overwriteMode = parameters.getValue(OVERWRITE_MODE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Creating a collection of images
        LinkedHashSet<ParameterCollection> collections = parameters.getValue(ADD_INPUT_IMAGE);
        Image[] inputImages = new Image[collections.size()];
        int i=0;
        for (ParameterCollection collection:collections) {
            inputImages[i++] = workspace.getImage(collection.getValue(INPUT_IMAGE));
        }

        // Ensuring the two image types are the same.  If they're not, they're set to the highest common type
//        forceSameType(inputImage1,inputImage2);

        Image mergedImage = combineImages(inputImages,outputImageName);

        // If the image is being saved as a new image, adding it to the workspace
        switch (overwriteMode) {
            case OverwriteModes.CREATE_NEW:
                Image outputImage = new Image(outputImageName,mergedImage.getImagePlus());
                workspace.addImage(outputImage);
                break;

            case OverwriteModes.OVERWRITE_IMAGE:
                inputImages[i-1].setImagePlus(mergedImage.getImagePlus());
                break;

        }

        if (showOutput) mergedImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        ParameterCollection collection = new ParameterCollection();
        collection.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new ParameterGroup(ADD_INPUT_IMAGE,this,collection,2));

        parameters.add(new ChoiceP(OVERWRITE_MODE,this,OverwriteModes.CREATE_NEW,OverwriteModes.ALL));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new IntegerP(IMAGE_INDEX_TO_OVERWRITE,this,1));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(ADD_INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(OVERWRITE_MODE));
        switch ((String) parameters.getValue(OVERWRITE_MODE)) {
            case OverwriteModes.CREATE_NEW:
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
                break;
            case OverwriteModes.OVERWRITE_IMAGE:
                returnedParameters.add(parameters.getParameter(IMAGE_INDEX_TO_OVERWRITE));
                break;
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
