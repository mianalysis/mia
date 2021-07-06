package wbif.sjx.MIA.GUI.ParameterControls;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import wbif.sjx.MIA.Object.Parameters.Text.MessageP;

public class MessageArea extends ParameterControl {
    protected JPanel control;
    protected JTextArea textArea;

    public MessageArea(MessageP parameter, int controlHeight) {
        super(parameter);

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
        objectsScrollPane.setPreferredSize(new Dimension(0,controlHeight));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        objectsScrollPane.getVerticalScrollBar().setValue(0);
        objectsScrollPane.setBorder(null);
        control.add(objectsScrollPane,c);


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
