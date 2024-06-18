package io.github.mianalysis.mia.process.logging;

import javax.swing.JLabel;

public class StatusPanelRenderer extends LogRenderer {
    private final JLabel textField;

    public StatusPanelRenderer(JLabel textField) {
        this.textField = textField;

        setWriteEnabled(LogRenderer.Level.MESSAGE, false);
        setWriteEnabled(LogRenderer.Level.ERROR, false);
        setWriteEnabled(LogRenderer.Level.WARNING, false);
        setWriteEnabled(LogRenderer.Level.MEMORY, false);
        setWriteEnabled(LogRenderer.Level.DEBUG, false);
        setWriteEnabled(LogRenderer.Level.STATUS, true);

    }

    @Override
    public void write(String message, Level level) {
        if (levelStatus.get(level)) {
            if (message.contains("[") && message.contains("]")) {
                int idx = message.indexOf("]");
                message = "<html><b>" + message.substring(1, idx) + ": </b>" + message.substring(idx + 1) + "</html>";
            } else {
                message = "<html><b>" + message + "</html>";
            }
            textField.setText(message);
        }
    }
}
