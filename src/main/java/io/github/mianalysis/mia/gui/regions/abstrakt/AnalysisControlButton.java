package io.github.mianalysis.mia.gui.regions.abstrakt;

import io.github.mianalysis.mia.gui.GUIAnalysisHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by steph on 28/07/2017.
 */
public class AnalysisControlButton extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 258574628718719585L;
    public static final String LOAD_ANALYSIS = "Load";
    public static final String SAVE_ANALYSIS = "Save";
    public static final String START_ANALYSIS = "Run";
    public static final String STOP_ANALYSIS = "Stop";


    public AnalysisControlButton(String command, int buttonSize) {
        addActionListener(this);
        setFocusPainted(false);
        setMargin(new Insets(0,0,0,0));
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setText(command);
        setPreferredSize(new Dimension(buttonSize, buttonSize));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (getText()) {
            case LOAD_ANALYSIS:
                GUIAnalysisHandler.loadAnalysis();
                break;

            case SAVE_ANALYSIS:
                GUIAnalysisHandler.saveAnalysis();
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
