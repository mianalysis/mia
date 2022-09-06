package io.github.mianalysis.mia.process.logging;

import java.util.HashMap;

public abstract class LogRenderer {
    protected HashMap<Level,Boolean> levelStatus = new HashMap<>();
    protected int progress = -1;
    
    public enum Level {
        MESSAGE, WARNING, ERROR, DEBUG, MEMORY, STATUS;
    }
    
    public abstract void write(String message, Level level);


    public boolean isWriteEnabled(Level level) {
        return levelStatus.get(level);
    }

    public void setWriteEnabled(Level level, boolean writeEnabled) {
        levelStatus.put(level, writeEnabled);
    }
}
