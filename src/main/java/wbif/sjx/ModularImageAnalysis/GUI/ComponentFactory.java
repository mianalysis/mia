package wbif.sjx.ModularImageAnalysis.GUI;

import org.apache.commons.io.FilenameUtils;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sc13967 on 23/06/2017.
 */
class ComponentFactory {
    private int elementHeight;
    private FocusListener focusListener;
    private ActionListener actionListener;

    ComponentFactory(int elementHeight, FocusListener focusListener, ActionListener actionListener) {
        this.elementHeight = elementHeight;
        this.focusListener = focusListener;
        this.actionListener = actionListener;

    }

    JPanel createParameterControl(HCParameter parameter, HCModuleCollection modules, HCModule module, int panelWidth) {
        JPanel paramPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5,5,0,0);

        JTextField parameterName = new JTextField(parameter.getName());
        parameterName.setPreferredSize(new Dimension(2*panelWidth/3, elementHeight));
        parameterName.setEditable(false);
        parameterName.setBorder(null);
        paramPanel.add(parameterName, c);

        JComponent parameterControl = null;

        if (parameter.getType() == HCParameter.INPUT_IMAGE) {
            // Getting a list of available images
            ArrayList<HCParameter> images = modules.getParametersMatchingType(HCParameter.OUTPUT_IMAGE,
                    module);

            parameterControl = new HCNameInputParameter(module, parameter);
            ((HCNameInputParameter) parameterControl).addItem(null);
            for (HCParameter image : images) {
                ((HCNameInputParameter) parameterControl).addItem(image.getValue());

            }
            ((HCNameInputParameter) parameterControl).setSelectedItem(parameter.getValue());
            parameterControl.addFocusListener(focusListener);
            parameterControl.setName("InputParameter");

        } else if (parameter.getType() == HCParameter.INPUT_OBJECTS) {
            // Getting a list of available images
            ArrayList<HCParameter> objects = modules.getParametersMatchingType(HCParameter.OUTPUT_OBJECTS,
                    module);

            parameterControl = new HCNameInputParameter(module, parameter);
            ((HCNameInputParameter) parameterControl).addItem(null);
            for (HCParameter object : objects) {
                ((HCNameInputParameter) parameterControl).addItem(object.getValue());

            }
            ((HCNameInputParameter) parameterControl).setSelectedItem(parameter.getValue());

            parameterControl.addFocusListener(focusListener);
            parameterControl.setName("InputParameter");

        } else if (parameter.getType() == HCParameter.INTEGER | parameter.getType() == HCParameter.DOUBLE
                | parameter.getType() == HCParameter.STRING | parameter.getType() == HCParameter.OUTPUT_IMAGE
                | parameter.getType() == HCParameter.OUTPUT_OBJECTS) {

            parameterControl = new TextParameter(module, parameter);
            String name = parameter.getValue() == null ? "" : parameter.getValue().toString();
            ((TextParameter) parameterControl).setText(name);
            parameterControl.addFocusListener(focusListener);
            parameterControl.setName("TextParameter");

        } else if (parameter.getType() == HCParameter.BOOLEAN) {
            parameterControl = new BooleanParameter(module,parameter);
            ((BooleanParameter) parameterControl).setSelected(parameter.getValue());
            ((BooleanParameter) parameterControl).addActionListener(actionListener);
            parameterControl.setName("BooleanParameter");

        } else if (parameter.getType() == HCParameter.FILE_PATH) {
            parameterControl = new FileParameter(module, parameter);
            ((FileParameter) parameterControl).setText(FilenameUtils.getName(parameter.getValue()));
            ((FileParameter) parameterControl).addActionListener(actionListener);
            parameterControl.setName("FileParameter");

        } else if (parameter.getType() == HCParameter.CHOICE_ARRAY) {
            String[] valueSource = parameter.getValueSource();
            parameterControl = new ChoiceArrayParameter(module, parameter, valueSource);
            if (parameter.getValue() != null) {
                ((ChoiceArrayParameter) parameterControl).setSelectedItem(parameter.getValue());
            }

            if (parameter.getValueSource() != null) {
                parameter.setValue(((ChoiceArrayParameter) parameterControl).getSelectedItem());
            }

            ((ChoiceArrayParameter) parameterControl).addActionListener(actionListener);

            parameterControl.setName("ChoiceArrayParameter");

        } else if (parameter.getType() == HCParameter.MEASUREMENT) {
            HCMeasurementCollection measurements = modules.getMeasurements(module);
            String[] measurementChoices = measurements.getMeasurementNames(parameter.getValueSource());
            Arrays.sort(measurementChoices);

            parameterControl = new ChoiceArrayParameter(module, parameter, measurementChoices);
            if (parameter.getValue() != null) {
                ((ChoiceArrayParameter) parameterControl).setSelectedItem(parameter.getValue());
            }
            if (parameter.getValueSource() != null) {
                parameter.setValue(((ChoiceArrayParameter) parameterControl).getSelectedItem());
            }
            ((ChoiceArrayParameter) parameterControl).addActionListener(actionListener);
            parameterControl.setName("ChoiceArrayParameter");

        } else if (parameter.getType() == HCParameter.CHILD_OBJECTS) {
            HCRelationshipCollection relationships = modules.getRelationships(module);
            HCName[] relationshipChoices = relationships.getChildNames(parameter.getValueSource());
            parameterControl = new HCNameInputParameter(module, parameter);
            if (relationshipChoices != null) {
                for (HCName relationship : relationshipChoices) {
                    ((HCNameInputParameter) parameterControl).addItem(relationship);

                }
                ((HCNameInputParameter) parameterControl).setSelectedItem(parameter.getValue());
            }
            ((HCNameInputParameter) parameterControl).addActionListener(actionListener);
            parameterControl.setName("InputParameter");

        } else if (parameter.getType() == HCParameter.PARENT_OBJECTS) {
            HCRelationshipCollection relationships = modules.getRelationships(module);
            HCName[] relationshipChoices = relationships.getParentNames(parameter.getValueSource());
            parameterControl = new HCNameInputParameter(module, parameter);
            if (relationshipChoices != null) {
                for (HCName relationship : relationshipChoices) {
                    ((HCNameInputParameter) parameterControl).addItem(relationship);

                }
                ((HCNameInputParameter) parameterControl).setSelectedItem(parameter.getValue());
            }
            ((HCNameInputParameter) parameterControl).addActionListener(actionListener);
            parameterControl.setName("InputParameter");

        }

