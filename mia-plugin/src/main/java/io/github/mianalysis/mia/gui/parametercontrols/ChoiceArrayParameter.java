package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;

/**
 * Created by sc13967 on 22/05/2017.
 */
public class ChoiceArrayParameter extends ParameterControl implements ActionListener, FocusListener, MouseListener {
    private WiderDropDownCombo choiceControl;
    private JTextField textControl;
    private ToggleModeMenu toggleModeMenu = new ToggleModeMenu();

    public ChoiceArrayParameter(ChoiceType parameter) {
        super(parameter);

        // Choices may have not been initialised when this first runs, so a blank list
        // is created
        String[] choices = parameter.getChoices();
        if (choices == null)
            choices = new String[] { "" };
        choiceControl = new WiderDropDownCombo(choices);

        choiceControl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        choiceControl.setSelectedItem(parameter.getChoice());
        choiceControl.addActionListener(this);
        choiceControl.addMouseListener(this);
        choiceControl.setWide(true);

        textControl = new JTextField();
        textControl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textControl.setText(parameter.getRawStringValue());
        textControl.addFocusListener(this);
        textControl.addMouseListener(this);

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
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        ((ChoiceType) parameter).setValueFromString((String) choiceControl.getSelectedItem());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval() & !(parameter.getModule() instanceof OutputControl))
            GUI.setLastModuleEval(idx - 1);

        GUI.updateModules();
        GUI.updateParameters();

        updateControl();

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

        // GUI.updateParameters();
        GUI.updateModuleStates();
        updateControl();

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

    @Override
    public JComponent getComponent() {
        if (((ChoiceType) parameter).isShowChoice())
            return choiceControl;
        else
            return textControl;
    }

    @Override
    public void updateControl() {
        if (((ChoiceType) parameter).isShowChoice()) {
            String[] choices = ((ChoiceType) parameter).getChoices();
            if (choices == null)
                choices = new String[] { "" };

            // Getting previously-selected item
            String selected = (String) choiceControl.getSelectedItem();

            // Creating a new model
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel(choices);
            model.setSelectedItem(selected);

            // Updating the control
            choiceControl.setModel(model);
            choiceControl.repaint();
            choiceControl.revalidate();

        }
    }

    class ToggleModeMenu extends JPopupMenu implements ActionListener {
        private static final String CHANGE_TO_TEXT = "Change to text entry";
        private static final String CHANGE_TO_CHOICE = "Change to choice";

        private JMenuItem menuItem = new JMenuItem();

        public ToggleModeMenu() {
            if (((ChoiceType) parameter).isShowChoice())
                menuItem.setText(CHANGE_TO_TEXT);
            else
                menuItem.setText(CHANGE_TO_CHOICE);

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
                    ((ChoiceType) parameter).setShowChoice(false);
                    menuItem.setText(CHANGE_TO_CHOICE);
                    break;
                case CHANGE_TO_CHOICE:
                    ((ChoiceType) parameter).setShowChoice(true);
                    menuItem.setText(CHANGE_TO_TEXT);
                    break;
            }

            GUI.updateModules();
            GUI.updateParameters();

        }
    }

}
