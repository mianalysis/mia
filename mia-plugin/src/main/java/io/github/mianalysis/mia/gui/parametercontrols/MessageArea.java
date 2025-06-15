package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.system.SwingPreferences;

public class MessageArea extends ParameterControl {
    protected JPanel control;
    protected JTextPane textPane;

    public MessageArea(MessageP parameter, int controlHeight) {
        super(parameter);

        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        control = new JPanel();
        control.setLayout(new GridBagLayout());
        control.setBorder(BorderFactory.createEmptyBorder());
        control.setBackground(null);

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(null);
        textPane.setContentType("text/html");
        textPane.setText(parameter.getRawStringValue());
        textPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textPane.setForeground(SwingParameterControlFactory.getColor(parameter.getState(), isDark));
        textPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        textPane.setBorder(BorderFactory.createEmptyBorder());
        textPane.addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JScrollPane objectsScrollPane = new JScrollPane(textPane);
        objectsScrollPane.setPreferredSize(new Dimension(0, controlHeight));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        objectsScrollPane.getVerticalScrollBar().setValue(0);
        objectsScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        objectsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        control.add(objectsScrollPane, c);

    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {
        textPane.setText(parameter.getRawStringValue());
    }
}
