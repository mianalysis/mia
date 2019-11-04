package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import java.awt.*;

public class HelpNotesPanel extends JSplitPane {
    private static final int minimumWidth = 200;

    private final NotesPanel notesPanel = new NotesPanel();
    private final HelpPanel helpPanel = new HelpPanel();

    public HelpNotesPanel() {
        super(JSplitPane.VERTICAL_SPLIT);
        setTopComponent(helpPanel);
        setBottomComponent(notesPanel);

        setMinimumSize(new Dimension(minimumWidth, 1));
        setBorder(null);
        setDividerLocation(0.5);
        setResizeWeight(0.5);
        updateSeparator();

    }

    public void showHelp(boolean showHelp) {
        getTopComponent().setVisible(showHelp);
        updateSeparator();
    }

    public void showNotes(boolean showNotes) {
        getBottomComponent().setVisible(showNotes);
        updateSeparator();
    }

    public void updatePanel(Module activeModule, Module lastHelpNotesModule) {
        // If null, show a special message
        if (activeModule == null) {
            helpPanel.showUsageMessage();
            notesPanel.showUsageMessage();
            return;
        }

        // Only update the help and notes if the module has changed
        if (activeModule != lastHelpNotesModule) {
            lastHelpNotesModule = activeModule;
            helpPanel.updatePanel();
            notesPanel.updatePanel();
        }
    }

    public void updateSeparator() {
        setVisible(getBottomComponent().isVisible() || getTopComponent().isVisible());
        setDividerLocation(0.5);

        // If only one is visible, hide the separator
        if (!getTopComponent().isVisible() || !getBottomComponent().isVisible()) {
            setDividerSize(0);
        } else {
            setDividerSize(5);
        }
    }

    public static int getMinimumWidth() {
        return minimumWidth;
    }
}
