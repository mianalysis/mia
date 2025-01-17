package io.github.mianalysis.mia.gui.regions.extrapanels.search;

import java.awt.Dimension;

import javax.swing.JCheckBox;

import io.github.mianalysis.mia.gui.GUI;

public class SearchIncludeCheckbox extends JCheckBox {
    public SearchIncludeCheckbox(String text) {
        setText(text);
        setFont(GUI.getDefaultFont().deriveFont(14f));
        setPreferredSize(new Dimension(0, 26));
        setMinimumSize(new Dimension(0, 26));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
    }
}
