package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.EvalButton;
import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.ModuleButton;
import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.ModuleEnabledCheck;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.BooleanParameter;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.ChoiceArrayParameter;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.FileParameter;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.TextParameter;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedHashSet;

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

    public JPanel createParameterControl(Parameter parameter, ModuleCollection modules, HCModule module, int panelWidth) {
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

        } else if (parameter.getType() == Parameter.INPUT_OBJECTS) {
            // Getting a list of available images
            LinkedHashSet<Parameter> objects = modules.getParametersMatchingType(Parameter.OUTPUT_OBJECTS,module);

            String[] names = new String[objects.size()];
            int i = 0;
            for (Parameter object : objects) {
                names[i++] = object.getValue();
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

        } else if (parameter.getType() == Parameter.MEASUREMENT) {
            MeasurementCollection measurements = modules.getMeasurements(module);
            String[] measurementChoices = measurements.getMeasurementNames(parameter.getValueSource());
            Arrays.sort(measurementChoices);

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

    public JPanel createAdvancedModuleControl(HCModule module, ButtonGroup group, HCModule activeModule, int panelWidth) {
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
        modulePanel.add(evalButton,c);

        return modulePanel;

    }

    public JPanel createBasicModuleHeading(HCModule module, int panelWidth) {
        JPanel modulePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Adding the state/evaluate button
        c.gridx = 0;
        c.weightx = 0;
        c.insets = new Insets(5, 5, 0, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;
//        EvalButton evalButton = new EvalButton(gui,module);
//        evalButton.setPreferredSize(new Dimension(elementHeight,elementHeight));
//        modulePanel.add(evalButton,c);
//
//        // Adding the module title
//        c.gridx++;
//        c.weightx = 1;
//        c.anchor = GridBagConstraints.FIRST_LINE_END;
        JTextField title = new JTextField(module.getNickname());
        title.setEditable(false);
        title.setBorder(null);
        title.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        title.setPreferredSize(new Dimension(panelWidth-elementHeight,elementHeight));
        modulePanel.add(title,c);

        return modulePanel;

    }

    public JPanel createBasicModuleControl(HCModule module, int panelWidth) {
        // Only show if the module is enabled
        if (!module.isEnabled()) return null;

        // Only displaying the module title if it has at least one visible parameter
        boolean hasVisibleParameters = false;
        for (Parameter parameter : module.getActiveParameters().values()) {
            if (parameter.isVisible()) hasVisibleParameters = true;
        }
        if (!hasVisibleParameters) return null;

        JPanel modulePanel = new JPanel(new GridBagLayout());
        JPanel titlePanel = createBasicModuleHeading(module, panelWidth);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;

        c.anchor = GridBagConstraints.FIRST_LINE_START;
        modulePanel.add(titlePanel, c);

        for (Parameter parameter : module.getActiveParameters().values()) {
            if (parameter.isVisible()) {
                JPanel paramPanel = createParameterControl(parameter, gui.getModules(), module, panelWidth);

                c.gridy++;
                modulePanel.add(paramPanel, c);

            }
        }

        c.gridy++;
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(0, 15));
        modulePanel.add(separator, c);

        return modulePanel;

    }

    public JPanel createMeasurementSelector(Workspace workspace) {
        JPanel measurementPanel = new JPanel(new GridBagLayout());
        JScrollPane measurementScrollPane = new JScrollPane(measurementPanel);

        // Getting the image and object measurements
        LinkedHashMap<String, LinkedHashSet<String>> imageMeasurements = gui.getModules().getMeasurements().getImageMeasurements();
        LinkedHashMap<String, LinkedHashSet<String>> objectMeasurements = gui.getModules().getMeasurements().getObjectMeasurements();

        // Iterating over the images
        for (String imageName:imageMeasurements.keySet()) {
            // Iterating over the measurements for the current image, adding a control for each
            for (String imageMeasurementName:imageMeasurements.get(imageName)) {

            }
        }

        return measurementPanel;

    }
}
