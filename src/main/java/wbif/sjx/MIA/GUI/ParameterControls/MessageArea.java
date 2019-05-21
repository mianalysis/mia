package wbif.sjx.MIA.GUI.ParameterControls;

import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;
import wbif.sjx.MIA.Object.Parameters.MessageP;

import javax.swing.*;
import java.awt.*;

public class MessageArea extends ParameterControl {
    protected TextType parameter;
    protected JPanel control;
    protected JTextArea textArea;

    public MessageArea(MessageP parameter) {
        this.parameter = parameter;

        control = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(null);
        textArea.setText(parameter.getRawStringValue());
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textArea.setForeground(parameter.getColor());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        control.add(textArea,c);

    }

    public TextType getParameter() {
        return parameter;
    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {
        textArea.setText(parameter.getRawStringValue());
    }
}
