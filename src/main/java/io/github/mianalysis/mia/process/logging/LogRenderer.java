package io.github.mianalysis.mia.process.logging;

import java.util.HashMap;

import io.github.mianalysis.mia.object.Workspaces;

public abstract class LogRenderer {
    protected static int progress = 0;
    protected static boolean showProgress = false;
    protected HashMap<Level,Boolean> levelStatus = new HashMap<>();
    
    public enum Level {
        MESSAGE, WARNING, ERROR, DEBUG, MEMORY, STATUS;
    }
    
    public abstract void write(String message, Level level);


    public boolean isWriteEnabled(Level level) {
        return levelStatus.get(level);
    }

    public void setWriteEnabled(Level level, boolean writeEnabled) {
        levelStatus.put(level, writeEnabled);
    }

    public static boolean isShowProgress() {
        return showProgress;
    }

    public static void setShowProgress(boolean showProgress) {
        LogRenderer.showProgress = showProgress;
    }

    public static double getProgress() {
        return progress;
    }

    public static void setProgress(int progress) {
        LogRenderer.progress = progress;
    }

    public static void setProgress(Workspaces workspaces) {
        progress = (int) Math.round(workspaces.getOverallProgress() * 100);
    }
}
