package io.github.mianalysis.MIA.GUI.Regions.HelpAndNotes;

import java.awt.Dimension;

import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import io.github.mianalysis.MIA.Module.Module;

public class HelpNotesPanel extends JSplitPane {
    /**
     *
     */
    private static final long serialVersionUID = 5990832671522949490L;
    private static final int minimumWidth = 200;
    private static final int preferredWidth = 300;

    private final NotesPanel notesPanel = new NotesPanel();
    private final HelpPanel helpPanel = new HelpPanel();

    public HelpNotesPanel() {
        super(JSplitPane.VERTICAL_SPLIT);
        setTopComponent(helpPanel);
        setBottomComponent(notesPanel);

        setMinimumSize(new Dimension(minimumWidth,1));
        setPreferredSize(new Dimension(preferredWidth,1));
        setBorder(null);
        setDividerLocation(0.5);
        setResizeWeight(0.5);

        BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI) getUI();
        splitPaneUI.getDivider().setBorder(new EmptyBorder(0,0,0,0));

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

    public static int getPreferredWidth() {
        return preferredWidth;
    }
}
