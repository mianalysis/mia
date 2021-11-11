package io.github.mianalysis.mia.process.logging;

import java.util.HashMap;

import ij.Prefs;

public class BasicLogRenderer implements LogRenderer {
    private HashMap<Level, Boolean> levelStatus = new HashMap<>();

    public BasicLogRenderer() {
        levelStatus.put(Level.DEBUG, Prefs.get("MIA_Log_Debug", false));
        levelStatus.put(Level.ERROR, true); // While this can be turned off during a session, it should always re-enable
                                            // by default
        levelStatus.put(Level.MEMORY, Prefs.get("MIA_Log_Memory", false));
        levelStatus.put(Level.MESSAGE, Prefs.get("MIA_Log_Message", false));
        levelStatus.put(Level.STATUS, Prefs.get("MIA_Log_Status", true));
        levelStatus.put(Level.WARNING, Prefs.get("MIA_Log_Warning", true));

    }

    @Override
    public void write(String message, Level level) {
        // If this level isn't currently being written, skip it
        if (levelStatus.get(level) == null || !levelStatus.get(level))
            return;

        switch (level) {
        default:
        case WARNING:
        case MESSAGE:
        case MEMORY:
        case DEBUG:
        case STATUS:
            System.out.println("[" + level.toString() + "] " + message);
            break;
        case ERROR:
            System.err.println("[" + level.toString() + "] " + message);
            break;
        }
    }

    @Override
    public boolean isWriteEnabled(Level level) {
        return levelStatus.get(level);
    }

    @Override
    public void setWriteEnabled(Level level, boolean writeEnabled) {
        switch (level) {
        default:
        case WARNING:
            Prefs.set("MIA_Log_Warning", writeEnabled);
            break;
        case MESSAGE:
            Prefs.set("MIA_Log_Message", writeEnabled);
            break;
        case MEMORY:
            Prefs.set("MIA_Log_Memory", writeEnabled);
            break;
        case DEBUG:
            Prefs.set("MIA_Log_Debug", writeEnabled);
            break;
        case STATUS:
            Prefs.set("MIA_Log_Status", writeEnabled);
            break;
        }
        levelStatus.put(level, writeEnabled);
    }
}
