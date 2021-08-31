package wbif.sjx.MIA.GUI.ControlObjects.SearchPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import wbif.sjx.MIA.Process.ModuleSearcher.SearchMatch;

public class ResultsPanel extends JPanel {
    public ResultsPanel() {
        setLayout(new GridBagLayout());

    }

    public void updateResults(ArrayList<SearchMatch> matches) {
        removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        for (SearchMatch match : matches) {
            JTextField moduleName = new JTextField(match.getModuleName());
            moduleName.setEditable(false);
            moduleName.setBackground(Color.MAGENTA);
            moduleName.setMinimumSize(new Dimension(1,26));
            c.gridx = 0;
            c.gridy++;
            c.weightx = 1;
            c.anchor = GridBagConstraints.WEST;
            add(moduleName, c);

            JButton addModuleButton = new JButton("+");
            addModuleButton.setMargin(new Insets(0, 0, 1, 1));
            addModuleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
            addModuleButton.setPreferredSize(new Dimension(26, 26));
            addModuleButton.setMinimumSize(new Dimension(26, 26));
            addModuleButton.setMaximumSize(new Dimension(26, 26));
            c.gridx++;
            c.weightx = 0;
            c.anchor = GridBagConstraints.EAST;
            add(addModuleButton, c);

        }
        
        revalidate();
        repaint();

    }
}
