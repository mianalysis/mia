package wbif.sjx.MIA.Process.Logging;

public class BasicLogRenderer implements LogRenderer {
    // This could just be a text window

    @Override
    public void write(String message, Level level) {

    }

    @Override
    public boolean isWriteEnabled(Level level) {
        return false;
    }

    @Override
    public void setWriteEnabled(Level level, boolean writeEnabled) {

    }

    @Override
    public String getLogText() {
        return null;
    }

    @Override
    public void clearLog() {

    }
}
