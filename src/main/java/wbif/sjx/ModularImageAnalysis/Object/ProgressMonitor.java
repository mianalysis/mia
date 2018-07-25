package wbif.sjx.ModularImageAnalysis.Object;

import wbif.sjx.ModularImageAnalysis.Process.Analysis;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.HashMap;

public class ProgressMonitor {
    private static HashMap<Workspace,Double> progressCollection = new HashMap<>();

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
}
