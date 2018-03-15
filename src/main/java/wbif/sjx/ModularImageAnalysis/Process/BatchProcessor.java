// TODO: Add methods for XLSX and JSON data export

package wbif.sjx.ModularImageAnalysis.Process;

import ij.IJ;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.FileConditions.FileCondition;
import wbif.sjx.common.FileConditions.NameContainsString;
import wbif.sjx.common.System.FileCrawler;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.*;

/**
 * Created by sc13967 on 21/10/2016.
 */
public class BatchProcessor extends FileCrawler {
    private boolean verbose = true;
    private int nThreads = Runtime.getRuntime().availableProcessors()/2;

    private ThreadPoolExecutor pool;

    private boolean shutdownEarly;
    private int counter = 0;


    // CONSTRUCTORS

    public BatchProcessor(File rootFolder) {
        super(rootFolder);

    }


    // PUBLIC METHODS

    public void runAnalysisOnStructure(Analysis analysis, Exporter exporter) throws IOException, GenericMIAException, InterruptedException {
        shutdownEarly = false;

        WorkspaceCollection workspaces = new WorkspaceCollection();

        // If no analysis has been specified, skip this method
        if (analysis == null) return;

        // The protocol to run will depend on if a single file or a folder was selected
        if (rootFolder.getFolderAsFile().isFile()) {
            runSingle(workspaces, analysis);

        } else {
            // The system can run multiple files in parallel or one at a time
            runParallel(workspaces, analysis, exporter);

        }

        // Saving the results
        if (shutdownEarly || exporter == null) return;
        exporter.exportResults(workspaces,analysis);

    }

    private void runParallel(WorkspaceCollection workspaces, Analysis analysis, Exporter exporter) throws InterruptedException {
        DecimalFormat dfInt = new DecimalFormat("0");
        DecimalFormat dfDec = new DecimalFormat("0.00");

        File next = getNextValidFileInStructure();

        boolean continuousExport = analysis.getOutputControl().getParameterValue(OutputControl.CONTINUOUS_DATA_EXPORT);
        int saveNFiles = analysis.getOutputControl().getParameterValue(OutputControl.SAVE_EVERY_N);

        System.out.println("Starting batch processor");

        // Setting up the ExecutorService, which will manage the threads
        pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        while (next != null) {
            Workspace workspace = workspaces.getNewWorkspace(next);
            File finalNext = next;

            // Adding a parameter to the metadata structure indicating the depth of the current file
            int fileDepth = 0;
            File parent = next.getParentFile();
            while (parent != rootFolder.getFolderAsFile() && parent != null) {
                parent = parent.getParentFile();
                fileDepth++;
            }
            workspace.getMetadata().put("FILE_DEPTH",fileDepth);

            Runnable task = () -> {
                try {
                    // Running the current analysis
                    analysis.execute(workspace, false);

                    // Getting the number of completed and total tasks
                    incrementCounter();
                    int nComplete = getCounter();
                    double nTotal = pool.getTaskCount();
                    double percentageComplete = (nComplete/nTotal)*100;

                    // Displaying the current progress
                    String string = "Completed "+dfInt.format(nComplete)+"/"+dfInt.format(nTotal)
                            +" ("+dfDec.format(percentageComplete)+"%), "+ finalNext.getName();
                    System.out.println(string);

                    if (continuousExport && nComplete%saveNFiles==0) exporter.exportResults(workspaces,analysis);

                } catch (GenericMIAException | IOException e ) {
                    e.printStackTrace();

                } catch (Throwable t) {
                    System.err.println("Failed for file "+finalNext.getName());
                    t.printStackTrace(System.err);

                    pool.shutdownNow();

                }
            };

            pool.submit(task);

            // Displaying the current progress
            double nTotal = pool.getTaskCount();
            String string = "Started processing "+dfInt.format(nTotal)+" jobs";
            System.out.println(string);

            next = getNextValidFileInStructure();

        }

        // Telling the pool not to accept any more jobs and to wait until all queued jobs have completed
        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    private void runSingle(WorkspaceCollection workspaces, Analysis analysis) throws InterruptedException {
        // Setting up the ExecutorService, which will manage the threads
        pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        Runnable task = () -> {
            // Running the analysis
            Workspace workspace = workspaces.getNewWorkspace(rootFolder.getFolderAsFile());
            try {
                analysis.execute(workspace, verbose);
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }

            // Clearing images from the workspace to prevent memory leak
            workspace.clearAllImages(true);

            // Adding a blank line to the output
            if (verbose) System.out.println(" ");

        };

        // Submit the one job, then telling the pool not to accept any more jobs and to wait until all queued jobs have
        // completed
        pool.submit(task);
        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    public void stopAnalysis() {
        if (pool == null) {
            Thread.currentThread().interrupt();
        } else {
            pool.shutdownNow();
        }
        shutdownEarly = true;

        System.out.println("Shutdown complete!");

    }

    /**
     * Iterprets inputs from InputControl and assigns the appropriate filters
     * @param filenameFilterType
     * @param filenameFilter
     */
    public void addFilenameFilter(String filenameFilterType, String filenameFilter) {
        switch (filenameFilterType) {
            case InputControl.FilterTypes.INCLUDE_MATCHES_PARTIALLY:
                addFileCondition(new NameContainsString(filenameFilter, FileCondition.INC_PARTIAL));
                break;

            case InputControl.FilterTypes.INCLUDE_MATCHES_COMPLETELY:
                addFileCondition(new NameContainsString(filenameFilter, FileCondition.INC_PARTIAL));
                break;

            case InputControl.FilterTypes.EXCLUDE_MATCHES_PARTIALLY:
                addFileCondition(new NameContainsString(filenameFilter, FileCondition.EXC_PARTIAL));
                break;

            case InputControl.FilterTypes.EXCLUDE_MATCHES_COMPLETELY:
                addFileCondition(new NameContainsString(filenameFilter, FileCondition.EXC_PARTIAL));
                break;
        }
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
