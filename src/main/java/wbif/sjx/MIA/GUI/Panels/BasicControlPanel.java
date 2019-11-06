package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.ComponentFactory;
import wbif.sjx.MIA.GUI.GUI;
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
    private static GUISeparator globalVariablesSeparator;
    private static GUISeparator loadSeparator;
    private JPanel panel;

    private static final int minimumWidth = 400;
    private static final int preferredWidth = 400;


    public BasicControlPanel() {
        panel = new JPanel();
        setViewportView(panel);

        globalVariablesSeparator = new GUISeparator(GUI.getModules());
        loadSeparator = new GUISeparator(GUI.getModules());

        // Initialising the scroll panel
        setMinimumSize(new Dimension(minimumWidth,1));
        setPreferredSize(new Dimension(preferredWidth, 1));
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

        globalVariablesSeparator.setNickname("Global variables");
        loadSeparator.setNickname("File selection");

    }

    public void updatePanel() {
        InputControl inputControl = GUI.getModules().getInputControl();
        OutputControl outputControl = GUI.getModules().getOutputControl();

        AnalysisTester.testModule(inputControl,GUI.getModules());
        AnalysisTester.testModule(outputControl,GUI.getModules());
        AnalysisTester.testModules(GUI.getModules());

        Analysis analysis = GUI.getAnalysis();
        ComponentFactory componentFactory = GUI.getComponentFactory();

        panel.removeAll();

        // Check if there are no controls to be displayed
        if (!analysis.hasVisibleParameters()) {
            showUsageMessage();
            return;
        }

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.insets = new Insets(0,0,0,5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Adding a separator between the input and main modules
        c.insets = new Insets(5,0,0,5);
        panel.add(componentFactory.createBasicSeparator(loadSeparator,preferredWidth),c);
        c.insets = new Insets(0,0,0,5);

        // Only modules below an expanded GUISeparator should be displayed
        BooleanP expanded = (loadSeparator.getParameter(GUISeparator.EXPANDED_BASIC));

        // Adding input control options
        if (expanded.isSelected()) {
            c.gridy++;
            JPanel inputPanel = componentFactory.createBasicModuleControl(inputControl, preferredWidth);
            if (inputPanel != null) panel.add(inputPanel, c);
        }

        // Adding module buttons
        GUISeparator separator = loadSeparator;
        ModuleCollection modules = analysis.getModules();
        for (Module module : modules) {
            // If the module is the special-case GUISeparator, create this module, then return
            JPanel modulePanel = null;
            if (module instanceof GUISeparator) {
                separator = (GUISeparator) module;

                // Not all GUI separators are shown on the basic panel
                BooleanP showBasic = module.getParameter(GUISeparator.SHOW_BASIC);
                if (!showBasic.isSelected()) continue;

                // If this separator doesn't control any visible modules, skip it
                if (((GUISeparator) module).getBasicModules().size() == 0 &! module.canBeDisabled()) continue;

//                // Adding a blank space before the next separator
//                if (expanded.isSelected()) {
//                    JPanel blankPanel = new JPanel();
//                    blankPanel.setPreferredSize(new Dimension(10, 10));
//                    c.gridy++;
//                    panel.add(blankPanel, c);
//                }

                expanded = module.getParameter(GUISeparator.EXPANDED_BASIC);
                modulePanel = componentFactory.createBasicSeparator(module, preferredWidth);

            } else {
                if (separator.isEnabled() && module.isRunnable() || module.invalidParameterIsVisible()) {
                    modulePanel = componentFactory.createBasicModuleControl(module, preferredWidth);
                }
            }

            if (modulePanel!=null && (expanded.isSelected())) {
                c.gridy++;
                panel.add(modulePanel,c);
            }

            if (module instanceof GUISeparator) {
                c.gridy++;
                panel.add(modulePanel,c);
            }
        }

        JPanel outputPanel =componentFactory.createBasicModuleControl(outputControl,preferredWidth);
        if (outputPanel != null && expanded.isSelected()) {
            c.gridy++;
            panel.add(outputPanel,c);
        }

        c.gridy++;
        c.weighty = 1;
        c.insets = new Insets(20,0,0,0);
        c.fill = GridBagConstraints.VERTICAL;
        JSeparator jSeparator = new JSeparator();
        jSeparator.setPreferredSize(new Dimension(-1,1));
        panel.add(jSeparator, c);

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
        usageMessage.setOpaque(false);
        panel.add(usageMessage);

        panel.revalidate();
        panel.repaint();

    }

    public static int getMinimumWidth() {
        return minimumWidth;
    }

    public static int getPreferredWidth() {
        return preferredWidth;
    }
}
