package wbif.sjx.MIA.GUI.Panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Core.InputControl;
import wbif.sjx.MIA.Module.Core.OutputControl;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisTester;

public class ModulePanel extends JPanel {
    private final InputOutputPanel inputPanel = new InputOutputPanel();
    private final InputOutputPanel outputPanel = new InputOutputPanel();
    private final ModuleListPanel moduleListPanel = new ModuleListPanel();

    private static final int minimumWidth = 310;

    public static int getMinimumWidth() {
        return minimumWidth;
    }

    public ModulePanel() {
        setLayout(new GridBagLayout());

        setMaximumSize(new Dimension(minimumWidth, Integer.MAX_VALUE));
        setMinimumSize(new Dimension(minimumWidth, 1));
        setPreferredSize(new Dimension(minimumWidth, Integer.MAX_VALUE));
        
        GridBagConstraints c = new GridBagConstraints();

        // Initialising the input control panel
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        add(inputPanel, c);

        // Initialising the module list panel
        c.gridy++;
        c.weighty = 1;
        c.insets = new Insets(5, 0, 0, 0);
        c.fill = GridBagConstraints.BOTH;
        add(moduleListPanel, c);

        // Initialising the output control panel
        c.gridy++;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(outputPanel, c);

    }

    public void updatePanel() {
        Analysis analysis = GUI.getAnalysis();
        InputControl inputControl = analysis.getModules().getInputControl();
        OutputControl outputControl = analysis.getModules().getOutputControl();

        boolean runnable = AnalysisTester.testModule(inputControl, analysis.getModules());
        inputControl.setRunnable(runnable);
        inputPanel.updateButtonState();
        inputPanel.updatePanel(inputControl);

        runnable = AnalysisTester.testModule(outputControl, analysis.getModules());
        outputControl.setRunnable(runnable);
        outputPanel.updateButtonState();
        outputPanel.updatePanel(outputControl);

        moduleListPanel.updateButtonStates();
        moduleListPanel.updatePanel();

    }

    public void updateModuleStates() {
        inputPanel.updateButtonState();
        moduleListPanel.updateButtonStates();
        outputPanel.updateButtonState();
    }
}
