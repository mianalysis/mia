package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JToggleButton;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.ReferenceEditingMenu;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GUISeparator;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.SwingPreferences;

/**
 * Created by Stephen on 20/05/2017.
 */
public class ModuleButton extends JToggleButton implements ActionListener, MouseListener {
    /**
     *
     */
    private static final long serialVersionUID = -7386345086449867288L;
    private Module module;
    private Color defaultColour;

    // CONSTRUCTOR

    public ModuleButton(Module module) {
        this.module = module;
        defaultColour = getForeground();
        setFocusPainted(false);
        setSelected(false);
        addActionListener(this);
        addMouseListener(this);
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setText(module.getNickname());
        setMinimumSize(new Dimension(1, 30));
        setPreferredSize(new Dimension(1, 30));
        updateState();
    }

    // PUBLIC METHODS

    public void updateState() {
        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        setText(module.getNickname()+"_"+module.getModuleID());

        if (module.getClass() == GUISeparator.class)
            setForeground(Colours.getDarkBlue(isDark));
        else if (module.isEnabled() && module.isReachable() && module.isRunnable())
            setForeground(defaultColour);
        else if (module.isEnabled() & !module.isReachable())
            setForeground(Colours.getOrange(isDark));
        else if (module.isEnabled() & !module.isRunnable())
            setForeground(Colours.getRed(isDark));
        else
            setForeground(Color.GRAY);

    }

    // GETTERS

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.setSelectedModules(new Module[] { module });
        GUI.updateModules(false, null);
        GUI.updateParameters(false, null);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON3:
                ReferenceEditingMenu refEditingMenu = new ReferenceEditingMenu(module);
                refEditingMenu.show(GUI.getFrame(), 0, 0);
                refEditingMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
                refEditingMenu.setVisible(true);

                break;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }
}
