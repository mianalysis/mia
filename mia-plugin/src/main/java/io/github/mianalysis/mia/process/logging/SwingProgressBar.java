package io.github.mianalysis.mia.process.logging;

import io.github.mianalysis.mia.gui.GUI;

public class SwingProgressBar extends ProgressBar {
    @Override
    public void updateProgressBar(int val) {
        GUI.updateProgressBar(val);
    }

    @Override
    public void updateProgressBar() {
        GUI.updateProgressBar();
    }
}
