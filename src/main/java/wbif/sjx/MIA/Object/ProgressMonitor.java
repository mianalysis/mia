package wbif.sjx.MIA.Object;

import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisRunner;
import wbif.sjx.common.MathFunc.CumStat;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.HashMap;

public class ProgressMonitor {
    private static HashMap<Workspace,Double> progressCollection = new HashMap<>();
    private static Workspace lastWorkspace = null;

    private final static DecimalFormat dfInt = new DecimalFormat("0");
    private final static DecimalFormat dfDec = new DecimalFormat("0.00");


    public synchronized static void resetProgress() {
        progressCollection.clear();
    }

    public synchronized static void setWorkspaceProgress(Workspace workspace, double progress) {
        progressCollection.put(workspace,progress);
    }

    public synchronized static double getWorkspaceProgress(Workspace workspace) {
        return progressCollection.get(workspace);
    }

    public synchronized static double getOverallProgress() {
        CumStat cs = new CumStat();
        for (double progress:progressCollection.values()) cs.addMeasure(progress);

        return cs.getMean();

    }

    public static void finaliseWorkspace(Workspace workspace) {
        // Get name of most recent file processed
        lastWorkspace = workspace;
        progressCollection.put(workspace, 100d);

        displayProgressMessage();

    }

    public static void displayProgressMessage() {
        String filename = "";
        if (lastWorkspace != null) filename = lastWorkspace.getMetadata().getFile().getName();

        // Get number of files processed
        double count = AnalysisRunner.getCounter();
        double nTotal = progressCollection.size();
        String percentageComplete = dfDec.format((count/nTotal)*100);

        String countString = dfInt.format(count);
        String nTotalString = dfInt.format(nTotal);

        System.out.println("Completed " + countString + "/" + nTotalString + " (" + percentageComplete + "%), " + filename);
    }
}
