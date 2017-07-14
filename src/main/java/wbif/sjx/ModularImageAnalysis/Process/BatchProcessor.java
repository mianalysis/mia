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
    private boolean verbose = false;
    private boolean parallel = true;
    private int nThreads = 4;


    // CONSTRUCTORS

    public BatchProcessor(File root_folder) {
        super(root_folder);

    }

    public BatchProcessor() {

    }


    // PUBLIC METHODS

    public WorkspaceCollection runAnalysisOnStructure(Analysis analysis, Exporter exporter) throws IOException, GenericMIAException {
        int num_valid_files = getNumberOfValidFilesInStructure();
        resetIterator();

        WorkspaceCollection workspaces = new WorkspaceCollection();

        folder = rootFolder;
        File next = getNextValidFileInStructure();

        int iter = 1;

        if (analysis != null) {
            if (parallel) {
                // Setting up the ExecutorService, which will manage the threads
                ExecutorService pool = Executors.newFixedThreadPool(nThreads);

                while (next != null) {
                    Workspace workspace = workspaces.getNewWorkspace(next);
                    Runnable task = () -> {
                        try {
                            analysis.execute(workspace, verbose);
                        } catch (GenericMIAException e) {
                            e.printStackTrace();
                        }
                    };
                    pool.submit(task);

                    next = getNextValidFileInStructure();

                }

                // Telling the pool not to accept any more jobs and to wait until all queued jobs have completed
                pool.shutdown();
                try {
                    pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {
                while (next != null) {
                    System.out.println("Processing file: " + next.getName() + " (file " + iter++ + " of " + num_valid_files + ")");

                    // Running the analysis
                    Workspace workspace = workspaces.getNewWorkspace(next);
                    analysis.execute(workspace, verbose);

                    // Clearing images from the workspace to prevent memory leak
                    workspace.clearAllImages(true);

                    next = getNextValidFileInStructure();

                    System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + " used");

                    // Adding a blank line to the output
                    if (verbose) System.out.println(" ");

                }
            }
        }

        System.out.println("Complete!");

        // Saving the results
        exporter.exportResults(workspaces,analysis);
        System.out.println("Saved!");
        return workspaces;

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
