// TODO: Add methods for XLS and JSON data export

package wbif.sjx.MIA.Process;

import ij.Prefs;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.ProgressMonitor;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.WorkspaceCollection;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;
import wbif.sjx.MIA.Process.Exporting.Exporter;
import wbif.sjx.common.System.FileCrawler;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by sc13967 on 21/10/2016.
 */
public class BatchProcessor extends FileCrawler {
    DecimalFormat dfInt = new DecimalFormat("0");
    DecimalFormat dfDec = new DecimalFormat("0.00");

    private boolean verbose = true;
    private int nThreads = Runtime.getRuntime().availableProcessors()/2;

    private ThreadPoolExecutor pool;

    private boolean shutdownEarly;
    private int counter = 0;
    private int origThreads = Prefs.getThreads();


    // CONSTRUCTORS

    public BatchProcessor(File rootFolder) {
        super(rootFolder);
    }


    // PUBLIC METHODS

    public void run(Analysis analysis, Exporter exporter) throws IOException, InterruptedException {
        MIA.log.clearLog();

        shutdownEarly = false;

        OutputControl outputControl = analysis.getModules().getOutputControl();
        String exportMode = ((ChoiceP) outputControl.getParameter(OutputControl.EXPORT_MODE)).getChoice();
        WorkspaceCollection workspaces = new WorkspaceCollection();

        // Running the analysis based on whether it's a single file or a folder
        if (rootFolder.getFolderAsFile().isFile()) runSingleFile(workspaces, analysis, exporter);
        else runFolder(workspaces, analysis, exporter);

        // Saving the results
        if (shutdownEarly || exporter == null) return;

        GUI.setProgress(0);

    }

