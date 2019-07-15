package wbif.sjx.MIA.GUI.Panels.MainPanels;

import ij.Prefs;
import wbif.sjx.MIA.GUI.ControlObjects.AnalysisControlButton;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.Panels.*;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class BasicPanel extends MainPanel {
    private static int frameWidth = GUI.getMinimumFrameWidth();
    private static int minimumFrameWidth = GUI.getMinimumFrameWidth();
    private static int frameHeight = GUI.getFrameHeight();
    private static int minimumFrameHeight = GUI.getMinimumFrameHeight();

    private static final StatusPanel statusPanel = new StatusPanel();
    private static final BasicControlPanel controlPanel = new BasicControlPanel();
    private static final ProgressBarPanel progressBarPanel = new ProgressBarPanel();
    private static final JPanel helpNotesPanel = new JPanel();
    private static final HelpPanel helpPanel = new HelpPanel();
    private static final NotesPanel notesPanel = new NotesPanel();

    private static boolean showHelp = Prefs.get("MIA.showBasicHelp",false);
    private static boolean showNotes = Prefs.get("MIA.showBasicNotes",false);
    private static Module lastHelpNotesModule = null;


    public BasicPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 0, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Initialising the control panel
        add(initialiseBasicControlPanel(), c);

        // Initialising the parameters panel
        c.gridy++;
        c.weighty = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.BOTH;
        add(controlPanel, c);

        // Initialising the help and notes panels
        initialiseBasicHelpNotesPanels();
        c.weightx = 0;
        c.gridx++;
        c.insets = new Insets(5, 0, 0, 5);
        add(helpNotesPanel,c);

        // Initialising the status panel
        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 2;
        c.insets = new Insets(5, 5, 0, 5);
        add(statusPanel,c);

        // Initialising the progress bar
        c.gridy++;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5,5,5,5);
        add(progressBarPanel,c);

        revalidate();
        repaint();

    }


    private static JPanel initialiseBasicControlPanel() {
        int bigButtonSize = GUI.getBigButtonSize();
        int frameHeight = GUI.getFrameHeight();
        int frameWidth = GUI.getFrameHeight();

        JPanel basicControlPanel = new JPanel();

        basicControlPanel.setPreferredSize(new Dimension(frameWidth-30, bigButtonSize + 15));
        basicControlPanel.setMinimumSize(new Dimension(frameWidth-30, bigButtonSize + 15));
        basicControlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        basicControlPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        // Load analysis protocol button
        AnalysisControlButton loadAnalysisButton
                = new AnalysisControlButton(AnalysisControlButton.LOAD_ANALYSIS,bigButtonSize);
        c.gridx++;
        c.anchor = GridBagConstraints.PAGE_END;
        basicControlPanel.add(loadAnalysisButton, c);

        // Save analysis protocol button
        AnalysisControlButton saveAnalysisButton
                = new AnalysisControlButton(AnalysisControlButton.SAVE_ANALYSIS,bigButtonSize);
        c.gridx++;
        basicControlPanel.add(saveAnalysisButton, c);

        // Start analysis button
        AnalysisControlButton startAnalysisButton
                = new AnalysisControlButton(AnalysisControlButton.START_ANALYSIS,bigButtonSize);
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        basicControlPanel.add(startAnalysisButton, c);

        // Stop analysis button
        AnalysisControlButton stopAnalysisButton
                = new AnalysisControlButton(AnalysisControlButton.STOP_ANALYSIS,bigButtonSize);
        c.gridx++;
        c.weightx = 0;
        basicControlPanel.add(stopAnalysisButton, c);

        basicControlPanel.validate();
        basicControlPanel.repaint();

        return basicControlPanel;

    }

    private static void initialiseBasicHelpNotesPanels() {
        // Adding panels to combined JPanel
        helpNotesPanel.setLayout(new GridBagLayout());
        GridBagConstraints cc = new GridBagConstraints();

        cc.fill = GridBagConstraints.BOTH;
        cc.gridx = 0;
        cc.gridy = 0;
        cc.weightx = 1;
        cc.weighty = 2;
        cc.insets = new Insets(0,0,5,0);
        helpNotesPanel.add(helpPanel,cc);
        helpPanel.setVisible(showHelp);

        cc.gridy++;
        cc.weighty = 1;
        cc.insets = new Insets(0,0,0,0);
        helpNotesPanel.add(notesPanel,cc);
        notesPanel.setVisible(showNotes);

    }

    @Override
    public void updatePanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0,5,0,0);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;

        statusPanel.add(GUI.getTextField(),c);
        helpNotesPanel.setVisible(showHelp || showNotes);

        GUI.updateTestFile();
        updateModules();
        updateParameters();

        if (showHelp || showNotes) updateHelpNotes();

        revalidate();
        repaint();

    }

    @Override
    public void updateModules() {
        controlPanel.updatePanel();
    }

    @Override
    public void updateModuleStates() {
        controlPanel.updatePanel();
    }

    @Override
    public void updateParameters() {
        controlPanel.updatePanel();
    }

    @Override
    public void updateHelpNotes() {
        helpPanel.setVisible(showHelp);
        notesPanel.setVisible(showNotes);
        helpNotesPanel.setVisible(showHelp || showNotes);

        Module activeModule  = GUI.getFirstSelectedModule();

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

    @Override
    public int getPreferredWidth() {
        if (showHelp || showNotes) {
            return frameWidth + 315;
        } else {
            return frameWidth;
        }
    }

    @Override
    public int getMinimumWidth() {
        if (showHelp || showNotes) {
            return minimumFrameWidth + 315;
        } else {
            return minimumFrameWidth;
        }
    }

    @Override
    public int getPreferredHeight() {
        return frameHeight;
    }

    @Override
    public int getMinimumHeight() {
        return minimumFrameHeight;
    }

    @Override
    public int getProgress() {
        return progressBarPanel.getValue();
    }

    @Override
    public void setProgress(int progress) {
        progressBarPanel.setValue(progress);
    }

    @Override
    public boolean showHelp() {
        return showHelp;
    }

    @Override
    public void setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
        Prefs.set("MIA.showBasicHelp",showHelp);

        helpNotesPanel.setVisible(showHelp);
        GUI.updatePanel();

    }

    @Override
    public boolean showNotes() {
        return showNotes;
    }

    @Override
    public void setShowNotes(boolean showNotes) {
        this.showNotes = showNotes;
        Prefs.set("MIA.showBasicNotes",showNotes);

        helpNotesPanel.setVisible(showNotes);
        GUI.updatePanel();

    }

    @Override
    public Module getLastHelpNotesModule() {
        return lastHelpNotesModule;
    }

    @Override
    public void setLastHelpNotesModule(Module module) {
        lastHelpNotesModule = module;
    }
}
