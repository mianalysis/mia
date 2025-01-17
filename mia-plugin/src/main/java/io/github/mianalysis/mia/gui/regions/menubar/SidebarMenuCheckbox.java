package io.github.mianalysis.mia.gui.regions.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;

import ij.Prefs;
import io.github.mianalysis.mia.gui.GUI;

public class SidebarMenuCheckbox extends JCheckBoxMenuItem implements ActionListener {
    public SidebarMenuCheckbox() {
        setFont(GUI.getDefaultFont().deriveFont(14f));
        setText("Show sidebar");
        addActionListener(this);
        setSelected(GUI.showSidebar());
        Prefs.savePreferences();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.setShowSidebar(isSelected());
    }
}
