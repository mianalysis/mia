package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.GUI;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class StatusPanel extends JPanel {
    public StatusPanel() {
        int statusHeight = GUI.getStatusHeight();
        int frameWidth = GUI.getMinimumFrameWidth();

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setMinimumSize(new Dimension(0,statusHeight+15));
        setMaximumSize(new Dimension(Integer.MAX_VALUE,statusHeight+15));
        setPreferredSize(new Dimension(frameWidth-30,statusHeight+15));
        setOpaque(false);

    }
}
