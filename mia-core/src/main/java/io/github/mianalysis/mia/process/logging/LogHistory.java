package io.github.mianalysis.mia.process.logging;

import java.util.HashMap;

public class LogHistory extends LogRenderer {
    private HashMap<Level,String> logHistory = new HashMap<>();

    public LogHistory() {
        levelStatus.put(Level.DEBUG,false);
        levelStatus.put(Level.ERROR,true);
        levelStatus.put(Level.MEMORY,false);
        levelStatus.put(Level.MESSAGE,false);
        levelStatus.put(Level.STATUS,false);
        levelStatus.put(Level.WARNING,true);

    }

    @Override
    public void write(String message, Level level) {
        // If this level isn't currently being written, skip it
        if (levelStatus.get(level) == null || !levelStatus.get(level))
            return;
        
        logHistory.putIfAbsent(level, "");
        logHistory.put(level, logHistory.get(level) + message);
        
    }
    
    public String getLogHistory(Level level) {
        logHistory.putIfAbsent(level,"");
        return logHistory.get(level);
    }

    public void clearLogHistory() {
        logHistory.clear();
    }
}
