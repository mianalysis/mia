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
            MIA.log.write(stringBuilder.toString(), Log.Level.ERROR);
            stringBuilder = new StringBuilder();
        }
    }
}