        // Adding the input component
        c.gridx++;
        c.weightx=1;
        c.anchor = GridBagConstraints.EAST;
        if (parameterControl != null) {
            paramPanel.add(parameterControl, c);
            parameterControl.setPreferredSize(new Dimension(panelWidth/3, elementHeight));

        }

        return paramPanel;

    }

    JPanel createAdvancedModuleControl(HCModule module, ButtonGroup group, HCModule activeModule, Color color, int panelWidth) {
        JPanel modulePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Adding the module enabled checkbox
        c.gridx = 0;
        c.weightx = 0;
        c.insets = new Insets(5, 0, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        ModuleEnabledCheck enabledCheck = new ModuleEnabledCheck(module);
        enabledCheck.addActionListener(actionListener);
        modulePanel.add(enabledCheck,c);

        // Adding the main module button
        c.gridx++;
        c.weightx = 1;
        c.insets = new Insets(5, 5, 0, 0);
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        ModuleButton button = new ModuleButton(module);
        button.addActionListener(actionListener);
        button.setPreferredSize(new Dimension(panelWidth-elementHeight-20,elementHeight));
        group.add(button);
        if (!module.isEnabled()) button.setForeground(Color.GRAY);
        if (activeModule != null) {
            if (module == activeModule) button.setSelected(true);
        }
        modulePanel.add(button,c);

        // Adding the state/evaluate button
        c.gridx++;
        c.weightx = 0;
        c.insets = new Insets(5, 0, 0, 0);
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        EvalButton evalButton = new EvalButton(module);
        evalButton.setPreferredSize(new Dimension(elementHeight,elementHeight));
        evalButton.addActionListener(actionListener);
        evalButton.setForeground(color);
        modulePanel.add(evalButton,c);

        return modulePanel;

    }

    JPanel createBasicModuleHeading(HCModule module, Color color, int panelWidth) {
        JPanel modulePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Adding the module title
        c.gridx = 0;
        c.weightx = 0;
        c.insets = new Insets(5, 5, 0, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        EvalButton evalButton = new EvalButton(module);
        evalButton.setPreferredSize(new Dimension(elementHeight,elementHeight));
        evalButton.addActionListener(actionListener);
        evalButton.setForeground(color);
        modulePanel.add(evalButton,c);

        // Adding the state/evaluate button
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        JTextField title = new JTextField(module.getTitle());
        title.setEditable(false);
        title.setBorder(null);
        title.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        title.setPreferredSize(new Dimension(panelWidth-elementHeight,elementHeight));
        modulePanel.add(title,c);

        return modulePanel;

    }
}
