package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUIAnalysisHandler;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.UndoRedoStore;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Process.Logging.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class CustomMenuBar extends JMenuBar implements ActionListener {
    private static MenuCheckbox helpNotesCheckbox = new MenuCheckbox(MenuCheckbox.TOGGLE_HELP_NOTES);
    private static MenuItem undo = new MenuItem(MenuItem.UNDO);
    private static MenuItem redo = new MenuItem(MenuItem.REDO);


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

        // Creating the analysis menu
        menu = new JMenu("Analysis");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        add(menu);
        menu.add(new MenuItem(MenuItem.RUN_ANALYSIS));
        menu.add(new MenuItem(MenuItem.STOP_ANALYSIS));
        menu.add(new MenuItem(MenuItem.RESET_ANALYSIS));
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
        menu.add(new MenuItem(MenuItem.SHOW_GLOBAL_VARIABLES));
        helpNotesCheckbox.setSelected(GUI.showHelpNotes());
        menu.add(helpNotesCheckbox);

        // Creating the help menu
        menu = new JMenu("Help");
        add(menu);
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        menu.add(new MenuItem(MenuItem.SHOW_ABOUT));
        menu.add(new MenuItem(MenuItem.SHOW_GETTING_STARTED));

        JMenu logMenu = new JMenu("Logging");
        menu.add(logMenu);
        logMenu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        Log.Level level = Log.Level.MESSAGE;
        MenuLogCheckbox menuLogCheckbox = new MenuLogCheckbox(level,MIA.log.isWriteEnabled(level));
        logMenu.add(menuLogCheckbox);

        level = Log.Level.WARNING;
        menuLogCheckbox = new MenuLogCheckbox(level,MIA.log.isWriteEnabled(level));
        logMenu.add(menuLogCheckbox);

        level = Log.Level.DEBUG;
        menuLogCheckbox = new MenuLogCheckbox(level,MIA.log.isWriteEnabled(level));
        logMenu.add(menuLogCheckbox);

        level = Log.Level.MEMORY;
        menuLogCheckbox = new MenuLogCheckbox(level,MIA.log.isWriteEnabled(level));
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

    public void setHelpNotesSelected(Boolean showHelpNotes) {
        helpNotesCheckbox.setSelected(showHelpNotes);

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
