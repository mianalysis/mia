package io.github.mianalysis.mia.gui.regions.extrapanels.search;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.object.system.SwingPreferences;

public class SearchButton extends JButton implements ActionListener {
    private SearchPanel searchPanel;

    public SearchButton(SearchPanel searchPanel) {
        this.searchPanel = searchPanel;

        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();
        if (isDark)
            setIcon(new ImageIcon(SearchPanel.class.getResource("/icons/search_darkgreyDM_12px.png"), ""));
        else
            setIcon(new ImageIcon(SearchPanel.class.getResource("/icons/search_black_12px.png"), ""));
        setPreferredSize(new Dimension(26, 26));
        setMinimumSize(new Dimension(26, 26));
        setMaximumSize(new Dimension(26, 26));
        addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        searchPanel.doSearch();

    }
}
