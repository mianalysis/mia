package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.*;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Miscellaneous.GUISeparator;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.BooleanP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.TextDisplayP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by sc13967 on 23/06/2017.
 */
public class ComponentFactory {
    private int elementHeight;

    private static final ImageIcon downArrow = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/downarrow_blue_12px.png"), "");
    private static final ImageIcon rightArrow = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/rightarrow_blue_12px.png"), "");
    private static final ImageIcon leftArrow = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/leftarrow_blue_12px.png"), "");

    public ComponentFactory(int elementHeight) {
        this.elementHeight = elementHeight;
    }

    public JPanel createParameterControl(Parameter parameter, ModuleCollection modules, Module module) {
        JPanel paramPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2,5,0,0);

        JLabel parameterName = new JLabel(parameter.getName());
        parameterName.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        parameterName.setPreferredSize(new Dimension(0,elementHeight));
        parameterName.setBorder(null);
        parameterName.setOpaque(false);
        paramPanel.add(parameterName, c);

        if (parameter.isValid()) {
            parameterName.setForeground(Color.BLACK);
        } else {
            parameterName.setForeground(Color.RED);
        }

        ParameterControl parameterControl = parameter.getControl();
        JComponent parameterComponent = parameterControl.getComponent();

        // Adding the input component
        c.gridx++;
        c.weightx=1;
        c.anchor = GridBagConstraints.EAST;
        if (parameterComponent != null) {
            String value = parameter.getValueAsString();
            parameterComponent.setToolTipText(value == null ? "" : value);
            if (!parameter.getClass().isInstance(TextDisplayP.class)) parameterComponent.setPreferredSize(new Dimension(0,elementHeight));
            paramPanel.add(parameterComponent, c);
        }