    private void runSingleFile(WorkspaceCollection workspaces, Analysis analysis, Exporter exporter) throws InterruptedException, IOException {
        // Setting up the ExecutorService, which will manage the threads
        pool = new ThreadPoolExecutor(1,1,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        // For the current file, determining how many series to processAutomatic (and which ones)
        InputControl inputControl = analysis.getModules().getInputControl();
        OutputControl outputControl = analysis.getModules().getOutputControl();
        TreeMap<Integer,String> seriesNumbers = inputControl.getSeriesNumbers(rootFolder.getFolderAsFile());

        // Only set verbose if a single series is being processed
        Module.setVerbose(seriesNumbers.size() == 1);

        // Iterating over all series to analyse, adding each one as a new workspace
        for (int seriesNumber:seriesNumbers.keySet()) {
            Workspace workspace = workspaces.getNewWorkspace(rootFolder.getFolderAsFile(),seriesNumber);

            // Setting a reference to the analysis (this may be wanted by certain modules to check input/output options)
            String seriesName = seriesNumbers.get(seriesNumber);
            if (seriesName.equals("")) seriesName = "FILE: "+rootFolder.getFolderAsFile().getName();
            workspace.getMetadata().setSeriesName(seriesName);

            // Adding this Workspace to the Progress monitor
            ProgressMonitor.setWorkspaceProgress(workspace,0d);

            Runnable task = () -> {
                try {
                    analysis.execute(workspace);
                } catch (Throwable t) {
                    t.printStackTrace(System.err);
                }

                // Getting the number of completed and total tasks
                incrementCounter();
                int nComplete = getCounter();
                double nTotal = pool.getTaskCount();
                double percentageComplete = (nComplete / nTotal) * 100;

                // Displaying the current progress
                String string = "Completed "+ dfInt.format(nComplete) + "/" + dfInt.format(nTotal)
                        + " (" + dfDec.format(percentageComplete) + "%)";
                System.out.println(string);

                // Exporting to Excel for this file
                if (outputControl.isExportIndividual() && exporter != null) {
                    String name = outputControl.getIndividualOutputPath(workspace.getMetadata());
                    try {
                        exporter.exportResults(workspace,analysis,name);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    workspace.clearAllObjects(false);
                    workspace.clearAllImages(false);}
            };

            // Submit the jobs for this file, then tell the pool not to accept any more jobs and to wait until all
            // queued jobs have completed
            pool.submit(task);

            // Displaying the current progress
            double nTotal = pool.getTaskCount();
            String string = "Started processing "+dfInt.format(nTotal)+" jobs";
            System.out.println(string);

        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        // Exporting to Excel for WorkspaceCollection
        if ((outputControl.isExportAllTogether() || outputControl.isExportGroupedByMetadata()) && exporter != null) {
            String name = outputControl.getGroupOutputPath(inputControl.getParameterValue(InputControl.INPUT_PATH));
            exporter.export(workspaces,analysis,name);
        }
    }

    private void runFolder(WorkspaceCollection workspaces, Analysis analysis, Exporter exporter) throws InterruptedException, IOException {
        File next = getNextValidFileInStructure();
        InputControl inputControl = analysis.getModules().getInputControl();
        OutputControl outputControl = analysis.getModules().getOutputControl();

        boolean continuousExport = outputControl.getParameterValue(OutputControl.CONTINUOUS_DATA_EXPORT);
        int saveNFiles = outputControl.getParameterValue(OutputControl.SAVE_EVERY_N);
        String exportMode = outputControl.getParameterValue(OutputControl.EXPORT_MODE);

        Module.setVerbose(false);

        // Set the number of Fiji threads to maximise the number of jobs, so it doesn't clash with MIA multi-threading.
        int nSimultaneousJobs = inputControl.getParameterValue(InputControl.SIMULTANEOUS_JOBS);
        if (nSimultaneousJobs != 1) {
            int nThreads = Math.floorDiv(origThreads,nSimultaneousJobs);
            Prefs.setThreads(nThreads);
            Prefs.savePreferences();
        }

        // Setting up the ExecutorService, which will manage the threads
        pool = new ThreadPoolExecutor(nSimultaneousJobs,nSimultaneousJobs,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        // Runnables are first stored in a HashSet, then loaded all at once to the ThreadPoolExecutor.  This means the
        // system isn't scanning files and reading for the analysis simultaneously.
        int loadTotal = 0;
        HashSet<Runnable> tasks = new HashSet<>();
        while (next != null) {
            File finalNext = next;

            // Adding a parameter to the metadata structure indicating the depth of the current file
            int fileDepth = 0;
            File parent = next.getParentFile();
            while (parent != null && !parent.getAbsolutePath().equals(rootFolder.getFolderAsFile().getAbsolutePath())) {
                parent = parent.getParentFile();
                fileDepth++;
            }

            // For the current file, determining how many series to processAutomatic (and which ones)
            TreeMap<Integer,String> seriesNumbers = inputControl.getSeriesNumbers(next);

            // Iterating over all series to analyse, adding each one as a new workspace
            for (int seriesNumber:seriesNumbers.keySet()) {
                Workspace workspace = workspaces.getNewWorkspace(next,seriesNumber);
                String seriesName = seriesNumbers.get(seriesNumber);
                if (seriesName.equals("")) seriesName = "FILE: "+finalNext.getName();
                workspace.getMetadata().setSeriesName(seriesName);

                workspace.getMetadata().put("FILE_DEPTH", fileDepth);

                // Adding this Workspace to the Progress monitor
                ProgressMonitor.setWorkspaceProgress(workspace,0d);

                Runnable task = () -> {
                    try {
                        // Running the current analysis
                        analysis.execute(workspace);

                        // Getting the number of completed and total tasks
                        incrementCounter();
                        String nComplete = dfInt.format(getCounter());
                        String nTotal = dfInt.format(pool.getTaskCount());
                        String percentageComplete = dfDec.format(((double) getCounter() / (double) pool.getTaskCount()) * 100);
                        System.out.println("Completed "+nComplete+"/"+nTotal+" ("+percentageComplete+"%), "+finalNext.getName());

                        if (exportMode.equals(OutputControl.ExportModes.INDIVIDUAL_FILES)) {
                            String name = outputControl.getIndividualOutputPath(workspace.getMetadata());
                            exporter.exportResults(workspace, analysis, name);
                            workspace.clearAllObjects(false);
                            workspace.clearAllImages(false);
                        }else if (continuousExport && getCounter() % saveNFiles == 0) {
                            String name = outputControl.getGroupOutputPath(inputControl.getRootFile());
                            exporter.exportResults(workspaces, analysis, name);
                        }

                    } catch (Throwable t) {
                        System.err.println("Failed for file " + finalNext.getName());
                        t.printStackTrace(System.err);

                        // We're only interested in the measurements now, so clearing images and object coordinates
                        workspace.clearAllImages(true);
                        workspace.clearAllObjects(true);

                        pool.shutdownNow();

                    }
                };

                loadTotal++;
                tasks.add(task);

            }

            // Displaying the current progress
            System.out.println("Initialising "+dfInt.format(loadTotal)+" jobs");

            next = getNextValidFileInStructure();

        }

        // Starting the jobs
        double nTotal = pool.getTaskCount();
        System.out.println("Started processing "+dfInt.format(loadTotal)+" jobs");
        for (Runnable task:tasks) pool.submit(task);

        // Telling the pool not to accept any more jobs and to wait until all queued jobs have completed
        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        Prefs.setThreads(origThreads);

        // Exporting to Excel for WorkspaceCollection
        if ((outputControl.isExportAllTogether() || outputControl.isExportGroupedByMetadata()) && exporter != null) {
            String name = outputControl.getGroupOutputPath(inputControl.getParameterValue(InputControl.INPUT_PATH));
            exporter.export(workspaces,analysis,name);
        }
    }

    public void stopAnalysis() {
        Prefs.setThreads(origThreads);
        Thread.currentThread().getThreadGroup().stop();
        System.out.println("Shutdown complete!");

    }


    // GETTERS AND SETTERS

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public int getnThreads() {
        return nThreads;
    }

    public void setnThreads(int nThreads) {
        this.nThreads = nThreads;
    }

    synchronized int getCounter() {
        return counter;

    }

    synchronized void incrementCounter() {
        counter++;

    }
}
