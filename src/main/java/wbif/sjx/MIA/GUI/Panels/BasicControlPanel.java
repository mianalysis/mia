package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.ComponentFactory;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Hidden.GlobalVariables;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Module.Miscellaneous.GUISeparator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisTester;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class BasicControlPanel extends JScrollPane {
    private static GUISeparator loadSeparator;
    private JPanel panel;

    public BasicControlPanel() {
        panel = new JPanel();
        setViewportView(panel);

        int frameWidth = GUI.getMinimumFrameWidth();
        int bigButtonSize = GUI.getBigButtonSize();

        loadSeparator = new GUISeparator(GUI.getModules());

        // Initialising the scroll panel
        setPreferredSize(new Dimension(frameWidth-30, -1));
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
        GlobalVariables globalVariables = MIA.getGlobalVariables();
        InputControl inputControl = GUI.getModules().getInputControl();
        OutputControl outputControl = GUI.getModules().getOutputControl();

        AnalysisTester.testModule(globalVariables,GUI.getModules());
        AnalysisTester.testModule(inputControl,GUI.getModules());
        AnalysisTester.testModule(outputControl,GUI.getModules());
        AnalysisTester.testModules(GUI.getModules());

        int frameWidth = GUI.getMinimumFrameWidth();
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

        // Check if there are no controls to be displayed
        if (!analysis.hasVisibleParameters()) {
            showUsageMessage();
            return;
        }

        // Adding a separator between the input and main modules
        panel.add(componentFactory.createBasicSeparator(loadSeparator,frameWidth-80),c);

        // Adding global variable options
        if (expanded.isSelected()) {
            c.gridy++;
            JPanel globalVariablesPanel = componentFactory.createBasicModuleControl(globalVariables, frameWidth - 80);
            if (globalVariablesPanel != null) panel.add(globalVariablesPanel, c);
        }

        // Adding input control options
        if (expanded.isSelected()) {
            c.gridy++;
            JPanel inputPanel = componentFactory.createBasicModuleControl(inputControl, frameWidth - 80);
            if (inputPanel != null) panel.add(inputPanel, c);
        }

        // Adding module buttons
        ModuleCollection modules = analysis.getModules();
        for (Module module : modules) {
            // If the module is the special-case GUISeparator, create this module, then return
            JPanel modulePanel = null;
            if (module instanceof GUISeparator) {
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
                modulePanel = componentFactory.createBasicSeparator(module, frameWidth-80);

            } else {
                if (module.isRunnable() || module.invalidParameterIsVisible()) {
                    modulePanel = componentFactory.createBasicModuleControl(module, frameWidth - 80);
                }
            }

            if (modulePanel!=null && (expanded.isSelected() || module instanceof GUISeparator)) {
                c.gridy++;
                panel.add(modulePanel,c);
            }
        }

        JPanel outputPanel =componentFactory.createBasicModuleControl(outputControl,frameWidth-80);
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

    public void showUsageMessage() {
        panel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        // Adding title to help window
        JTextPane usageMessage = new JTextPane();
        usageMessage.setContentType("text/html");
        usageMessage.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        usageMessage.setText("<html><center><font face=\"sans-serif\" size=\"3\">" +
                "To load an existing workflow,<br>click \"Load\" and select a .mia file."+
                "<br><br>" +
                "To start creating a new workflow,<br>go to View > Switch to editing view." +
                "</font></center></html>");
        usageMessage.setEditable(false);
        usageMessage.setBackground(null);
        panel.add(usageMessage);

        panel.revalidate();
        panel.repaint();

    }

}
