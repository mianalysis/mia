package wbif.sjx.ModularImageAnalysis.Object;

import java.io.IOException;
import java.io.OutputStream;

public class ErrorLog extends OutputStream {
    private final static OutputStream originalStream = System.err;
    private StringBuilder stringBuilder = new StringBuilder();

    public String getStreamContents() {
        return stringBuilder.toString();
    }

    @Override
    public void write(int b) throws IOException {
        originalStream.write((char) b);
        stringBuilder.append((char) b);
    }
}
