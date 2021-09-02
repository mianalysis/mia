package io.github.mianalysis.mia.gui.regions.abstrac;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPopupMenu;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.GUIAnalysisHandler;

/**
 * Created by Stephen on 28/07/2017.
 */
public class ModuleControlButton extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -8671884116762686858L;
    public static final String ADD_MODULE = "+";
    public static final String REMOVE_MODULE = "-";
    public static final String MOVE_MODULE_UP = "▲";
    public static final String MOVE_MODULE_DOWN = "▼";

    private JPopupMenu moduleListMenu;

    public ModuleControlButton(String command, int buttonSize, JPopupMenu moduleListMenu) {
        this.moduleListMenu = moduleListMenu;

        setText(command);
        addActionListener(this);
        setMargin(new Insets(0,0,0,0));
        setFocusPainted(false);
        setMargin(new Insets(0,0,0,0));
        setPreferredSize(new Dimension(buttonSize, buttonSize));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (getText()) {
            case ADD_MODULE:
                addModule();
                break;

            case REMOVE_MODULE:
                GUIAnalysisHandler.removeModules();
                break;

            case MOVE_MODULE_UP:
                GUIAnalysisHandler.moveModuleUp();
                break;

            case MOVE_MODULE_DOWN:
                GUIAnalysisHandler.moveModuleDown();
                break;

        }
    }

    public void addModule() {
        GUI.addUndo();

        // Populating the list containing all available modules
        moduleListMenu.show(GUI.getFrame(), 0, 0);
        moduleListMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
        moduleListMenu.setVisible(true);

        GUI.updateModules();
        GUI.updateModuleStates(true);

    }
}
