package wbif.sjx.MIA.GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedHashMap;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.apache.batik.ext.swing.GridBagConstants;

import wbif.sjx.MIA.GUI.ControlObjects.ExportCheck;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleTitle;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleList.ModuleEnabledButton;
import wbif.sjx.MIA.GUI.ControlObjects.ParameterList.DisableRefsButton;
import wbif.sjx.MIA.GUI.ControlObjects.ParameterList.DisableableCheck;
import wbif.sjx.MIA.GUI.ControlObjects.ParameterList.EnableRefsButton;
import wbif.sjx.MIA.GUI.ControlObjects.ParameterList.ExportEnableButton;
import wbif.sjx.MIA.GUI.ControlObjects.ParameterList.ExportName;
import wbif.sjx.MIA.GUI.ControlObjects.ParameterList.ResetExport;
import wbif.sjx.MIA.GUI.ControlObjects.ParameterList.VisibleCheck;
import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Module.Miscellaneous.GUISeparator;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ObjMeasurementSelectorP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.Text.MessageP;
import wbif.sjx.MIA.Object.Parameters.Text.TextAreaP;
import wbif.sjx.MIA.Object.References.Abstract.ExportableRef;
import wbif.sjx.MIA.Object.References.Abstract.SummaryRef;
import wbif.sjx.MIA.Object.References.Collections.RefCollection;

/**
 * Created by Stephen on 23/06/2017.
 */
public class ComponentFactory {
    private int elementHeight;

    private static final ImageIcon downArrow = new ImageIcon(
            ComponentFactory.class.getResource("/Icons/downarrow_darkblue_12px.png"), "");
    private static final ImageIcon rightArrow = new ImageIcon(
            ComponentFactory.class.getResource("/Icons/rightarrow_darkblue_12px.png"), "");
    private static final ImageIcon leftArrow = new ImageIcon(
            ComponentFactory.class.getResource("/Icons/leftarrow_darkblue_12px.png"), "");
    private static final ImageIcon circle = new ImageIcon(
            ComponentFactory.class.getResource("/Icons/dot_blue_12px.png"), "");

    public ComponentFactory(int elementHeight) {
        this.elementHeight = elementHeight;
    }

    public JPanel createParameterControl(Parameter parameter, ModuleCollection modules, Module module,
            boolean editable) {
        JPanel paramPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 5, 0, 0);

        ParameterControl parameterControl = parameter.getControl();
        parameterControl.updateControl();
        JComponent parameterComponent = parameterControl.getComponent();

        if (parameter instanceof MessageP || parameter instanceof ObjMeasurementSelectorP) {
            String value = parameter.getAlternativeString();
            parameterComponent.setToolTipText(value == null ? "" : value);
            c.insets = new Insets(10, 3, 5, 5);
            paramPanel.add(parameterComponent, c);

        } else if (parameter instanceof ParamSeparatorP) {
            if (module.updateAndGetParameters().values().iterator().next() == parameter) {
                c.insets = new Insets(0, 5, 5, 8);
            } else {
                c.insets = new Insets(30, 5, 5, 8);
            }
            paramPanel.add(parameterComponent, c);

        } else {
            JComponent parameterName = editable ? new ExportName(parameter) : new JLabel(parameter.getNickname());
            parameterName.setBorder(null);
            parameterName.setOpaque(false);
            parameterName.setPreferredSize(new Dimension(0, elementHeight));
            parameterName.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            parameterName.setToolTipText("<html><p width=\"500\">" + parameter.getDescription() + "</p></html>");
            paramPanel.add(parameterName, c);

            if (parameter.isValid())
                parameterName.setForeground(Color.BLACK);
            else
                parameterName.setForeground(Colours.RED);

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
                c.insets = new Insets(2, 5, 0, 5);
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
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 0, 0);

        // Adding the nickname control to the top of the panel
        ExportName moduleName = new ExportName(activeModule);
        moduleName.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        moduleName.setForeground(Color.BLACK);
        paramPanel.add(moduleName, c);

        JSeparator separator = new JSeparator();
        separator.setOrientation(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(5, 25));
        c.gridx++;
        c.weightx = 0;
        paramPanel.add(separator, c);

        DisableableCheck disableableCheck = new DisableableCheck(activeModule);
        disableableCheck.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        if (activeModule.getClass() == InputControl.class) {
            disableableCheck.setEnabled(false);
            disableableCheck.setOpaque(false);
        }
        c.gridx++;
        c.anchor = GridBagConstraints.EAST;

        paramPanel.add(disableableCheck, c);

