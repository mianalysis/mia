package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUIAnalysisHandler;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.UndoRedoStore;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Process.Logging.LogRenderer;
import wbif.sjx.MIA.Process.Logging.LogRenderer.Level;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class CustomMenuBar extends JMenuBar implements ActionListener {
    private static MenuItem undo = new MenuItem(MenuItem.UNDO);
    private static MenuItem redo = new MenuItem(MenuItem.REDO);
    private static MenuCheckbox helpCheckbox = new MenuCheckbox(MenuCheckbox.TOGGLE_HELP);
    private static MenuCheckbox notesCheckbox = new MenuCheckbox(MenuCheckbox.TOGGLE_NOTES);
    private static MenuCheckbox fileListCheckbox = new MenuCheckbox(MenuCheckbox.TOGGLE_FILE_LIST);


    public CustomMenuBar() {
        // Creating the file menu
        JMenu menu = new JMenu("File");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        add(menu);
        menu.add(new MenuItem(MenuItem.NEW_PIPELINE));
        menu.add(new MenuItem(MenuItem.LOAD_PIPELINE));
        menu.add(new MenuItem(MenuItem.SAVE_PIPELINE));
        menu.add(new MenuItem(MenuItem.SAVE_PIPELINE_AS));

        // Creating the edit menu
        menu = new JMenu("Edit");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        add(menu);
        menu.add(undo);
        menu.add(redo);
        menu.addSeparator();
        menu.add(new MenuItem(MenuItem.COPY));
        menu.add(new MenuItem(MenuItem.PASTE));

        // Creating the analysis menu
        menu = new JMenu("Analysis");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        add(menu);
        menu.add(new MenuItem(MenuItem.RUN_ANALYSIS));
        menu.add(new MenuItem(MenuItem.STOP_ANALYSIS));
        menu.add(new MenuItem(MenuItem.RESET_ANALYSIS));
        menu.addSeparator();
        menu.add(new MenuItem(MenuItem.ENABLE_ALL));
        menu.add(new MenuItem(MenuItem.DISABLE_ALL));
        menu.add(new MenuItem(MenuItem.OUTPUT_ALL));
        menu.add(new MenuItem(MenuItem.SILENCE_ALL));

        // Creating the new menu
        menu = new JMenu("View");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        add(menu);
        if (MIA.isDebug()) {
            menu.add(new MenuItem(MenuItem.BASIC_VIEW));
        } else {
            menu.add(new MenuItem(MenuItem.EDITING_VIEW));
        }
        helpCheckbox.setSelected(GUI.showHelp());
        menu.add(helpCheckbox);
        notesCheckbox.setSelected(GUI.showNotes());
        menu.add(notesCheckbox);
        fileListCheckbox.setSelected(GUI.showFileList());
        menu.add(fileListCheckbox);

        // Creating the help menu
        menu = new JMenu("Help");
        add(menu);
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        menu.add(new MenuItem(MenuItem.SHOW_ABOUT));
        menu.add(new MenuItem(MenuItem.SHOW_GETTING_STARTED));

        JMenu logMenu = new JMenu("Logging");
        menu.add(logMenu);
        logMenu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        LogRenderer renderer = MIA.getMainRenderer();

        Level level = Level.DEBUG;
        MenuLogCheckbox menuLogCheckbox = new MenuLogCheckbox(level,renderer.isWriteEnabled(level));
        logMenu.add(menuLogCheckbox);

        level = Level.MEMORY;
        menuLogCheckbox = new MenuLogCheckbox(level,renderer.isWriteEnabled(level));
        logMenu.add(menuLogCheckbox);

        level = Level.MESSAGE;
        menuLogCheckbox = new MenuLogCheckbox(level,renderer.isWriteEnabled(level));
        logMenu.add(menuLogCheckbox);

        level = Level.STATUS;
        menuLogCheckbox = new MenuLogCheckbox(level,renderer.isWriteEnabled(level));
        logMenu.add(menuLogCheckbox);

        level = Level.WARNING;
        menuLogCheckbox = new MenuLogCheckbox(level,renderer.isWriteEnabled(level));
        logMenu.add(menuLogCheckbox);

        add(Box.createHorizontalGlue());
        menu = new JMenu("");
        add(menu);
        menu.add(new MenuItem(MenuItem.SHOW_PONY));

        KeyStroke saveAnalysis = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK);
        registerKeyboardAction(this,"Save",saveAnalysis,JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke newAnalysis = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK);
        registerKeyboardAction(this,"New",newAnalysis,JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke undoAction = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK);
        registerKeyboardAction(this,"Undo",undoAction,JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke redoAction = KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK);
        registerKeyboardAction(this,"Redo",redoAction,JComponent.WHEN_IN_FOCUSED_WINDOW);

    }

    public void setHelpSelected(Boolean showHelp) {
        helpCheckbox.setSelected(showHelp);

    }

    public void setFileListSelected(boolean showFileList) {
        fileListCheckbox.setSelected(showFileList);
    }

    public void setNotesSelected(Boolean showNotes) {
        notesCheckbox.setSelected(showNotes);

    }

    public void setUndoRedoStatus(UndoRedoStore undoRedoStatus) {
        undo.setEnabled(undoRedoStatus.getUndoSize() != 0);
        redo.setEnabled(undoRedoStatus.getRedoSize() != 0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "New":
                GUIAnalysisHandler.newAnalysis();
                break;
            case "Save":
                GUIAnalysisHandler.saveAnalysis();
                break;
            case "Undo":
                GUI.undo();
                break;
            case "Redo":
                GUI.redo();
                break;
        }
    }
}
