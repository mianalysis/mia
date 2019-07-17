package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.GUI;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.io.File;

public class FileListPanel extends JScrollPane {
    private JPanel panel;

    public FileListPanel() {
        panel = new JPanel();
        setViewportView(panel);

        int frameWidth = GUI.getMinimumFrameWidth();
        int bigButtonSize = GUI.getBigButtonSize();

        // Initialising the scroll panel
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(10);
        setPreferredSize(new Dimension(frameWidth-45-bigButtonSize, bigButtonSize+15));
        setMinimumSize(new Dimension(frameWidth-45-bigButtonSize, bigButtonSize+15));

        panel.setLayout(new GridBagLayout());
        panel.validate();
        panel.repaint();

        validate();
        repaint();

    }

    public void updatePanel() {
        panel.removeAll();

        // Get list of available images
        String[][] testFiles = new String[][]{{"dfddf"},{"sdfsfsdf"},{"sdhghg"}};
        String[] columnNames = new String[]{"Filename"};

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 20, 0);
        c.anchor = GridBagConstraints.NORTHWEST;

        // Creating table containing all file names
        JTable table = new JTable(testFiles,columnNames);
        panel.add(table,c);

        panel.revalidate();
        panel.repaint();

        revalidate();
        repaint();

    }
}
