package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.*;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.*;
import wbif.sjx.ModularImageAnalysis.Module.Miscellaneous.GUISeparator;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Created by sc13967 on 23/06/2017.
 */
public class ComponentFactory {
    private GUI gui;
    private int elementHeight;

    public ComponentFactory(GUI gui, int elementHeight) {
        this.gui = gui;
        this.elementHeight = elementHeight;

    }

    public JPanel createParameterControl(Parameter parameter, ModuleCollection modules, Module module, int panelWidth) {
        JPanel paramPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(2,5,0,0);

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
            LinkedHashSet<String> namesSet = new LinkedHashSet<>();
            namesSet.add(null);
            for (Parameter image : outputImages) {
                namesSet.add(image.getValue());

            }

            // Removing any images which have since been removed from the workspace
            for (Parameter image : removedImages) {
                namesSet.remove(image.getValue());
            }

            String[] names = new String[namesSet.size()];
            int i = 0;
            for (String name:namesSet) {
                names[i++] = name;
            }

            parameterControl = new ChoiceArrayParameter(gui,module,parameter,names);

        } else if (parameter.getType() == Parameter.INPUT_OBJECTS || parameter.getType() == Parameter.REMOVED_OBJECTS) {
            // Getting a list of available images
            LinkedHashSet<Parameter> objects = modules.getParametersMatchingType(Parameter.OUTPUT_OBJECTS,module);
            LinkedHashSet<Parameter> removedObjects = modules.getParametersMatchingType(Parameter.REMOVED_OBJECTS,module);

            // Adding any output images to the list
            LinkedHashSet<String> namesSet = new LinkedHashSet<>();
            namesSet.add(null);
            for (Parameter object : objects) {
                namesSet.add(object.getValue());
            }

            // Removing any images which have since been removed from the workspace
            for (Parameter object : removedObjects) {
                namesSet.remove(object.getValue());
            }

            String[] names = new String[namesSet.size()];
            int i = 0;
            for (String name:namesSet) {
                names[i++] = name;
            }

            parameterControl = new ChoiceArrayParameter(gui,module,parameter,names);

        } else if (parameter.getType() == Parameter.INTEGER | parameter.getType() == Parameter.DOUBLE
                | parameter.getType() == Parameter.STRING | parameter.getType() == Parameter.OUTPUT_IMAGE
                | parameter.getType() == Parameter.OUTPUT_OBJECTS) {

            parameterControl = new TextParameter(gui, module, parameter);

        } else if (parameter.getType() == Parameter.BOOLEAN) {
            parameterControl = new BooleanParameter(gui,module,parameter);

        } else if (parameter.getType() == Parameter.FILE_PATH) {
            parameterControl = new FileParameter(gui, module, parameter, FileParameter.FileTypes.FILE_TYPE);

        } else if (parameter.getType() == Parameter.FOLDER_PATH) {
            parameterControl = new FileParameter(gui, module, parameter, FileParameter.FileTypes.FOLDER_TYPE);

        } else if (parameter.getType() == Parameter.CHOICE_ARRAY) {
            String[] valueSource = parameter.getValueSource();
            parameterControl = new ChoiceArrayParameter(gui, module, parameter, valueSource);

        } else if (parameter.getType() == Parameter.IMAGE_MEASUREMENT) {
            String[] measurementChoices = modules.getImageReferences((String) parameter.getValueSource(),module).getMeasurementNickNames();

            parameterControl = new ChoiceArrayParameter(gui, module, parameter, measurementChoices);

        } else if (parameter.getType() == Parameter.OBJECT_MEASUREMENT) {
            String[] measurementChoices = modules.getObjectReferences((String) parameter.getValueSource(),module).getMeasurementNickNames();

            parameterControl = new ChoiceArrayParameter(gui, module, parameter, measurementChoices);

        } else if (parameter.getType() == Parameter.CHILD_OBJECTS) {
            RelationshipCollection relationships = modules.getRelationships(module);
            String[] relationshipChoices = relationships.getChildNames(parameter.getValueSource());

            parameterControl = new ChoiceArrayParameter(gui,module,parameter,relationshipChoices);

        } else if (parameter.getType() == Parameter.PARENT_OBJECTS) {
            RelationshipCollection relationships = gui.getModules().getRelationships(module);
            String[] relationshipChoices = relationships.getParentNames(parameter.getValueSource());

            parameterControl = new ChoiceArrayParameter(gui,module,parameter,relationshipChoices);

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

    public JPanel createAdvancedModuleControl(Module module, ButtonGroup group, Module activeModule, int panelWidth) {
        JPanel modulePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Adding the module enabled checkbox
        c.gridx = 0;
        c.weightx = 0;
        c.insets = new Insets(2, 0, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        ModuleEnabledCheck enabledCheck = new ModuleEnabledCheck(gui,module);
        modulePanel.add(enabledCheck,c);

        // Adding the main module button
        c.gridx++;
        c.weightx = 1;
        c.insets = new Insets(2, 5, 0, 0);
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
        c.insets = new Insets(2, 0, 0, 0);
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        EvalButton evalButton = new EvalButton(gui,module);
        evalButton.setPreferredSize(new Dimension(elementHeight,elementHeight));
        modulePanel.add(evalButton,c);

        return modulePanel;

    }

    public JPanel createParametersTopRow(Module activeModule) {
        JPanel paramPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5,5,0,0);

        // Adding the nickname control to the top of the panel
        DisableableCheck disableableCheck = new DisableableCheck(activeModule);
        if (activeModule.getClass() == InputControl.class) {
            disableableCheck.setEnabled(false);
        }
        paramPanel.add(disableableCheck,c);

        JSeparator separator = new JSeparator();
        separator.setOrientation(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(5, 25));
        c.gridx++;
        paramPanel.add(separator);

        ModuleName moduleName = new ModuleName(gui, activeModule);
        c.gridx++;
        paramPanel.add(moduleName, c);

        ResetModuleName resetModuleName = new ResetModuleName(gui, activeModule);
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        paramPanel.add(resetModuleName, c);

        return paramPanel;

    }

    public JPanel createBasicModuleHeading(Module module, int panelWidth) {
        JPanel modulePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Adding the state/evaluate button
        c.gridx = 0;
        c.weightx = 0;
        c.insets = new Insets(0, 5, 0, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        ModuleEnabledCheck moduleEnabledCheck = new ModuleEnabledCheck(gui,module);
        modulePanel.add(moduleEnabledCheck,c);

        JTextField title = new JTextField(module.getNickname());
        title.setEditable(false);
        title.setBorder(null);
        title.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        title.setPreferredSize(new Dimension(panelWidth-elementHeight,elementHeight));
        c.gridx++;
        modulePanel.add(title,c);

        return modulePanel;

    }

    public JPanel getSeparator(int panelWidth) {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;

        JSeparator separator = new JSeparator();
        c.insets = new Insets(10,0,0,0);
        separator.setPreferredSize(new Dimension(panelWidth, 10));
        panel.add(separator,c);

        return panel;

    }

    public JPanel createBasicModuleControl(Module module, int panelWidth) {
        // If the module is the special-case GUISeparator, create this module, then return
        if (module.getClass().isInstance(new GUISeparator())) return getSeparator(panelWidth);

        // Only displaying the module title if it has at least one visible parameter
        boolean hasVisibleParameters = false;
        for (Parameter parameter : module.updateAndGetParameters().values()) {
            if (parameter.isVisible()) hasVisibleParameters = true;
        }
        if (!hasVisibleParameters &! module.canBeDisabled()) return null;

        JPanel modulePanel = new JPanel(new GridBagLayout());
        JPanel titlePanel = createBasicModuleHeading(module, panelWidth);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;

        c.anchor = GridBagConstraints.FIRST_LINE_START;
        modulePanel.add(titlePanel, c);

        // If there are visible parameters, but the module isn't enabled only return the heading
        if (!module.isEnabled()) return modulePanel;

        c.insets = new Insets(0,35,0,0);
        for (Parameter parameter : module.updateAndGetParameters().values()) {
            if (parameter.isVisible()) {
                JPanel paramPanel = createParameterControl(parameter, gui.getModules(), module, panelWidth-35);

                c.gridy++;
                modulePanel.add(paramPanel, c);

            }
        }

        return modulePanel;

    }

    public JPanel createMeasurementHeader(String name, int panelWidth) {
        JPanel headerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5,0,0,0);
        c.anchor = GridBagConstraints.WEST;

        JTextField headerName = new JTextField("      "+name);
        headerName.setPreferredSize(new Dimension(panelWidth, elementHeight));
        headerName.setEditable(false);
        headerName.setBorder(null);
        headerPanel.add(headerName, c);

        return headerPanel;
    }

    public JPanel createMeasurementControl(MeasurementReference measurement, int panelWidth) {
        JPanel measurementPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5,5,0,0);

        JTextField measurementName = new JTextField("            "+measurement.getNickName());
        measurementName.setPreferredSize(new Dimension(2*panelWidth/3, elementHeight));
        measurementName.setEditable(false);
        measurementName.setBorder(null);
        measurementPanel.add(measurementName, c);

        MeasurementExportCheck exportCheck = new MeasurementExportCheck(measurement);
        exportCheck.setPreferredSize(new Dimension(panelWidth/3, elementHeight));
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        exportCheck.setSelected(measurement.isExportable());
        measurementPanel.add(exportCheck,c);

        return measurementPanel;
    }
}
