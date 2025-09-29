package io.github.mianalysis.mia.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.regions.parameterlist.DisableRefsButton;
import io.github.mianalysis.mia.gui.regions.parameterlist.DisableableCheck;
import io.github.mianalysis.mia.gui.regions.parameterlist.EnableRefsButton;
import io.github.mianalysis.mia.gui.regions.parameterlist.ExportCheck;
import io.github.mianalysis.mia.gui.regions.parameterlist.ExportEnableButton;
import io.github.mianalysis.mia.gui.regions.parameterlist.ExportName;
import io.github.mianalysis.mia.gui.regions.parameterlist.ResetExport;
import io.github.mianalysis.mia.gui.regions.parameterlist.ShowProcessingTitleCheck;
import io.github.mianalysis.mia.gui.regions.parameterlist.VisibleCheck;
import io.github.mianalysis.mia.gui.regions.processingpanel.ModuleTitle;
import io.github.mianalysis.mia.gui.regions.workflowmodules.ModuleEnabledButton;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.module.system.GUISeparator;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ObjMeasurementSelectorP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.object.refs.abstrakt.ExportableRef;
import io.github.mianalysis.mia.object.refs.abstrakt.SummaryRef;
import io.github.mianalysis.mia.object.refs.collections.Refs;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.SwingPreferences;

/**
 * Created by Stephen on 23/06/2017.
 */
public class ComponentFactory {
    private int elementHeight;

    private static final ImageIcon downArrow = new ImageIcon(
            ComponentFactory.class.getResource("/icons/downarrow_darkblue_12px.png"), "");
    private static final ImageIcon downArrowDM = new ImageIcon(
            ComponentFactory.class.getResource("/icons/downarrow_darkblueDM_12px.png"), "");
    private static final ImageIcon rightArrow = new ImageIcon(
            ComponentFactory.class.getResource("/icons/rightarrow_darkblue_12px.png"), "");
    private static final ImageIcon rightArrowDM = new ImageIcon(
            ComponentFactory.class.getResource("/icons/rightarrow_darkblueDM_12px.png"), "");
    private static final ImageIcon leftArrow = new ImageIcon(
            ComponentFactory.class.getResource("/icons/leftarrow_darkblue_12px.png"), "");
    private static final ImageIcon leftArrowDM = new ImageIcon(
            ComponentFactory.class.getResource("/icons/leftarrow_darkblueDM_12px.png"), "");
    // private static final ImageIcon circle = new ImageIcon(
    // ComponentFactory.class.getResource("/Icons/dot_blue_12px.png"), "");
    private static final ImageIcon warningIcon = new ImageIcon(
            ComponentFactory.class.getResource("/icons/warning_red_12px.png"), "");
    private static final ImageIcon warningIconDM = new ImageIcon(
            ComponentFactory.class.getResource("/icons/warning_redDM_12px.png"), "");

    public ComponentFactory(int elementHeight) {
        this.elementHeight = elementHeight;
    }

    public JPanel createParameterControl(Parameter parameter, Modules modules, Module module, boolean editable) {
        JPanel paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 10, 0, 0);

