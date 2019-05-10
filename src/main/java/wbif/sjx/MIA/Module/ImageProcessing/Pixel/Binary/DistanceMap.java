package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.Resizer;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.plugins.GeodesicDistanceMap3D;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.InterpolateZAxis;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

public class DistanceMap extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String MATCH_Z_TO_X= "Match Z to XY";

    public DistanceMap(ModuleCollection modules) {
        super(modules);
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
    public String getTitle() {
        return "Calculate distance map";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public boolean process(Workspace workspace) {
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
        writeMessage("Adding image ("+outputImageName+") to workspace");
        Image outputImage = new Image(outputImageName,inputImagePlus);
        workspace.addImage(outputImage);
        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new BooleanP(MATCH_Z_TO_X, this, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return objectMeasurementRefs;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

}
