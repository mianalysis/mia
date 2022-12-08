package io.github.mianalysis.mia.process.deepimagej;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import deepimagej.DeepImageJ;
import deepimagej.DeepLearningModel;
import deepimagej.RunnerProgress;
import deepimagej.RunnerPt;
import deepimagej.RunnerTf;
import deepimagej.tools.ArrayOperations;
import deepimagej.tools.DijRunnerPostprocessing;
import deepimagej.tools.DijRunnerPreprocessing;
import deepimagej.tools.DijTensor;
import deepimagej.tools.Log;
import deepimagej.tools.ModelLoader;
import deepimagej.tools.StartTensorflowService;
import deepimagej.tools.SystemUsage;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

/*
 * The following is adapted from
 * https://github.com/deepimagej/deepimagej-plugin/blob/master/src/main/java/
 * deepimagej/DeepImageJ.java (accessed 2022-12-07)
 */
public class PrepareDeepImageJ implements PlugIn {
    // private static String path = IJ.getDirectory("imagej") + File.separator +
    // "models" + File.separator;
    private static String path = "C:\\Users\\steph\\Programs\\Fiji.app\\models\\";
    private static HashMap<String, DeepImageJ> dps = list(path);

    public static void main(String[] args) {
        ImagePlus imp = IJ.openImage(
                "C:\\Users\\steph\\Documents\\People\\Qiao Tong\\2022-10-06 DL scale segmentation\\TIF\\Test_raw\\Test\\OSX_mCH_4M_D10_REGEN_ALP_S33.tif");
        DeepImageJ model = getModel("my_model");
        ImagePlus outputIpl = new PrepareDeepImageJ().runModel(imp, model, "Tensorflow", "no preprocessing",
                "no postprocessing", "X,Y,C", "400,400,1");
        outputIpl.duplicate().show();

    }

    @Override
    public void run(String arg) {
        ImagePlus imp = IJ.openImage(
                "C:\\Users\\steph\\Documents\\People\\Qiao Tong\\2022-10-06 DL scale segmentation\\TIF\\Test_raw\\Test\\OSX_mCH_4M_D10_REGEN_ALP_S33.tif");
        DeepImageJ model = getModel("my_model");
        new PrepareDeepImageJ().runModel(imp, model, "Tensorflow", "no preprocessing", "no postprocessing",
                "X,Y,C", "400,400,1");
    }

    public static HashMap<String, DeepImageJ> list(String modelDir) {
        HashMap<String, DeepImageJ> list = new HashMap<String, DeepImageJ>();
        File[] dirs = new File(modelDir).listFiles();
        if (dirs == null) {
            System.err.println("No models found at: " + System.lineSeparator() + " - " + modelDir);
            return list;
        }

        for (File dir : dirs) {
            if (dir.isDirectory()) {
                DeepImageJ dp = new DeepImageJ(modelDir + File.separator, dir.getName(), false);
                if (dp.getValid() && dp.params != null)
                    list.put(dp.dirname, dp);
            }
        }

        return list;

    }

    public static String[] getAvailableModels() {
        String[] all = new String[dps.size()];

        int i = 0;
        for (DeepImageJ dij : dps.values())
            all[i++] = dij.getName();

        return all;

    }

    public static DeepImageJ getModel(String modelName) {
        for (DeepImageJ dp : dps.values())
            if (dp.getName().equals(modelName))
                return dp;

        return null;

    }

    public static String[] getFormats(String modelName) {
        for (DeepImageJ dp : dps.values())
            if (dp.getName().equals(modelName)) {
                if (dp.params.framework.toLowerCase().equals("tensorflow/pytorch"))
                    return new String[] { "Pytorch", "Tensorflow" };
                else if (dp.params.framework.toLowerCase().equals("pytorch"))
                    return new String[] { "Pytorch" };
                else if (dp.params.framework.toLowerCase().equals("tensorflow"))
                    return new String[] { "Tensorflow" };
            }

        return new String[0];
        
    }

