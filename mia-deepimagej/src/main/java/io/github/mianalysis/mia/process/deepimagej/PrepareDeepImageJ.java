package io.github.mianalysis.mia.process.deepimagej;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.SystemUtils;

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
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import io.github.mianalysis.mia.MIA;

/*
 * The following is adapted from
 * https://github.com/deepimagej/deepimagej-plugin/blob/master/src/main/java/
 * deepimagej/DeepImageJ.java (accessed 2022-12-07)
 */
public class PrepareDeepImageJ implements PlugIn {
    private static HashMap<String, DeepImageJ> dps = list(getModelsPath());

    public interface Formats {
        String PYTORCH = "Pytorch";
        String TENSORFLOW = "Tensorflow";

        String[] ALL = new String[] { PYTORCH, TENSORFLOW };

    }

    public static String getModelsPath() {
        if (MIA.isDebug())
            if (SystemUtils.OS_NAME.equals("Mac OS X"))
                return "/Users/sc13967/Applications/Fiji.app/models/";
            else
                // return "C:\\Users\\steph\\Programs\\Fiji.app\\models\\";
                return "C:\\Users\\sc13967\\Downloads\\fiji-win64\\Fiji.app\\models\\";
        else
            return IJ.getDirectory("imagej") + File.separator + "models" + File.separator;
    }

    @Override
    public void run(String arg) {
        ImagePlus imp = IJ.openImage(
                "C:\\Users\\steph\\Documents\\People\\Qiao Tong\\2022-10-06 DL scale segmentation\\TIF\\Test_raw\\Test\\OSX_mCH_4M_D10_REGEN_ALP_S33.tif");
        DeepImageJ model = getModel("my_model");
        new PrepareDeepImageJ().runModel(imp, model, "Tensorflow", false, false, "400,400,1");
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
                    return new String[] { Formats.PYTORCH, Formats.TENSORFLOW };
                else if (dp.params.framework.toLowerCase().equals("pytorch"))
                    return new String[] { Formats.PYTORCH };
                else if (dp.params.framework.toLowerCase().equals("tensorflow"))
                    return new String[] { Formats.TENSORFLOW };
            }

