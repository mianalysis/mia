package wbif.sjx.MIA.Process.AnalysisHandling;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Process.Logging.LogRenderer;

/**
 * Created by sc13967 on 21/10/2016.
 */
public class Analysis {
    private ModuleCollection modules = new ModuleCollection();
    private boolean shutdown = false;
    private String analysisFilename = "";

    // CONSTRUCTOR

    public Analysis() {
        initialise();

    }


    // PUBLIC METHODS

    /*
     * Initialisation method is where workspace is populated with modules and module-specific parameters.
     */
    public void initialise() {}

    /*
     * The method that gets called by the AnalysisRunner.  This shouldn't have any user interaction elements
     * @param workspace Workspace containing stores for images and objects
     */
    public boolean execute(Workspace workspace) {
        MIA.log.writeDebug("Processing file \""+workspace.getMetadata().getFile().getAbsolutePath()+"\"");

        // Running through modules
        int total = modules.size();
        int count = 0;
        boolean status = true;
        for (Module module:modules) {
            if (Thread.currentThread().isInterrupted()) break;
            if (status && module.isEnabled() && module.isRunnable()) {
                status = module.execute(workspace);
                if (!status) {
                    workspace.setAnalysisFailed(true);

                    // The module failed or requested analysis termination.  Add this message to the log
                    MIA.log.writeWarning("Analysis terminated early for file \""+workspace.getMetadata().getFile()+
                            "\" (series "+workspace.getMetadata().getSeriesNumber()+") by module \""+module.getName()+
                            "\" (\""+module.getNickname()+"\").");
                }
            }

            // Updating progress bar
            double fractionComplete = ((double) ++count)/((double) total);
            workspace.setProgress(fractionComplete);
            if (!MIA.isHeadless()) GUI.updateProgressBar();

        }

        // We're only interested in the measurements now, so clearing images and object coordinates
        workspace.clearAllImages(true);
        workspace.clearAllObjects(true);

        // If enabled, write the current memory usage to the console
        if (MIA.getMainRenderer().isWriteEnabled(LogRenderer.Level.MEMORY)) {
            double totalMemory = Runtime.getRuntime().totalMemory();
            double usedMemory = totalMemory - Runtime.getRuntime().freeMemory();
            String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

            DecimalFormat df = new DecimalFormat("#.0");

            String memoryMessage = df.format(usedMemory*1E-6)+" MB of "+df.format(totalMemory*1E-6)+" MB" +
                    ", analysis complete"+
                    ", file \""+workspace.getMetadata().getFile() +
                    ", time "+dateTime;

            MIA.log.write(memoryMessage, LogRenderer.Level.MEMORY);


        }

        return true;

    }

    public ModuleCollection getModules() {
        return modules;

    }

    public void setModules(ModuleCollection modules) {
        this.modules = modules;
    }

    public void removeAllModules() {
        modules.clear();

    }

    public void shutdown() {
        this.shutdown = true;

    }

    public String getAnalysisFilename() {
        return analysisFilename;
    }

    public void setAnalysisFilename(String analysisFilename) {
        this.analysisFilename = analysisFilename;
    }

    public boolean hasVisibleParameters() {
        return (modules.hasVisibleParameters());
    }
}
