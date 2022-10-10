package io.github.mianalysis.mia.process.logging;

import java.text.DecimalFormat;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.scijava.ui.UIService;

import ij.IJ;

/**
 * Created by Stephen Cross on 14/06/2019.
 */
public class ImageJGUIRenderer extends ConsoleRenderer {
    DecimalFormat df = new DecimalFormat("#.0");

    // CONSTRUCTOR

    public ImageJGUIRenderer(UIService uiService) {
        super(uiService);
    }

    @Override
    public void write(String message, Level level, int progress) {
        // If this level isn't currently being written, skip it
        if (!levelStatus.get(level))
            return;

        if (level == Level.STATUS) {
            IJ.showProgress(((double) progress)/100d);          
            IJ.showStatus(message);  
        } else {
            // Ensuring the console is visible
            uiService.getDefaultUI().getConsolePane().show();

            StyledDocument document = consoleTextPane.getStyledDocument();
            String formattedMessage = "[" + level.toString() + "] " + message + "\n";
            try {
                document.insertString(document.getLength(), formattedMessage, logStyles.get(level));
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            // Moving the panel to the bottom
            consoleTextPane.select(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
    }
}
