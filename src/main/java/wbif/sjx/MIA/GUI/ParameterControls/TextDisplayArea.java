package wbif.sjx.MIA.GUI.ParameterControls;

import wbif.sjx.MIA.GUI.ParameterControl;
import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;

import javax.swing.*;
import java.awt.*;

public class TextDisplayArea extends ParameterControl {
    private TextType parameter;
    private JPanel control;
    private JTextArea textArea;

    public TextDisplayArea(TextType parameter) {
        this.parameter = parameter;

        control = new JPanel();

        control.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setText(parameter.getValueAsString());
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        JScrollPane objectsScrollPane = new JScrollPane(textArea);
        control.setPreferredSize(new Dimension(0,150));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        control.add(objectsScrollPane,c);

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
        textArea.setText(parameter.getValueAsString());
    }
}
