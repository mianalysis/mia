package io.github.mianalysis.mia.process.logging;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.scijava.ui.UIService;
import org.scijava.ui.console.ConsolePane;
import org.scijava.ui.swing.console.ConsolePanel;

import io.github.mianalysis.mia.object.system.Colours;

/**
 * Created by Stephen Cross on 14/06/2019.
 */
public class ConsoleRenderer extends LogRenderer {
    protected UIService uiService = null;
    protected JTextPane consoleTextPane = null;

    protected HashMap<Level,Style> logStyles = new HashMap<>();
    


    // CONSTRUCTOR

    public ConsoleRenderer(UIService uiService) {
        this.uiService = uiService;

        ConsolePane<?> consolePane = uiService.getDefaultUI().getConsolePane();
        JPanel panel = (JPanel) consolePane.getComponent();
        JTabbedPane tabbedPane = (JTabbedPane) panel.getComponent(0);
        ConsolePanel consolePanel = (ConsolePanel) tabbedPane.getComponent(0);
        consolePanel.setAutoscrolls(true);
        consoleTextPane = consolePanel.getTextPane();
        consoleTextPane.setAutoscrolls(true);

        Style messageStyle = consoleTextPane.addStyle("Message style", null);
        StyleConstants.setForeground(messageStyle, new Color(44, 38, 37));
        logStyles.put(Level.MESSAGE,messageStyle);
        levelStatus.put(Level.MESSAGE,true);

        Style warningStyle = consoleTextPane.addStyle("Warning style", null);
        StyleConstants.setForeground(warningStyle, Colours.getOrange(false));
        logStyles.put(Level.WARNING,warningStyle);
        levelStatus.put(Level.WARNING,true);

        Style errorStyle = consoleTextPane.addStyle("Error style", null);
        StyleConstants.setForeground(errorStyle, Colours.getRed(false));
        logStyles.put(Level.ERROR,errorStyle);
        levelStatus.put(Level.ERROR,true);

        Style debugStyle = consoleTextPane.addStyle("Debug style", null);
        StyleConstants.setForeground(debugStyle, Colours.getBlue(false));
        logStyles.put(Level.DEBUG,debugStyle);
        levelStatus.put(Level.DEBUG,false);

        Style memoryStyle = consoleTextPane.addStyle("Memory style", null);
        StyleConstants.setForeground(memoryStyle, Colours.getGreen(false));
        logStyles.put(Level.MEMORY,memoryStyle);
        levelStatus.put(Level.MEMORY,false);

        Style statusStyle = consoleTextPane.addStyle("Status style", null);
        StyleConstants.setForeground(statusStyle, Colours.getDarkGrey(false));
        logStyles.put(Level.STATUS,statusStyle);
        levelStatus.put(Level.STATUS,false);

    }


    // PUBLIC METHODS

    public void write(String message, Level level) {
        write(message, level, -1);
    }

    public void write(String message, Level level, int progress) {
        // If this level isn't currently being written, skip it
        if (!levelStatus.get(level)) return;

        // Ensuring the console is visible
        uiService.getDefaultUI().getConsolePane().show();

        StyledDocument document = consoleTextPane.getStyledDocument();
        String formattedMessage = "["+level.toString()+"] "+message+"\n";
        try {
            document.insertString(document.getLength(),formattedMessage , logStyles.get(level));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // Moving the panel to the bottom
        consoleTextPane.select(Integer.MAX_VALUE,Integer.MAX_VALUE);

    }


    // GETTERS AND SETTERS

    public UIService getService() {
        return uiService;
    }

    public JTextPane getConsoleTextPane() {
        return consoleTextPane;

    }
}
