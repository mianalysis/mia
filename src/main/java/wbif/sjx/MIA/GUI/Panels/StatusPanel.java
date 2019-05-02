package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.GUI;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class StatusPanel extends JPanel {
    ImageIcon logo = new ImageIcon(this.getClass().getResource("/Icons/Logo_wide_fade_35.png"),"");

    public StatusPanel() {
        int statusHeight = GUI.getStatusHeight();
        int frameWidth = GUI.getMinimumFrameWidth();

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setMinimumSize(new Dimension(0,statusHeight+15));
        setMaximumSize(new Dimension(Integer.MAX_VALUE,statusHeight+15));
        setPreferredSize(new Dimension(frameWidth-30,statusHeight+15));

    }

//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//
//        Rectangle bounds = g.getClipBounds();
//        int width = logo.getIconWidth();
//        int height = logo.getIconHeight();
//
//        int x = bounds.width-width;
//        int y = (bounds.height-width)/2;
//
//        g.drawImage(logo.getImage(),x,0,null);
//
//    }
}
