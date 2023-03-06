package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JTextField;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.SliderTextSwitch;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.NumberType;

/**
 * Created by Stephen on 20/05/2017.
 */
public class SliderParameter extends ParameterControl implements FocusListener, MouseListener {
    protected JSlider sliderControl;
    protected JTextField textControl;

    private SliderTextSwitch sliderTextSwitch;

    public SliderParameter(NumberType parameter) {
        super(parameter);

        sliderTextSwitch = new SliderTextSwitch(parameter);

        sliderControl = new JSlider();        
        sliderControl.addMouseListener(this);
        sliderControl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        sliderControl.setValue(parameter.getValue(null));
        sliderControl.addFocusListener(this);

        textControl = new JTextField();
        textControl.addMouseListener(this);
        textControl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textControl.setText(parameter.getRawStringValue());
        textControl.addFocusListener(this);

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        GUI.addUndo();

        if (((NumberType) parameter).isSlider())
            parameter.setValueFromString(String.valueOf(sliderControl.getValue()));
        else
            parameter.setValueFromString(textControl.getText());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval() & !(parameter.getModule() instanceof OutputControl))
            GUI.setLastModuleEval(idx - 1);

        GUI.updateModuleStates();
        updateControl();

    }

    @Override
    public JComponent getComponent() {
        if (((NumberType) parameter).isSlider())
            return sliderControl;
        else
            return textControl;
    }

    @Override
    public void updateControl() {
        if (((NumberType) parameter).isSlider())
            sliderControl.setValue(Integer.parseInt(parameter.getRawStringValue()));
        else
            textControl.setText(parameter.getRawStringValue());
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        // Only display menu if the right mouse button is clicked
        if (arg0.getButton() != MouseEvent.BUTTON3)
            return;

        sliderTextSwitch.show(GUI.getFrame(), 0, 0);
        sliderTextSwitch.setLocation(MouseInfo.getPointerInfo().getLocation());
        sliderTextSwitch.setVisible(true);
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }
}
