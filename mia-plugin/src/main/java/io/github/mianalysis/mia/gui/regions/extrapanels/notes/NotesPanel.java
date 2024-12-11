package io.github.mianalysis.mia.gui.regions.extrapanels.notes;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.ClosePanelButton;
import io.github.mianalysis.mia.module.Module;

public class NotesPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = -3629896187837314617L;

    public NotesPanel() {
        // Initialising the panel
        setOpaque(false);
        setLayout(new GridBagLayout());

    }

    public void updatePanel() {
        Module activeModule = GUI.getFirstSelectedModule();

        removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.insets = new Insets(5, 5, 0, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        NotesArea notesArea = new NotesArea(activeModule);
        
        JScrollPane scrollPane = new JScrollPane(notesArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        c.gridy++;
        c.weighty = 1;
        c.insets = new Insets(5,5,5,5);
        add(scrollPane,c);

        validate();
        repaint();

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
        JLabel notesLabel = new JLabel();
        notesLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        notesLabel.setText("Notes");
        add(notesLabel, c);

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
        add(separator,c);

        // Adding title to help window
        JTextPane usageMessage = new JTextPane();
        usageMessage.setContentType("text/html");
        usageMessage.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        usageMessage.setText("<html><center><font face=\"sans-serif\" size=\"3\">" +
                "Click a module title to<br>see an editable notes panel."+
                "<br><br>" +
                "To hide this, click the X button or<br>go to View > Show notes panel" +
                "</font></center></html>");
        usageMessage.setEditable(false);
        usageMessage.setBackground(null);
        usageMessage.setOpaque(false);
        c.weighty = 1;
        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        add(usageMessage,c);

        revalidate();
        repaint();

    }
}