        return paramPanel;

    }

    public JPanel createBasicModuleHeading(Module module) {
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
        if (module.isRunnable() | !module.isEnabled())
            title.setForeground(Color.BLACK);
        else
            title.setForeground(Colours.RED);
        title.setToolTipText("<html><p width=\"500\">" + module.getDescription() + "</p></html>");
        c.insets = new Insets(0, 0, 0, 0);
        c.weightx = 1;
        c.gridx++;
        modulePanel.add(title, c);

        return modulePanel;

    }

    public JPanel createBasicSeparator(Module module) {
        JPanel panel = new JPanel(new GridBagLayout());

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

        BooleanP expandedBasic = (BooleanP) module.getParameter(GUISeparator.EXPANDED_BASIC);
        JLabel leftArrowLabel = new JLabel();

        if (((GUISeparator) module).getBasicModules().size() == 0 & !module.getNickname().equals("File selection")) {
            leftArrowLabel.setIcon(circle);
        } else {
            if (expandedBasic.isSelected()) {
                leftArrowLabel.setIcon(downArrow);
            } else {
                leftArrowLabel.setIcon(rightArrow);
            }
        }
        c.insets = new Insets(0, 0, 0, 5);
        c.gridx++;
        panel.add(leftArrowLabel, c);

        JSeparator separatorLeft = new JSeparator();
        separatorLeft.setForeground(Colours.DARK_BLUE);
        c.weightx = 1;
        c.gridx++;
        panel.add(separatorLeft, c);

        JLabel label = new JLabel();
        label.setText(module.getNickname());
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setForeground(Colours.DARK_BLUE);
        c.weightx = 0;
        c.gridx++;
        c.insets = new Insets(0, 0, 0, 0);
        panel.add(label, c);

        JSeparator separatorRight = new JSeparator();
        separatorRight.setForeground(Colours.DARK_BLUE);
        c.weightx = 1;
        c.gridx++;
        c.anchor = GridBagConstants.EAST;
        c.insets = new Insets(0, 5, 0, 0);
        panel.add(separatorRight, c);

        JLabel rightArrowLabel = new JLabel();
        if (((GUISeparator) module).getBasicModules().size() == 0 & !module.getNickname().equals("File selection")) {
            rightArrowLabel.setIcon(circle);
        } else {
            if (expandedBasic.isSelected()) {
                rightArrowLabel.setIcon(downArrow);
            } else {
                rightArrowLabel.setIcon(leftArrow);
            }
        }

        c.weightx = 0;
        c.gridx++;
        panel.add(rightArrowLabel, c);

        // panel.setPreferredSize(new Dimension(panelWidth,25));

        int labelWidth = label.getPreferredSize().width;
        label.setPreferredSize(new Dimension(labelWidth + 20, 25));
        label.setHorizontalAlignment(JLabel.CENTER);
        // separatorLeft.setPreferredSize(new Dimension((panelWidth-labelWidth)/2-10,
        // 1));
        // separatorRight.setPreferredSize(new Dimension((panelWidth-labelWidth)/2-10,
        // 1));

        // Adding an MouseListener to check if it was clicked
        panel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    BooleanP expandedBasic = (BooleanP) module.getParameter(GUISeparator.EXPANDED_BASIC);
                    expandedBasic.flipBoolean();
                    GUI.updateModules();

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

    public JPanel createBasicModuleControl(Module module) {
        // Only displaying the module title if it has at least one visible parameter
        if (!module.hasVisibleParameters() & !module.canBeDisabled())
            return null;

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

        // If there are visible parameters, but the module isn't enabled only return the
        // heading
        if (!module.isEnabled())
            return modulePanel;
        if (!module.isRunnable() & !module.invalidParameterIsVisible())
            return modulePanel;

        c.insets = new Insets(0, 40, 0, 17);
        addParameters(module, module.updateAndGetParameters(), modulePanel, c, false);

        return modulePanel;

    }

    private void addParameters(Module module, ParameterCollection parameters, JPanel modulePanel, GridBagConstraints c,
            boolean editable) {
        for (Parameter parameter : parameters.values()) {
            if (parameter.getClass() == ParameterGroup.class) {
                LinkedHashMap<Integer, ParameterCollection> collections = ((ParameterGroup) parameter)
                        .getCollections(true);
                for (ParameterCollection collection : collections.values())
                    addParameters(module, collection, modulePanel, c, editable);

            }

            if (parameter.isVisible()) {
                JPanel paramPanel = createParameterControl(parameter, GUI.getModules(), module, editable);
                c.gridy++;
                modulePanel.add(paramPanel, c);
            }
        }
    }

    private JPanel createSummaryExportLabels(boolean includeSummary) {
        ParameterCollection outputParameters = GUI.getModules().getOutputControl().updateAndGetParameters();
        String exportMode = outputParameters.getValue(OutputControl.EXPORT_MODE);
        BooleanP exportIndividual = outputParameters.getParameter(OutputControl.EXPORT_INDIVIDUAL_OBJECTS);
        BooleanP exportSummary = outputParameters.getParameter(OutputControl.EXPORT_SUMMARY);

        JPanel labelPanel = new JPanel(new GridBagLayout());
        labelPanel.setPreferredSize(new Dimension(200, 25));

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
        exportLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        exportLabel.setEnabled(exportIndividual.isSelected());
        c.gridx++;
        labelPanel.add(exportLabel, c);

        if (includeSummary) {
            exportLabel = new JLabel("Mean");
            exportLabel.setPreferredSize(new Dimension(40, 25));
            exportLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            exportLabel.setEnabled(exportSummary.isSelected());
            c.gridx++;
            labelPanel.add(exportLabel, c);

            exportLabel = new JLabel("Min");
            exportLabel.setPreferredSize(new Dimension(40, 25));
            exportLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            exportLabel.setEnabled(exportSummary.isSelected());
            c.gridx++;
            labelPanel.add(exportLabel, c);

            exportLabel = new JLabel("Max");
            exportLabel.setPreferredSize(new Dimension(40, 25));
            exportLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            exportLabel.setEnabled(exportSummary.isSelected());
            c.gridx++;
            labelPanel.add(exportLabel, c);

            exportLabel = new JLabel("Sum");
            exportLabel.setPreferredSize(new Dimension(40, 25));
            exportLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            exportLabel.setEnabled(exportSummary.isSelected());
            c.gridx++;
            labelPanel.add(exportLabel, c);

            exportLabel = new JLabel("Std");
            exportLabel.setPreferredSize(new Dimension(40, 25));
            exportLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            exportLabel.setEnabled(exportSummary.isSelected());
            c.gridx++;
            labelPanel.add(exportLabel, c);
        }

        return labelPanel;

    }

    private JPanel createExportControls(ExportableRef ref, ExportCheck.Type type) {
        ParameterCollection outputParameters = GUI.getModules().getOutputControl().updateAndGetParameters();
        String exportMode = outputParameters.getValue(OutputControl.EXPORT_MODE);
        BooleanP exportIndividual = (BooleanP) outputParameters.getParameter(OutputControl.EXPORT_INDIVIDUAL_OBJECTS);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setPreferredSize(new Dimension(200, 25));

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
        ParameterCollection outputParameters = GUI.getModules().getOutputControl().updateAndGetParameters();
        BooleanP exportIndividual = (BooleanP) outputParameters.getParameter(OutputControl.EXPORT_INDIVIDUAL_OBJECTS);
        BooleanP exportSummary = (BooleanP) outputParameters.getParameter(OutputControl.EXPORT_SUMMARY);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setPreferredSize(new Dimension(200, 25));

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

    public JPanel createRefExportHeader(String name, RefCollection refs, boolean includeSummary) {
        JPanel headerPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5, 5, 0, 0);
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
        headerName.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        headerName.setPreferredSize(new Dimension(-1, elementHeight));
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

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 0, 0);

        ExportEnableButton enabledButton = new ExportEnableButton(ref);
        enabledButton.setPreferredSize(new Dimension(elementHeight, elementHeight));
        measurementPanel.add(enabledButton, c);

        ExportName exportName = new ExportName(ref);
        exportName.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        exportName.setPreferredSize(new Dimension(-1, elementHeight));
        exportName.setToolTipText("<html><p width=\"500\">" + ref.getDescription() + "</p></html>");
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
        c.insets = new Insets(5, 5, 0, 5);
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
        c.insets = new Insets(5, 5, 0, 0);

        ExportEnableButton enabledButton = new ExportEnableButton(ref);
        enabledButton.setPreferredSize(new Dimension(elementHeight, elementHeight));
        measurementPanel.add(enabledButton, c);

        ExportName exportName = new ExportName(ref);
        exportName.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        exportName.setPreferredSize(new Dimension(-1, elementHeight));
        exportName.setToolTipText("<html><p width=\"500\">" + ref.getDescription() + "</p></html>");
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
        c.insets = new Insets(5, 5, 0, 5);
        measurementPanel.add(resetExport, c);

        return measurementPanel;

    }
}
