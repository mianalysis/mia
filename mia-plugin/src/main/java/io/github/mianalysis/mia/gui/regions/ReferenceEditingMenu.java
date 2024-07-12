package io.github.mianalysis.mia.gui.regions;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.abstrakt.CaretReporter;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextSwitchableParameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.MetadataRef;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.abstrakt.Ref;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;

public class ReferenceEditingMenu extends JPopupMenu implements ActionListener {
    private static final long serialVersionUID = 3459551119073952948L;
    private final Ref reference;

    private static final String RENAME = "Rename";
    private static final String RESET_NAME = "Reset name";
    private static final String CHANGE_TO_TEXT = "Change to text entry";
    private static final String CHANGE_TO_DEFAULT = "Change to default entry";
    private static final String INSERT_DYNAMIC_VALUE = "Insert dynamic value";
    private static final String IMAGE_MEASUREMENTS = "Image measurements";
    private static final String METADATA_ITEM = "Metadata item";
    private static final String OBJECT_COUNTS = "Object counts";
    private static final String OBJECT_MEASUREMENTS = "Object measurements";
    private static final String INSERT_GLOBAL_VARIABLE = "Insert global variable";

    public ReferenceEditingMenu(Ref reference) {
        this.reference = reference;

        JMenuItem renameMenuItem = new JMenuItem();
        renameMenuItem.setText(RENAME);
        renameMenuItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        renameMenuItem.addActionListener(this);
        add(renameMenuItem);

        JMenuItem resetMenuItem = new JMenuItem("");
        resetMenuItem.setText(RESET_NAME);
        resetMenuItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        resetMenuItem.addActionListener(this);
        add(resetMenuItem);

        if (reference instanceof TextType || reference instanceof TextSwitchableParameter)
            addSeparator();

        if (reference instanceof TextSwitchableParameter) {
            JMenuItem textSwitchMenuItem = new JMenuItem();
            if (((TextSwitchableParameter) reference).isShowText())
                textSwitchMenuItem.setText(CHANGE_TO_DEFAULT);
            else
                textSwitchMenuItem.setText(CHANGE_TO_TEXT);
            textSwitchMenuItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            textSwitchMenuItem.addActionListener(this);

            add(textSwitchMenuItem);

        }

        if (reference instanceof TextType || (reference instanceof TextSwitchableParameter
                && ((TextSwitchableParameter) reference).isShowText())) {
            add(createDynamicVariableMenu((Parameter) reference));
            add(createGlobalVariablesMenu((Parameter) reference));
        }
    }

    private static JMenu createDynamicVariableMenu(Parameter parameter) {
        JMenu dynamicVariableMenu = new JMenu(INSERT_DYNAMIC_VALUE);

        dynamicVariableMenu.add(createImageMeasurementsMenu(parameter));
        dynamicVariableMenu.add(createMetadataMenu(parameter));
        dynamicVariableMenu.add(createObjectCountsMenu(parameter));
        dynamicVariableMenu.add(createObjectMeasurementsMenu(parameter));

        return dynamicVariableMenu;

    }

    private static JMenu createImageMeasurementsMenu(Parameter parameter) {
        JMenu measurementsMenu = new JMenu(IMAGE_MEASUREMENTS);

        Module module = parameter.getModule();

        LinkedHashSet<OutputImageP> availableImages = module.getModules().getAvailableImages(module);
        for (OutputImageP availableImage : availableImages) {
            // Getting measurements
            ImageMeasurementRefs measurements = module.getModules().getImageMeasurementRefs(
                    availableImage.getImageName(),
                    module);

            if (measurements == null || measurements.size() == 0)
                continue;

            JMenu imageMenu = new JMenu(availableImage.getImageName());
            measurementsMenu.add(imageMenu);

            for (ImageMeasurementRef measurement : measurements.values()) {
                JMenuItem measurementMenuItem = new JMenuItem(measurement.getName());
                measurementMenuItem.addActionListener(new DynamicVariableActionListener(parameter,
                        "Im{" + availableImage.getImageName() + "|" + measurement.getName() + "}"));
                imageMenu.add(measurementMenuItem);

            }
        }

        if (measurementsMenu.getMenuComponentCount() == 0) {
            JMenuItem none = new JMenuItem("None");
            none.setEnabled(false);
            measurementsMenu.add(none);
        }

        return measurementsMenu;

    }

    private static JMenu createMetadataMenu(Parameter parameter) {
        JMenu measurementsMenu = new JMenu(METADATA_ITEM);

        Module module = parameter.getModule();

        MetadataRefs availableMetadata = module.getModules().getMetadataRefs(module);
        for (MetadataRef availableMetadataItem : availableMetadata.values()) {
            JMenuItem objectItem = new JMenuItem(availableMetadataItem.getName());
            objectItem.addActionListener(
                    new DynamicVariableActionListener(parameter, "Me{" + availableMetadataItem.getName() + "}"));

            measurementsMenu.add(objectItem);
        }

        if (measurementsMenu.getMenuComponentCount() == 0) {
            JMenuItem none = new JMenuItem("None");
            none.setEnabled(false);
            measurementsMenu.add(none);
        }

        return measurementsMenu;

    }

