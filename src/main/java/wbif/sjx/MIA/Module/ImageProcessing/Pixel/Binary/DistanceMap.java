package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.Resizer;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.plugins.GeodesicDistanceMap3D;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.InterpolateZAxis;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

public class DistanceMap extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String DISTANCE_MAP_SEPARATOR = "Distance map controls";
    public static final String MATCH_Z_TO_X= "Match Z to XY";

    public DistanceMap(ModuleCollection modules) {
        super("Calculate distance map",modules);
    }


    public static ImagePlus getDistanceMap(ImagePlus ipl, boolean matchZToXY) {
        int nSlices = ipl.getNSlices();

        // If necessary, interpolating the image in Z to match the XY spacing
        if (matchZToXY && nSlices > 1) ipl = InterpolateZAxis.matchZToXY(ipl);

        // Calculating the distance map using MorphoLibJ
        float[] weights = ChamferWeights3D.WEIGHTS_3_4_5_7.getFloatWeights();

        // Creating duplicates of the input image
        ipl = new Duplicator().run(ipl);
        ImagePlus maskIpl = new Duplicator().run(ipl);

        IJ.run(maskIpl,"Invert","stack");
        ipl.setStack(new GeodesicDistanceMap3D().process(ipl,maskIpl,"Dist",weights,true).getStack());

        // If the input image as interpolated, it now needs to be returned to the original scaling
        if (matchZToXY && nSlices > 1) {
            Resizer resizer = new Resizer();
            resizer.setAverageWhenDownsizing(true);
            ipl = resizer.zScale(ipl, nSlices, Resizer.IN_PLACE);
        }

        return ipl;

    }



    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getDescription() {
        return "Creates a 32-bit greyscale image from an input binary image, where the value of each foreground pixel in the input image is equal to its Euclidean distance to the nearest background pixel.  The input image must be 8-bit and have the logic black foreground (intensity 0) and white background (intensity 255).  The output image will have pixel values of 0 coincident with background pixels in the input image and values greater than zero coincident with foreground pixels.  Uses the plugin \"<a href=\"https://github.com/ijpb/MorphoLibJ\">MorphoLibJ</a>\".";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean matchZToXY = parameters.getValue(MATCH_Z_TO_X);

        // Running distance map
        inputImagePlus = getDistanceMap(inputImagePlus,matchZToXY);

        // If the image is being saved as a new image, adding it to the workspace
        writeStatus("Adding image ("+outputImageName+") to workspace");
        Image outputImage = new Image(outputImageName,inputImagePlus);
        workspace.addImage(outputImage);
        if (showOutput) outputImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(DISTANCE_MAP_SEPARATOR,this));
        parameters.add(new BooleanP(MATCH_Z_TO_X, this, true));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;

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
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
      parameters.get(INPUT_IMAGE).setDescription("Image from workspace to calculate distance map for.  This must be an 8-bit binary image (255 = background, 0 = foreground).");

      parameters.get(OUTPUT_IMAGE).setDescription("The output distance map will be saved to the workspace with this name.  This image will be 32-bit format.");

      parameters.get(MATCH_Z_TO_X).setDescription("When selected, an image is interpolated in Z (so that all pixels are isotropic) prior to calculation of the distance map.  This prevents warping of the distance map along the Z-axis if XY and Z sampling aren't equal.");

    }
}
