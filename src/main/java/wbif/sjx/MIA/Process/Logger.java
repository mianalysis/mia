package wbif.sjx.MIA.Process;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
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
public class Logger {
    private UIService uiService = null;
    private JTextPane consoleTextPane = null;
    private LoggingLevel activeLoggingLevel = LoggingLevel.MESSAGE;

    public enum LoggingLevel {
        MESSAGE, WARNING, ERROR, DEBUG;
    }

    private HashMap<LoggingLevel,Style> logStyles = new HashMap<>();


    // CONSTRUCTOR

    public Logger(UIService uiService) {
        this.uiService = uiService;

        ConsolePane<?> consolePane = uiService.getDefaultUI().getConsolePane();
        JPanel panel = (JPanel) consolePane.getComponent();
        JTabbedPane tabbedPane = (JTabbedPane) panel.getComponent(0);
        ConsolePanel consolePanel = (ConsolePanel) tabbedPane.getComponent(0);
        consoleTextPane = consolePanel.getTextPane();

        Style messageStyle = consoleTextPane.addStyle("Message style", null);
        StyleConstants.setForeground(messageStyle, Color.BLACK);
        logStyles.put(LoggingLevel.MESSAGE,messageStyle);

        Style warningStyle = consoleTextPane.addStyle("Warning style", null);
        StyleConstants.setForeground(warningStyle, new Color(251,120,0));
        logStyles.put(LoggingLevel.WARNING,warningStyle);

        Style errorStyle = consoleTextPane.addStyle("Error style", null);
        StyleConstants.setForeground(errorStyle, Color.RED);
        logStyles.put(LoggingLevel.ERROR,errorStyle);

        Style debugStyle = consoleTextPane.addStyle("Debug style", null);
        StyleConstants.setForeground(debugStyle, new Color(57,172,229));
        logStyles.put(LoggingLevel.DEBUG,debugStyle);

    }


    // PUBLIC METHODS

    public void log(String message, LoggingLevel level) {
        // Ensuring the console is visible
        uiService.getDefaultUI().getConsolePane().show();

        StyledDocument document = consoleTextPane.getStyledDocument();

        String prefix = "";
        switch (level) {
            case MESSAGE:
                prefix = "[MESSAGE] ";
                break;
            case WARNING:
                prefix = "[WARNING] ";
                break;
            case ERROR:
                prefix = "[ERROR] ";
                break;
            case DEBUG:
                prefix = "[DEBUG] ";
                break;
        }

        try {
            document.insertString(document.getLength(), prefix+message+"\n", logStyles.get(level));

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }


    // GETTERS AND SETTERS

    public UIService getService() {
        return uiService;
    }

    public JTextPane getConsoleTextPane() {
        return consoleTextPane;

    }
}
