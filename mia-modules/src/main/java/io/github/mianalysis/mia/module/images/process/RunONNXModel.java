// package io.github.mianalysis.mia.module.images.process;

// import java.util.HashMap;
// import java.util.Map;

// import org.scijava.Priority;
// import org.scijava.plugin.Plugin;

// import ai.onnxruntime.NodeInfo;
// import ai.onnxruntime.OnnxTensor;
// import ai.onnxruntime.OrtEnvironment;
// import ai.onnxruntime.OrtException;
// import ai.onnxruntime.OrtSession;
// import ai.onnxruntime.OrtSession.Result;
// import ai.onnxruntime.TensorInfo;
// import ij.IJ;
// import ij.ImagePlus;
// import ij.process.ImageProcessor;
// import io.github.mianalysis.mia.MIA;
// import io.github.mianalysis.mia.module.Categories;
// import io.github.mianalysis.mia.module.Category;
// import io.github.mianalysis.mia.module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.object.Workspace;
// import io.github.mianalysis.mia.object.image.Image;
// import io.github.mianalysis.mia.object.image.ImageFactories;
// import io.github.mianalysis.mia.object.parameters.InputImageP;
// import io.github.mianalysis.mia.object.parameters.OutputImageP;
// import io.github.mianalysis.mia.object.parameters.Parameters;
// import io.github.mianalysis.mia.object.parameters.SeparatorP;
// import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
// import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
// import io.github.mianalysis.mia.object.system.Status;

// @Plugin(type = Module.class, priority = Priority.LOW, visible = true)
// public class RunONNXModel extends Module {

//     public static final String INPUT_SEPARATOR = "Image input/output";

//     public static final String INPUT_IMAGE = "Input image";

//     public static final String OUTPUT_IMAGE = "Output image";

//     public RunONNXModel(Modules modules) {
//         super("Run ONNX model", modules);
//     }

//     @Override
//     public String getVersionNumber() {
//         return "1.0.0";
//     }

//     @Override
//     public String getDescription() {
//         return "";
//     }

//     @Override
//     public Category getCategory() {
//         return Categories.IMAGES_PROCESS;
//     }

//     @Override
//     public Status process(WorkspaceI workspace) {
//         // Getting input image
//         String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
//         Image inputImage = workspace.getImages().get(inputImageName);
//         ImagePlus inputIpl = inputImage.getImagePlus();

//         // Getting parameters
//         String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);

//         String modelPath = "/Users/sc13967/Library/CloudStorage/OneDrive-UniversityofBristol/People/Erika Kague/2025-04-04 RAMAN cell analysis/ONNX models/2025-09-01_4class.onnx.zip";

//         ImagePlus outputIpl;

//         try {
//             OrtEnvironment environment = OrtEnvironment.getEnvironment();

//             OrtSession session = environment.createSession(modelPath);
//             MIA.log.writeDebug("Num inputs: "+session.getNumInputs());
//             MIA.log.writeDebug("Input names: "+session.getInputNames());
//             MIA.log.writeDebug("Num outputs: "+session.getNumOutputs());
//             MIA.log.writeDebug("Output names: "+session.getOutputNames());
            
//             Map<String,NodeInfo> m = session.getInputInfo();
//             for (String s:m.keySet()) {
//                 MIA.log.writeDebug(s+"_"+m.get(s));
//             }
//             String inputName = session.getInputNames().iterator().next();
//             String outputName = session.getOutputNames().iterator().next();

//             int width = inputIpl.getWidth();
//             int height = inputIpl.getHeight();

//             ImageProcessor inputIpr = inputIpl.getProcessor();
//             float[][][][] floatArray = new float[1][width][height][1];
//             for (int x = 0; x < width; x++)
//                 for (int y = 0; y < height; y++)
//                     floatArray[0][x][y][0] = inputIpr.getPixelValue(x, y);

//             OnnxTensor tensor = OnnxTensor.createTensor(environment, floatArray);

//             HashMap<String, OnnxTensor> inputMap = new HashMap<>();
//             inputMap.put(inputName, tensor);

//             Result outputMap = session.run(inputMap);
//             float[][][][] output = (float[][][][]) outputMap.get(0).getValue();

//             long[] shape = ((TensorInfo) session.getOutputInfo().get(outputName).getInfo()).getShape();
//             outputIpl = IJ.createHyperStack(outputName, width, height, (int) shape[3], 1, 1, 32);

//             for (int c = 0; c < shape[3]; c++) {
//                 outputIpl.setC(c + 1);
//                 ImageProcessor outputIpr = outputIpl.getProcessor();
//                 for (int x = 0; x < width; x++)
//                     for (int y = 0; y < height; y++)
//                         outputIpr.setf(x, y, output[0][x][y][c]);
//             }

//         } catch (OrtException e) {
//             MIA.log.writeError(e);
//             return Status.FAIL;
//         }

//         Image outputImage = ImageFactories.getDefaultFactory().create(outputImageName, outputIpl);
//         workspace.addImage(outputImage);

//         // If the image is being saved as a new image, adding it to the workspace
//         if (showOutput)
//             outputImage.show();

//         return Status.PASS;

//     }

//     @Override
//     public void initialiseParameters() {
//         parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
//         parameters.add(new InputImageP(INPUT_IMAGE, this));
//         parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

//     }

//     @Override
//     public Parameters updateAndGetParameters() {
//         return parameters;

//     }

//     @Override
//     public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
//         return null;
//     }

//     @Override
//     public MetadataRefs updateAndGetMetadataReferences() {
//         return null;
//     }

//     @Override
//     public ParentChildRefs updateAndGetParentChildRefs() {
//         return null;
//     }

//     @Override
//     public PartnerRefs updateAndGetPartnerRefs() {
//         return null;
//     }

//     @Override
//     public boolean verify() {
//         return true;
//     }
// }
