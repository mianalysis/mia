package wbif.sjx.MIA.Process.Logging;

import org.scijava.ui.UIService;
import org.scijava.ui.console.ConsolePane;
import org.scijava.ui.swing.console.ConsolePanel;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.HashMap;

/**
 * Created by Stephen Cross on 14/06/2019.
 */
public interface Log {
    public enum Level {
        MESSAGE, WARNING, ERROR, DEBUG, MEMORY;
    }

    public void write(String message, Level level);
    public boolean isWriteEnabled(Level level);
    public void setWriteEnabled(Level level, boolean writeEnabled);
    public String getLogText();
}
