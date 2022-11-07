package io.github.mianalysis.mia.process.logging;

import javax.swing.JLabel;

public class StatusPanelRenderer extends LogRenderer {
    private final JLabel textField;

    public StatusPanelRenderer(JLabel textField) {
        this.textField = textField;

        setWriteEnabled(LogRenderer.Level.MESSAGE,true);
        setWriteEnabled(LogRenderer.Level.ERROR,false);
        setWriteEnabled(LogRenderer.Level.WARNING,false);
        setWriteEnabled(LogRenderer.Level.MEMORY,false);
        setWriteEnabled(LogRenderer.Level.DEBUG,false);
        setWriteEnabled(LogRenderer.Level.STATUS,true);

    }

    @Override
    public void write(String message, Level level) {
        if (levelStatus.get(level)) textField.setText(message);
    }
}
