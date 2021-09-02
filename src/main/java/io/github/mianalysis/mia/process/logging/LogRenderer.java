package io.github.mianalysis.mia.process.logging;

public interface LogRenderer {
    public enum Level {
        MESSAGE, WARNING, ERROR, DEBUG, MEMORY, STATUS;
    }

    public void write(String message, Level level);

    public boolean isWriteEnabled(Level level);
    public void setWriteEnabled(Level level, boolean writeEnabled);

}