    private static JMenu createObjectCountsMenu(Parameter parameter) {
        JMenu measurementsMenu = new JMenu(OBJECT_COUNTS);

        Module module = parameter.getModule();

        LinkedHashSet<OutputObjectsP> availableObjects = module.getModules().getAvailableObjects(module);
        
        for (OutputObjectsP availableObject : availableObjects) {
            JMenuItem objectItem = new JMenuItem(availableObject.getObjectsName());
            objectItem.addActionListener(
                    new DynamicVariableActionListener(parameter, "Oc{" + availableObject.getObjectsName() + "}"));

            measurementsMenu.add(objectItem);
        }

        if (measurementsMenu.getMenuComponentCount() == 0) {
            JMenuItem none = new JMenuItem("None");
            none.setEnabled(false);
            measurementsMenu.add(none);
        }

        return measurementsMenu;

    }

    private static JMenu createObjectMeasurementsMenu(Parameter parameter) {
        JMenu measurementsMenu = new JMenu(OBJECT_MEASUREMENTS);

        Module module = parameter.getModule();

        LinkedHashSet<OutputObjectsP> availableObjects = module.getModules().getAvailableObjects(module);
        for (OutputObjectsP availableObject : availableObjects) {
            // Getting measurements
            ObjMeasurementRefs measurements = module.getModules()
                    .getObjectMeasurementRefs(availableObject.getObjectsName(), module);

            if (measurements == null || measurements.size() == 0)
                continue;

            JMenu objectMenu = new JMenu(availableObject.getObjectsName());
            measurementsMenu.add(objectMenu);

            for (ObjMeasurementRef measurement : measurements.values()) {
                JMenu measurementMenu = new JMenu(measurement.getName());
                objectMenu.add(measurementMenu);

                JMenuItem maxMenuItem = new JMenuItem("Maximum");
                maxMenuItem.addActionListener(new DynamicVariableActionListener(parameter,
                        "Os{" + availableObject.getObjectsName() + "|" + measurement.getName() + "|MAX}"));
                measurementMenu.add(maxMenuItem);

                JMenuItem meanMenuItem = new JMenuItem("Mean");
                meanMenuItem.addActionListener(new DynamicVariableActionListener(parameter,
                        "Os{" + availableObject.getObjectsName() + "|" + measurement.getName() + "|MEAN}"));
                measurementMenu.add(meanMenuItem);

                JMenuItem minMenuItem = new JMenuItem("Minimum");
                minMenuItem.addActionListener(new DynamicVariableActionListener(parameter,
                        "Os{" + availableObject.getObjectsName() + "|" + measurement.getName() + "|MIN}"));
                measurementMenu.add(minMenuItem);

                JMenuItem stdMenuItem = new JMenuItem("Standard deviation");
                stdMenuItem.addActionListener(new DynamicVariableActionListener(parameter,
                        "Os{" + availableObject.getObjectsName() + "|" + measurement.getName() + "|STD}"));
                measurementMenu.add(stdMenuItem);

                JMenuItem sumMenuItem = new JMenuItem("Sum");
                sumMenuItem.addActionListener(new DynamicVariableActionListener(parameter,
                        "Os{" + availableObject.getObjectsName() + "|" + measurement.getName() + "|SUM}"));
                measurementMenu.add(sumMenuItem);

            }
        }

        if (measurementsMenu.getMenuComponentCount() == 0) {
            JMenuItem none = new JMenuItem("None");
            none.setEnabled(false);
            measurementsMenu.add(none);
        }

        return measurementsMenu;

    }

    private static JMenu createGlobalVariablesMenu(Parameter parameter) {
        JMenu globalVariablesMenu = new JMenu(INSERT_GLOBAL_VARIABLE);

        TreeSet<String> sortedNames = GlobalVariables.getGlobalVariables().keySet().stream()
                .map((v) -> v.getRawStringValue()).collect(Collectors.toCollection(TreeSet::new));
        for (String globalVariableName : sortedNames) {
            JMenuItem globalVariableItem = new JMenuItem(globalVariableName);
            globalVariableItem
                    .addActionListener(new DynamicVariableActionListener(parameter, "V{" + globalVariableName + "}"));
            globalVariablesMenu.add(globalVariableItem);
        }

        if (globalVariablesMenu.getMenuComponentCount() == 0) {
            JMenuItem none = new JMenuItem("None");
            none.setEnabled(false);
            globalVariablesMenu.add(none);
        }

        return globalVariablesMenu;

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();
        setVisible(false);

        switch (e.getActionCommand()) {
            case RENAME:
                String nickname = (String) JOptionPane.showInputDialog(new JFrame(), "Enter new name", "",
                        JOptionPane.PLAIN_MESSAGE, null, null, reference.getNickname());
                if (nickname != null)
                    reference.setNickname(nickname);
                break;
            case RESET_NAME:
                reference.setNickname(reference.getName());
                break;
            case CHANGE_TO_TEXT:
                ((TextSwitchableParameter) reference).setShowText(true);
                break;
            case CHANGE_TO_DEFAULT:
                ((TextSwitchableParameter) reference).setShowText(false);
                break;
        }

        GUI.updateModules(false,null);
        GUI.updateParameters(false,null);

    }
}

class DynamicVariableActionListener implements ActionListener {
    private Parameter parameter;
    private String stringToAdd;

    public DynamicVariableActionListener(Parameter parameter, String stringToAdd) {
        this.parameter = parameter;
        this.stringToAdd = stringToAdd;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String inputString = parameter.getRawStringValue();
        int position = inputString.length();

        if (parameter.getControl() instanceof CaretReporter)
            position = ((CaretReporter) parameter.getControl()).getCaretPosition();

        parameter
                .setValueFromString(inputString.substring(0, position) + stringToAdd + inputString.substring(position));

        GUI.updateParameters(true,parameter.getModule());

    }
}