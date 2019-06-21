package wbif.sjx.MIA.GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class Splash extends JFrame {
    private int height = 192;
    private int width = 231;
    private JLabel image;

    private ImageIcon blankLogo = new ImageIcon(Splash.class.getResource("/Images/Logo_splash.png"));
    private ImageIcon detectingModulesLogo = new ImageIcon(Splash.class.getResource("/Images/Logo_splash_detecting-modules.png"));
    private ImageIcon creatingInterfaceLogo = new ImageIcon(Splash.class.getResource("/Images/Logo_splash_creating-interface.png"));

    public enum Status {
        BLANK, DETECTING_MODULES, CREATING_INTERFACE;
    }

    public Splash() {
        setUndecorated(true);
        setBackground(new Color(0,0,0,0));
        setAlwaysOnTop(true);
        setPreferredSize(new Dimension(width,250));
        setMinimumSize(new Dimension(width,250));
        setMaximumSize(new Dimension(width,250));
        setResizable(false);

        image = new JLabel("", blankLogo, SwingConstants.CENTER);
        image.setOpaque(false);
        add(image);

    }

    public void setStatus(Status status) {
        switch (status) {
            default:
            case BLANK:
                image.setIcon(blankLogo);
                break;
            case DETECTING_MODULES:
                image.setIcon(detectingModulesLogo);
                break;
            case CREATING_INTERFACE:
                image.setIcon(creatingInterfaceLogo);
                break;
        }
    }
}
