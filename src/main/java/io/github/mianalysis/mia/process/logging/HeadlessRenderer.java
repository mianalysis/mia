package io.github.mianalysis.mia.process.logging;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.mianalysis.mia.object.Workspaces;

/**
 * Created by Stephen Cross on 14/06/2019.
 */
public class HeadlessRenderer extends LogRenderer {
    private static int progress = 0;
    private static boolean showProgress = false;
    DecimalFormat df = new DecimalFormat("#.0");

    // CONSTRUCTOR

    public HeadlessRenderer() {
        levelStatus.put(Level.MESSAGE, true);
        levelStatus.put(Level.WARNING, true);
        levelStatus.put(Level.ERROR, true);
        levelStatus.put(Level.DEBUG, false);
        levelStatus.put(Level.MEMORY, false);
        levelStatus.put(Level.STATUS, true);

    }

    // PUBLIC METHODS

    public void write(String message, Level level) {
        // If this level isn't currently being written, skip it
        if (!levelStatus.get(level))
            return;

        // "\033[2K" removes any content on this line (since Status returns the
        // carriage)
        // "\u001B[33m" sets the colour
        switch (level) {
            default:
            case WARNING:
                System.out.println("\033[2K\u001B[33m[" + level.toString() + "] " + message+"\u001B[37m");
                break;
            case MESSAGE:
                System.out.println("\033[2K\u001B[37m[" + level.toString() + "] " + message+"\u001B[37m");
                break;
            case MEMORY:
                System.out.println("\033[2K\u001B[32m[" + level.toString() + "] " + message+"\u001B[37m");
                break;
            case DEBUG:
                System.out.println("\033[2K\u001B[36m[" + level.toString() + "] " + message+"\u001B[37m");
                break;
            case STATUS:
                if (showProgress) {
                    Matcher matcher = Pattern.compile("\\[(.+)\\](.+)").matcher(message);
                    if (matcher.find())
                        message = matcher.group(1) + " |" + matcher.group(2);
                    System.out.print("\033[2K\u001B[37m" + getProgressString(progress) + message + "\u001B[37m\r");
                } else {
                    System.out.print("\033[2K\u001B[37m[" + level.toString() + "] " + message +"\u001B[37m\r");
                }
                break;
            case ERROR:
                System.err.println("\033[2K\u001B[31m[" + level.toString() + "] " + message+"\u001B[37m");
                break;
        }
    }

    public void write(String message, Level level, int progress) {
        String progressString = getProgressString(progress);
        write(progressString + message, level);
    }

    public void write(String message, Level level, Workspaces workspaces) {
        int progress = (int) Math.round(workspaces.getOverallProgress() * 100);
        write(message, level, progress);
    }

    public static boolean isShowProgress() {
        return showProgress;
    }

    public static void setShowProgress(boolean showProgress) {
        HeadlessRenderer.showProgress = showProgress;
    }

    public static double getProgress() {
        return progress;
    }

    public static void setProgress(int progress) {
        HeadlessRenderer.progress = progress;
    }

    public static void setProgress(Workspaces workspaces) {
        progress = (int) Math.round(workspaces.getOverallProgress() * 100);
    }

    protected String getProgressString(int progress) {
        int nBlocks = 25;
        double pcPerBlock = 100d / nBlocks;

        String startPad = "";
        if (progress < 10)
            startPad += "  ";
        else if (progress < 100)
            startPad += " ";

        int nFullBlocks = (int) Math.floor((double) progress / pcPerBlock);

        String progressString = "";        
        for (int i = 0; i < nFullBlocks; i++)
            progressString += "#";
            // progressString += "█";

        // The following will look prettier, but isn't compatible with some systems
        // double remainder = ((double) progress / pcPerBlock) - nFullBlocks;
        // if (remainder < 0.25)
        //     progressString += "";
        // else if (remainder < 0.5)
        //     progressString += "\u258E";
        // else if (remainder < 0.75)
        //     progressString += "\u258C";
        // else if (remainder < 1)
        //     progressString += "\u258A";

        while (progressString.length() < nBlocks)
            progressString += " ";

        return startPad + progress + "% | " + progressString + " | ";

    }
}
