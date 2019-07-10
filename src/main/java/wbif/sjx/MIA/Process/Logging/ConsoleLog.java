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
public class ConsoleLog implements Log {
    private UIService uiService = null;
    private JTextPane consoleTextPane = null;
    private Level activeLevel = Level.MESSAGE;

    private HashMap<Level,Style> logStyles = new HashMap<>();
    private HashMap<Level,Boolean> writeToConsole = new HashMap<>();


    // CONSTRUCTOR

    public ConsoleLog(UIService uiService) {
        this.uiService = uiService;

        ConsolePane<?> consolePane = uiService.getDefaultUI().getConsolePane();
        JPanel panel = (JPanel) consolePane.getComponent();
        JTabbedPane tabbedPane = (JTabbedPane) panel.getComponent(0);
        ConsolePanel consolePanel = (ConsolePanel) tabbedPane.getComponent(0);
        consolePanel.setAutoscrolls(true);
        consoleTextPane = consolePanel.getTextPane();
        consoleTextPane.setAutoscrolls(true);

        Style messageStyle = consoleTextPane.addStyle("Message style", null);
        StyleConstants.setForeground(messageStyle, Color.BLACK);
        logStyles.put(Level.MESSAGE,messageStyle);
        writeToConsole.put(Level.MESSAGE,false);

        Style warningStyle = consoleTextPane.addStyle("Warning style", null);
        StyleConstants.setForeground(warningStyle, new Color(251,120,0));
        logStyles.put(Level.WARNING,warningStyle);
        writeToConsole.put(Level.WARNING,true);

        Style errorStyle = consoleTextPane.addStyle("Error style", null);
        StyleConstants.setForeground(errorStyle, Color.RED);
        logStyles.put(Level.ERROR,errorStyle);
        writeToConsole.put(Level.ERROR,true);

        Style debugStyle = consoleTextPane.addStyle("Debug style", null);
        StyleConstants.setForeground(debugStyle, new Color(57,172,229));
        logStyles.put(Level.DEBUG,debugStyle);
        writeToConsole.put(Level.DEBUG,false);

        Style memoryStyle = consoleTextPane.addStyle("Memory style", null);
        StyleConstants.setForeground(memoryStyle, new Color(57,142,27));
        logStyles.put(Level.MEMORY,memoryStyle);
        writeToConsole.put(Level.MEMORY,false);

    }


    // PUBLIC METHODS

    public void write(String message, Level level) {
        // If this level isn't currently being written, skip it
        if (!writeToConsole.get(level)) return;

        // Ensuring the console is visible
        uiService.getDefaultUI().getConsolePane().show();

        StyledDocument document = consoleTextPane.getStyledDocument();

        try {
            document.insertString(document.getLength(), "["+level.toString()+"] "+message+"\n", logStyles.get(level));

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

    public String getLogText() {
        return consoleTextPane.getText();
    }

    public boolean isWriteEnabled(Level level) {
        return writeToConsole.get(level);
    }

    public void setWriteEnabled(Level level, boolean writeEnabled) {
        writeToConsole.put(level,writeEnabled);
    }

    public void clearLog() {
        consoleTextPane.setText("");
    }
}
