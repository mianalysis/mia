package io.github.mianalysis.MIA.GUI.Regions.AvailableModuleList;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import io.github.mianalysis.MIA.GUI.GUI;

public class SearchForModuleItem extends JMenuItem implements ActionListener {
    public SearchForModuleItem() {
        setText("Search for module");
        setIcon(new ImageIcon(SearchForModuleItem.class.getResource("/Icons/search_black_12px.png"), ""));
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.setShowSearch(true);
        GUI.updatePanel();
        
    }
}
