package io.github.mianalysis.mia.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


public class Splash extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = 6710454328949590953L;
    private JLabel label;
    private ImageIcon blankLogo;

    public enum Status {
        BLANK, DETECTING_MODULES, INITIALISING_MODULES, CREATING_INTERFACE;
    }

    public Splash() {        
        // Determine special date
        String suffix = getSpecialSuffix();
        blankLogo = new ImageIcon(Splash.class.getResource("/images/Logo_splash"+suffix+".png"));

        label = new JLabel("", blankLogo, SwingConstants.CENTER);
        // label.setOpaque(false);
        label.setBackground(new Color(0,100,0,100));

        final JPanel pane = new JPanel();
		// pane.setOpaque(false);
		pane.setLayout(new BorderLayout());
		pane.add(label, BorderLayout.CENTER);
        pane.setBackground(new Color(100,0,0,100));
		setContentPane(pane);
        setUndecorated(true);
		pack();
        
		setLocationRelativeTo(null);
        setBackground(new Color(0,0,0,0));
        getRootPane().setBackground(new Color(0,0,0,0));
        
    }

    static String getSpecialSuffix() {
        Date date = new Date();
        switch (new SimpleDateFormat("dd-MM").format(date)) {
            case "31-10":
                if (Integer.parseInt(new SimpleDateFormat("HH").format(date)) >= 18) return "_3110";
        }

        return "";

    }
}
