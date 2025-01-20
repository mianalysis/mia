package io.github.mianalysis.mia.gui.regions.extrapanels.search;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.system.SwingPreferences;
import io.github.mianalysis.mia.process.ModuleSearcher.SearchMatch;

public class ResultsPanel extends JPanel {
    public ResultsPanel() {
        setLayout(new GridBagLayout());
        setOpaque(false);

    }

    public void updateResults(ArrayList<SearchMatch> matches) {
        removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;

        if (matches.size() == 0) {
            JTextPane noResultsMessage = new JTextPane();
                noResultsMessage.setContentType("text/html");
                noResultsMessage.setFont(GUI.getDefaultFont().deriveFont(14f));
                noResultsMessage.setText(
                    "<html><center>No results</center></html>");
                noResultsMessage.setEditable(false);
                noResultsMessage.setOpaque(false);                
                c.weightx = 1;
                c.weighty = 1;
                c.anchor = GridBagConstraints.CENTER;
                add(noResultsMessage, c);

        }

        for (SearchMatch match : matches) {
            Module module = match.getModule();

            boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();
            if (!module.isDeprecated() || isDark) {
                JLabel moduleName = new JLabel(module.getName());
                Font font = GUI.getDefaultFont().deriveFont(Font.BOLD, 14f);
                if (module.isDeprecated()) {
                    Map attributes = font.getAttributes();
                    attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                    font = new Font(attributes);
                }
                moduleName.setFont(font);
                moduleName.setMinimumSize(new Dimension(0, 26));
                moduleName.setPreferredSize(new Dimension(0, 26));
                moduleName.setToolTipText("<html><div style=\"width:500;\">" + module.getDescription() + "</div></html>");
                c.gridx = 0;
                c.gridy++;
                c.weightx = 1;
                c.gridwidth = 1;
                c.insets = new Insets(5, 5, 0, 0);
                c.anchor = GridBagConstraints.NORTHWEST;
                add(moduleName, c);

                AddModuleFromSearchButton addModuleButton = new AddModuleFromSearchButton(module);
                c.gridx++;
                c.weightx = 0;
                c.anchor = GridBagConstraints.NORTHEAST;
                add(addModuleButton, c);

                JTextArea moduleDescription = new JTextArea(module.getShortDescription());
                moduleDescription.setFont(GUI.getDefaultFont().deriveFont(14f));
                moduleDescription.setLineWrap(true);
                moduleDescription.setWrapStyleWord(true);
                moduleDescription.setEditable(false);
                moduleDescription.setOpaque(false);
                moduleDescription.setToolTipText("<html><div style=\"width:500;\">" + module.getDescription() + "</div></html>");
                c.gridx = 0;
                c.gridy++;
                c.weightx = 1;
                c.insets = new Insets(0, 5, 5, 0);
                c.anchor = GridBagConstraints.NORTHWEST;
                c.gridwidth = 2;
                add(moduleDescription, c);

            }
        }

        if (matches.size() != 0) {
            JSeparator separator = new JSeparator();
            separator.setOpaque(true);
            separator.setSize(new Dimension(0, 0));
            c.weighty = 1;
            c.gridy++;
            c.insets = new Insets(20, 0, 0, 0);
            c.fill = GridBagConstraints.VERTICAL;
            add(separator, c);
        }

        revalidate();
        repaint();

    }
}
