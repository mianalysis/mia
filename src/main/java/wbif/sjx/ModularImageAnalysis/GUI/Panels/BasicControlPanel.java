package wbif.sjx.ModularImageAnalysis.GUI.Panels;

import wbif.sjx.ModularImageAnalysis.GUI.ComponentFactory;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Miscellaneous.GUISeparator;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.BooleanP;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandling.Analysis;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class BasicControlPanel extends JScrollPane {
    private static final GUISeparator loadSeparator = new GUISeparator();
    private JPanel panel;

    public BasicControlPanel() {
        panel = new JPanel();
        setViewportView(panel);

        int basicFrameWidth = GUI.getBasicFrameWidth();
        int bigButtonSize = GUI.getBigButtonSize();

        // Initialising the scroll panel
        setPreferredSize(new Dimension(basicFrameWidth-30, -1));
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

        loadSeparator.setNickname("File loading");

    }

    public void updatePanel() {
        int basicFrameWidth = GUI.getBasicFrameWidth();
        Analysis analysis = GUI.getAnalysis();
        ComponentFactory componentFactory = GUI.getComponentFactory();

        panel.removeAll();

        // Only modules below an expanded GUISeparator should be displayed
        BooleanP expanded = ((BooleanP) loadSeparator.getParameter(GUISeparator.EXPANDED_BASIC));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.insets = new Insets(0,5,0,5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Check if there are no modules
        if (analysis.modules.size()==0) return;

        // Adding a separator between the input and main modules
        panel.add(componentFactory.createBasicSeparator(loadSeparator,basicFrameWidth-80),c);

        // Adding input control options
        if (expanded.isSelected()) {
            c.gridy++;
            JPanel inputPanel = componentFactory.createBasicModuleControl(analysis.getInputControl(), basicFrameWidth - 80);
            if (inputPanel != null) panel.add(inputPanel, c);
        }

        // Adding module buttons
        ModuleCollection modules = analysis.getModules();
        for (Module module : modules) {
            // If the module is the special-case GUISeparator, create this module, then return
            JPanel modulePanel = null;
            if (module.getClass().isInstance(new GUISeparator())) {
                // Not all GUI separators are shown on the basic panel
                BooleanP showBasic = (BooleanP) module.getParameter(GUISeparator.SHOW_BASIC);
                if (!showBasic.isSelected()) continue;

                // Adding a blank space before the next separator
                if (expanded.isSelected()) {
                    JPanel blankPanel = new JPanel();
                    blankPanel.setPreferredSize(new Dimension(10, 10));
                    c.gridy++;
                    panel.add(blankPanel, c);
                }

                expanded = (BooleanP) module.getParameter(GUISeparator.EXPANDED_BASIC);
                modulePanel = componentFactory.createBasicSeparator(module, basicFrameWidth-80);
            } else {
                if (module.isRunnable() || module.invalidParameterIsVisible()) {
                    modulePanel = componentFactory.createBasicModuleControl(module, basicFrameWidth - 80);
                }
            }

            if (modulePanel!=null && (expanded.isSelected() || module.getClass().isInstance(new GUISeparator()))) {
                c.gridy++;
                panel.add(modulePanel,c);
            }
        }

        JPanel outputPanel =componentFactory.createBasicModuleControl(analysis.getOutputControl(),basicFrameWidth-80);
        if (outputPanel != null && expanded.isSelected()) {
            c.gridy++;
            panel.add(outputPanel,c);
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
}
