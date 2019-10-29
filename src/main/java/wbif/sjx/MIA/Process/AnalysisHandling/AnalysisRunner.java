// TODO: Add methods for XLS and JSON data export

package wbif.sjx.MIA.Process.AnalysisHandling;


import ij.Prefs;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.FileFolderPathP;
import wbif.sjx.MIA.Object.ProgressMonitor;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.WorkspaceCollection;
import wbif.sjx.MIA.Process.Exporting.Exporter;
import wbif.sjx.MIA.Process.Logging.LogRenderer;
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
public class AnalysisRunner {
    private final static DecimalFormat dfInt = new DecimalFormat("0");
    private final static DecimalFormat dfDec = new DecimalFormat("0.00");

    private static ThreadPoolExecutor pool;
    private static int counter = 0;
    private static int origThreads = Prefs.getThreads();


    // PUBLIC METHODS

    public static void run(Analysis analysis) throws InterruptedException, IOException {
        // Resetting progress monitor
        ProgressMonitor.resetProgress();
        GUI.setProgress(0);
        MIA.log.clearLog();
        counter = 0;

        // Get jobs and exit if no images found
        HashSet<Job> jobs = getJobs(analysis);
        if (jobs.size() == 0) {
            MIA.log.writeWarning("No valid images found at specified path");
            return;
        }

        InputControl inputControl = analysis.getModules().getInputControl();
        OutputControl outputControl = analysis.getModules().getOutputControl();

        // Initialising Exporter
        Exporter exporter = initialiseExporter(outputControl);

        // Set verbose
        Module.setVerbose(jobs.size() == 1);

        // Setting up the pool
        // Set the number of Fiji threads to maximise the number of jobs, so it doesn't clash with MIA multi-threading.
        int nSimultaneousJobs = inputControl.getParameterValue(InputControl.SIMULTANEOUS_JOBS);
        nSimultaneousJobs = Math.min(jobs.size(),nSimultaneousJobs);
        int nThreads = Math.floorDiv(origThreads,nSimultaneousJobs);
        Prefs.setThreads(nThreads);
        Prefs.savePreferences();

        pool = new ThreadPoolExecutor(nSimultaneousJobs,nSimultaneousJobs,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        // Runnables are first stored in a HashSet, then loaded all at once to the ThreadPoolExecutor.  This means the
        // system isn't scanning files and reading for the analysis simultaneously.
        WorkspaceCollection workspaces = new WorkspaceCollection();
        for (Job job:jobs) {
            // Iterating over all seriesNumber to analyse, adding each one as a new workspace
            Workspace workspace = workspaces.getNewWorkspace(job.getFile(),job.getSeriesNumber());
            workspace.getMetadata().setSeriesName(job.getSeriesName());
            workspace.getMetadata().put("FILE_DEPTH", job.getFileDepth());

            // Adding this Workspace to the Progress monitor
            ProgressMonitor.setWorkspaceProgress(workspace,0d);

            pool.submit(createRunnable(analysis,workspaces,workspace,exporter));

        }

        // Telling the pool not to accept any more jobs and to wait until all queued jobs have completed
        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        Prefs.setThreads(origThreads);

        // Exporting to Excel for WorkspaceCollection
        if ((outputControl.isExportAllTogether() || outputControl.isExportGroupedByMetadata()) && exporter != null) {
            File outputFile = new File((String) inputControl.getParameterValue(InputControl.INPUT_PATH));
            String name = outputControl.getGroupOutputPath(outputFile);
            exporter.export(workspaces,analysis,name);
        }

        // Cleaning up
        System.out.println("Complete!");

    }

    public static HashSet<Job> getJobs(Analysis analysis) {
        HashSet<Job> jobs = new HashSet<>();

        InputControl inputControl = analysis.getModules().getInputControl();

        File inputFile = getInputFile(inputControl);
        if (inputFile == null) return new HashSet<>();

        FileCrawler fileCrawler = new FileCrawler(inputFile);
        inputControl.addFilenameFilters(fileCrawler);

        boolean firstPerFolder = inputControl.getParameterValue(InputControl.LOAD_FIRST_PER_FOLDER);

        File rootFolder = fileCrawler.getRootFolderAsFile();
        if (rootFolder.isFile()) {
            TreeMap<Integer,String> seriesNumbers = inputControl.getSeriesNumbers(rootFolder);
            for (int seriesNumber:seriesNumbers.keySet()) {
                jobs.add(new Job(rootFolder,seriesNumber,seriesNumbers.get(seriesNumber),0));
            }
        } else {
            File next = fileCrawler.getNextValidFileInStructure();
            int loadTotal = 0;
            while (next != null && fileCrawler.getCurrentFolderAsFolder() != null) {
                TreeMap<Integer,String> seriesNumbers = inputControl.getSeriesNumbers(next);
                for (int seriesNumber:seriesNumbers.keySet()) {
                    jobs.add(new Job(next,seriesNumber,seriesNumbers.get(seriesNumber),fileCrawler.getCurrentDepth()));

                    // Displaying the current progress
                    System.out.println("Initialising "+dfInt.format(++loadTotal)+" jobs");

                }

                if (firstPerFolder) fileCrawler.goToNextValidFolder();
                next = fileCrawler.getNextValidFileInStructure();

            }
        }

        return jobs;

    }

    public static File getInputFile(InputControl inputControl) {
        String inputPath = ((FileFolderPathP) inputControl.getParameter(InputControl.INPUT_PATH)).getPath();

        if (!checkInputFileValidity(inputPath)) return null;
        return new File(inputPath);

    }

    public static boolean checkInputFileValidity(String path) {
        // Checking if a file/folder had been selected
        if (path == null) {
            MIA.log.write("Select an input file/folder first", LogRenderer.Level.WARNING);
            return false;
        }

        // Checking if the specified input file is present
        if (!new File(path).exists()) {
            MIA.log.write("Selected input file/folder can't be found", LogRenderer.Level.WARNING);
            return false;
        }

        return true;

    }

    static Exporter initialiseExporter(OutputControl outputControl) {
        String exportMode = outputControl.getParameterValue(OutputControl.EXPORT_MODE);
        String metadataItemForGrouping = outputControl.getParameterValue(OutputControl.METADATA_ITEM_FOR_GROUPING);
        boolean exportXLS = outputControl.isEnabled();
        boolean exportSummary = outputControl.getParameterValue(OutputControl.EXPORT_SUMMARY);
        String summaryType = outputControl.getParameterValue(OutputControl.SUMMARY_MODE);
        boolean exportIndividualObjects = outputControl.getParameterValue(OutputControl.EXPORT_INDIVIDUAL_OBJECTS);
        String appendDateTimeMode = outputControl.getParameterValue(OutputControl.APPEND_DATETIME_MODE);
        boolean showObjectCounts = outputControl.getParameterValue(OutputControl.SHOW_OBJECT_COUNTS);

        // Initialising the exporter (if one was requested)
        Exporter exporter = exportXLS ? new Exporter() : null;
        if (exporter != null) {
            exporter.setMetadataItemForGrouping(metadataItemForGrouping);
            exporter.setExportSummary(exportSummary);
            exporter.setShowObjectCounts(showObjectCounts);
            exporter.setExportIndividualObjects(exportIndividualObjects);

            switch (exportMode) {
                case OutputControl.ExportModes.ALL_TOGETHER:
                    exporter.setExportMode(Exporter.ExportMode.ALL_TOGETHER);
                    break;

                case OutputControl.ExportModes.GROUP_BY_METADATA:
                    exporter.setExportMode(Exporter.ExportMode.GROUP_BY_METADATA);
                    break;

                case OutputControl.ExportModes.INDIVIDUAL_FILES:
                    exporter.setExportMode(Exporter.ExportMode.INDIVIDUAL_FILES);
                    break;
            }

            switch (summaryType) {
                case OutputControl.SummaryModes.ONE_AVERAGE_PER_FILE:
                    exporter.setSummaryMode(Exporter.SummaryMode.PER_FILE);
                    break;

                case OutputControl.SummaryModes.AVERAGE_PER_TIMEPOINT:
                    exporter.setSummaryMode(Exporter.SummaryMode.PER_TIMEPOINT_PER_FILE);
                    break;

                case OutputControl.SummaryModes.GROUP_BY_METADATA:
                    exporter.setSummaryMode(Exporter.SummaryMode.GROUP_BY_METADATA);
                    exporter.setMetadataItemForSummary(outputControl.getParameterValue(OutputControl.METADATA_ITEM_FOR_SUMMARY));
                    break;
            }

            switch (appendDateTimeMode) {
                case OutputControl.AppendDateTimeModes.ALWAYS:
                    exporter.setAppendDateTimeMode(Exporter.AppendDateTimeMode.ALWAYS);
                    break;

                case OutputControl.AppendDateTimeModes.IF_FILE_EXISTS:
                    exporter.setAppendDateTimeMode(Exporter.AppendDateTimeMode.IF_FILE_EXISTS);
                    break;

                case OutputControl.AppendDateTimeModes.NEVER:
                    exporter.setAppendDateTimeMode(Exporter.AppendDateTimeMode.NEVER);
                    break;
            }
        }

        return exporter;

    }

    static Runnable createRunnable(Analysis analysis, WorkspaceCollection workspaces, Workspace workspace, Exporter exporter) {
        return () -> {
            File file = workspace.getMetadata().getFile();

            try {
                InputControl inputControl = analysis.getModules().getInputControl();
                OutputControl outputControl = analysis.getModules().getOutputControl();
                boolean continuousExport = outputControl.getParameterValue(OutputControl.CONTINUOUS_DATA_EXPORT);
                int saveNFiles = outputControl.getParameterValue(OutputControl.SAVE_EVERY_N);

                // Running the current analysis
                analysis.execute(workspace);

                // Getting the number of completed and total tasks
                incrementCounter();
                String nComplete = dfInt.format(getCounter());
                String nTotal = dfInt.format(pool.getTaskCount());
                String percentageComplete = dfDec.format(((double) getCounter() / (double) pool.getTaskCount()) * 100);
                System.out.println("Completed " + nComplete + "/" + nTotal + " (" + percentageComplete + "%), " + file.getName());

                if (outputControl.isExportIndividual()) {
                    String name = outputControl.getIndividualOutputPath(workspace.getMetadata());
                    exporter.exportResults(workspace, analysis, name);
                    workspace.clearAllObjects(false);
                    workspace.clearAllImages(false);
                } else if (continuousExport && getCounter() % saveNFiles == 0) {
                    String name = outputControl.getGroupOutputPath(inputControl.getRootFile());
                    exporter.exportResults(workspaces, analysis, name);
                }

            } catch (Throwable t) {
                System.err.println("Failed for file " + file.getName());
                t.printStackTrace(System.err);

                workspace.clearAllImages(true);
                workspace.clearAllObjects(true);

                pool.shutdownNow();

            }
        };
    }

    static public void stopAnalysis() {
        MIA.log.write("STOPPING", LogRenderer.Level.WARNING);
        Prefs.setThreads(origThreads);
        GUI.setModuleBeingEval(-1);
        GUI.updateModules();
        Thread.currentThread().getThreadGroup().stop();
        MIA.log.write("Shutdown complete!", LogRenderer.Level.MESSAGE);

    }


    // GETTERS AND SETTERS

    static synchronized int getCounter() {
        return counter;

    }

    static synchronized void incrementCounter() {
        counter++;

    }
}

class Job {
    private final File file;
    private final int seriesNumber;
    private final String seriesName;
    private final int fileDepth;

    Job(File file, int seriesNumber, String seriesName, int fileDepth) {
        this.file = file;
        this.seriesNumber = seriesNumber;
        this.seriesName = seriesName;
        this.fileDepth = fileDepth;
    }

    File getFile() {
        return file;
    }

    int getSeriesNumber() {
        return seriesNumber;
    }

    String getSeriesName() {
        return seriesName;
    }

    int getFileDepth() {
        return fileDepth;
    }
}