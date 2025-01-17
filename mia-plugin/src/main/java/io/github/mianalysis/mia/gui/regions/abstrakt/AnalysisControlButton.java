package io.github.mianalysis.mia.gui.regions.abstrakt;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.formdev.flatlaf.FlatClientProperties;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.GUIAnalysisHandler;

/**
 * Created by steph on 28/07/2017.
 */
public class AnalysisControlButton extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 258574628718719585L;
    public static final String LOAD_MODULES = "Load";
    public static final String SAVE_MODULES = "Save";
    public static final String START_ANALYSIS = "Run";
    public static final String STOP_ANALYSIS = "Stop";


    public AnalysisControlButton(String command, int buttonSize) {
        putClientProperty( FlatClientProperties.STYLE, "arc: 12" );
        addActionListener(this);
        setFocusPainted(false);
        setMargin(new Insets(0,0,0,0));
        setFont(GUI.getDefaultFont().deriveFont(14f));
        setText(command);
        setPreferredSize(new Dimension(buttonSize, buttonSize));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (getText()) {
            case LOAD_MODULES:
                GUIAnalysisHandler.loadModules();
                break;

            case SAVE_MODULES:
                GUIAnalysisHandler.saveModules();
                break;

            case START_ANALYSIS:
                GUIAnalysisHandler.runAnalysis();
                break;

            case STOP_ANALYSIS:
                GUIAnalysisHandler.stopAnalysis();
                break;
        }
    }
}
