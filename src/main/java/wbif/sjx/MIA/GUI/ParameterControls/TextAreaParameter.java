package wbif.sjx.MIA.GUI.ParameterControls;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.Parameters.TextAreaP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class TextAreaParameter extends ParameterControl implements FocusListener {
    protected TextAreaP parameter;
    protected JPanel control;
    private  JEditorPane textArea;
    private JScrollPane objectsScrollPane;

    public TextAreaParameter(TextAreaP parameter) {
        this.parameter = parameter;

        control = new JPanel();

        control.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        textArea = new JEditorPane();
        textArea.setEditable(parameter.isEditable());
        textArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textArea.setText(parameter.getRawStringValue());
        textArea.addFocusListener(this);

        objectsScrollPane = new JScrollPane(textArea);
        control.setPreferredSize(new Dimension(0,250));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        objectsScrollPane.getVerticalScrollBar().setValue(0);
        control.add(objectsScrollPane,c);

    }

    public TextAreaP getParameter() {
        return parameter;
    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {
        textArea.setText(parameter.getRawStringValue());
        textArea.repaint();
        objectsScrollPane.getVerticalScrollBar().setValue(0);

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        if (!parameter.isEditable()) return;

        parameter.setValueFromString(textArea.getText());
        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx-1);

        updateControl();

        GUI.updateModuleStates(true);
    }
}
