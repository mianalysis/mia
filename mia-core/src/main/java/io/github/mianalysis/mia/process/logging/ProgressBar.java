package io.github.mianalysis.mia.process.logging;

public abstract class ProgressBar {
    public static ProgressBar activeProgressBar = null;

    public abstract void updateProgressBar();
    public abstract void updateProgressBar(int val);
    
    public static void setActiveProgressBar(ProgressBar newProgressBar) {
        activeProgressBar = newProgressBar;
    }

    public static ProgressBar getActiveProgressBar() {
        return activeProgressBar;
    }
}