        return new String[0];

    }

    public static String[] getPreprocessings(String modelName) {
        for (DeepImageJ dp : dps.values()) {
            if (dp.getName().equals(modelName)) {
                int count = 0;
                for (String preKey : dp.params.pre.keySet())
                    if (dp.params.pre.get(preKey) != null)
                        count = count + dp.params.pre.get(preKey).length;

                String[] choices = new String[count];
                count = 0;
                for (String preKey : dp.params.pre.keySet())
                    if (dp.params.pre.get(preKey) != null)
                        for (String currPre : dp.params.pre.get(preKey))
                            choices[count++] = currPre;

                return choices;

            }
        }

        return new String[0];

    }

    public static String[] getPostprocessings(String modelName) {
        for (DeepImageJ dp : dps.values()) {
            if (dp.getName().equals(modelName)) {
                int count = 0;
                for (String postKey : dp.params.post.keySet())
                    if (dp.params.post.get(postKey) != null)
                        count = count + dp.params.post.get(postKey).length;

                String[] choices = new String[count];
                count = 0;
                for (String postKey : dp.params.post.keySet())
                    if (dp.params.post.get(postKey) != null)
                        for (String currPost : dp.params.post.get(postKey))
                            choices[count++] = currPost;

                return choices;

            }
        }

        return new String[0];

    }

    public static String getAxes(String modelName) {
        for (DeepImageJ dp : dps.values()) {
            if (dp.getName().equals(modelName)) {
                DijTensor inp = dp.params.inputList.get(0);
                return String.join(",", inp.getWorkingDims(inp.form));
            }
        }

        return "";

    }

    public static String getOptimalPatch(String modelName, ImagePlus ipl) {
        for (DeepImageJ dp : dps.values()) {
            if (dp.getName().equals(modelName)) {
                // Get basic specifications for the input from the yaml
                String tensorForm = dp.params.inputList.get(0).form;
                // Minimum size if it is not fixed, 0s if it is
                int[] tensorMin = dp.params.inputList.get(0).minimum_size;
                // Step if the size is not fixed, 0s if it is
                int[] tensorStep = dp.params.inputList.get(0).step;
                float[] haloSize = ArrayOperations.findTotalPadding(dp.params.inputList.get(0), dp.params.outputList,
                        dp.params.pyramidalNetwork);
                // Get the minimum tile size given by the yaml without batch
                int[] min = DijTensor.getWorkingDimValues(tensorForm, tensorMin);
                // Get the step given by the yaml without batch
                int[] step = DijTensor.getWorkingDimValues(tensorForm, tensorStep);
                // Get the halo given by the yaml without batch
                float[] haloVals = DijTensor.getWorkingDimValues(tensorForm, haloSize);
                // Get the axes given by the yaml without batch
                String[] dim = DijTensor.getWorkingDims(tensorForm);

                return ArrayOperations.optimalPatch(ipl, haloVals, dim, step, min, dp.params.allowPatching);

            }
        }

        return "";

    }

    public static void setTilingInfo(String[] dim, int[] min, int[] step, String optimalPatch) {
        HashMap<String, String> letterDefinition = new HashMap<String, String>();
        letterDefinition.put("X", "width");
        letterDefinition.put("Y", "height");
        letterDefinition.put("C", "channels");
        letterDefinition.put("Z", "depth");
        String infoString = "";
        for (String dd : dim)
            infoString += dd + ": " + letterDefinition.get(dd) + ", ";
        infoString = infoString.substring(0, infoString.length() - 2);

        String minString = "";
        for (int i = 0; i < dim.length; i++)
            minString += dim[i] + "=" + min[i] + ", ";
        minString = minString.substring(0, minString.length() - 2);

        String stepString = "";
        for (int i = 0; i < dim.length; i++)
            stepString += dim[i] + "=" + step[i] + ", ";
        stepString = stepString.substring(0, stepString.length() - 2);

    }

    public Object runModel(ImagePlus imp, DeepImageJ dp, String format, boolean usePreprocessing,
            boolean usePostprocessing,
            String patchString) {
        String loadInfo = "ImageJ";

        // First load Tensorflow
        if (SystemUsage.checkFiji() && format.toLowerCase().contentEquals("tensorflow"))
            loadInfo = StartTensorflowService.loadTfLibrary();
        else if (format.toLowerCase().contentEquals("pytorch"))
            Thread.currentThread().setContextClassLoader(IJ.getClassLoader());

        String cudaVersion = loadInfo.contains("GPU") ? SystemUsage.getCUDAEnvVariables() : "noCUDA";

        // Check if the patch size is editable or not
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

        if (usePreprocessing) {
            String[] preprocessing = getPreprocessings(dp.getName());
            if (preprocessing.length > 0) {
                dp.params.firstPreprocessing = dp.getPath() + File.separator + preprocessing[0];
                if (preprocessing.length > 1)
                    dp.params.secondPreprocessing = dp.getPath() + File.separator + preprocessing[1];
            }
        }

        if (usePostprocessing) {
            String[] postprocessing = getPostprocessings(dp.getName());
            if (postprocessing.length > 0) {
                dp.params.firstPostprocessing = dp.getPath() + File.separator + postprocessing[0];
                if (postprocessing.length > 1)
                    dp.params.secondPostprocessing = dp.getPath() + File.separator + postprocessing[1];
            }
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

        Object outputIpl = calculateImage(imp, rp, service, dp);
        service.shutdown();

        return outputIpl;

    }

    public Object calculateImage(ImagePlus inp, RunnerProgress rp, ExecutorService service, DeepImageJ dp) {
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

        ImagePlus ipl = null;

        // System.out.println("Size "+output.size());
        // System.out.println(output.get("output").getClass());
        // for (String k : output.keySet())
        //     System.out.println("KEY " + k);

        if (output == null || output.size() == 0)
            return null;

        Object firstOutput = output.values().iterator().next();
        if (firstOutput instanceof ResultsTable) {
            ResultsTable rt = (ResultsTable) firstOutput;
            float[] vals = rt.getColumn(0);
            int maxIdx = -1;
            float maxVal = 0;
            for (int idx=0;idx<vals.length;idx++)
                if (vals[idx] > maxVal) {
                    maxIdx = idx;
                    maxVal = vals[idx];
                }
            
            IJ.selectWindow(rt.getTitle());
            IJ.run("Close");

            return new float[]{maxIdx,maxVal};

        }

        Iterator<Object> iter = output.values().iterator();
        int minID = 0;
        while (iter.hasNext()) {
            Object nx = iter.next();
            if (nx != null && nx instanceof ImagePlus && ((ImagePlus) nx).getProcessor() != null) {
                ImagePlus currIpl = (ImagePlus) nx;
                currIpl.hide();
                
                if (currIpl.getID() < minID) {
                    ipl = currIpl;
                    minID = currIpl.getID();
                }
            }
        }

        return ipl.duplicate();

    }
}
