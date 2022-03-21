package io.github.mianalysis.mia.process.logging;

import javax.swing.*;
import java.util.HashMap;

public class StatusPanelRenderer implements LogRenderer {
    private final JLabel textField;

    private HashMap<Level,Boolean> levelStatus = new HashMap<>();

    public StatusPanelRenderer(JLabel textField) {
        this.textField = textField;

        setWriteEnabled(LogRenderer.Level.MESSAGE,false);
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

    @Override
    public boolean isWriteEnabled(Level level) {
        return levelStatus.get(level);
    }

    @Override
    public void setWriteEnabled(Level level, boolean writeEnabled) {
        levelStatus.put(level,writeEnabled);
    }
}
