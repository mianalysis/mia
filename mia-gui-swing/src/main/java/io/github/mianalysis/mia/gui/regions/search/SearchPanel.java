package io.github.mianalysis.mia.gui.regions.search;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.ClosePanelButton;
import io.github.mianalysis.mia.process.ModuleSearcher;
import io.github.mianalysis.mia.process.ModuleSearcher.SearchMatch;

public class SearchPanel extends JPanel {
    private final static int minimumWidth = 300;

    private final ModuleSearcher searcher = new ModuleSearcher(GUI.getAvailableModules());

    private JTextField queryEntry;
    private SearchIncludeCheckbox moduleDescriptionCheckBox;
    private SearchIncludeCheckbox parameterDescriptionCheckBox;
    private ResultsPanel resultsPanel;

    public SearchPanel() {
        // Initialising the panel
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setLayout(new GridBagLayout());

        updatePanel();

    }

    public void updatePanel() {
        removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.insets = new Insets(5, 5, 0, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Adding title to help window
        JLabel helpLabel = new JLabel();
        helpLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        helpLabel.setText("Module search");
        add(helpLabel, c);

        // Adding close button
        ClosePanelButton closeButton = new ClosePanelButton(this);
        c.anchor = GridBagConstraints.EAST;
        c.weightx = 0;
        c.gridx++;
        add(closeButton, c);

        // Adding separator
        JSeparator separator = new JSeparator();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy++;
        add(separator, c);

        GridBagConstraints c2 = new GridBagConstraints();
        JPanel sPanel = new JPanel(new GridBagLayout());

        queryEntry = new JTextField();
        queryEntry.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        queryEntry.setPreferredSize(new Dimension(0, 26));
        queryEntry.setMinimumSize(new Dimension(0, 26));
        queryEntry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        queryEntry.addKeyListener(new EnterKeyListener());
        c2.gridx = 0;
        c2.gridy = 0;
        c2.weightx = 1;
        c2.insets = new Insets(5, 0, 0, 0);
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.anchor = GridBagConstraints.NORTH;
        sPanel.add(queryEntry, c2);

        SearchButton searchButton = new SearchButton(this);
        c2.gridx++;
        c2.weightx = 0;
        c2.insets = new Insets(5, 5, 0, 5);
        sPanel.add(searchButton, c2);

        moduleDescriptionCheckBox = new SearchIncludeCheckbox("Include module descriptions");
        moduleDescriptionCheckBox.setSelected(true);
        c2.gridx = 0;
        c2.gridy++;
        c2.weightx = 1;
        c2.gridwidth = 2;
        c2.insets = new Insets(5, 0, 0, 5);
        sPanel.add(moduleDescriptionCheckBox, c2);

        parameterDescriptionCheckBox = new SearchIncludeCheckbox("Include parameter descriptions");
        c2.gridy++;
        c2.weightx = 1;
        sPanel.add(parameterDescriptionCheckBox, c2);

        JSeparator separator2 = new JSeparator();
        c.anchor = GridBagConstraints.WEST;
        c.gridy++;
        sPanel.add(separator2, c2);

        resultsPanel = new ResultsPanel();
        c2.gridy++;
        c2.weighty = 1;
        c2.fill = GridBagConstraints.BOTH;
        sPanel.add(resultsPanel, c2);

        JScrollPane scrollPane = new JScrollPane(sPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setBackground(Color.CYAN);
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        c.gridy++;
        c.weighty = 1;
        c.insets = new Insets(5, 5, 5, 5);
        add(scrollPane, c);

        revalidate();
        repaint();

    }

    public void doSearch() {
        String query = queryEntry.getText();
        boolean includeModules = moduleDescriptionCheckBox.isSelected();
        boolean includeParameters = parameterDescriptionCheckBox.isSelected();
        ArrayList<SearchMatch> matches = searcher.getMatches(query, includeModules, includeParameters);
        resultsPanel.updateResults(matches);

    }

    public void showUsageMessage() {
        removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.insets = new Insets(5, 5, 0, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Adding title to help window
        JLabel fileListLabel = new JLabel();
        fileListLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        fileListLabel.setText("Module search");
        add(fileListLabel, c);

        // Adding close button
        ClosePanelButton closeButton = new ClosePanelButton(this);
        c.anchor = GridBagConstraints.EAST;
        c.weightx = 0;
        c.gridx++;
        add(closeButton, c);

        // Adding separator
        JSeparator separator = new JSeparator();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridy++;
        add(separator, c);

        // Adding title to help window
        JTextPane usageMessage = new JTextPane();
        usageMessage.setContentType("text/html");
        usageMessage.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        usageMessage.setText("<html><center><font face=\"sans-serif\" size=\"3\">"
                + "Click a module title to<br>see help about it" + "<br><br>"
                + "To hide this, click the X button or<br>go to View > Show help panel" + "</font></center></html>");
        usageMessage.setEditable(false);
        usageMessage.setBackground(null);
        usageMessage.setOpaque(false);
        c.weighty = 1;
        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        add(usageMessage, c);

        revalidate();
        repaint();

    }

    public static int getMinimumWidth() {
        return minimumWidth;
    }

    class EnterKeyListener implements KeyListener {

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
                doSearch();
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {
            
            
        }

        @Override
        public void keyTyped(KeyEvent e) {
            
            
        }
    }
}
