// TODO: Add methods for XLSX and JSON data export

package wbif.sjx.ModularImageAnalysis.Process;

import ij.IJ;
import ij.Prefs;
import loci.common.DebugTools;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.meta.MetadataStore;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import ome.xml.meta.IMetadata;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.Module.Module;
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
import java.util.stream.IntStream;

/**
 * Created by sc13967 on 21/10/2016.
 */
public class BatchProcessor extends FileCrawler {
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

    public void runAnalysisOnStructure(Analysis analysis, Exporter exporter) throws IOException, GenericMIAException, InterruptedException {
        shutdownEarly = false;

        WorkspaceCollection workspaces = new WorkspaceCollection();

        // If no analysis has been specified, skip this method
        if (analysis == null) return;

        // The protocol to run will depend on if a single file or a folder was selected
        if (rootFolder.getFolderAsFile().isFile()) {
            Module.setVerbose(true);
            runSingle(workspaces, analysis);

        } else {
            // The system can run multiple files in parallel or one at a time
            Module.setVerbose(false);
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
        Module.setVerbose(false);

        Prefs.setThreads(1);

        // Setting up the ExecutorService, which will manage the threads
        pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        while (next != null) {
            File finalNext = next;

            // Adding a parameter to the metadata structure indicating the depth of the current file
            int fileDepth = 0;
            File parent = next.getParentFile();
            while (parent != rootFolder.getFolderAsFile() && parent != null) {
                parent = parent.getParentFile();
                fileDepth++;
            }

            // For the current file, determining how many series to process (and which ones)
            int[] seriesNumbers = getSeriesNumbers(analysis, next);

            // Iterating over all series to analyse, adding each one as a new workspace
            for (int seriesNumber:seriesNumbers) {
                Workspace workspace = workspaces.getNewWorkspace(next,seriesNumber);
                workspace.getMetadata().put("FILE_DEPTH", fileDepth);

                Runnable task = () -> {
                    try {
                        // Running the current analysis
                        analysis.execute(workspace);

                        // Getting the number of completed and total tasks
                        incrementCounter();
                        int nComplete = getCounter();
                        double nTotal = pool.getTaskCount();
                        double percentageComplete = (nComplete / nTotal) * 100;

                        // Displaying the current progress
                        String string = "Completed " + dfInt.format(nComplete) + "/" + dfInt.format(nTotal)
                                + " (" + dfDec.format(percentageComplete) + "%), " + finalNext.getName();
                        System.out.println(string);

                        if (continuousExport && nComplete % saveNFiles == 0)
                            exporter.exportResults(workspaces, analysis);

                    } catch (GenericMIAException | IOException e) {
                        e.printStackTrace();

                    } catch (Throwable t) {
                        System.err.println("Failed for file " + finalNext.getName());
                        t.printStackTrace(System.err);

                        pool.shutdownNow();

                    }
                };

                pool.submit(task);

            }

            // Displaying the current progress
            double nTotal = pool.getTaskCount();
            String string = "Started processing "+dfInt.format(nTotal)+" jobs";
            System.out.println(string);

            next = getNextValidFileInStructure();

        }

        // Telling the pool not to accept any more jobs and to wait until all queued jobs have completed
        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        Prefs.setThreads(origThreads);

    }

    private void runSingle(WorkspaceCollection workspaces, Analysis analysis) throws InterruptedException {
        // Setting up the ExecutorService, which will manage the threads
        pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        Prefs.setThreads(1);

        // For the current file, determining how many series to process (and which ones)
        int[] seriesNumbers = getSeriesNumbers(analysis, rootFolder.getFolderAsFile());

        // Iterating over all series to analyse, adding each one as a new workspace
        for (int seriesNumber:seriesNumbers) {
            Workspace workspace = workspaces.getNewWorkspace(rootFolder.getFolderAsFile(),seriesNumber);

            Runnable task = () -> {
                try {
                    analysis.execute(workspace);
                } catch (Throwable t) {
                    t.printStackTrace(System.err);
                }

                // Clearing images from the workspace to prevent memory leak
                workspace.clearAllImages(true);

                // Adding a blank line to the output
                if (verbose) System.out.println(" ");

            };

            // Submit the jobs for this file, then tell the pool not to accept any more jobs and to wait until all
            // queued jobs have completed
            pool.submit(task);

        }

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
        Prefs.setThreads(origThreads);

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

    private int[] getSeriesNumbers(Analysis analysis, File inputFile) {
        String seriesMode = analysis.getInputControl().getParameterValue(InputControl.SERIES_MODE);
        int[] seriesNumbers = null;

        switch (seriesMode) {
            case InputControl.SeriesModes.ALL_SERIES:
                // Using BioFormats to get the number of series
                DebugTools.enableLogging("off");
                DebugTools.setRootLevel("off");
                ImageProcessorReader reader = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));
                reader.setGroupFiles(false);
                try {
                    reader.setId(inputFile.getAbsolutePath());
                } catch (FormatException | IOException e) {
                    e.printStackTrace();
                }

                int seriesCount = reader.getSeriesCount();
                if (seriesCount == 1) {
                    seriesNumbers = new int[]{1};
                } else {
                    seriesNumbers = IntStream.range(1, seriesCount).toArray();
                }

                break;

            case InputControl.SeriesModes.SINGLE_SERIES:
                seriesNumbers = new int[]{analysis.getInputControl().getParameterValue(InputControl.SERIES_NUMBER)};
                break;
        }

        return seriesNumbers;

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
