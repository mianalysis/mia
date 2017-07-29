// TODO: Add methods for XLSX and JSON data export

package wbif.sjx.ModularImageAnalysis.Process;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.System.FileCrawler;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by sc13967 on 21/10/2016.
 */
public class BatchProcessor extends FileCrawler {
    private boolean verbose = true;
    private boolean parallel = true;
    private int nThreads = 6;

    private ExecutorService pool;

    private boolean shutdownEarly;


    // CONSTRUCTORS

    public BatchProcessor(File root_folder) {
        super(root_folder);

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
            if (parallel) runParallel(workspaces, analysis);
            else runLinear(workspaces, analysis);

        }

        // Saving the results
        if (shutdownEarly) return;
        exporter.exportResults(workspaces,analysis);
        System.out.println("Complete!");

    }

    private void runParallel(WorkspaceCollection workspaces, Analysis analysis) throws InterruptedException {
        File next = getNextValidFileInStructure();

        // Setting up the ExecutorService, which will manage the threads
        pool = Executors.newFixedThreadPool(nThreads);

        while (next != null) {
            Workspace workspace = workspaces.getNewWorkspace(next);
            Runnable task = () -> {
                try {
                    analysis.execute(workspace, false);
                } catch (GenericMIAException e) {
                    e.printStackTrace();
                }
            };
            pool.submit(task);

            next = getNextValidFileInStructure();

        }

        // Telling the pool not to accept any more jobs and to wait until all queued jobs have completed
        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    private void runLinear(WorkspaceCollection workspaces, Analysis analysis) throws GenericMIAException {
        File next = getNextValidFileInStructure();

        while (next != null) {
            // Running the analysis
            Workspace workspace = workspaces.getNewWorkspace(next);
            analysis.execute(workspace, verbose);

            // Clearing images from the workspace to prevent memory leak
            workspace.clearAllImages(true);

            next = getNextValidFileInStructure();

            // Adding a blank line to the output
            if (verbose) System.out.println(" ");

        }
    }

    private void runSingle(WorkspaceCollection workspaces, Analysis analysis) throws GenericMIAException {
        // Running the analysis
        Workspace workspace = workspaces.getNewWorkspace(rootFolder.getFolderAsFile());
        analysis.execute(workspace, verbose);

        // Clearing images from the workspace to prevent memory leak
        workspace.clearAllImages(true);

        // Adding a blank line to the output
        if (verbose) System.out.println(" ");

    }

    public void stopAnalysis() {
        pool.shutdownNow();
        shutdownEarly = true;

        System.out.println("Shutdown complete!");

    }


    // GETTERS AND SETTERS

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isParallel() {
        return parallel;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    public int getnThreads() {
        return nThreads;
    }

    public void setnThreads(int nThreads) {
        this.nThreads = nThreads;
    }
}
