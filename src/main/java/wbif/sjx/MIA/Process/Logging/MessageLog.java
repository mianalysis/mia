package wbif.sjx.MIA.Process.Logging;

import wbif.sjx.MIA.MIA;

import java.io.IOException;
import java.io.OutputStream;

public class MessageLog extends OutputStream {
    private static StringBuilder stringBuilder = new StringBuilder();
    private static final MessageLog messageLog = new MessageLog();

    private MessageLog() {

    }

    public static MessageLog getInstance() {
        return messageLog;
    }

    @Override
    public void write(int b) throws IOException {
        if (b != 10) {
            stringBuilder.append((char) b);
        } else {
            String message = stringBuilder.toString();

            if (message.trim().substring(0,7).equals("[ERROR]")) {
                MIA.log.write(message.trim().substring(8).trim(), LogRenderer.Level.ERROR);
            } else if (message.trim().substring(0,9).equals("[WARNING]")) {
                MIA.log.write(message.trim().substring(10).trim(), LogRenderer.Level.WARNING);
            } else if (message.trim().substring(0,6).equals("[WARN]")) {
                MIA.log.write(message.trim().substring(7).trim(), LogRenderer.Level.WARNING);
            } else if (message.trim().substring(0,7).equals("[DEBUG]")) {
                MIA.log.write(message.trim().substring(8).trim(), LogRenderer.Level.DEBUG);
            } else if (message.trim().substring(0,8).equals("[MEMORY]")) {
                MIA.log.write(message.trim().substring(9).trim(), LogRenderer.Level.MEMORY);
            } else {
                MIA.log.write(message, LogRenderer.Level.MESSAGE);
            }

            stringBuilder = new StringBuilder();
            
        }
    }
}
