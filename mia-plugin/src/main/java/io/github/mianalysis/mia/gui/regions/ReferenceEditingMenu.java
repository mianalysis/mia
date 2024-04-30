package io.github.mianalysis.mia.gui.regions;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import ij.measure.UserFunction;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextSwitchableParameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.abstrakt.Ref;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;

public class ReferenceEditingMenu extends JPopupMenu implements ActionListener {
    private static final long serialVersionUID = 3459551119073952948L;
    private final Ref reference;

    private static final String RENAME = "Rename";
    private static final String RESET_NAME = "Reset name";
    private static final String CHANGE_TO_TEXT = "Change to text entry";
    private static final String CHANGE_TO_DEFAULT = "Change to default entry";
    private static final String INSERT_DYNAMIC_VALUE = "Insert dynamic value";
    private static final String IMAGE_MEASUREMENTS = "Image measurements";

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

        if (reference instanceof TextSwitchableParameter) {
            addSeparator();
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
            Module module = ((Parameter) reference).getModule();

            JMenu dynamicVariableMenu = createDynamicVariableMenu((Parameter) reference, module);
            if (dynamicVariableMenu.getMenuComponentCount() > 0) {
                addSeparator();                
                add(dynamicVariableMenu);
            }
        }
    }

    private static JMenu createDynamicVariableMenu(Parameter parameter, Module module) {
        JMenu dynamicVariableMenu = new JMenu(INSERT_DYNAMIC_VALUE);

        // Adding image measurements
        JMenu imageMeasurementsMenu = createImageMeasurementsMenu(parameter, module);
        if (imageMeasurementsMenu.getMenuComponentCount() > 0)
            dynamicVariableMenu.add(imageMeasurementsMenu);

        return dynamicVariableMenu;

    }

    private static JMenu createImageMeasurementsMenu(Parameter parameter, Module module) {
        JMenu measurementsMenu = new JMenu(IMAGE_MEASUREMENTS);

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
                measurementMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        parameter.setValueFromString(parameter.getRawStringValue() + "Im{"
                                + availableImage.getImageName() + "|" + measurement.getName() + "}");
                        GUI.updateParameters();
                    }
                });
                imageMenu.add(measurementMenuItem);

            }
        }

        return measurementsMenu;

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

        GUI.updateModules();
        GUI.updateParameters();

    }
}