    public ImagePlus runModel(ImagePlus imp, DeepImageJ dp, String format, String preprocessing, String postprocessing,
            String axes, String patchString) {
        String loadInfo = "ImageJ";

        // First load Tensorflow
        if (SystemUsage.checkFiji() && format.toLowerCase().contentEquals("tensorflow"))
            loadInfo = StartTensorflowService.loadTfLibrary();
        else if (format.toLowerCase().contentEquals("pytorch"))
            Thread.currentThread().setContextClassLoader(IJ.getClassLoader());

        String cudaVersion = loadInfo.contains("GPU") ? SystemUsage.getCUDAEnvVariables() : "noCUDA";

        // Get the arguments for the model execution
        String[] processingFile = new String[2];
        processingFile[0] = preprocessing;
        processingFile[1] = postprocessing;

        // Check if the patxh size is editable or not
        dp.params.framework = format.toLowerCase().contains("pytorch") ? "pytorch" : "tensorflow";
        // Select the needed attachments for the version used
        if (dp.params.framework.toLowerCase().contentEquals("pytorch"))
            dp.params.attachments = dp.params.ptAttachments;
        else if (dp.params.framework.toLowerCase().contentEquals("tensorflow"))
            dp.params.attachments = dp.params.tfAttachments;

        dp.params.firstPreprocessing = null;
        dp.params.secondPreprocessing = null;
        dp.params.firstPostprocessing = null;
        dp.params.secondPostprocessing = null;

        if (!processingFile[0].equals("no preprocessing")) {
            if (!processingFile[0].startsWith("["))
                processingFile[0] = "[" + processingFile[0];
            if (!processingFile[0].endsWith("]"))
                processingFile[0] = processingFile[0] + "]";
            String[] preprocArray = processingFile[0]
                    .substring(processingFile[0].indexOf("[") + 1, processingFile[0].lastIndexOf("]")).split(",");
            dp.params.firstPreprocessing = dp.getPath() + File.separator + preprocArray[0].trim();
            if (preprocArray.length > 1)
                dp.params.secondPreprocessing = dp.getPath() + File.separator + preprocArray[1].trim();
        }

        if (!processingFile[1].equals("no postprocessing")) {
            if (!processingFile[1].startsWith("["))
                processingFile[1] = "[" + processingFile[1];
            if (!processingFile[1].endsWith("]"))
                processingFile[1] = processingFile[1] + "]";
            String[] postprocArray = processingFile[1]
                    .substring(processingFile[1].indexOf("[") + 1, processingFile[1].lastIndexOf("]")).split(",");
            dp.params.firstPostprocessing = dp.getPath() + File.separator + postprocArray[0].trim();
            if (postprocArray.length > 1)
                dp.params.secondPostprocessing = dp.getPath() + File.separator + postprocArray[1].trim();
        }

        int[] patch = null;
        for (DijTensor inp : dp.params.inputList) {
            String tensorForm = inp.form;
            int[] tensorStep = inp.step;
            int[] step = DijTensor.getWorkingDimValues(tensorForm, tensorStep);
            String[] dims = DijTensor.getWorkingDims(tensorForm);

            float[] haloSize = ArrayOperations.findTotalPadding(inp, dp.params.outputList, dp.params.pyramidalNetwork);
            // haloSize is null if any of the offset definitions of the outputs is not a
            // multiple of 0.5
            if (haloSize == null) {
                System.err.println("The rdf.yaml of this model contains an error at 'outputs>shape>offset'.\n"
                        + "The output offsets defined in the rdf.yaml should be multiples of 0.5.\n"
                        + " If not, the outputs defined will not have a round number of pixels, which\n"
                        + "is impossible.");
                return null;
            }

            patch = ArrayOperations.getPatchSize(dims, inp.form, patchString, false);
            if (patch == null) {
                System.err.println("Please, introduce the patch size as integers separated by commas.\n"
                        + "For the axes order 'Y,X,C' with:\n"
                        + "Y=256, X=256 and C=1, we need to introduce:\n"
                        + "'256,256,1'\n"
                        + "Note: the key 'auto' can only be used by the plugin.");
                return null;
            }

            for (int i = 0; i < patch.length; i++) {
                if (haloSize[i] * 2 >= patch[i] && patch[i] != -1) {
                    System.err.println(
                            "Error: Tiles cannot be smaller or equal than 2 times the halo at any dimension.\n"
                                    + "Please, either choose a bigger tile size or change the halo in the rdf.yaml.");
                    return null;
                }
            }

            for (int i = 0; i < inp.minimum_size.length; i++) {
                if (inp.step[i] != 0 && (patch[i] - inp.minimum_size[i]) % inp.step[i] != 0 && patch[i] != -1
                        && dp.params.allowPatching) {
                    int approxTileSize = ((patch[i] - inp.minimum_size[i]) / inp.step[i]) * inp.step[i]
                            + inp.minimum_size[i];
                    System.err.println("Tile size at dim: " + tensorForm.split("")[i] + " should be product of:\n  "
                            + inp.minimum_size[i] +
                            " + " + step[i] + "*N, where N can be any integer >= 0.\n"
                            + "The immediately smaller valid tile size is " + approxTileSize);
                    return null;
                } else if (inp.step[i] == 0 && patch[i] != inp.minimum_size[i]) {
                    System.err.println(
                            "Patch size at dim: " + tensorForm.split("")[i] + " should be " + inp.minimum_size[i]);
                    return null;
                }
            }
        }
        dp.params.inputList.get(0).recommended_patch = patch;

        ExecutorService service = Executors.newFixedThreadPool(1);

        RunnerProgress rp = null;
        boolean iscuda = DeepLearningModel.TensorflowCUDACompatibility(loadInfo, cudaVersion).equals("");
        ModelLoader loadModel = new ModelLoader(dp, rp, loadInfo.contains("GPU"), iscuda, true,
                SystemUsage.checkFiji());

        Future<Boolean> f1 = service.submit(loadModel);
        boolean output = false;
        try {
            output = f1.get();
        } catch (InterruptedException | ExecutionException e) {
            if (rp != null && rp.getUnzipping())
                System.err.println("Unable to unzip model");
            else
                System.err.println("Unable to load model");
            e.printStackTrace();
            if (rp != null)
                rp.stop();
        }

        // If the user has pressed stop button, stop execution and return
        if (rp != null && rp.isStopped()) {
            service.shutdown();
            rp.dispose();
            return null;
        }

        // If the model was not loaded, run again the plugin
        if (!output) {
            IJ.error("Load model error: " + (dp.getTfModel() == null || dp.getTorchModel() == null));
            service.shutdown();
            return null;
        }

        if (rp != null)
            rp.setService(null);

        ImagePlus outputIpl = calculateImage(imp, rp, service, dp);
        service.shutdown();

        return outputIpl;

    }

