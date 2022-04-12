package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.batik.ext.swing.GridBagConstants;

import io.github.mianalysis.mia.object.parameters.text.MessageP;

public class MessageArea extends ParameterControl {
    protected JPanel control;
    protected JTextArea textArea;

    private static final ImageIcon alertIcon = new ImageIcon(
            MessageArea.class.getResource("/icons/alert_orange_12px.png"), "");
    private static final ImageIcon warningIcon = new ImageIcon(
            MessageArea.class.getResource("/icons/warning_red_12px.png"), "");

    public MessageArea(MessageP parameter, int controlHeight) {
        super(parameter);

        control = new JPanel();
        control.setLayout(new GridBagLayout());
        control.setBorder(BorderFactory.createEmptyBorder());
        control.setBackground(null);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        ImageIcon imageIcon = getIcon(parameter.getIcon());
        if (imageIcon != null) {
            c.weightx = 0;            
            c.insets = new Insets(0,2,0,5);
            c.anchor = GridBagConstants.CENTER;
            JLabel iconLabel = new JLabel();
            iconLabel.setIcon(imageIcon);
            control.add(iconLabel, c);
            c.gridx++;
            c.insets = new Insets(0,0,0,0);
            c.weightx = 1;
        }

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(null);
        textArea.setText(parameter.getRawStringValue());
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textArea.setForeground(parameter.getColor());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        textArea.setBorder(BorderFactory.createEmptyBorder());

        control.add(textArea, c);

    }

    ImageIcon getIcon(String icon) {
        if (icon == null)
            return null;

        switch (icon) {
            case MessageP.Icons.ALERT:
                return alertIcon;
            case MessageP.Icons.WARNING:
                return warningIcon;
            case MessageP.Icons.NONE:
            default:
                return null;
        }
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
