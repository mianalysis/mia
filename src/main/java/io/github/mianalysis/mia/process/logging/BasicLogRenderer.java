package io.github.mianalysis.mia.process.logging;

public class BasicLogRenderer extends LogRenderer {
    public BasicLogRenderer() {
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
}