        ParameterControl parameterControl = parameter.getControl();
        parameterControl.updateControl();
        JComponent parameterComponent = parameterControl.getComponent();

        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        if (parameter instanceof MessageP || parameter instanceof ObjMeasurementSelectorP) {
            String value = parameter.getAlternativeString();
            parameterComponent.setToolTipText(value == null ? "" : value);
            c.insets = new Insets(10, 3, 10, 10);
            paramPanel.add(parameterComponent, c);

            if (editable) {
                c.insets = new Insets(2, 5, 0, 5);
                c.gridx++;
                c.weightx = 0;
                c.anchor = GridBagConstraints.EAST;
                VisibleCheck visibleCheck = new VisibleCheck(parameter);
                visibleCheck.setPreferredSize(new Dimension(elementHeight, elementHeight));
                paramPanel.add(visibleCheck, c);
            }

        } else if (parameter instanceof SeparatorP) {
            Parameter firstParameter = module.updateAndGetParameters().values().iterator().next();
            // Add an extra space above a separator, unless it's the first parameter. We
            // also have to check if the first parameter is a ParameterGroup and if so,
            // whether the first parameter within this group is also a separator (this is
            // ugly, but works).
            if (firstParameter == parameter
                    || (firstParameter instanceof ParameterGroup && ((ParameterGroup) firstParameter)
                            .getCollections(true).values().iterator().next().values().iterator().next() == parameter)) {
                c.insets = new Insets(0, 10, 10, 10);
            } else {
                c.insets = new Insets(30, 10, 10, 10);
            }
            paramPanel.add(parameterComponent, c);

        } else {
            JLabel parameterName = editable ? new ExportName(parameter) : new JLabel(parameter.getNickname());
            parameterName.setBorder(null);
            parameterName.setOpaque(false);
            parameterName.setPreferredSize(new Dimension(0, elementHeight));
            parameterName.setFont(GUI.getDefaultFont().deriveFont(14f));
            parameterName
                    .setToolTipText("<html><div style=\"width:500;\">" + parameter.getDescription() + "</div></html>");
            paramPanel.add(parameterName, c);

            if (!parameter.isValid()) {
                parameterName.setForeground(Colours.getRed(isDark));
                if (isDark)
                    parameterName.setIcon(warningIconDM);
                else
                    parameterName.setIcon(warningIcon);
            }

            c.gridx++;
            c.weightx = 1;
            c.anchor = GridBagConstraints.EAST;
            if (parameterComponent != null) {
                String value = parameter.getAlternativeString();
                parameterComponent.setToolTipText(value == null ? "" : value);
                if (!(parameter instanceof TextAreaP))
                    parameterComponent.setPreferredSize(new Dimension(0, elementHeight));
                paramPanel.add(parameterComponent, c);
            }

            if (editable) {
                c.insets = new Insets(2, 10, 0, 10);
                c.gridx++;
                c.weightx = 0;
                c.anchor = GridBagConstraints.EAST;
                VisibleCheck visibleCheck = new VisibleCheck(parameter);
                visibleCheck.setPreferredSize(new Dimension(elementHeight, elementHeight));
                paramPanel.add(visibleCheck, c);
            }
        }

