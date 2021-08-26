package wbif.sjx.MIA.GUI.Panels;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.ControlObjects.ClosePanelButton;
import wbif.sjx.MIA.GUI.ControlObjects.HelpArea;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;

public class SearchPanel extends JPanel {
    private final static int minimumWidth = 300;

    public SearchPanel() {
        // Initialising the panel
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setLayout(new GridBagLayout());

        int frameHeight = GUI.getFrameHeight();
        int statusHeight = GUI.getStatusHeight();

        setMaximumSize(new Dimension(minimumWidth, Integer.MAX_VALUE));
        setMinimumSize(new Dimension(minimumWidth, frameHeight - statusHeight));
        setPreferredSize(new Dimension(minimumWidth, frameHeight - statusHeight));

    }

    public void updatePanel() {
        Module activeModule = GUI.getFirstSelectedModule();
        ModuleCollection modules = GUI.getModules();

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
        helpLabel.setText("Moduule search");
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

        // If no Module is selected, also skip
        HelpArea helpArea = new HelpArea(activeModule, modules);

        JScrollPane jsp = new JScrollPane(helpArea);
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
