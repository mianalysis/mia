// TODO: Add methods for XLS and JSON data export

package wbif.sjx.MIA.Process.AnalysisHandling;


import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ij.Prefs;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.WorkspaceCollection;
import wbif.sjx.MIA.Object.Parameters.FileFolderPathP;
import wbif.sjx.MIA.Process.Exporting.Exporter;
import wbif.sjx.common.System.FileCrawler;

/**
 * Created by sc13967 on 21/10/2016.
 */
public class AnalysisRunner {
    private static ThreadPoolExecutor pool;
    private static int counter = 0;
    private static int origThreads = Prefs.getThreads();

    private final WorkspaceCollection workspaces = new WorkspaceCollection();

    private final static DecimalFormat dfInt = new DecimalFormat("0");
    private final static DecimalFormat dfDec = new DecimalFormat("0.00");

    // PUBLIC METHODS

    public void run(Analysis analysis) throws InterruptedException, IOException {
        // Resetting progress display
        GUI.updateProgressBar(0);
        MIA.clearLogHistory();
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

        // Restarting the WorkspaceCollection
        workspaces.clear();

        // Runnables are first stored in a HashSet, then loaded all at once to the ThreadPoolExecutor.  This means the
        // system isn't scanning files and reading for the analysis simultaneously.
        for (Job job:jobs) {
            // Iterating over all seriesNumber to analyse, adding each one as a new workspace
            Workspace workspace = workspaces.getNewWorkspace(job.getFile(),job.getSeriesNumber());
            workspace.getMetadata().setSeriesName(job.getSeriesName());
            workspace.getMetadata().put("FILE_DEPTH", job.getFileDepth());

            // Adding this Workspace to the Progress monitor
            workspace.setProgress(0);
        }

        for (Workspace workspace:workspaces) pool.submit(createRunnable(analysis,workspace,exporter));

        // Telling the pool not to accept any more jobs and to wait until all queued jobs have completed
        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        Prefs.setThreads(origThreads);

        // Exporting to Excel for WorkspaceCollection
        if ((outputControl.isExportAllTogether() || outputControl.isExportGroupedByMetadata()) && exporter != null) {
            MIA.log.writeStatus("Exporting data");
            File outputFile = new File((String) inputControl.getParameterValue(InputControl.INPUT_PATH));
            String name = outputControl.getGroupOutputPath(outputFile);
            exporter.export(workspaces, analysis, name);
        }

        // Running macro (if enabled)
        outputControl.runMacro(workspaces.iterator().next());

        // Cleaning up
        MIA.log.writeStatus("Complete!");
        GUI.updateProgressBar(100);

    }

    public HashSet<Job> getJobs(Analysis analysis) {
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
                    MIA.log.writeStatus("Initialising "+dfInt.format(++loadTotal)+" jobs");

                }

                if (firstPerFolder) fileCrawler.goToNextValidFolder();
                next = fileCrawler.getNextValidFileInStructure();

            }
        }

        return jobs;

    }

    public File getInputFile(InputControl inputControl) {
        String inputPath = ((FileFolderPathP) inputControl.getParameter(InputControl.INPUT_PATH)).getPath();

        if (!checkInputFileValidity(inputPath)) return null;
        return new File(inputPath);

    }

    public boolean checkInputFileValidity(String path) {
        // Checking if a file/folder had been selected
        if (path == null) {
            MIA.log.writeWarning("Select an input file/folder first");
            return false;
        }

        // Checking if the specified input file is present
        if (!new File(path).exists()) {
            MIA.log.writeWarning("Selected input file/folder can't be found");
            return false;
        }

        return true;

    }

    Exporter initialiseExporter(OutputControl outputControl) {
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

    Runnable createRunnable(Analysis analysis, Workspace workspace, Exporter exporter) {
        return () -> {
            File file = workspace.getMetadata().getFile();
            int seriesNumber = workspace.getMetadata().getSeriesNumber();

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
                MIA.log.writeStatus(
                        "Completed " + nComplete + "/" + nTotal + " (" + percentageComplete + "%), " + file.getName());

                if (outputControl.isExportIndividual()) {
                    String name = outputControl.getIndividualOutputPath(workspace.getMetadata());
                    exporter.exportResults(workspace, analysis, name);
                    workspace.clearAllObjects(false);
                    workspace.clearAllImages(false);
                } else if (continuousExport && getCounter() % saveNFiles == 0) {
                    String name = outputControl.getGroupOutputPath(inputControl.getRootFile());
                    exporter.exportResults(workspace, analysis, name);
                }

            } catch (Throwable t) {
                DecimalFormat df = new DecimalFormat("#.0");

                double totalMemory = Runtime.getRuntime().totalMemory();
                double usedMemory = totalMemory - Runtime.getRuntime().freeMemory();
                String memoryMessage = "Memory: " + df.format(usedMemory * 1E-6) + " MB of "
                        + df.format(totalMemory * 1E-6) + " MB";

                MIA.log.writeError(
                        "Failed for file " + file.getName() + ", series " + seriesNumber + " (" + memoryMessage + ")");
                MIA.log.writeError(t);

                workspace.clearAllImages(true);
                workspace.clearAllObjects(true);

                pool.shutdownNow();

            }
        };
    }

    public static void stopAnalysis() {
        MIA.log.writeWarning("STOPPING");
        Prefs.setThreads(origThreads);
        GUI.setModuleBeingEval(-1);
        GUI.updateModules();
        Thread.currentThread().getThreadGroup().stop();
        MIA.log.writeStatus("Shutdown complete!");

    }


    // GETTERS AND SETTERS

    public static synchronized int getCounter() {
        return counter;

    }

    static synchronized void incrementCounter() {
        counter++;

    }

    public WorkspaceCollection getWorkspaces() {
        return workspaces;
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