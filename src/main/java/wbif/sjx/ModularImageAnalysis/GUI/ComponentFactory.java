package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.*;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.*;
import wbif.sjx.ModularImageAnalysis.Module.Miscellaneous.GUISeparator;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;

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

        JTextField parameterName = new JTextField(parameter.getName());
        parameterName.setPreferredSize(new Dimension(0,elementHeight));
        parameterName.setEditable(false);
        parameterName.setBorder(null);
        parameterName.setOpaque(false);
//        parameterName.setHorizontalAlignment(JTextField.RIGHT);
        paramPanel.add(parameterName, c);

        if (parameter.isValid()) {
            parameterName.setForeground(Color.BLACK);
        } else {
            parameterName.setForeground(Color.RED);
        }

        JComponent parameterControl = null;

        switch (parameter.getType()) {
            case Parameter.INPUT_IMAGE:
            case Parameter.REMOVED_IMAGE:
                LinkedHashSet<Parameter> images = modules.getAvailableImages(module);

                // Adding any output images to the list
                String[] names = new String[images.size()];
                int i = 0;
                for (Parameter image:images) names[i++] = image.getValue();

                parameterControl = new ChoiceArrayParameter(module,parameter,names);
                break;

            case Parameter.INPUT_OBJECTS:
            case Parameter.REMOVED_OBJECTS:
                LinkedHashSet<Parameter> objects = modules.getAvailableObjects(module);

                // Adding any output images to the list
                names = new String[objects.size()];
                i = 0;
                for (Parameter object:objects) names[i++] = object.getValue();

                parameterControl = new ChoiceArrayParameter(module,parameter,names);
                break;

            case Parameter.INTEGER:
            case Parameter.DOUBLE:
            case Parameter.STRING:
            case Parameter.OUTPUT_IMAGE:
            case Parameter.OUTPUT_OBJECTS:
                parameterControl = new TextParameter(module, parameter);
                break;

            case Parameter.BOOLEAN:
                parameterControl = new BooleanParameter(module,parameter);
                parameterControl.setOpaque(false);
                break;

            case Parameter.FILE_PATH:
                parameterControl = new FileParameter(module, parameter, FileParameter.FileTypes.FILE_TYPE);
                break;

            case Parameter.FOLDER_PATH:
                parameterControl = new FileParameter(module, parameter, FileParameter.FileTypes.FOLDER_TYPE);
                break;

            case Parameter.CHOICE_ARRAY:
                String[] valueSource = parameter.getValueSource();
                parameterControl = new ChoiceArrayParameter(module, parameter, valueSource);
                break;

            case Parameter.IMAGE_MEASUREMENT:
                String[] measurementChoices = modules.getImageMeasurementReferences((String) parameter.getValueSource(),module).getMeasurementNames();
                parameterControl = new ChoiceArrayParameter(module, parameter, measurementChoices);
                break;

            case Parameter.OBJECT_MEASUREMENT:
                measurementChoices = modules.getObjectMeasurementReferences((String) parameter.getValueSource(),module).getMeasurementNames();
                parameterControl = new ChoiceArrayParameter(module, parameter, measurementChoices);
                break;

            case Parameter.CHILD_OBJECTS:
                RelationshipCollection relationships = modules.getRelationships(module);
                String[] relationshipChoices = relationships.getChildNames(parameter.getValueSource());
                parameterControl = new ChoiceArrayParameter(module,parameter,relationshipChoices);
                break;

            case Parameter.PARENT_OBJECTS:
                relationships = GUI.getModules().getRelationships(module);
                relationshipChoices = relationships.getParentNames(parameter.getValueSource());
                parameterControl = new ChoiceArrayParameter(module,parameter,relationshipChoices);
                break;

            case Parameter.METADATA_ITEM:
                String[] metadataChoices = modules.getMetadataReferences(module).getMetadataNames();
                parameterControl = new ChoiceArrayParameter(module,parameter,metadataChoices);
                break;

            case Parameter.TEXT_DISPLAY:
                parameterControl = new TextDisplayArea(parameter);
                break;
        }

        // Adding the input component
        c.gridx++;
        c.weightx=1;
        c.anchor = GridBagConstraints.EAST;
        if (parameterControl != null) {
            if (parameter.getType() != Parameter.TEXT_DISPLAY) parameterControl.setPreferredSize(new Dimension(0,elementHeight));
            paramPanel.add(parameterControl, c);
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
        button.setForeground(Color.BLUE);
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
        c.gridx++;
        c.weightx = 1;
        paramPanel.add(moduleName, c);

        ResetModuleName resetModuleName = new ResetModuleName(activeModule);
        c.gridx++;
        c.weightx = 0;
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
        if (module.getParameterValue(GUISeparator.EXPANDED_BASIC)) {
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
        if (module.getParameterValue(GUISeparator.EXPANDED_BASIC)) {
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
                boolean expanded = module.getParameterValue(GUISeparator.EXPANDED_BASIC);
                module.updateParameterValue(GUISeparator.EXPANDED_BASIC,!expanded);
                GUI.updateModules();
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
        for (Parameter parameter : module.updateAndGetParameters().values()) {
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

        c.insets = new Insets(0,35,0,0);
        for (Parameter parameter : module.updateAndGetParameters().values()) {
            if (parameter.isVisible()) {
                JPanel paramPanel = createParameterControl(parameter, GUI.getModules(), module);

                c.gridy++;
                modulePanel.add(paramPanel, c);

            }
        }

        return modulePanel;

    }

    public JPanel createMeasurementHeader(String name) {
        JPanel headerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5,0,0,0);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JTextField headerName = new JTextField("      "+name);
        headerName.setPreferredSize(new Dimension(-1, elementHeight));
        headerName.setEditable(false);
        headerName.setBorder(null);
        headerPanel.add(headerName, c);

        return headerPanel;
    }

    public JPanel createMeasurementControl(MeasurementReference measurement) {
        JPanel measurementPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5,5,0,0);

        JTextField measurementName = new JTextField("            "+measurement.getName());
        measurementName.setPreferredSize(new Dimension(-1, elementHeight));
        measurementName.setEditable(false);
        measurementName.setBorder(null);
        measurementName.setToolTipText("<html><p width=\"500\">" +measurement.getDescription()+"</p></html>");
        measurementPanel.add(measurementName, c);

        MeasurementExportCheck exportCheck = new MeasurementExportCheck(measurement);
        exportCheck.setPreferredSize(new Dimension(-1, elementHeight));
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        exportCheck.setSelected(measurement.isExportable());
        measurementPanel.add(exportCheck,c);

        return measurementPanel;
    }

}
