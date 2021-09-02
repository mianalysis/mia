package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.Object.Parameters.Text.TextAreaP;

public class TextAreaParameter extends ParameterControl implements FocusListener {
    protected JPanel control;
    private  JTextArea textArea;
    private JScrollPane objectsScrollPane;
    private String prevString = "";

    public TextAreaParameter(TextAreaP parameter) {
        super(parameter);
        this.prevString = parameter.getRawStringValue();

        createControl(250);
        
    }

    public TextAreaParameter(TextAreaP parameter, int height) {
        super(parameter);
        this.prevString = parameter.getRawStringValue();

        createControl(height);

    }
    
    void createControl(int height) {
        control = new JPanel();

        control.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        textArea = new JTextArea();
        Document document = textArea.getDocument();
        if (document instanceof PlainDocument) {
            document.putProperty(PlainDocument.tabSizeAttribute, 4);
        }
        textArea.setEditable(((TextAreaP) parameter).isEditable());
        textArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setText(parameter.getRawStringValue());
        textArea.addFocusListener(this);
        textArea.setCaretPosition(0);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        objectsScrollPane = new JScrollPane(textArea);
        control.setPreferredSize(new Dimension(0, height));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        objectsScrollPane.getVerticalScrollBar().setValue(0);
        control.add(objectsScrollPane, c);
    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {
        if (!parameter.getRawStringValue().equals(prevString)) {
            this.prevString = parameter.getRawStringValue();

            textArea.setText(parameter.getRawStringValue());
            textArea.setCaretPosition(0);
            textArea.repaint();
        }
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        GUI.addUndo();

        if (!((TextAreaP) parameter).isEditable()) return;

        parameter.setValueFromString(textArea.getText());
        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval() & !(parameter.getModule() instanceof OutputControl)) GUI.setLastModuleEval(idx-1);

        updateControl();

        GUI.updateModuleStates(true);

    }
}
