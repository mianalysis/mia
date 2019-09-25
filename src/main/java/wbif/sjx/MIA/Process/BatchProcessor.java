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

    public HashSet<Job> getJobs(Analysis analysis) {
        HashSet<Job> jobs = new HashSet<>();

        InputControl inputControl = analysis.getModules().getInputControl();

        if (rootFolder.getFolderAsFile().isFile()) {
            TreeMap<Integer,String> seriesNumbers = inputControl.getSeriesNumbers(rootFolder.getFolderAsFile());
            for (int seriesNumber:seriesNumbers.keySet()) {
                jobs.add(new Job(rootFolder.getFolderAsFile(),seriesNumber,seriesNumbers.get(seriesNumber)));
            }
        } else {
            File next = getNextValidFileInStructure();
            int loadTotal = 0;

            while (next != null) {
                TreeMap<Integer,String> seriesNumbers = inputControl.getSeriesNumbers(next);
                for (int seriesNumber:seriesNumbers.keySet()) {
                    jobs.add(new Job(next,seriesNumber,seriesNumbers.get(seriesNumber)));
                }

                // Displaying the current progress
                System.out.println("Initialising "+dfInt.format(++loadTotal)+" jobs");

                next = getNextValidFileInStructure();
            }
        }

        return jobs;

    }

    public void run(Analysis analysis, Exporter exporter) throws InterruptedException, IOException {
        // Get jobs
        HashSet<Job> jobs = getJobs(analysis);

        InputControl inputControl = analysis.getModules().getInputControl();
        OutputControl outputControl = analysis.getModules().getOutputControl();

        boolean continuousExport = outputControl.getParameterValue(OutputControl.CONTINUOUS_DATA_EXPORT);
        int saveNFiles = outputControl.getParameterValue(OutputControl.SAVE_EVERY_N);
        String exportMode = outputControl.getParameterValue(OutputControl.EXPORT_MODE);

        // Set verbose
        Module.setVerbose(jobs.size() == 1);

        // Setting up the pool
        // Set the number of Fiji threads to maximise the number of jobs, so it doesn't clash with MIA multi-threading.
        int nSimultaneousJobs = inputControl.getParameterValue(InputControl.SIMULTANEOUS_JOBS);
        nSimultaneousJobs = Math.min(jobs.size(),nSimultaneousJobs);
        int nThreads = Math.floorDiv(origThreads,nSimultaneousJobs);
        Prefs.setThreads(nThreads);
        Prefs.savePreferences();

        pool = new ThreadPoolExecutor(nSimultaneousJobs,nSimultaneousJobs,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        // Runnables are first stored in a HashSet, then loaded all at once to the ThreadPoolExecutor.  This means the
        // system isn't scanning files and reading for the analysis simultaneously.
        WorkspaceCollection workspaces = new WorkspaceCollection();
        for (Job job:jobs) {
            File jobFile = job.getFile();

            // Iterating over all seriesNumber to analyse, adding each one as a new workspace
            Workspace workspace = workspaces.getNewWorkspace(jobFile,job.getSeriesNumber());
            workspace.getMetadata().setSeriesName(job.getSeriesName());
            workspace.getMetadata().put("FILE_DEPTH", calculateFileDepth(jobFile));

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
                    System.out.println("Completed "+nComplete+"/"+nTotal+" ("+percentageComplete+"%), "+jobFile.getName());

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
                    System.err.println("Failed for file " + jobFile.getName());
                    t.printStackTrace(System.err);
                    
                    workspace.clearAllImages(true);
                    workspace.clearAllObjects(true);

                    pool.shutdownNow();

                }
            };

            pool.submit(task);

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
    }

    int calculateFileDepth(File jobFile) {
        int fileDepth = 0;
        File parent = jobFile.getParentFile();
        while (parent != null && !parent.getAbsolutePath().equals(rootFolder.getFolderAsFile().getAbsolutePath())) {
            parent = parent.getParentFile();
            fileDepth++;
        }

        return fileDepth;

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

    class Job {
        private final File file;
        private final int seriesNumber;
        private final String seriesName;

        public Job(File file, int seriesNumber, String seriesName) {
            this.file = file;
            this.seriesNumber = seriesNumber;
            this.seriesName = seriesName;
        }

        public File getFile() {
            return file;
        }

        public int getSeriesNumber() {
            return seriesNumber;
        }

        public String getSeriesName() {
            return seriesName;
        }
    }
}
