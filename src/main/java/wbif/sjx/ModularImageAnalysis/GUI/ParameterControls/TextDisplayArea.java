package wbif.sjx.ModularImageAnalysis.GUI.ParameterControls;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameter;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class TextDisplayArea extends JPanel {
    private Parameter parameter;

    public TextDisplayArea(Parameter parameter) {
        this.parameter = parameter;

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        JTextArea textArea = new JTextArea();
        JScrollPane objectsScrollPane = new JScrollPane(textArea);
        setPreferredSize(new Dimension(0,150));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        add(objectsScrollPane,c);

        String name = parameter.getValue() == null ? "" : parameter.getValue().toString();
        textArea.setText(name);

    }

    public Parameter getParameter() {
        return parameter;
    }
}
