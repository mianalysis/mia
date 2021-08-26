package wbif.sjx.MIA.GUI.Panels;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;

import wbif.sjx.MIA.GUI.ControlObjects.ClosePanelButton;

public class SearchPanel extends JPanel {
    private final static int minimumWidth = 300;

    public SearchPanel() {
        // Initialising the panel
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setLayout(new GridBagLayout());

        // setMaximumSize(new Dimension(minimumWidth, Integer.MAX_VALUE));
        // setMinimumSize(new Dimension(minimumWidth, 1));
        // setPreferredSize(new Dimension(minimumWidth, Integer.MAX_VALUE));

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

        JTextField queryEntry = new JTextField();
        queryEntry.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        queryEntry.setPreferredSize(new Dimension(0, 26));
        queryEntry.setMinimumSize(new Dimension(0, 26));
        queryEntry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        // queryEntry.addFocusListener(this);
        c2.gridx = 0;
        c2.gridy = 0;
        c2.weightx = 1;
        c2.insets = new Insets(5, 0, 0, 0);
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.anchor = GridBagConstraints.NORTH;
        sPanel.add(queryEntry, c2);

        JButton searchButton = new JButton();
        searchButton.setIcon(new ImageIcon(SearchPanel.class.getResource("/Icons/arrowopen_black_12px.png"), ""));
        searchButton.setPreferredSize(new Dimension(26, 26));
        searchButton.setMinimumSize(new Dimension(26, 26));
        searchButton.setMaximumSize(new Dimension(26, 26));
        // searchButton.addFocusListener(this);
        c2.gridx++;
        c2.weightx = 0;
        c2.insets = new Insets(5, 5, 0, 5);
        sPanel.add(searchButton, c2);

        JCheckBox moduleDescriptionCheckBox = new JCheckBox();
        moduleDescriptionCheckBox.setText("Include module descriptions");
        moduleDescriptionCheckBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        moduleDescriptionCheckBox.setPreferredSize(new Dimension(0, 26));
        moduleDescriptionCheckBox.setMinimumSize(new Dimension(0, 26));
        moduleDescriptionCheckBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        c2.gridx = 0;
        c2.gridy++;
        c2.weightx = 1;
        c2.insets = new Insets(5, 0, 0, 5);
        sPanel.add(moduleDescriptionCheckBox, c2);

        JCheckBox parameterDescriptionCheckBox = new JCheckBox();
        parameterDescriptionCheckBox.setText("Include parameter descriptions");
        parameterDescriptionCheckBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        parameterDescriptionCheckBox.setPreferredSize(new Dimension(0, 26));
        parameterDescriptionCheckBox.setMinimumSize(new Dimension(0, 26));
        parameterDescriptionCheckBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        c2.gridy++;
        c2.weightx = 1;
        c2.weighty = 1;
        sPanel.add(parameterDescriptionCheckBox, c2);

        JScrollPane jsp = new JScrollPane(sPanel);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.getVerticalScrollBar().setUnitIncrement(10);
        jsp.setBorder(null);
        c.gridy++;
        c.weighty = 1;
        c.insets = new Insets(5, 5, 5, 5);
        add(jsp, c);

        revalidate();
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
        usageMessage.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
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
}
