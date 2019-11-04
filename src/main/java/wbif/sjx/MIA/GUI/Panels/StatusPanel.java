package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.GUI;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class StatusPanel extends JLayeredPane {
//    ImageIcon logo = new ImageIcon(this.getClass().getResource("/Icons/Logo_wide_fade_35.png"),"");

    public StatusPanel() {
        int statusHeight = GUI.getStatusHeight();
        int frameWidth = GUI.getMinimumFrameWidth();

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setMinimumSize(new Dimension(1,statusHeight+15));
        setMaximumSize(new Dimension(1,statusHeight+15));
        setPreferredSize(new Dimension(1,statusHeight+15));

    }
}