        return paramPanel;

    }

    public JPanel createParametersTopRow(Module activeModule) {
        JPanel paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 0, 0);

        // Adding the nickname control to the top of the panel
        ExportName moduleName = new ExportName(activeModule);
        Font font = GUI.getDefaultFont().deriveFont(Font.BOLD, 14f);
        if (activeModule.isDeprecated()) {
            Map attributes = font.getAttributes();
            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            font = new Font(attributes);
        }
        moduleName.setFont(font);
        // moduleName.setForeground(Color.BLACK);
        moduleName.setToolTipText("<html><div style=\"width:500;\">" + activeModule.getDescription() + "</div></html>");
        paramPanel.add(moduleName, c);

        JSeparator separator = new JSeparator();
        separator.setOrientation(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(5, 25));
        c.gridx++;
        c.weightx = 0;
        paramPanel.add(separator, c);

        ShowProcessingTitleCheck showProcessingTitleCheck = new ShowProcessingTitleCheck(activeModule);
        showProcessingTitleCheck.setFont(GUI.getDefaultFont().deriveFont(14f));
        if (activeModule.getClass() == GUISeparator.class) {
            showProcessingTitleCheck.setEnabled(false);
            showProcessingTitleCheck.setOpaque(false);
        }
        c.gridx++;
        c.anchor = GridBagConstraints.EAST;
        paramPanel.add(showProcessingTitleCheck, c);

        separator = new JSeparator();
        separator.setOrientation(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(5, 25));
        c.gridx++;
        c.weightx = 0;
        c.insets = new Insets(10, 10, 0, 10);
        paramPanel.add(separator, c);

        DisableableCheck disableableCheck = new DisableableCheck(activeModule);
        disableableCheck.setFont(GUI.getDefaultFont().deriveFont(14f));
        if (activeModule.getClass() == InputControl.class) {
            disableableCheck.setEnabled(false);
            disableableCheck.setOpaque(false);
        }
        c.gridx++;
        c.anchor = GridBagConstraints.EAST;
        paramPanel.add(disableableCheck, c);

        return paramPanel;

    }

    public JPanel createProcessingModuleHeading(Module module) {
        JPanel modulePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Adding the state/evaluate button
        c.gridx = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        ModuleEnabledButton moduleEnabledButton = new ModuleEnabledButton(module);
        moduleEnabledButton.setPreferredSize(new Dimension(elementHeight, elementHeight));
        moduleEnabledButton.setEnabled(module.canBeDisabled());
        moduleEnabledButton.setBorderPainted(false);
        moduleEnabledButton.setOpaque(false);
        moduleEnabledButton.setContentAreaFilled(false);
        c.insets = new Insets(0, 19, 0, 0);
        modulePanel.add(moduleEnabledButton, c);

        ModuleTitle title = new ModuleTitle(module);
        // title.setForeground(Color.BLACK);
        title.setToolTipText("<html><div style=\"width:500px;\">" + module.getDescription() + "</div></html>");
        c.insets = new Insets(0, 0, 0, 0);
        c.weightx = 1;
        c.gridx++;
        modulePanel.add(title, c);

        return modulePanel;

    }

    public JPanel createProcessingSeparator(Module module) {
        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        ModuleEnabledButton moduleEnabledButton = new ModuleEnabledButton(module);
        moduleEnabledButton.setPreferredSize(new Dimension(elementHeight, elementHeight));
        moduleEnabledButton.setMinimumSize(new Dimension(elementHeight, elementHeight));
        moduleEnabledButton.setEnabled(module.canBeDisabled());
        moduleEnabledButton.setBorderPainted(false);
        moduleEnabledButton.setOpaque(false);
        moduleEnabledButton.setContentAreaFilled(false);
        panel.add(moduleEnabledButton, c);

        BooleanP expandedProcessing = (BooleanP) module.getParameter(GUISeparator.EXPANDED_PROCESSING);
        JLabel leftArrowLabel = new JLabel();
        if (expandedProcessing.isSelected()) {
            if (isDark)
                leftArrowLabel.setIcon(downArrowDM);
            else
                leftArrowLabel.setIcon(downArrow);
        } else {
            if (isDark)
                leftArrowLabel.setIcon(rightArrowDM);
            else
                leftArrowLabel.setIcon(rightArrow);
        }

        c.insets = new Insets(0, 0, 0, 10);
        c.gridx++;
        panel.add(leftArrowLabel, c);

        JSeparator separatorLeft = new JSeparator();
        separatorLeft.setForeground(Colours.getDarkBlue(isDark));
        separatorLeft.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Colours.getDarkBlue(isDark)));
        c.weightx = 1;
        c.gridx++;
        panel.add(separatorLeft, c);

        JLabel label = new JLabel();
        label.setText(module.getNickname());
        label.setFont(GUI.getDefaultFont().deriveFont(14f));
        label.setForeground(Colours.getDarkBlue(isDark));
        c.weightx = 0;
        c.gridx++;
        c.insets = new Insets(0, 0, 0, 0);
        panel.add(label, c);

        JSeparator separatorRight = new JSeparator();
        separatorRight.setForeground(Colours.getDarkBlue(isDark));
        separatorRight.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Colours.getDarkBlue(isDark)));
        c.weightx = 1;
        c.gridx++;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(0, 10, 0, 0);
        panel.add(separatorRight, c);

        JLabel rightArrowLabel = new JLabel();
        if (expandedProcessing.isSelected()) {
            if (isDark)
                rightArrowLabel.setIcon(downArrowDM);
            else
                rightArrowLabel.setIcon(downArrow);
        } else {
            if (isDark)
                rightArrowLabel.setIcon(leftArrowDM);
            else
                rightArrowLabel.setIcon(leftArrow);
        }

        c.weightx = 0;
        c.gridx++;
        panel.add(rightArrowLabel, c);

        int labelWidth = label.getPreferredSize().width;
        label.setPreferredSize(new Dimension(labelWidth + 20, 25));
        label.setHorizontalAlignment(JLabel.CENTER);

        // Adding an MouseListener to check if it was clicked
        panel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    BooleanP expandedProcessing = (BooleanP) module.getParameter(GUISeparator.EXPANDED_PROCESSING);
                    expandedProcessing.flipBoolean();
                    GUI.updateModules(false, null);
                    GUI.updateParameters(false, null);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        return panel;

    }

    public JPanel createProcessingModuleControl(Module module) {
        // Only displaying the module title if it has at least one visible parameter
        if (!module.hasVisibleParameters() & !module.canBeDisabled())
            return null;

        JPanel modulePanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        if (module.canBeDisabled() || module.canShowProcessingTitle()) {
            JPanel titlePanel = createProcessingModuleHeading(module);
            modulePanel.add(titlePanel, c);
        }

        // If there are visible parameters, but the module isn't enabled only return the
        // heading
        if (!module.isEnabled())
            return modulePanel;
        if (!module.isRunnable() & !module.invalidParameterIsVisible())
            return modulePanel;

        c.insets = new Insets(0, 40, 0, 0);
        addProcessingParameters(module, module.updateAndGetParameters(), modulePanel, c, false);

        return modulePanel;

    }

    private void addProcessingParameters(Module module, Parameters parameters, JPanel modulePanel, GridBagConstraints c,
            boolean editable) {
        for (Parameter parameter : parameters.values()) {
            if (parameter.getClass() == ParameterGroup.class) {
                LinkedHashMap<Integer, Parameters> collections = ((ParameterGroup) parameter).getCollections(true);
                for (Parameters collection : collections.values())
                    addProcessingParameters(module, collection, modulePanel, c, editable);

            }

            if (parameter.isVisible()) {
                JPanel paramPanel = createParameterControl(parameter, GUI.getModules(), module, editable);
                c.gridy++;
                modulePanel.add(paramPanel, c);
            }
        }
    }

    private JPanel createSummaryExportLabels(boolean includeSummary) {
        Parameters outputParameters = GUI.getModules().getOutputControl().updateAndGetParameters();
        String exportMode = outputParameters.getValue(OutputControl.EXPORT_MODE, null);
        BooleanP exportIndividual = outputParameters.getParameter(OutputControl.EXPORT_INDIVIDUAL_OBJECTS);
        BooleanP exportSummary = outputParameters.getParameter(OutputControl.EXPORT_SUMMARY);

        JPanel labelPanel = new JPanel(new GridBagLayout());
        labelPanel.setPreferredSize(new Dimension(200, 25));
        labelPanel.setOpaque(false);

        // If we're not exporting anything, skip this
        if (exportMode.equals(OutputControl.ExportModes.NONE))
            return labelPanel;

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JLabel exportLabel = new JLabel("Ind");
        exportLabel.setPreferredSize(new Dimension(40, 25));
        exportLabel.setFont(GUI.getDefaultFont().deriveFont(14f));
        exportLabel.setEnabled(exportIndividual.isSelected());
        exportLabel.setOpaque(false);
        c.gridx++;
        labelPanel.add(exportLabel, c);

        if (includeSummary) {
            exportLabel = new JLabel("Mean");
            exportLabel.setPreferredSize(new Dimension(40, 25));
            exportLabel.setFont(GUI.getDefaultFont().deriveFont(14f));
            exportLabel.setEnabled(exportSummary.isSelected());
            c.gridx++;
            labelPanel.add(exportLabel, c);

            exportLabel = new JLabel("Min");
            exportLabel.setPreferredSize(new Dimension(40, 25));
            exportLabel.setFont(GUI.getDefaultFont().deriveFont(14f));
            exportLabel.setEnabled(exportSummary.isSelected());
            c.gridx++;
            labelPanel.add(exportLabel, c);

            exportLabel = new JLabel("Max");
            exportLabel.setPreferredSize(new Dimension(40, 25));
            exportLabel.setFont(GUI.getDefaultFont().deriveFont(14f));
            exportLabel.setEnabled(exportSummary.isSelected());
            c.gridx++;
            labelPanel.add(exportLabel, c);

            exportLabel = new JLabel("Sum");
            exportLabel.setPreferredSize(new Dimension(40, 25));
            exportLabel.setFont(GUI.getDefaultFont().deriveFont(14f));
            exportLabel.setEnabled(exportSummary.isSelected());
            c.gridx++;
            labelPanel.add(exportLabel, c);

            exportLabel = new JLabel("Std");
            exportLabel.setPreferredSize(new Dimension(40, 25));
            exportLabel.setFont(GUI.getDefaultFont().deriveFont(14f));
            exportLabel.setEnabled(exportSummary.isSelected());
            c.gridx++;
            labelPanel.add(exportLabel, c);
        }

        return labelPanel;

    }

    private JPanel createExportControls(ExportableRef ref, ExportCheck.Type type) {
        Parameters outputParameters = GUI.getModules().getOutputControl().updateAndGetParameters();
        String exportMode = outputParameters.getValue(OutputControl.EXPORT_MODE, null);
        BooleanP exportIndividual = (BooleanP) outputParameters.getParameter(OutputControl.EXPORT_INDIVIDUAL_OBJECTS);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setPreferredSize(new Dimension(200, 25));
        controlPanel.setOpaque(false);

        // If we're not exporting anything, skip this
        if (exportMode.equals(OutputControl.ExportModes.NONE))
            return controlPanel;

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        ExportCheck.Statistic statistic = ExportCheck.Statistic.INDIVIDUAL;
        ExportCheck exportCheck = new ExportCheck(ref, statistic, type);
        exportCheck.setSelected(ref.isExportIndividual());
        exportCheck.setPreferredSize(new Dimension(40, 25));
        exportCheck.setEnabled(exportIndividual.isSelected() && ref.isExportGlobal());
        c.gridx++;
        controlPanel.add(exportCheck, c);

        return controlPanel;

    }

    private JPanel createExportControls(SummaryRef ref, ExportCheck.Type type) {
        Parameters outputParameters = GUI.getModules().getOutputControl().updateAndGetParameters();
        BooleanP exportIndividual = (BooleanP) outputParameters.getParameter(OutputControl.EXPORT_INDIVIDUAL_OBJECTS);
        BooleanP exportSummary = (BooleanP) outputParameters.getParameter(OutputControl.EXPORT_SUMMARY);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setPreferredSize(new Dimension(200, 25));
        controlPanel.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        ExportCheck.Statistic statistic = ExportCheck.Statistic.INDIVIDUAL;
        ExportCheck exportCheck = new ExportCheck(ref, statistic, type);
        exportCheck.setSelected(ref.isExportIndividual());
        exportCheck.setPreferredSize(new Dimension(40, 25));
        exportCheck.setEnabled(exportIndividual.isSelected() && ref.isExportGlobal());
        c.gridx++;
        controlPanel.add(exportCheck, c);

        statistic = ExportCheck.Statistic.MEAN;
        exportCheck = new ExportCheck(ref, statistic, type);
        exportCheck.setSelected(ref.isExportMean());
        exportCheck.setPreferredSize(new Dimension(40, 25));
        exportCheck.setEnabled(exportSummary.isSelected() && ref.isExportGlobal());
        c.gridx++;
        controlPanel.add(exportCheck, c);

        statistic = ExportCheck.Statistic.MIN;
        exportCheck = new ExportCheck(ref, statistic, type);
        exportCheck.setSelected(ref.isExportMin());
        exportCheck.setPreferredSize(new Dimension(40, 25));
        exportCheck.setEnabled(exportSummary.isSelected() && ref.isExportGlobal());
        c.gridx++;
        controlPanel.add(exportCheck, c);

        statistic = ExportCheck.Statistic.MAX;
        exportCheck = new ExportCheck(ref, statistic, type);
        exportCheck.setSelected(ref.isExportMax());
        exportCheck.setPreferredSize(new Dimension(40, 25));
        exportCheck.setEnabled(exportSummary.isSelected() && ref.isExportGlobal());
        c.gridx++;
        controlPanel.add(exportCheck, c);

        statistic = ExportCheck.Statistic.SUM;
        exportCheck = new ExportCheck(ref, statistic, type);
        exportCheck.setSelected(ref.isExportSum());
        exportCheck.setPreferredSize(new Dimension(40, 25));
        exportCheck.setEnabled(exportSummary.isSelected() && ref.isExportGlobal());
        c.gridx++;
        controlPanel.add(exportCheck, c);

        statistic = ExportCheck.Statistic.STD;
        exportCheck = new ExportCheck(ref, statistic, type);
        exportCheck.setSelected(ref.isExportStd());
        exportCheck.setPreferredSize(new Dimension(40, 25));
        exportCheck.setEnabled(exportSummary.isSelected() && ref.isExportGlobal());
        c.gridx++;
        controlPanel.add(exportCheck, c);

        return controlPanel;

    }

    // public JPanel createGlobalExportControl(ExportableRef ref) {
    // JPanel summaryPanel = new JPanel(new GridBagLayout());
    //
    // GridBagConstraints c = new GridBagConstraints();
    // c.gridx = 0;
    // c.gridy = 0;
    // c.anchor = GridBagConstraints.EAST;
    // c.fill = GridBagConstraints.HORIZONTAL;
    // c.insets = new Insets(5,5,0,0);
    //
    // JSeparator separator= new JSeparator();
    // separator.setOrientation(JSeparator.HORIZONTAL);
    // separator.setPreferredSize(new Dimension(elementHeight,-1));
    // c.weightx = 1;
    // summaryPanel.addRef(separator,c);
    //
    // JPanel controlPanel = createExportControls(ref,ExportCheck.Type.ALL);
    // c.weightx = 0;
    // c.gridx++;
    // summaryPanel.addRef(controlPanel,c);
    //
    // separator= new JSeparator();
    // separator.setOrientation(JSeparator.HORIZONTAL);
    // separator.setPreferredSize(new Dimension(elementHeight,-1));
    // c.gridx++;
    // summaryPanel.addRef(separator,c);
    //
    // return summaryPanel;
    // }

    public JPanel createRefExportHeader(String name, Refs refs, boolean includeSummary) {
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(10, 10, 0, 0);
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        if (refs != null) {
            EnableRefsButton enableButton = new EnableRefsButton(refs);
            enableButton.setPreferredSize(new Dimension(elementHeight, elementHeight));
            headerPanel.add(enableButton, c);

            DisableRefsButton disableButton = new DisableRefsButton(refs);
            disableButton.setPreferredSize(new Dimension(elementHeight, elementHeight));
            c.gridx++;
            headerPanel.add(disableButton, c);

        } else {
            c.gridx = -1;
        }

        JLabel headerName = new JLabel(name);
        headerName.setFont(GUI.getDefaultFont().deriveFont(14f));
        headerName.setPreferredSize(new Dimension(-1, elementHeight));
        headerName.setOpaque(false);        
        headerName.setBorder(null);
        c.gridx++;
        c.weightx = 1;
        headerPanel.add(headerName, c);

        JPanel labelPanel = createSummaryExportLabels(includeSummary);
        c.gridx++;
        c.weightx = 0;
        headerPanel.add(labelPanel, c);

        JSeparator separator = new JSeparator();
        separator.setOrientation(JSeparator.HORIZONTAL);
        separator.setPreferredSize(new Dimension(elementHeight, -1));
        c.gridx++;
        c.anchor = GridBagConstraints.EAST;
        headerPanel.add(separator, c);

        return headerPanel;

    }

    public JPanel createSingleRefControl(ExportableRef ref) {
        JPanel measurementPanel = new JPanel(new GridBagLayout());
        measurementPanel.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 0, 0);

        ExportEnableButton enabledButton = new ExportEnableButton(ref);
        enabledButton.setPreferredSize(new Dimension(elementHeight, elementHeight));
        measurementPanel.add(enabledButton, c);

        ExportName exportName = new ExportName(ref);
        exportName.setFont(GUI.getDefaultFont().deriveFont(14f));
        exportName.setPreferredSize(new Dimension(-1, elementHeight));
        exportName.setToolTipText("<html><div style=\"width:500px;\">" + ref.getDescription() + "</div></html>");
        exportName.setEnabled(ref.isExportGlobal());
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        measurementPanel.add(exportName, c);

        JPanel controlPanel = createExportControls(ref, ExportCheck.Type.SINGLE);
        c.weightx = 0;
        c.gridx++;
        measurementPanel.add(controlPanel, c);

        ResetExport resetExport = new ResetExport(ref);
        resetExport.setPreferredSize(new Dimension(elementHeight, elementHeight));
        resetExport.setEnabled(ref.isExportGlobal());
        c.gridx++;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(10, 10, 0, 10);
        measurementPanel.add(resetExport, c);

        return measurementPanel;

    }

    public JPanel createSingleSummaryRefControl(SummaryRef ref) {
        JPanel measurementPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 0, 0);

        ExportEnableButton enabledButton = new ExportEnableButton(ref);
        enabledButton.setPreferredSize(new Dimension(elementHeight, elementHeight));
        measurementPanel.add(enabledButton, c);

        ExportName exportName = new ExportName(ref);
        exportName.setFont(GUI.getDefaultFont().deriveFont(14f));
        exportName.setPreferredSize(new Dimension(-1, elementHeight));
        exportName.setToolTipText("<html><div style=\"width:500px;\">" + ref.getDescription() + "</div></html>");
        exportName.setEnabled(ref.isExportGlobal());
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        measurementPanel.add(exportName, c);

        JPanel controlPanel = createExportControls(ref, ExportCheck.Type.SINGLE);
        c.weightx = 0;
        c.gridx++;
        measurementPanel.add(controlPanel, c);

        ResetExport resetExport = new ResetExport(ref);
        resetExport.setPreferredSize(new Dimension(elementHeight, elementHeight));
        resetExport.setEnabled(ref.isExportGlobal());
        c.gridx++;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(10, 10, 0, 10);
        measurementPanel.add(resetExport, c);

        return measurementPanel;

    }
}
