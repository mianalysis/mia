package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.ControlObjects.HelpArea;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class HelpPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 3704479016316750858L;

    public HelpPanel() {
        // Initialising the panel
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setLayout(new GridBagLayout());

    }

    public void updatePanel() {
        Module activeModule = GUI.getFirstSelectedModule();
        ModuleCollection modules = GUI.getModules();

        removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.insets = new Insets(5,5,0,5);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        // Adding title to help window
        JLabel helpLabel = new JLabel();
        helpLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        if (activeModule != null) helpLabel.setText("About \""+activeModule.getName()+"\"");
        add(helpLabel,c);

        // Adding separator
        JSeparator separator = new JSeparator();
        c.gridy++;
        add(separator,c);

        // If no Module is selected, also skip
        HelpArea helpArea = new HelpArea(activeModule,modules);

        JScrollPane jsp = new JScrollPane(helpArea);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.getVerticalScrollBar().setUnitIncrement(10);
        jsp.setBorder(null);
        c.gridy++;
        c.weighty = 1;
        c.insets = new Insets(5,5,5,5);
        add(jsp,c);

        revalidate();
        repaint();

    }

    public void showUsageMessage() {
        removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        // Adding title to help window
        JTextPane usageMessage = new JTextPane();
        usageMessage.setContentType("text/html");
        usageMessage.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        usageMessage.setText("<html><center><font face=\"sans-serif\" size=\"3\">" +
                "Click a module title to<br>see help about it"+
                "<br><br>" +
                "To hide this, go to<br>View > Show help panel" +
                "</font></center></html>");
        usageMessage.setEditable(false);
        usageMessage.setBackground(null);
        usageMessage.setOpaque(false);
        add(usageMessage);

        revalidate();
        repaint();

    }
}
