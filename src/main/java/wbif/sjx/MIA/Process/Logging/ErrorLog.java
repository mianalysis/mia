package wbif.sjx.MIA.Process.Logging;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Process.Logging.Log;

import java.io.IOException;
import java.io.OutputStream;

public class ErrorLog extends OutputStream {
    private static StringBuilder stringBuilder = new StringBuilder();

    @Override
    public void write(int b) throws IOException {
        if (b != 10) {
            stringBuilder.append((char) b);
        } else {
            String errorMessage = stringBuilder.toString();

            // Some other tools may append "Error" or "Warning" messages.  This attempts to identify these and handle
            // them accordingly.
            if (errorMessage.trim().substring(0,7).equals("[ERROR]")) {
                MIA.log.write(errorMessage.trim().substring(8).trim(), Log.Level.ERROR);
            } else if (errorMessage.trim().substring(0,9).equals("[WARNING]")) {
                MIA.log.write(errorMessage.trim().substring(10).trim(), Log.Level.WARNING);
            } else if (errorMessage.trim().substring(0,6).equals("[WARN]")) {
                MIA.log.write(errorMessage.trim().substring(7).trim(), Log.Level.WARNING);
            } else {
                MIA.log.write(errorMessage, Log.Level.ERROR);
            }

            stringBuilder = new StringBuilder();
        }
    }
}
