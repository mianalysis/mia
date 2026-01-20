package io.github.mianalysis.mia.module.images.process;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.Result;
import ai.onnxruntime.TensorInfo;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class RunONNXModel extends Module {

    public static final String INPUT_SEPARATOR = "Image input";

    public static final String INPUT_IMAGE = "Input image";

    public static final String INPUT_NOTE = "Input note";

    public static final String OUTPUT_SEPARATOR = "Image output";

    public static final String OUTPUT_IMAGE = "Output image";

    public static final String MODEL_SEPARATOR = "Model controls";

    public static final String MODEL_PATH = "Model path";

    public RunONNXModel(Modules modules) {
        super("Run ONNX model", modules);
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    public static OnnxTensor getInputTensor(OrtEnvironment environment, OrtSession session, Image inputImage)
            throws OrtException {
        ImagePlus inputIpl = inputImage.getImagePlus();

        TensorInfo inputInfo = (TensorInfo) session.getInputInfo().get("input").getInfo();
        long[] inputShape = inputInfo.getShape();
        int inputChannels = (int) inputShape[1];
        int inputWidth = (int) inputShape[2];
        int inputHeight = (int) inputShape[3];

        float[][][][] floatArray = new float[1][inputChannels][inputWidth][inputHeight];
        for (int c = 0; c < inputChannels; c++) {
            inputIpl.setC(c + 1);
            ImageProcessor inputIpr = inputIpl.getProcessor();
            for (int x = 0; x < inputWidth; x++)
                for (int y = 0; y < inputHeight; y++)
                    floatArray[0][0][x][y] = inputIpr.getPixelValue(x, y);
        }

        return OnnxTensor.createTensor(environment, floatArray);

    }

    public static Image getOutputImage(OrtSession session, Result outputMap, String outputImageName) throws OrtException {
        TensorInfo outputInfo = (TensorInfo) session.getOutputInfo().get("target").getInfo();
        long[] outputShape = outputInfo.getShape();
        int outputChannels = (int) outputShape[1];
        int outputWidth = (int) outputShape[2];
        int outputHeight = (int) outputShape[3];

        float[][][][] output = (float[][][][]) outputMap.get(0).getValue();

        ImagePlus outputIpl = IJ.createHyperStack(outputImageName, outputWidth, outputHeight, outputChannels, 1, 1,
                32);

        for (int c = 0; c < outputChannels; c++) {
            outputIpl.setC(c + 1);
            ImageProcessor outputIpr = outputIpl.getProcessor();
            for (int x = 0; x < outputWidth; x++)
                for (int y = 0; y < outputHeight; y++)
                    outputIpr.setf(x, y, output[0][c][x][y]);
        }

        return ImageFactory.createImage(outputImageName, outputIpl);

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String modelPath = parameters.getValue(MODEL_PATH, workspace);

        // Getting input image
        Image inputImage = workspace.getImages().get(inputImageName);

        try {
            OrtEnvironment environment = OrtEnvironment.getEnvironment();
            OrtSession session = environment.createSession(modelPath);

            // Preparing input data
            String inputName = session.getInputNames().iterator().next();
            OnnxTensor inputTensor = getInputTensor(environment, session, inputImage);

            // Running model
            HashMap<String, OnnxTensor> inputMap = new HashMap<>();
            inputMap.put(inputName, inputTensor);
            Result outputMap = session.run(inputMap);

            // Preparing output data
            Image outputImage = getOutputImage(session, outputMap, outputImageName);
            workspace.addImage(outputImage);

            // If the image is being saved as a new image, adding it to the workspace
            if (showOutput)
                outputImage.show();

            return Status.PASS;

        } catch (OrtException e) {
            MIA.log.writeError(e);
            return Status.FAIL;
        }

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new MessageP(INPUT_NOTE, this, "Input images are expected to be 32-bit with values in the range 0-1.", ParameterState.MESSAGE));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(MODEL_SEPARATOR, this));
        parameters.add(new FilePathP(MODEL_PATH, this));
        

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;

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
