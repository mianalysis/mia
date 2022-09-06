package io.github.mianalysis.mia.process.logging;

import ij.IJ;

public class IJ1Renderer extends LogRenderer {
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
}
