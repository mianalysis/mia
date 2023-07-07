// // NOTES:
// // Engines are stored in Fiji at Fiji.app/engines/

// package io.github.mianalysis.mia.module.images.process;

// import java.io.BufferedReader;
// import java.io.File;
// import java.io.FileNotFoundException;
// import java.io.FileReader;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;

// import org.scijava.Priority;
// import org.scijava.plugin.Plugin;
// import org.yaml.snakeyaml.LoaderOptions;
// import org.yaml.snakeyaml.Yaml;

// import deepimagej.ImagePlus2TensorNew;
// import ij.ImagePlus;
// import io.bioimage.modelrunner.bioimageio.BioimageioRepo;
// import io.bioimage.modelrunner.engine.installation.EngineManagement;
// import io.bioimage.modelrunner.model.Model;
// import io.bioimage.modelrunner.tensor.Tensor;
// import io.github.mianalysis.mia.MIA;
// import io.github.mianalysis.mia.module.Categories;
// import io.github.mianalysis.mia.module.Category;
// import io.github.mianalysis.mia.module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.object.Workspace;
// import io.github.mianalysis.mia.object.image.Image;
// import io.github.mianalysis.mia.object.image.ImageFactory;
// import io.github.mianalysis.mia.object.parameters.ChoiceP;
// import io.github.mianalysis.mia.object.parameters.InputImageP;
// import io.github.mianalysis.mia.object.parameters.OutputImageP;
// import io.github.mianalysis.mia.object.parameters.Parameters;
// import io.github.mianalysis.mia.object.parameters.SeparatorP;
// import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
// import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
// import io.github.mianalysis.mia.object.system.Status;
// import net.imagej.ImgPlus;
// import net.imglib2.RandomAccessibleInterval;
// import net.imglib2.type.numeric.real.FloatType;

// @Plugin(type = Module.class, priority = Priority.LOW, visible = true)
// public class ApplyDeepLearningModel extends Module {
//     public static final String INPUT_SEPARATOR = "Image input/output";
//     public static final String INPUT_IMAGE = "Input image";
//     public static final String OUTPUT_IMAGE = "Output image";

//     public static final String MODEL_SEPARATOR = "Model controls";
//     public static final String MODEL = "Model";

//     private static String enginesDir = "/Users/sc13967/Applications/Fiji.app/engines";
//     private static String modelsDir = "/Users/sc13967/Applications/Fiji.app/models/";

//     private static HashMap<String, String> modelPaths = getModelPaths(modelsDir);

//     // public static void main(String[] args) {

//     //     String modelPath = modelPaths.values().iterator().next();
//     //     try {
//     //         // Ensuring the correct engine is installed
//     //         boolean ins = EngineManagement.installEnginesinDirForModelInFolder(modelPath, enginesDir);
//     //         System.out.println(ins);
//     //         Model model = Model.createBioimageioModel(modelPath, enginesDir);
//     //     } catch (Exception e) {
//     //         e.printStackTrace();
//     //     }

//     // }

//     public interface Models {
//         String[] ALL = modelPaths.keySet().toArray(new String[modelPaths.size()]);
//     }

//     public ApplyDeepLearningModel(Modules modules) {
//         super("Apply deep learning model", modules);
//     }

//     @Override
//     public Category getCategory() {
//         return Categories.IMAGES_PROCESS;
//     }

//     public static HashMap<String, String> getModelPaths(String modelsDir) {
//         HashMap<String, String> modelPaths = new HashMap<>();

//         File[] dirs = new File(modelsDir).listFiles();

//         // Check there are models present
//         if (dirs == null) {
//             MIA.log.writeWarning("No models found at: " + System.lineSeparator() + " - " + modelsDir);
//             return modelPaths;
//         }

//         for (File dir : dirs) {
//             if (dir.isDirectory()) {
//                 File yamlFile = new File(dir + File.separator + "rdf.yaml");
//                 if (!yamlFile.exists())
//                     continue;

//                 LoaderOptions options = new LoaderOptions();
//                 Yaml yaml = new Yaml(options);
//                 try {
//                     HashMap<String, String> yamlString = yaml.load(new BufferedReader(new FileReader(yamlFile)));
//                     modelPaths.put(yamlString.get("name"), dir.getAbsolutePath());
//                 } catch (FileNotFoundException e) {
//                     MIA.log.writeWarning("Model file \"" + dir + "\"could not be read.  Skipping model.");
//                     continue;
//                 }
//             }
//         }

//         return modelPaths;

//     }

//     @Override
//     protected Status process(Workspace workspace) {
//         // Getting parameters
//         String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
//         String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
//         String modelName = parameters.getValue(MODEL, workspace);

//         // Get input image
//         Image inputImage = workspace.getImage(inputImageName);
//         ImgPlus img = inputImage.getImgPlus();

//         String modelPath = modelPaths.values().iterator().next();
//         try {
//             // It's necessary to connect to BioimageioRepo, else we get exceptions
//             BioimageioRepo.connect();
//             EngineManagement.installEnginesinDirForModelInFolder(modelPath, enginesDir);

//             // Creating the model
//             Model model = Model.createBioimageioModel(modelPath, enginesDir);

//             RandomAccessibleInterval<FloatType> inputRAI = ImagePlus2TensorNew.imPlus2tensor(inputImage.getImagePlus(),
//                     "bcyx");
//             Tensor inputTensor = Tensor.build("input0", "bcyx", inputRAI);
//             Tensor outputTensor = Tensor.buildEmptyTensor("output0", "bcyx");

//             List<Tensor<?>> inputTensors = new ArrayList<Tensor<?>>();
//             List<Tensor<?>> outputTensors = new ArrayList<Tensor<?>>();
//             inputTensors.add(inputTensor);
//             outputTensors.add(outputTensor);
//             model.runModel(inputTensors, outputTensors);

//             ImagePlus iplOut = ImagePlus2TensorNew.tensor2ImagePlus(outputTensor.getData(), "bcyx");

//             // Storing output image
//             Image outputImage = ImageFactory.createImage(outputImageName, iplOut);
//             workspace.addImage(outputImage);

//             if (showOutput)
//                 outputImage.show();

//         } catch (Exception e) {
//             e.printStackTrace();
//         }

//         return Status.PASS;

//     }

//     @Override
//     protected void initialiseParameters() {
//         parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
//         parameters.add(new InputImageP(INPUT_IMAGE, this));
//         parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

//         parameters.add(new SeparatorP(MODEL_SEPARATOR, this));
//         parameters.add(new ChoiceP(MODEL, this, "", Models.ALL));

//         addParameterDescriptions();

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

//     @Override
//     public String getDescription() {
//         return "Uses <a href=\"https://deepimagej.github.io/deepimagej/\">DeepImageJ</a> to run Tensorflow and Pytorch models from the <a href=\"https://bioimage.io/#/\">BioImage Model Zoo</a>.  This module will detect and run any models already installed in the active copy of Fiji.";
//     }

//     protected void addParameterDescriptions() {
//         parameters.get(INPUT_IMAGE).setDescription("Image from the workspace to apply deep learning model to.");
//         parameters.get(OUTPUT_IMAGE).setDescription(
//                 "Final image generated by model, which will be stored in the workspace with this name.");
//         parameters.get(MODEL).setDescription(
//                 "Model to apply to input image.  This can be any model currently installed in MIA.  When using MIA's GUI, the available modules will automatically appear as options.");

//     }
// }

// // In attendance: Evelyn and Ada