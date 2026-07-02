package io.github.mianalysis.mia.module.images.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.thirdparty.Fit_Polynomial;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FitPolynomial extends Module {

    public static final String INPUT_SEPARATOR = "Image input/output";

    public static final String INPUT_IMAGE = "Input image";

    public static final String OUTPUT_IMAGE = "Output image";

    public static final String FIT_SEPARATOR = "Polynomial fitting controls";

    public static final String X_DIRECTION_ORDER = "X-direction order";

    public static final String Y_DIRECTION_ORDER = "Y-direction order";

    public static final String XY_DIRECTION_ORDER = "Mixed XY order";

    public static final String FIT_WITHIN_MASK = "Fit within mask";

    public static final String MASK_OBJECTS = "Mask objects";


    public FitPolynomial(Modules modules) {
        super("Fit polynomial", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Fits a polynomial to an image and outputs this as a new image.  This module uses the \"Fit polynomial\" plugin (version 2007-Jul-13) by Michael Schmid (https://imagejdocu.list.lu/plugin/filter/fit_polynomial/start)";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        int xOrder = parameters.getValue(X_DIRECTION_ORDER, workspace);
        int yOrder = parameters.getValue(Y_DIRECTION_ORDER, workspace);
        int xyOrder = parameters.getValue(XY_DIRECTION_ORDER, workspace);
        boolean fitWithinMask = parameters.getValue(FIT_WITHIN_MASK, workspace);
        String maskObjectsName = parameters.getValue(MASK_OBJECTS, workspace);
                
        // Getting input image and objects
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputIpl = inputImage.getImagePlus();
        Objs maskObjects = fitWithinMask ? workspace.getObjects(maskObjectsName) : null;
        Obj maskObject = fitWithinMask ? maskObjects.getAsSingleObject() : null;
        
        // Creating output image
        ImagePlus outputIpl = inputIpl.duplicate();
        if (outputIpl.getBitDepth() != 32)
            ImageTypeConverter.process(outputIpl, 32, ImageTypeConverter.ScalingModes.CLIP);

        // Running the correction
        Fit_Polynomial fitPolynomial = new Fit_Polynomial();
        Fit_Polynomial.xOrder = xOrder;
        Fit_Polynomial.yOrder = yOrder;
        Fit_Polynomial.xyOrder = xyOrder;
        Fit_Polynomial.outputFit = true;

        ImageStack outputIst = outputIpl.getImageStack();
        for (int i=0;i<outputIst.getSize();i++) {
            ImageProcessor ipr = outputIst.getProcessor(i+1);
            
            if (fitWithinMask) {
                int[] pos = outputIpl.convertIndexToPosition(i+1);
                Roi roi = maskObjects.getAsSingleObject().getRoi(pos[1]-1);
                ipr.setRoi(roi);
            }

            fitPolynomial.run(ipr);

        }
        
        Image outputImage = ImageFactory.createImage(outputImageName, outputIpl);
        workspace.addImage(outputImage);

        if (showOutput)
            outputImage.show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(FIT_SEPARATOR, this));
        parameters.add(new IntegerP(X_DIRECTION_ORDER, this, 1));
        parameters.add(new IntegerP(Y_DIRECTION_ORDER, this, 1));
        parameters.add(new IntegerP(XY_DIRECTION_ORDER, this, 0));
        parameters.add(new BooleanP(FIT_WITHIN_MASK, this, false));
        parameters.add(new InputObjectsP(MASK_OBJECTS, this));
    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(FIT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(X_DIRECTION_ORDER));
        returnedParameters.add(parameters.getParameter(Y_DIRECTION_ORDER));
        returnedParameters.add(parameters.getParameter(XY_DIRECTION_ORDER));
        returnedParameters.add(parameters.getParameter(FIT_WITHIN_MASK));

        if ((Boolean) parameters.getValue(FIT_WITHIN_MASK, null))
            returnedParameters.add(parameters.getParameter(MASK_OBJECTS));

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
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
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
        return true;
    }
}
