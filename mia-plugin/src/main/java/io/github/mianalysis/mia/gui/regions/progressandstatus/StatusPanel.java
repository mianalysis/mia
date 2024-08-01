package io.github.mianalysis.mia.gui.regions.progressandstatus;

import io.github.mianalysis.mia.gui.GUI;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class StatusPanel extends JLayeredPane {
//    ImageIcon logo = new ImageIcon(this.getClass().getResource("/Icons/Logo_wide_fade_35.png"),"");

    /**
 *
 */
private static final long serialVersionUID = -5685268881319325735L;

public StatusPanel() {
        int statusHeight = GUI.getStatusHeight();

        setLayout(new GridBagLayout());
        // setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setMinimumSize(new Dimension(1,statusHeight+15));
        setMaximumSize(new Dimension(1,statusHeight+15));
        setPreferredSize(new Dimension(1,statusHeight+15));

    }
}