    public ImagePlus calculateImage(ImagePlus inp, RunnerProgress rp, ExecutorService service, DeepImageJ dp) {
        int runStage = 0;
        Log log = new Log();
        HashMap<String, Object> output = null;

        try {
            // Name of the image to be processed
            String imTitle = inp.getTitle();
            DijRunnerPreprocessing preprocess = new DijRunnerPreprocessing(dp, rp, inp, true, false);
            Future<HashMap<String, Object>> f0 = service.submit(preprocess);
            HashMap<String, Object> inputsMap = f0.get();

            if ((rp != null && rp.isStopped()) || inputsMap == null) {
                // Remove possible hidden images from IJ workspace
                ArrayOperations.removeProcessedInputsFromMemory(inputsMap);
                service.shutdown();
                if (rp != null)
                    rp.dispose();
                return null;
            }

            runStage++;

            if (dp.params.framework.equals("tensorflow")) {
                RunnerTf runner = new RunnerTf(dp, rp, inputsMap, log);
                if (rp != null)
                    rp.setRunner(runner);
                Future<HashMap<String, Object>> f1 = service.submit(runner);
                output = f1.get();
            } else {
                RunnerPt runner = new RunnerPt(dp, rp, inputsMap, log);
                if (rp != null)
                    rp.setRunner(runner);
                Future<HashMap<String, Object>> f1 = service.submit(runner);
                output = f1.get();
            }

            inp.changes = false;
            inp.close();

            if (output == null || (rp != null && rp.isStopped())) {
                // Remove possible hidden images from IJ workspace
                ArrayOperations.removeProcessedInputsFromMemory(inputsMap);
                if (rp != null) {
                    rp.allowStopping(true);
                    rp.stop();
                    rp.dispose();
                }
                service.shutdown();
                return null;
            }
            runStage++;

            Future<HashMap<String, Object>> f2 = service.submit(new DijRunnerPostprocessing(dp, rp, output));
            output = f2.get();

            if (rp != null) {
                rp.allowStopping(true);
                rp.stop();
                rp.dispose();
            }

            // Remove possible hidden images from IJ workspace
            ArrayOperations.removeProcessedInputsFromMemory(inputsMap, imTitle, true);

        } catch (IllegalStateException ex) {
            IJ.error("Error during the application of the model.\n"
                    + "Pytorch native library not found.");
            ex.printStackTrace();
        } catch (InterruptedException | ExecutionException ex) {
            IJ.error("Error during the application of the model.");
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            if (runStage == 0)
                IJ.error("Error during preprocessing.");
            else if (runStage == 1)
                IJ.error("Error during the application of the model.");
            else if (runStage == 2)
                IJ.error("Error during postprocessing.");
        }

        // Close the parallel processes
        service.shutdown();
        if (rp != null && !rp.isStopped()) {
            rp.allowStopping(true);
            rp.stop();
            rp.dispose();
        }

        if (output.size() > 1)
            System.err.println("More than 1 image output by network.  This behaviour needs handling.");

        ImagePlus ipl = (ImagePlus) output.values().iterator().next();
        if (ipl.isVisible())
            ipl.hide();

        return ipl;

    }
}
