// TODO: Add methods for XLSX and JSON data export

package wbif.sjx.ModularImageAnalysis.Process;

import ij.Prefs;
import loci.common.DebugTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.meta.MetadataStore;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import ome.xml.meta.IMetadata;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.FileConditions.FileCondition;
import wbif.sjx.common.FileConditions.NameContainsString;
import wbif.sjx.common.FileConditions.ParentContainsString;
import wbif.sjx.common.System.FileCrawler;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;


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
        shutdownEarly = false;
        String exportMode = analysis.getOutputControl().getParameterValue(OutputControl.EXPORT_MODE);

        WorkspaceCollection workspaces = new WorkspaceCollection();

        // If no analysis has been specified, skip this method
        if (analysis == null) return;

        // The protocol to run will depend on if a single file or a folder was selected
        if (rootFolder.getFolderAsFile().isFile()) {
            runSingle(workspaces, analysis);

            if (!exportMode.equals(OutputControl.ExportModes.NONE)) {
                System.out.println("Exporting results to spreadsheet");
                exporter.exportResults(workspaces, analysis);
            }

        } else {
            // The system can run multiple files in parallel or one at a time
            runParallel(workspaces, analysis, exporter);

            switch (exportMode) {
                case OutputControl.ExportModes.ALL_TOGETHER:
                case OutputControl.ExportModes.GROUP_BY_METADATA:
                    System.out.println("Exporting results to spreadsheet");
                    exporter.exportResults(workspaces,analysis);
                    break;
            }
        }

        // Saving the results
        if (shutdownEarly || exporter == null) return;

        GUI.setProgress(0);

    }

    private void runParallel(WorkspaceCollection workspaces, Analysis analysis, Exporter exporter) throws InterruptedException {
        File next = getNextValidFileInStructure();

        boolean continuousExport = analysis.getOutputControl().getParameterValue(OutputControl.CONTINUOUS_DATA_EXPORT);
        int saveNFiles = analysis.getOutputControl().getParameterValue(OutputControl.SAVE_EVERY_N);
        String exportMode = analysis.getOutputControl().getParameterValue(OutputControl.EXPORT_MODE);

        Module.setVerbose(false);

        // Set the number of Fiji threads to maximise the number of jobs, so it doesn't clash with MIA multi-threading.
        int nSimultaneousJobs = analysis.getInputControl().getParameterValue(InputControl.SIMULTANEOUS_JOBS);
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
            while (parent != rootFolder.getFolderAsFile() && parent != null) {
                parent = parent.getParentFile();
                fileDepth++;
            }

            // For the current file, determining how many series to process (and which ones)
            TreeMap<Integer,String> seriesNumbers = getSeriesNumbers(analysis, next);

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
                        int nComplete = getCounter();
                        double nTotal = pool.getTaskCount();
                        double percentageComplete = (nComplete / nTotal) * 100;

                        // Displaying the current progress
                        String string = "Completed " + dfInt.format(nComplete) + "/" + dfInt.format(nTotal)
                                + " (" + dfDec.format(percentageComplete) + "%), " + finalNext.getName();
                        System.out.println(string);

                        if (continuousExport && nComplete % saveNFiles == 0) exporter.exportResults(workspaces, analysis);

                        if (exportMode.equals(OutputControl.ExportModes.INDIVIDUAL_FILES)) exporter.exportResults(workspace, analysis);

                    } catch (IOException e) {
                        e.printStackTrace();

                    } catch (Throwable t) {
                        System.err.println("Failed for file " + finalNext.getName());
                        t.printStackTrace(System.err);

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

    }

    private void runSingle(WorkspaceCollection workspaces, Analysis analysis) throws InterruptedException {
        // Setting up the ExecutorService, which will manage the threads
        pool = new ThreadPoolExecutor(1,1,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        // For the current file, determining how many series to process (and which ones)
        TreeMap<Integer,String> seriesNumbers = getSeriesNumbers(analysis, rootFolder.getFolderAsFile());

        // Only set verbose if a single series is being processed
        Module.setVerbose(seriesNumbers.size() == 1);

        // Iterating over all series to analyse, adding each one as a new workspace
        for (int seriesNumber:seriesNumbers.keySet()) {
            Workspace workspace = workspaces.getNewWorkspace(rootFolder.getFolderAsFile(),seriesNumber);
            String seriesName = seriesNumbers.get(seriesNumber);
            if (seriesName.equals("")) seriesName = "FILE: "+rootFolder.getFolderAsFile().getName();
            workspace.getMetadata().setSeriesName(seriesName);

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
                String string = "Completed " + dfInt.format(nComplete) + "/" + dfInt.format(nTotal)
                        + " (" + dfDec.format(percentageComplete) + "%)";
                System.out.println(string);

                // Clearing images from the workspace to prevent memory leak
                workspace.clearAllImages(true);

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

    }

    public void stopAnalysis() {
        Prefs.setThreads(origThreads);
        Thread.currentThread().getThreadGroup().stop();
        System.out.println("Shutdown complete!");

    }

    /**
     * Iterprets inputs from InputControl and assigns the appropriate filters
     * @param filenameFilterType
     * @param filenameFilter
     */
    public void addFilenameFilter(String filenameFilterType, String filenameFilter, String filenameSource) {
        addFileCondition(getFilenameFilter(filenameFilterType,filenameFilter,filenameSource));
    }

    private FileCondition getFilenameFilter(String filenameFilterType, String filenameFilter, String filenameFilterSource) {
        int fileCondition;
        switch (filenameFilterType) {
            case InputControl.FilterTypes.INCLUDE_MATCHES_PARTIALLY:
            default:
                fileCondition = FileCondition.INC_PARTIAL;
                break;
            case InputControl.FilterTypes.INCLUDE_MATCHES_COMPLETELY:
                fileCondition = FileCondition.INC_COMPLETE;
                break;
            case InputControl.FilterTypes.EXCLUDE_MATCHES_PARTIALLY:
                fileCondition = FileCondition.EXC_PARTIAL;
                break;
            case InputControl.FilterTypes.EXCLUDE_MATCHES_COMPLETELY:
                fileCondition = FileCondition.EXC_COMPLETE;
                break;
        }

        switch (filenameFilterSource) {
            case InputControl.FilenameFilterSource.FILENAME:
                default:
                return new NameContainsString(filenameFilter, fileCondition);
            case InputControl.FilenameFilterSource.FILEPATH:
                return new ParentContainsString(filenameFilter, fileCondition);
        }
    }

    private TreeMap<Integer,String> getSeriesNumbers(Analysis analysis, File inputFile) {
        try {
            ParameterCollection parameters = analysis.getInputControl().getAllParameters();
            String seriesMode = parameters.getValue(InputControl.SERIES_MODE);
            boolean useFilter1 = parameters.getValue(InputControl.USE_SERIESNAME_FILTER_1);
            String filter1 = parameters.getValue(InputControl.SERIESNAME_FILTER_1);
            String filterType1 = parameters.getValue(InputControl.SERIESNAME_FILTER_TYPE_1);
            boolean useFilter2 = parameters.getValue(InputControl.USE_SERIESNAME_FILTER_2);
            String filter2 = parameters.getValue(InputControl.SERIESNAME_FILTER_2);
            String filterType2 = parameters.getValue(InputControl.SERIESNAME_FILTER_TYPE_2);
            boolean useFilter3 = parameters.getValue(InputControl.USE_SERIESNAME_FILTER_3);
            String filter3 = parameters.getValue(InputControl.SERIESNAME_FILTER_3);
            String filterType3 = parameters.getValue(InputControl.SERIESNAME_FILTER_TYPE_3);

            TreeMap<Integer,String> namesAndNumbers = new TreeMap<>();
            switch (seriesMode) {
                case InputControl.SeriesModes.ALL_SERIES:
                    // Using BioFormats to get the number of series
                    DebugTools.enableLogging("off");
                    DebugTools.setRootLevel("off");

                    ServiceFactory factory = new ServiceFactory();
                    OMEXMLService service = factory.getInstance(OMEXMLService.class);
                    IMetadata meta = service.createOMEXMLMetadata();
                    ImageProcessorReader reader = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));
                    reader.setMetadataStore((MetadataStore) meta);
                    reader.setGroupFiles(false);
                    reader.setId(inputFile.getAbsolutePath());

                    for (int seriesNumber=0;seriesNumber<reader.getSeriesCount();seriesNumber++) {
                        String name = meta.getImageName(seriesNumber);
                        String source = InputControl.FilenameFilterSource.FILENAME;

                        NameContainsString test1 = (NameContainsString) getFilenameFilter(filterType1,filter1,source);
                        if (name!= null && useFilter1 &! test1.test(name)) continue;

                        NameContainsString test2 = (NameContainsString) getFilenameFilter(filterType2,filter2,source);
                        if (useFilter2 &!test2.test(name)) continue;

                        NameContainsString test3 = (NameContainsString) getFilenameFilter(filterType3,filter3,source);
                        if (useFilter3 &!test3.test(name)) continue;
                        namesAndNumbers.put(seriesNumber+1,name);
                    }
                    break;

                case InputControl.SeriesModes.SERIES_LIST:
                    // Converting series list to a list of numbers
                    String seriesList = analysis.getInputControl().getParameterValue(InputControl.SERIES_LIST);
                    seriesList = seriesList.replace(" ","");
                    List<String> list = new ArrayList<String>(Arrays.asList(seriesList.split(",")));

                    // Using BioFormats to get the number of series
                    DebugTools.enableLogging("off");
                    DebugTools.setRootLevel("off");

                    factory = new ServiceFactory();
                    service = factory.getInstance(OMEXMLService.class);
                    meta = service.createOMEXMLMetadata();
                    reader = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));
                    reader.setMetadataStore((MetadataStore) meta);
                    reader.setGroupFiles(false);
                    reader.setId(inputFile.getAbsolutePath());

                    for (int i=0;i<list.size();i++) {
                        int seriesNumber = Integer.parseInt(list.get(i))-1;
                        namesAndNumbers.put(seriesNumber+1,meta.getImageName(seriesNumber));
                    }

                    break;

                case InputControl.SeriesModes.SINGLE_SERIES:
                    namesAndNumbers.put(analysis.getInputControl().getParameterValue(InputControl.SERIES_NUMBER),"");
                    break;
            }

            return namesAndNumbers;

        } catch (DependencyException | FormatException | ServiceException | IOException e) {
            e.printStackTrace();
        }

        return null;

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
