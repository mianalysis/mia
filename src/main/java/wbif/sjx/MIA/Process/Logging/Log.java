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
    public void clearLog();

    default public void writeError(String message) {
        write(message,Level.ERROR);
    }

    default public void writeWarning(String message) {
        write(message,Level.WARNING);
    }

    default public void writeMessage(String message) {
        write(message,Level.MESSAGE);
    }

    default public void writeDebug(String message) {
        write(message,Level.DEBUG);
    }

    default public void writeMemory(String message) {
        write(message,Level.MEMORY);
    }

    default public void writeError(Object message) {
        if (message == null) write("null",Level.ERROR);
        else write(message.toString(),Level.ERROR);
    }

    default public void writeWarning(Object message) {
        if (message == null) write("null",Level.WARNING);
        else write(message.toString(),Level.WARNING);
    }

    default public void writeMessage(Object message) {
        if (message == null) write("null",Level.MESSAGE);
        else write(message.toString(),Level.MESSAGE);
    }

    default public void writeDebug(Object message) {
        if (message == null) write("null",Level.DEBUG);
        else write(message.toString(),Level.DEBUG);
    }

    default public void writeMemory(Object message) {
        if (message == null) write("null",Level.MEMORY);
        else write(message.toString(),Level.MEMORY);
    }
}
