package io.github.mianalysis.mia.Process.Logging;

import java.util.HashMap;

public class BasicLogRenderer implements LogRenderer {
    private HashMap<Level,Boolean> levelStatus = new HashMap<>();

    public BasicLogRenderer() {
        levelStatus.put(Level.DEBUG,false);
        levelStatus.put(Level.ERROR,true);
        levelStatus.put(Level.MEMORY,false);
        levelStatus.put(Level.MESSAGE,true);
        levelStatus.put(Level.STATUS,true);
        levelStatus.put(Level.WARNING,true);

    }

    @Override
    public void write(String message, Level level) {
        // If this level isn't currently being written, skip it
        if (levelStatus.get(level) == null || !levelStatus.get(level)) return;

        switch (level) {
            default:
            case WARNING:
            case MESSAGE:
            case MEMORY:
            case DEBUG:
            case STATUS:
                System.out.println("["+level.toString()+"] "+message);
                break;
            case ERROR:
                System.err.println("["+level.toString()+"] "+message);
                break;
        }
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
