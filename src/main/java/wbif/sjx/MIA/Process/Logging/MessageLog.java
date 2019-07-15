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
            MIA.log.write(stringBuilder.toString(), LogRenderer.Level.MESSAGE);
            stringBuilder = new StringBuilder();
        }
    }
}
