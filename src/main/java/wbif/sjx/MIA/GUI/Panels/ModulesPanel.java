package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.ComponentFactory;
import wbif.sjx.MIA.GUI.ControlObjects.EvalButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleEnabledButton;
import wbif.sjx.MIA.GUI.ControlObjects.ShowOutputButton;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Miscellaneous.GUISeparator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class ModulesPanel extends JScrollPane {
    private JPanel panel;
    private ButtonGroup buttonGroup;

    public ModulesPanel(ButtonGroup buttonGroup) {
        this.buttonGroup = buttonGroup;

        panel = new JPanel();

        setViewportView(panel);

        int frameWidth = GUI.getMinimumFrameWidth();
        int bigButtonSize = GUI.getBigButtonSize();

        // Initialising the scroll panel
        setPreferredSize(new Dimension(frameWidth-45-bigButtonSize, -1));
        setMinimumSize(new Dimension(frameWidth-45-bigButtonSize, -1));
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(10);

        // Initialising the panel for module buttons
        panel.setLayout(new GridBagLayout());
        panel.validate();
        panel.repaint();

        validate();
        repaint();

    }

    public void updatePanel() {
        Analysis analysis = GUI.getAnalysis();
        ComponentFactory componentFactory = GUI.getComponentFactory();
        Module activeModule = GUI.getActiveModule();
        int moduleButtonWidth = GUI.getModuleButtonWidth();

        panel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        boolean expanded = true;
        // Adding module buttons
        ModuleCollection modules = analysis.getModules();
        c.insets = new Insets(2,0,0,0);
        for (int i=0;i<modules.size();i++) {
            Module module = modules.get(i);
            int idx = modules.indexOf(module);
            if (idx == modules.size() - 1) c.weighty = 1;

            JPanel modulePanel = null;
            if (module instanceof GUISeparator) {
                expanded = ((BooleanP) module.getParameter(GUISeparator.EXPANDED_EDITING)).isSelected();
                modulePanel = componentFactory.createEditingSeparator(module, buttonGroup, activeModule, moduleButtonWidth - 25);
            } else {
                if (!expanded) continue;
                modulePanel = componentFactory.createAdvancedModuleControl(module, buttonGroup, activeModule, moduleButtonWidth - 25);
            }

            // If this is the final module, add a gap at the bottom
            if (i==modules.size()-1) modulePanel.setBorder(new EmptyBorder(0,0,5,0));

            panel.add(modulePanel, c);
            c.insets = new Insets(0,0,0,0);
            c.gridy++;

        }

        c.gridy++;
        c.weighty = 1;
        c.fill = GridBagConstraints.VERTICAL;
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(-1,1));
        panel.add(separator, c);

        panel.revalidate();
        panel.repaint();

        revalidate();
        repaint();

    }

    public void updateButtonStates() {
        for (Component panel : panel.getComponents()) {
            if (panel.getClass() == JPanel.class) {
                for (Component component: ((JPanel) panel).getComponents()) {
                    if (component.getClass() == ModuleEnabledButton.class) {
                        ((ModuleEnabledButton) component).updateState();
                    } else if (component.getClass() == ShowOutputButton.class) {
                        ((ShowOutputButton) component).updateState();
                    } else if (component.getClass() == ModuleButton.class) {
                        ((ModuleButton) component).updateState();
                    } else if (component.getClass() == EvalButton.class) {
                        ((EvalButton) component).updateState();
                    }
                }
            }
        }
    }
}
