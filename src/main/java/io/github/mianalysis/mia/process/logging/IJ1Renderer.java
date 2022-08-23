package io.github.mianalysis.mia.process.logging;

import java.util.HashMap;

import ij.IJ;
import ij.Prefs;

public class IJ1Renderer implements LogRenderer {
    private HashMap<Level, Boolean> levelStatus = new HashMap<>();

    public IJ1Renderer() {
        levelStatus.put(Level.DEBUG, false);
        levelStatus.put(Level.ERROR, true); // While this can be turned off during a session, it should always re-enable
                                            // by default
        levelStatus.put(Level.MEMORY, false);
        levelStatus.put(Level.MESSAGE, false);
        levelStatus.put(Level.STATUS, false);
        levelStatus.put(Level.WARNING, true);

    }

    @Override
    public void write(String message, Level level) {
        // If this level isn't currently being written, skip it
        if (levelStatus.get(level) == null || !levelStatus.get(level))
            return;

        IJ.log("[" + level.toString() + "] " + message);

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
            Prefs.set("MIA.Log.Warning", writeEnabled);
            break;
        case MESSAGE:
            Prefs.set("MIA.Log.Message", writeEnabled);
            break;
        case MEMORY:
            Prefs.set("MIA.Log.Memory", writeEnabled);
            break;
        case DEBUG:
            Prefs.set("MIA.Log.Debug", writeEnabled);
            break;
        case STATUS:
            Prefs.set("MIA.Log.Status", writeEnabled);
            break;
        }
        levelStatus.put(level, writeEnabled);
    }
}