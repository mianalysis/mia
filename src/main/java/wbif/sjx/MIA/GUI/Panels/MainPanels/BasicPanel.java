package wbif.sjx.MIA.GUI.Panels.MainPanels;

import ij.Prefs;
import wbif.sjx.MIA.GUI.ControlObjects.AnalysisControlButton;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.Panels.*;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

public class BasicPanel extends MainPanel {
    private static int frameHeight = GUI.getFrameHeight();
    private static int minimumFrameHeight = GUI.getMinimumFrameHeight();

    private static final StatusPanel statusPanel = new StatusPanel();
    private static final BasicControlPanel controlPanel = new BasicControlPanel();
    private static final ProgressBarPanel progressBarPanel = new ProgressBarPanel();
    private final HelpNotesPanel helpNotesPanel = new HelpNotesPanel();
    private static final HelpPanel helpPanel = new HelpPanel();
    private static final NotesPanel notesPanel = new NotesPanel();
    private final FileListPanel fileListPanel = new FileListPanel(GUI.getAnalysisRunner().getWorkspaces());
    private final JSplitPane splitPane1;
    private final JSplitPane splitPane2;

    private static boolean showHelp = Prefs.get("MIA.showBasicHelp",false);
    private static boolean showNotes = Prefs.get("MIA.showBasicNotes",false);
    private boolean showFileList = Prefs.get("MIA.showBasicFileList",false);
    private static Module lastHelpNotesModule = null;


    public BasicPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        // Initialising the control panel
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(initialiseBasicControlPanel(), c);

        // Initialising the parameters panel
        updateFileList();
        updateHelpNotes();

        splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,fileListPanel,helpNotesPanel);
        splitPane1.setPreferredSize(new Dimension(1,1));
        splitPane1.setBorder(null);
        splitPane1.setDividerSize(5);
        splitPane1.setDividerLocation(0.5);
        BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI) splitPane1.getUI();
        splitPaneUI.getDivider().setBorder(new EmptyBorder(0,0,0,0));

        splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,controlPanel,splitPane1);
        splitPane2.setPreferredSize(new Dimension(1,1));
        splitPane2.setBorder(null);
        splitPane2.setDividerSize(5);
        splitPane2.setDividerLocation(0.5);
        splitPaneUI = (BasicSplitPaneUI) splitPane2.getUI();
        splitPaneUI.getDivider().setBorder(new EmptyBorder(0,0,0,0));

        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 5, 5, 5);
        add(splitPane2,c);

        // Initialising the status panel
        c.gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        c.insets = new Insets(0, 5, 5, 5);
        add(statusPanel,c);

        // Initialising the progress bar
        c.gridy++;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(progressBarPanel,c);

        revalidate();
        repaint();

        updateSeparators();
        helpNotesPanel.updateSeparator();

    }

    private static JPanel initialiseBasicControlPanel() {
        int bigButtonSize = GUI.getBigButtonSize();
        int frameHeight = GUI.getFrameHeight();
        int frameWidth = GUI.getFrameHeight();

        JPanel basicControlPanel = new JPanel();

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

        GUI.updateTestFile(false);
        updateModules();
        updateParameters();

        if (showHelp || showNotes) updateHelpNotes();
        if (showFileList) updateFileList();

        revalidate();
        repaint();

    }

    void updateSeparators() {
        splitPane1.getLeftComponent().setVisible(showFileList);
        splitPane1.getRightComponent().setVisible(showHelp || showNotes);

        // If both helpnotes and filelist are visible, show the separator for splitPane1
        if ((showHelp || showNotes) && showFileList) {
            splitPane1.setDividerSize(5);
            splitPane1.setDividerLocation(0.5);
        } else {
            splitPane1.setDividerSize(0);
        }

        // If either the helpnotes or filelist is visible, show the separator for splitPane2
        if (showHelp || showNotes || showFileList) {
            splitPane2.setDividerSize(5);
            splitPane2.getRightComponent().setVisible(true);
            splitPane2.setDividerLocation(0.5);
        } else {
            splitPane2.setDividerSize(0);
            splitPane2.getRightComponent().setVisible(false);
        }
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
        helpNotesPanel.showHelp(showHelp);
        helpNotesPanel.showNotes(showNotes);
        helpNotesPanel.setVisible(showHelp || showNotes);
        helpNotesPanel.updatePanel(GUI.getFirstSelectedModule(),lastHelpNotesModule);
    }

    @Override
    public void updateFileList() {
        fileListPanel.setVisible(showFileList);
        fileListPanel.updatePanel();
    }

    @Override
    public int getPreferredWidth() {
        int currentWidth = BasicControlPanel.getPreferredWidth() + 5;

        if (showHelp || showNotes) currentWidth = currentWidth + HelpNotesPanel.getPreferredWidth() + 5;
        if (showFileList) currentWidth = currentWidth + FileListPanel.getPreferredWidth() + 5;

        return currentWidth;

    }

    @Override
    public int getMinimumWidth() {
        int currentWidth = BasicControlPanel.getMinimumWidth() + 5;

        if (showHelp || showNotes) currentWidth = currentWidth + HelpNotesPanel.getPreferredWidth() + 5;
        if (showFileList) currentWidth = currentWidth + FileListPanel.getPreferredWidth() + 5;

        return currentWidth;

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
        fileListPanel.updatePanel();
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

        updateSeparators();

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

        updateSeparators();

    }

    @Override
    public boolean showFileList() {
        return showFileList;
    }

    @Override
    public void setShowFileList(boolean showFileList) {
        this.showFileList = showFileList;
        Prefs.set("MIA.showBasicFileList",showFileList);

        fileListPanel.setVisible(showFileList);
        GUI.updatePanel();

        updateSeparators();
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
