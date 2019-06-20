package wbif.sjx.MIA.GUI;

import wbif.sjx.MIA.GUI.ControlObjects.ModuleEnabledCheck;

import javax.swing.*;
import java.awt.*;

public class Splash extends JFrame {
    private JLabel text = new JLabel();

    public Splash() {
        Container container = getContentPane();
        setLayout(new BoxLayout(container,BoxLayout.PAGE_AXIS));
        setUndecorated(true);
        setBackground(new Color(0,0,0,0));
        setAlwaysOnTop(true);
        setPreferredSize(new Dimension(500,500));
        setMinimumSize(new Dimension(500,500));
        setMaximumSize(new Dimension(500,500));
        setResizable(false);

        JLabel image = new JLabel("", new ImageIcon(Splash.class.getResource("/Images/Logo_128.png")), SwingConstants.CENTER);
        image.setOpaque(false);
        add(image);

        text.setPreferredSize(new Dimension(500,50));
        text.setMinimumSize(new Dimension(500,50));
        text.setMaximumSize(new Dimension(500,50));
        text.setText("sfgsddfsdf");
        add(text);

    }
}
