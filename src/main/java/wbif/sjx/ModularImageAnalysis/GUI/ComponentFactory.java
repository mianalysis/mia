package wbif.sjx.ModularImageAnalysis.GUI;

import org.apache.commons.io.FilenameUtils;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 23/06/2017.
 */
class ComponentFactory {
    private MainGUI gui;
    private int elementHeight;
    private FocusListener focusListener;
    private ActionListener actionListener;

    ComponentFactory(MainGUI gui, int elementHeight, FocusListener focusListener, ActionListener actionListener) {
        this.gui = gui;
        this.elementHeight = elementHeight;
        this.focusListener = focusListener;
        this.actionListener = actionListener;

    }

    JPanel createParameterControl(Parameter parameter, ModuleCollection modules, HCModule module, int panelWidth) {
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

        if (parameter.getType() == Parameter.INPUT_IMAGE | parameter.getType() == Parameter.REMOVED_IMAGE) {
            // Getting a list of available images
            LinkedHashSet<Parameter> outputImages = modules.getParametersMatchingType(Parameter.OUTPUT_IMAGE,module);
            LinkedHashSet<Parameter> removedImages = modules.getParametersMatchingType(Parameter.REMOVED_IMAGE,module);

            // Adding any output images to the list
            LinkedHashSet<String> names = new LinkedHashSet<>();
            names.add(null);
            for (Parameter image : outputImages) {
                names.add(image.getValue());

            }

            // Removing any images which have since been removed from the workspace
            for (Parameter image : removedImages) {
                names.remove(image.getValue());
            }

            parameterControl = new ImageObjectInputParameter(module, parameter);
            for (String name:names) ((ImageObjectInputParameter) parameterControl).addItem(name);
            ((ImageObjectInputParameter) parameterControl).setSelectedItem(parameter.getValue());
            parameterControl.addFocusListener(focusListener);
            parameterControl.setName("InputParameter");

        } else if (parameter.getType() == Parameter.INPUT_OBJECTS) {
            // Getting a list of available images
            LinkedHashSet<Parameter> objects = modules.getParametersMatchingType(Parameter.OUTPUT_OBJECTS,module);

            LinkedHashSet<String> names = new LinkedHashSet<>();
            names.add(null);
            for (Parameter object : objects) {
                names.add(object.getValue());

            }

            parameterControl = new ImageObjectInputParameter(module, parameter);
            for (String name:names) ((ImageObjectInputParameter) parameterControl).addItem(name);
            ((ImageObjectInputParameter) parameterControl).setSelectedItem(parameter.getValue());

            parameterControl.addFocusListener(focusListener);
            parameterControl.setName("InputParameter");

        } else if (parameter.getType() == Parameter.INTEGER | parameter.getType() == Parameter.DOUBLE
                | parameter.getType() == Parameter.STRING | parameter.getType() == Parameter.OUTPUT_IMAGE
                | parameter.getType() == Parameter.OUTPUT_OBJECTS) {

            parameterControl = new TextParameter(module, parameter);
            String name = parameter.getValue() == null ? "" : parameter.getValue().toString();
            ((TextParameter) parameterControl).setText(name);
            parameterControl.addFocusListener(focusListener);
            parameterControl.setName("TextParameter");

        } else if (parameter.getType() == Parameter.BOOLEAN) {
            parameterControl = new BooleanParameter(module,parameter);
            ((BooleanParameter) parameterControl).setSelected(parameter.getValue());
            ((BooleanParameter) parameterControl).addActionListener(actionListener);
            parameterControl.setName("BooleanParameter");

        } else if (parameter.getType() == Parameter.FILE_PATH) {
            parameterControl = new FileParameter(module, parameter);
            ((FileParameter) parameterControl).setText(FilenameUtils.getName(parameter.getValue()));
            ((FileParameter) parameterControl).addActionListener(actionListener);
            parameterControl.setName("FileParameter");

        } else if (parameter.getType() == Parameter.CHOICE_ARRAY) {
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
            ((ChoiceArrayParameter) parameterControl).setWide(true);

        } else if (parameter.getType() == Parameter.MEASUREMENT) {
            MeasurementCollection measurements = modules.getMeasurements(module);
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
            ((ChoiceArrayParameter) parameterControl).setWide(true);

        } else if (parameter.getType() == Parameter.CHILD_OBJECTS) {
            RelationshipCollection relationships = modules.getRelationships(module);
            String[] relationshipChoices = relationships.getChildNames(parameter.getValueSource());
            parameterControl = new ImageObjectInputParameter(module, parameter);
            if (relationshipChoices != null) {
                for (String relationship : relationshipChoices) {
                    ((ImageObjectInputParameter) parameterControl).addItem(relationship);

                }
                ((ImageObjectInputParameter) parameterControl).setSelectedItem(parameter.getValue());
            }
            ((ImageObjectInputParameter) parameterControl).addActionListener(actionListener);
            parameterControl.setName("InputParameter");
            ((ImageObjectInputParameter) parameterControl).setWide(true);

        } else if (parameter.getType() == Parameter.PARENT_OBJECTS) {
            RelationshipCollection relationships = modules.getRelationships(module);
            String[] relationshipChoices = relationships.getParentNames(parameter.getValueSource());
            parameterControl = new ImageObjectInputParameter(module, parameter);
            if (relationshipChoices != null) {
                for (String relationship : relationshipChoices) {
                    ((ImageObjectInputParameter) parameterControl).addItem(relationship);

                }
                ((ImageObjectInputParameter) parameterControl).setSelectedItem(parameter.getValue());
            }
            ((ImageObjectInputParameter) parameterControl).addActionListener(actionListener);
            parameterControl.setName("InputParameter");
            ((ImageObjectInputParameter) parameterControl).setWide(true);

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
        ModuleEnabledCheck enabledCheck = new ModuleEnabledCheck(gui,module);
        modulePanel.add(enabledCheck,c);

        // Adding the main module button
        c.gridx++;
        c.weightx = 1;
        c.insets = new Insets(5, 5, 0, 0);
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        ModuleButton button = new ModuleButton(gui,module);
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
        EvalButton evalButton = new EvalButton(gui,module);
        evalButton.setPreferredSize(new Dimension(elementHeight,elementHeight));
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
        EvalButton evalButton = new EvalButton(gui,module);
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
