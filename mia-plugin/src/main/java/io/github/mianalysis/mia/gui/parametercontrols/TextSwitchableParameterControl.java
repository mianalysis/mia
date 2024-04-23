package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import java.awt.Font;
import java.awt.MouseInfo;

import javax.swing.DefaultComboBoxModel;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextSwitchableParameter;

public abstract class TextSwitchableParameterControl extends ParameterControl implements FocusListener, MouseListener {
    private JTextField textControl;

    private ToggleModeMenu toggleModeMenu = new ToggleModeMenu();

    public abstract JComponent getDefaultComponent();
    public abstract void updateDefaultControl();

    public TextSwitchableParameterControl(TextSwitchableParameter parameter) {
        super(parameter);

        textControl = new JTextField();
        textControl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textControl.setText(parameter.getRawStringValue());
        textControl.addFocusListener(this);
        textControl.addMouseListener(this);

    }

    @Override
    public JComponent getComponent() {
        if (((TextSwitchableParameter) parameter).isShowText())
            return getTextComponent();
        else {
            JComponent defaultComponent = getDefaultComponent();
            defaultComponent.addMouseListener(this);
            return defaultComponent;
        }
    }

    public JComponent getTextComponent() {
        return textControl;
    }

    public void updateTextControl() {
        
    }

    @Override
    public void updateControl() {
        if (((TextSwitchableParameter) parameter).isShowText())
            updateTextControl();
        else
            updateDefaultControl();
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        GUI.addUndo();

        parameter.setValueFromString(textControl.getText());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval() & !(parameter.getModule() instanceof OutputControl))
            GUI.setLastModuleEval(idx - 1);

        GUI.updateParameters();
        GUI.updateModuleStates();

        updateControl();

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Only display menu if the right mouse button is clicked
        if (e.getButton() != MouseEvent.BUTTON3)
            return;

        // Populating the list containing all available modules
        toggleModeMenu.show(GUI.getFrame(), 0, 0);
        toggleModeMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
        toggleModeMenu.setVisible(true);

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

    class ToggleModeMenu extends JPopupMenu implements ActionListener {
        private static final String CHANGE_TO_TEXT = "Change to text entry";
        private static final String CHANGE_TO_DEFAULT = "Change to default entry";

        private JMenuItem menuItem = new JMenuItem();

        public ToggleModeMenu() {
            if (((TextSwitchableParameter) parameter).isShowText())
                menuItem.setText(CHANGE_TO_DEFAULT);
            else
                menuItem.setText(CHANGE_TO_TEXT);

            menuItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            menuItem.addActionListener(this);

            add(menuItem);

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            GUI.addUndo();
            setVisible(false);

            switch (e.getActionCommand()) {
                case CHANGE_TO_TEXT:
                    ((TextSwitchableParameter) parameter).setShowText(true);
                    menuItem.setText(CHANGE_TO_DEFAULT);
                    textControl.setText(parameter.getRawStringValue());
                    break;
                case CHANGE_TO_DEFAULT:
                    ((TextSwitchableParameter) parameter).setShowText(false);
                    menuItem.setText(CHANGE_TO_TEXT);
                    break;
            }

            GUI.updateModules();
            GUI.updateParameters();

        }
    }
}
