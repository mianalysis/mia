package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.ControlObjects.NotesArea;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class NotesPanel extends JPanel {
    public NotesPanel() {
        int frameWidth = GUI.getMinimumFrameWidth();
        int bigButtonSize = GUI.getBigButtonSize();

        // Initialising the panel
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setPreferredSize(new Dimension(frameWidth-45-bigButtonSize, bigButtonSize+15));
        setMinimumSize(new Dimension(frameWidth-45-bigButtonSize, bigButtonSize+15));
        setLayout(new GridBagLayout());

    }

    public void updatePanel() {
        Module activeModule = GUI.getFirstSelectedModule();

        removeAll();

        if (activeModule == null) return;

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.insets = new Insets(5,5,0,5);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        // Adding title to help window
        JLabel notesLabel = new JLabel();
        notesLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        notesLabel.setText("Notes for \""+activeModule.getName()+"\"");
        add(notesLabel,c);

        // Adding separator
        JSeparator separator = new JSeparator();
        c.gridy++;
        add(separator,c);

        NotesArea notesArea = new NotesArea(activeModule);
        c.gridy++;
        c.weighty = 1;
        c.insets = new Insets(5,5,5,5);

        JScrollPane jsp = new JScrollPane(notesArea);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.getVerticalScrollBar().setUnitIncrement(10);
        add(jsp,c);

        validate();
        repaint();

    }
}
