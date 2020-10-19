package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Analysis.IntensityCalculator;

public class WhiteBalanceCorrection extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String REFERENCE_OBJECT = "Reference object(s)";


    public WhiteBalanceCorrection(ModuleCollection modules) {
        super("White balance correction", modules);
    }



    static double[] getRGBIntensities(Image image, Obj refObj) {
        // Splitting channels
        ImagePlus[] channels = ChannelSplitter.split(image.getImagePlus());

        // Getting RGB mean intensities
        double redMean = IntensityCalculator.calculate(channels[0].getImageStack(),refObj).getMean();
        double greenMean = IntensityCalculator.calculate(channels[1].getImageStack(),refObj).getMean();
        double blueMean = IntensityCalculator.calculate(channels[2].getImageStack(),refObj).getMean();

        return new double[]{redMean,greenMean,blueMean};

    }

    static void applyWhiteBalanceCorrection(Image inputImage, double[] rgbMeans) {
        ImagePlus ipl = inputImage.getImagePlus();

        // Applying white balance correction
        double rgbMean = (rgbMeans[0] + rgbMeans[1] + rgbMeans[2])/3;

        for (int t=0;t<ipl.getNFrames();t++) {
            for (int z=0;z<ipl.getNSlices();z++) {
                for (int c=0;c<3;c++) {
                    ipl.setPosition(c+1,z+1,t+1);
                    ipl.getProcessor().add(rgbMean - rgbMeans[c]);
                }
            }
        }
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input image and reference object(s)
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String refObjectsName = parameters.getValue(REFERENCE_OBJECT);
        ObjCollection refObjects = workspace.getObjectSet(refObjectsName);

        // Checking input image has 3 channels
        if (inputImage.getImagePlus().getNChannels() != 3) {
            MIA.log.writeWarning("Input image doesn't have 3 channels.  Skipping white balance correction.");
            return Status.PASS;
        }

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImage = new Image(outputImageName,inputImage.getImagePlus().duplicate());}

        // Getting the reference object.  If there is more than 1 object in the collection, use the largest.
        Obj refObj = null;
        if (refObjects.size() == 0) {
            MIA.log.writeWarning("No objects found to use as reference.  Skipping white balance correction.");
            return Status.PASS;
        } else if (refObjects.size() == 1) {
            refObj = refObjects.getFirst();
        } else {
            refObj = refObjects.getLargestObject(-1);
        }

        // Getting mean red, green and blue intensity for object
        double[] rgbMeans = getRGBIntensities(inputImage,refObj);

        // Correcting for the difference between the reference mean for each channel to the mean-of-means.  This
        // correction is applied to all pixels in that channel.
        applyWhiteBalanceCorrection(inputImage,rgbMeans);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) workspace.addImage(inputImage);
        if (showOutput) inputImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image to apply white balance correction to."));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true, "Select if the white balance correction should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "", "Name of the output image created during the correction process.  This image will be added to the workspace."));
        parameters.add(new InputObjectsP(REFERENCE_OBJECT,this, "", "Object to use as background reference.  Relative channel brightness will be corrected against the pixels contained within this object.  If more than one object is present in the object collection the largest object will be used."));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(REFERENCE_OBJECT));

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

    @Override
    public String getDescription() {
        return "Apply whitebalance correction to an image based on a reference region (specified as an object).<br>" +
                "<br>Method based on the <a href=\"https://github.com/pmascalchi/ImageJ_Auto-white-balance-correction\">macro</a> by Patrice Mascalchi ().";
    }
}
