package wbif.sjx.ModularImageAnalysis.Object;

import ij.IJ;

import javax.sound.midi.SysexMessage;
import java.io.IOException;
import java.io.OutputStream;

public class ErrorLog extends OutputStream {
    private final static StringBuilder stringBuilder = new StringBuilder();

    public String getStreamContents() {
        return stringBuilder.toString();
    }

    @Override
    public void write(int b) throws IOException {
        stringBuilder.append((char) b);

    }
}
