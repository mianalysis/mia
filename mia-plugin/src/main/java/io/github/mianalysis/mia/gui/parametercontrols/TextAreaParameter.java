package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.CaretReporter;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.SwingPreferences;

public class TextAreaParameter extends ParameterControl implements CaretReporter, FocusListener {
    protected JPanel control;
    protected int caretPosition = 0;

    private JLabel resizeBar;
    private  JTextArea textArea;
    private JScrollPane objectsScrollPane;
    private String prevString = "";
    private int dragStartY;
    
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
        // textArea.setEditable(((TextAreaP) parameter).isEditable());
        textArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setText(parameter.getRawStringValue());
        textArea.addFocusListener(this);
        textArea.setCaretPosition(0);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        objectsScrollPane = new JScrollPane(textArea);
        objectsScrollPane.setPreferredSize(new Dimension(0, height));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        objectsScrollPane.getVerticalScrollBar().setValue(0);
        objectsScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        control.add(objectsScrollPane, c);

        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        resizeBar = new JLabel("Drag to resize", SwingConstants.CENTER);
        resizeBar.setForeground(Colours.getDarkBlue(isDark));
        resizeBar.setPreferredSize(new Dimension(0,20));
        resizeBar.addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent e) {
                dragStartY = e.getYOnScreen();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
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
        });

        resizeBar.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dy = e.getYOnScreen() - dragStartY;
                dragStartY = e.getYOnScreen();

                Dimension size = objectsScrollPane.getPreferredSize();
                size.height = Math.max(50, size.height + dy);
                objectsScrollPane.setPreferredSize(size);
                objectsScrollPane.revalidate();
                control.revalidate();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
            
        });
        c.gridy++;
        c.gridy++;
        control.add(resizeBar, c);

    }

    public int getCaretPosition() {
        return caretPosition;
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
        caretPosition = textArea.getCaretPosition();
        GUI.addUndo();

        if (!((TextAreaP) parameter).isEditable()) return;

        parameter.setValueFromString(textArea.getText());
        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval() & !(parameter.getModule() instanceof OutputControl)) GUI.setLastModuleEval(idx-1);

        updateControl();

        GUI.updateModuleStates(true, parameter.getModule());
        updateControl();

    }
}
