package io.github.mianalysis.mia.process.logging;

public abstract class ProgressBar {
    public static ProgressBar activeProgressBar = null;

    protected abstract void updateProgressBar();
    protected abstract void updateProgressBar(int val);
    
    public static void setActive(ProgressBar newProgressBar) {
        activeProgressBar = newProgressBar;
    }

    public static ProgressBar getActive() {
        return activeProgressBar;
    }

    public static void update() {
        if (activeProgressBar != null)
            activeProgressBar.updateProgressBar(0);
    }

    public static void update(int val) {
        if (activeProgressBar != null)
            activeProgressBar.updateProgressBar(val);
    }

    public static void update(double val) {
        if (activeProgressBar != null)
            activeProgressBar.updateProgressBar((int) (val*100));
    }
}
