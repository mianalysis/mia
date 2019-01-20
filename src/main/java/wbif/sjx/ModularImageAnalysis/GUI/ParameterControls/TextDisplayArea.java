package wbif.sjx.ModularImageAnalysis.GUI.ParameterControls;

import wbif.sjx.ModularImageAnalysis.Object.ParameterOld;

import javax.swing.*;
import java.awt.*;

public class TextDisplayArea extends JPanel {
    private ParameterOld parameter;

    public TextDisplayArea(ParameterOld parameter) {
        this.parameter = parameter;

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        String name = parameter.getValue() == null ? "" : parameter.getValue().toString();
        textArea.setText(name);
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        JScrollPane objectsScrollPane = new JScrollPane(textArea);
        setPreferredSize(new Dimension(0,150));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        add(objectsScrollPane,c);

    }

    public ParameterOld getParameter() {
        return parameter;
    }
}