        return paramPanel;

    }

    public JPanel createAdvancedModuleControl(Module module, ButtonGroup group, Module activeModule, int panelWidth) {
        JPanel modulePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Adding the module enabled checkbox
        c.gridx = 0;
        c.weightx = 0;
        c.insets = new Insets(2, 2, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        ModuleEnabledButton enabledCheck = new ModuleEnabledButton(module);
        enabledCheck.setPreferredSize(new Dimension(elementHeight,elementHeight));
        modulePanel.add(enabledCheck,c);

        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        ShowOutputButton showOutput = new ShowOutputButton(module);
        showOutput.setPreferredSize(new Dimension(elementHeight,elementHeight));
        modulePanel.add(showOutput,c);

        // Adding the main module button
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        ModuleButton button = new ModuleButton(module);
        button.setPreferredSize(new Dimension(panelWidth-3*elementHeight+6,elementHeight));
        group.add(button);
        if (activeModule != null) {
            if (module == activeModule) button.setSelected(true);
        }
        modulePanel.add(button,c);

        // Adding the state/evaluate button
        c.gridx++;
        c.weightx = 0;
        c.insets = new Insets(2, 2, 0, 0);
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        EvalButton evalButton = new EvalButton(module);
        evalButton.setPreferredSize(new Dimension(elementHeight,elementHeight));
        modulePanel.add(evalButton,c);

        return modulePanel;

    }

    public JPanel createEditingSeparator(Module module, ButtonGroup group, Module activeModule, int panelWidth) {
        JPanel modulePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Adding the module enabled checkbox
        c.gridx = 0;
        c.weightx = 0;
        c.insets = new Insets(2, 2, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        ModuleEnabledButton enabledCheck = new ModuleEnabledButton(module);
        enabledCheck.setPreferredSize(new Dimension(elementHeight,elementHeight));
        modulePanel.add(enabledCheck,c);

        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        SeparatorButton leftArrowButton = new SeparatorButton(module,true);
        leftArrowButton.setPreferredSize(new Dimension(elementHeight,elementHeight));
        modulePanel.add(leftArrowButton,c);

        // Adding the main module button
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        ModuleButton button = new ModuleButton(module);
        button.setPreferredSize(new Dimension(panelWidth-3*elementHeight+6,elementHeight));
        group.add(button);
        if (activeModule != null) {
            if (module == activeModule) button.setSelected(true);
        }
        modulePanel.add(button,c);

        // Adding the right arrow
        c.gridx++;
        c.weightx = 0;
        c.insets = new Insets(2, 2, 0, 0);
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        SeparatorButton rightArrowButton = new SeparatorButton(module,false);
        rightArrowButton.setPreferredSize(new Dimension(elementHeight,elementHeight));
        modulePanel.add(rightArrowButton,c);

        return modulePanel;
    }

    public JPanel createParametersTopRow(Module activeModule) {
        JPanel paramPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5,5,0,0);

        // Adding the nickname control to the top of the panel
        DisableableCheck disableableCheck = new DisableableCheck(activeModule);
        disableableCheck.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        if (activeModule.getClass() == InputControl.class || activeModule.getClass() == GUISeparator.class) {
            disableableCheck.setEnabled(false);
            disableableCheck.setOpaque(false);
        }
        paramPanel.add(disableableCheck,c);

        JSeparator separator = new JSeparator();
        separator.setOrientation(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(5, 25));
        c.gridx++;
        paramPanel.add(separator);

        ModuleName moduleName = new ModuleName(activeModule);
        moduleName.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        c.gridx++;
        c.weightx = 1;
        paramPanel.add(moduleName, c);

        ResetModuleName resetModuleName = new ResetModuleName(activeModule);
        resetModuleName.setPreferredSize(new Dimension(elementHeight,elementHeight));
        c.gridx++;
        c.weightx = 0;
        c.insets = new Insets(5,5,0,5);
        c.anchor = GridBagConstraints.EAST;
        paramPanel.add(resetModuleName, c);

        return paramPanel;

    }

    public JPanel createBasicModuleHeading(Module module) {
        JPanel modulePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Adding the state/evaluate button
        c.gridx = 0;
        c.weightx = 0;
        c.insets = new Insets(0, 5, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        ModuleEnabledButton moduleEnabledButton = new ModuleEnabledButton(module);
        moduleEnabledButton.setPreferredSize(new Dimension(elementHeight,elementHeight));
        moduleEnabledButton.setEnabled(module.canBeDisabled());
        modulePanel.add(moduleEnabledButton,c);

        JTextField title = new JTextField(module.getNickname());
        title.setEditable(false);
        title.setBorder(null);
        title.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        title.setOpaque(false);
        Color color = module.isRunnable() ? Color.BLACK : Color.RED;
        title.setForeground(color);
        c.weightx = 1;
        c.gridx++;
        modulePanel.add(title,c);

        return modulePanel;

    }

    public JPanel createBasicSeparator(Module module, int panelWidth) {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;

        JLabel leftArrowLabel = new JLabel();
        BooleanP expandedBasic = (BooleanP) module.getParameter(GUISeparator.EXPANDED_BASIC);
        if (expandedBasic.isSelected()) {
            leftArrowLabel.setIcon(downArrow);
        } else {
            leftArrowLabel.setIcon(rightArrow);
        }
        c.insets = new Insets(0,0,0,5);
        panel.add(leftArrowLabel,c);

        JSeparator separatorLeft = new JSeparator();
        separatorLeft.setForeground(Color.BLUE);
        c.weightx = 1;
        c.gridx++;
        panel.add(separatorLeft,c);

        JLabel label = new JLabel();
        label.setText(module.getNickname());
        label.setForeground(Color.BLUE);
        c.weightx = 0;
        c.gridx++;
        c.insets = new Insets(0,0,0,0);
        panel.add(label,c);

        JSeparator separatorRight = new JSeparator();
        separatorRight.setForeground(Color.BLUE);
        c.weightx = 1;
        c.gridx++;
        c.insets = new Insets(0,5,0,0);
        panel.add(separatorRight,c);

        JLabel rightArrowLabel = new JLabel();
        if (expandedBasic.isSelected()) {
            rightArrowLabel.setIcon(downArrow);
        } else {
            rightArrowLabel.setIcon(leftArrow);
        }
        c.weightx = 0;
        c.gridx++;
        panel.add(rightArrowLabel,c);

        panel.setPreferredSize(new Dimension(panelWidth,25));

        int labelWidth = label.getPreferredSize().width;
        label.setPreferredSize(new Dimension(labelWidth+20,25));
        label.setHorizontalAlignment(JLabel.CENTER);
        separatorLeft.setPreferredSize(new Dimension((panelWidth-labelWidth)/2-10, 1));
        separatorRight.setPreferredSize(new Dimension((panelWidth-labelWidth)/2-10, 1));

        // Adding an MouseListener to check if it was clicked
        panel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BooleanP expandedBasic = (BooleanP) module.getParameter(GUISeparator.EXPANDED_BASIC);
                expandedBasic.flipBoolean();
                GUI.updateModules(true);
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

    public JPanel createBasicModuleControl(Module module, int panelWidth) {
        // Only displaying the module title if it has at least one visible parameter
        boolean hasVisibleParameters = false;
        for (Parameter parameter : module.updateAndGetParameters()) {
            if (parameter.isVisible()) hasVisibleParameters = true;
        }
        if (!hasVisibleParameters &! module.canBeDisabled()) return null;

        JPanel modulePanel = new JPanel(new GridBagLayout());
        JPanel titlePanel = createBasicModuleHeading(module);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        modulePanel.add(titlePanel, c);

        // If there are visible parameters, but the module isn't enabled only return the heading
        if (!module.isEnabled()) return modulePanel;
        if (!module.isRunnable()) return modulePanel;

        c.insets = new Insets(0,35,0,0);
        for (Parameter parameter : module.updateAndGetParameters()) {
            if (parameter.isVisible()) {
                JPanel paramPanel = createParameterControl(parameter, GUI.getModules(), module);

                c.gridy++;
                modulePanel.add(paramPanel, c);

            }
        }

        return modulePanel;

    }

    private JPanel createMeasurementExportLabels() {
        ParameterCollection outputParamters = GUI.getAnalysis().getOutputControl().updateAndGetParameters();
        BooleanP exportIndividual = (BooleanP) outputParamters.getParameter(OutputControl.EXPORT_INDIVIDUAL_OBJECTS);
        BooleanP exportSummary = (BooleanP) outputParamters.getParameter(OutputControl.EXPORT_SUMMARY);

        JPanel labelPanel = new JPanel(new GridBagLayout());
        labelPanel.setPreferredSize(new Dimension(200,25));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel exportLabel = new JLabel("Ind");
        exportLabel.setPreferredSize(new Dimension(40,25));
        exportLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        exportLabel.setEnabled(exportIndividual.isSelected());
        c.gridx++;
        labelPanel.add(exportLabel, c);

        exportLabel = new JLabel("Mean");
        exportLabel.setPreferredSize(new Dimension(40,25));
        exportLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        exportLabel.setEnabled(exportSummary.isSelected());
        c.gridx++;
        labelPanel.add(exportLabel, c);

        exportLabel = new JLabel("Min");
        exportLabel.setPreferredSize(new Dimension(40,25));
        exportLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        exportLabel.setEnabled(exportSummary.isSelected());
        c.gridx++;
        labelPanel.add(exportLabel, c);

        exportLabel = new JLabel("Max");
        exportLabel.setPreferredSize(new Dimension(40,25));
        exportLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        exportLabel.setEnabled(exportSummary.isSelected());
        c.gridx++;
        labelPanel.add(exportLabel, c);

        exportLabel = new JLabel("Sum");
        exportLabel.setPreferredSize(new Dimension(40,25));
        exportLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        exportLabel.setEnabled(exportSummary.isSelected());
        c.gridx++;
        labelPanel.add(exportLabel, c);

        exportLabel = new JLabel("Std");
        exportLabel.setPreferredSize(new Dimension(40,25));
        exportLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        exportLabel.setEnabled(exportSummary.isSelected());
        c.gridx++;
        labelPanel.add(exportLabel, c);

        return labelPanel;

    }

    private JPanel createMeasurementExportControls(MeasurementRef measurement, MeasurementExportCheck.Type type) {
        ParameterCollection outputParamters = GUI.getAnalysis().getOutputControl().updateAndGetParameters();
        BooleanP exportIndividual = (BooleanP) outputParamters.getParameter(OutputControl.EXPORT_INDIVIDUAL_OBJECTS);
        BooleanP exportSummary = (BooleanP) outputParamters.getParameter(OutputControl.EXPORT_SUMMARY);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setPreferredSize(new Dimension(200,25));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        MeasurementExportCheck.Statistic statistic = MeasurementExportCheck.Statistic.INDIVIDUAL;
        MeasurementExportCheck exportCheck = new MeasurementExportCheck(measurement,statistic,type);
        exportCheck.setSelected(measurement.isExportIndividual());
        exportCheck.setPreferredSize(new Dimension(40,25));
        exportCheck.setEnabled(exportIndividual.isSelected() && measurement.isExportGlobal());
        c.gridx++;
        controlPanel.add(exportCheck, c);

        statistic = MeasurementExportCheck.Statistic.MEAN;
        exportCheck = new MeasurementExportCheck(measurement,statistic,type);
        exportCheck.setSelected(measurement.isExportMean());
        exportCheck.setPreferredSize(new Dimension(40,25));
        exportCheck.setEnabled(exportSummary.isSelected() && measurement.isExportGlobal());
        c.gridx++;
        controlPanel.add(exportCheck, c);

        statistic = MeasurementExportCheck.Statistic.MIN;
        exportCheck = new MeasurementExportCheck(measurement,statistic,type);
        exportCheck.setSelected(measurement.isExportMin());
        exportCheck.setPreferredSize(new Dimension(40,25));
        exportCheck.setEnabled(exportSummary.isSelected() && measurement.isExportGlobal());
        c.gridx++;
        controlPanel.add(exportCheck, c);

        statistic = MeasurementExportCheck.Statistic.MAX;
        exportCheck = new MeasurementExportCheck(measurement,statistic,type);
        exportCheck.setSelected(measurement.isExportMax());
        exportCheck.setPreferredSize(new Dimension(40,25));
        exportCheck.setEnabled(exportSummary.isSelected() && measurement.isExportGlobal());
        c.gridx++;
        controlPanel.add(exportCheck, c);

        statistic = MeasurementExportCheck.Statistic.SUM;
        exportCheck = new MeasurementExportCheck(measurement,statistic,type);
        exportCheck.setSelected(measurement.isExportSum());
        exportCheck.setPreferredSize(new Dimension(40,25));
        exportCheck.setEnabled(exportSummary.isSelected() && measurement.isExportGlobal());
        c.gridx++;
        controlPanel.add(exportCheck, c);

        statistic = MeasurementExportCheck.Statistic.STD;
        exportCheck = new MeasurementExportCheck(measurement,statistic,type);
        exportCheck.setSelected(measurement.isExportStd());
        exportCheck.setPreferredSize(new Dimension(40,25));
        exportCheck.setEnabled(exportSummary.isSelected() && measurement.isExportGlobal());
        c.gridx++;
        controlPanel.add(exportCheck, c);

        return controlPanel;

    }

    public JPanel createGlobalMeasurementControl(MeasurementRef measurement) {
        JPanel measurementPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5,5,0,0);

        JSeparator separator= new JSeparator();
        separator.setOrientation(JSeparator.HORIZONTAL);
        separator.setPreferredSize(new Dimension(elementHeight,-1));
        c.weightx = 1;
        measurementPanel.add(separator,c);

        JPanel controlPanel = createMeasurementExportControls(measurement,MeasurementExportCheck.Type.ALL_MEASUREMENTS);
        c.weightx = 0;
        c.gridx++;
        measurementPanel.add(controlPanel,c);

        separator= new JSeparator();
        separator.setOrientation(JSeparator.HORIZONTAL);
        separator.setPreferredSize(new Dimension(elementHeight,-1));
        c.gridx++;
        measurementPanel.add(separator,c);

        return measurementPanel;
    }

    public JPanel createMeasurementHeader(String name, MeasurementRefCollection measurementReferences) {
        JPanel headerPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5,5,0,0);
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        if (measurementReferences != null) {
            EnableMeasurementsButton enableButton = new EnableMeasurementsButton(measurementReferences);
            enableButton.setPreferredSize(new Dimension(elementHeight, elementHeight));
            headerPanel.add(enableButton, c);

            DisableMeasurementsButton disableButton = new DisableMeasurementsButton(measurementReferences);
            disableButton.setPreferredSize(new Dimension(elementHeight, elementHeight));
            c.gridx++;
            headerPanel.add(disableButton, c);

        } else {
            c.gridx = -1;
        }

        JLabel headerName = new JLabel(name);
        headerName.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        headerName.setPreferredSize(new Dimension(-1, elementHeight));
        headerName.setBorder(null);
        c.gridx++;
        c.weightx = 1;
        headerPanel.add(headerName, c);

        JPanel labelPanel = createMeasurementExportLabels();
        c.gridx++;
        c.weightx = 0;
        headerPanel.add(labelPanel,c);

        JSeparator separator = new JSeparator();
        separator.setOrientation(JSeparator.HORIZONTAL);
        separator.setPreferredSize(new Dimension(elementHeight,-1));
        c.gridx++;
        c.anchor = GridBagConstraints.EAST;
        headerPanel.add(separator,c);

        return headerPanel;

    }

    public JPanel createMeasurementControl(MeasurementRef measurement) {
        JPanel measurementPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5,5,0,0);

        MeasurementEnabledButton enabledButton = new MeasurementEnabledButton(measurement);
        enabledButton.setPreferredSize(new Dimension(elementHeight,elementHeight));
        measurementPanel.add(enabledButton, c);

        MeasurementName measurementName = new MeasurementName(measurement);
        measurementName.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        measurementName.setPreferredSize(new Dimension(-1, elementHeight));
        measurementName.setEditable(true);
        measurementName.setToolTipText("<html><p width=\"500\">" +measurement.getDescription()+"</p></html>");
        measurementName.setEnabled(measurement.isExportGlobal());
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        measurementPanel.add(measurementName, c);

        JPanel controlPanel = createMeasurementExportControls(measurement,MeasurementExportCheck.Type.SINGLE_MEASUREMENT);
        c.weightx = 0;
        c.gridx++;
        measurementPanel.add(controlPanel,c);

        ResetMeasurement resetMeasurement = new ResetMeasurement(measurement);
        resetMeasurement.setPreferredSize(new Dimension(elementHeight,elementHeight));
        resetMeasurement.setEnabled(measurement.isExportGlobal());
        c.gridx++;
        c.anchor = GridBagConstraints.EAST;
        measurementPanel.add(resetMeasurement,c);

        return measurementPanel;

    }
}
