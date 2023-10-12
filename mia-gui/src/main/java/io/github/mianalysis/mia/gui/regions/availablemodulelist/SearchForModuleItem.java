package io.github.mianalysis.mia.gui.regions.availablemodulelist;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.object.system.SwingPreferences;

public class SearchForModuleItem extends JMenuItem implements ActionListener {
    public SearchForModuleItem() {
        setText("Search for module");
        
        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();
        if (isDark)
            setIcon(new ImageIcon(SearchForModuleItem.class.getResource("/icons/search_darkgreyDM_12px.png"), ""));
        else
            setIcon(new ImageIcon(SearchForModuleItem.class.getResource("/icons/search_black_12px.png"), ""));
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.setShowSearch(true);
        GUI.updatePanel();

    }
}
