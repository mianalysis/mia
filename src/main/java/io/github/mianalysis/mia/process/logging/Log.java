package io.github.mianalysis.mia.process.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;

import io.github.mianalysis.mia.process.logging.LogRenderer.Level;

/**
 * Created by Stephen Cross on 14/06/2019.
 */
public class Log {
    private HashSet<LogRenderer> renderers = new HashSet<>();

    public Log(HashSet<LogRenderer> renderers) {
        this.renderers = renderers;
    }

    public Log(LogRenderer renderer) {
        renderers.add(renderer);
    }

    public void write(String message, Level level) {
        for (LogRenderer renderer : renderers)
            renderer.write(message, level);

    }

    public void writeError(String message) {
        write(message, Level.ERROR);
    }

    public void writeWarning(String message) {
        write(message, Level.WARNING);
    }

    public void writeMessage(String message) {
        write(message, Level.MESSAGE);
    }

    public void writeDebug(String message) {
        write(message, Level.DEBUG);
    }

    public void writeMemory(String message) {
        write(message, Level.MEMORY);
    }

    public void writeStatus(String message) {
        write(message, Level.STATUS);
    }

    public void writeError(Exception e) {
        if (e == null) {
            write("null", Level.ERROR);
        } else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            write(sw.toString(), Level.ERROR);
        }
    }

    public void writeError(Object message) {
        if (message == null)
            write("null", Level.ERROR);
        else
            write(message.toString(), Level.ERROR);
    }

    public void writeWarning(Object message) {
        if (message == null)
            write("null", Level.WARNING);
        else
            write(message.toString(), Level.WARNING);
    }

    public void writeMessage(Object message) {
        if (message == null)
            write("null", Level.MESSAGE);
        else
            write(message.toString(), Level.MESSAGE);
    }

    public void writeDebug(Object message) {
        if (message == null)
            write("null", Level.DEBUG);
        else
            write(message.toString(), Level.DEBUG);
    }

    public void writeMemory(Object message) {
        if (message == null)
            write("null", Level.MEMORY);
        else
            write(message.toString(), Level.MEMORY);
    }

    public void writeStatus(Object message) {
        if (message == null)
            write("null", Level.STATUS);
        else
            write(message.toString(), Level.STATUS);
    }

    public HashSet<LogRenderer> getRenderers() {
        return renderers;
    }

    public void addRenderer(LogRenderer renderer) {
        renderers.add(renderer);
    }

    public void removeRenderer(LogRenderer renderer) {
        renderers.remove(renderer);
    }
}
