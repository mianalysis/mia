package io.github.mianalysis.mia.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;


public class Splash extends JWindow {
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

        blankLogo = new ImageIcon(Splash.class.getResource("/images/Logo_splash."+suffix+"png"));

        label = new JLabel("", blankLogo, SwingConstants.CENTER);
        label.setOpaque(false);

        final JPanel pane = new JPanel();
		pane.setOpaque(false);
		pane.setLayout(new BorderLayout());
		pane.add(label, BorderLayout.CENTER);
		setContentPane(pane);
		pack();

        setAlwaysOnTop(true);
		setLocationRelativeTo(null);
		setBackground(new Color(0, 0, 0, 0));

        
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
