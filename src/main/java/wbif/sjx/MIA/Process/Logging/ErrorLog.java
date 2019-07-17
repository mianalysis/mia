package wbif.sjx.MIA.Process.Logging;

import wbif.sjx.MIA.MIA;

import java.io.IOException;
import java.io.OutputStream;

public class ErrorLog extends OutputStream {
    private static StringBuilder stringBuilder = new StringBuilder();
    private static final ErrorLog errorLog = new ErrorLog();

    private ErrorLog() {

    }

    public static ErrorLog getInstance() {
        return errorLog;
    }

    @Override
    public void write(int b) throws IOException {
        if (b != 10) {
            stringBuilder.append((char) b);
        } else {
            String errorMessage = stringBuilder.toString();

            // Some other tools may append "Error" or "Warning" messages.  This attempts to identify these and handle
            // them accordingly.
            if (errorMessage.trim().substring(0,7).equals("[ERROR]")) {
                MIA.log.write(errorMessage.trim().substring(8).trim(), LogRenderer.Level.ERROR);
            } else if (errorMessage.trim().substring(0,9).equals("[WARNING]")) {
                MIA.log.write(errorMessage.trim().substring(10).trim(), LogRenderer.Level.WARNING);
            } else if (errorMessage.trim().substring(0,6).equals("[WARN]")) {
                MIA.log.write(errorMessage.trim().substring(7).trim(), LogRenderer.Level.WARNING);
            } else {
                MIA.log.write(errorMessage, LogRenderer.Level.ERROR);
            }

            stringBuilder = new StringBuilder();
        }
    }
}
