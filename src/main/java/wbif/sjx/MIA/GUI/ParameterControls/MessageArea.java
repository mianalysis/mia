package wbif.sjx.MIA.GUI.ParameterControls;

import wbif.sjx.MIA.GUI.Colours;
import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;
import wbif.sjx.MIA.Object.Parameters.MessageP;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;

public class MessageArea extends ParameterControl {
    protected TextType parameter;
    protected JPanel control;
    protected JTextArea textArea;

    public MessageArea(MessageP parameter) {
        this.parameter = parameter;

        control = new JPanel();

        control.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(null);
        textArea.setText(parameter.getRawStringValue());
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textArea.setForeground(parameter.getColor());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        textArea.setBorder(null);

        JScrollPane objectsScrollPane = new JScrollPane(textArea);
        objectsScrollPane.setPreferredSize(new Dimension(0,50));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        objectsScrollPane.getVerticalScrollBar().setValue(0);
        objectsScrollPane.setBorder(null);
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
        textArea.setText(parameter.getRawStringValue());
    }
}
