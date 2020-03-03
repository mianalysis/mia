package wbif.sjx.MIA.GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;


public class Splash extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = 6710454328949590953L;
    private int width = 231;
    private JLabel image;

    private final static String DETECTING_MODULES = "/Images/Logo_splash_detecting-modules";
    private final static String INITIALISING_MODULES = "/Images/Logo_splash_initialising-modules";
    private final static String CREATING_INTERFACE = "/Images/Logo_splash_creating-interface";

    private ImageIcon blankLogo = new ImageIcon(Splash.class.getResource("/Images/Logo_splash.png"));
    private final ImageIcon detectingModulesLogo;
    private final ImageIcon initialisingModulesLogo;
    private final ImageIcon creatingInterfaceLogo;

    public enum Status {
        BLANK, DETECTING_MODULES, INITIALISING_MODULES, CREATING_INTERFACE;
    }

    public Splash() {
        // Determine special date
        String suffix = getSpecialSuffix();

        detectingModulesLogo = new ImageIcon(Splash.class.getResource(DETECTING_MODULES+suffix+".png"));
        initialisingModulesLogo = new ImageIcon(Splash.class.getResource(INITIALISING_MODULES+suffix+".png"));
        creatingInterfaceLogo = new ImageIcon(Splash.class.getResource(CREATING_INTERFACE+suffix+".png"));

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
            case INITIALISING_MODULES:
                image.setIcon(initialisingModulesLogo);
                break;
            case CREATING_INTERFACE:
                image.setIcon(creatingInterfaceLogo);
                break;
        }
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
