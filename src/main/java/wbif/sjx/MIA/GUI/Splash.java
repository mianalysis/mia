package wbif.sjx.MIA.GUI;

import wbif.sjx.MIA.GUI.ControlObjects.ModuleEnabledCheck;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class Splash extends JFrame {
    private JLabel text;

    public Splash() {
        Container container = getContentPane();
        setLayout(new BoxLayout(container,BoxLayout.PAGE_AXIS));
        setUndecorated(true);
        setBackground(new Color(0,0,0,0));
        setAlwaysOnTop(true);
        setPreferredSize(new Dimension(190,200));
        setMinimumSize(new Dimension(190,200));
        setMaximumSize(new Dimension(190,200));
        setResizable(false);

        JLabel image = new JLabel("", new ImageIcon(Splash.class.getResource("/Images/Logo_128.png")), SwingConstants.CENTER);
        image.setOpaque(false);
        add(image);

        text = new JLabel("", SwingConstants.CENTER);
        text.setPreferredSize(new Dimension(190,30));
        text.setMinimumSize(new Dimension(190,30));
        text.setMaximumSize(new Dimension(190,30));
        text.setBorder(new EmptyBorder(10,0,0,0));
        text.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        text.setForeground(Color.WHITE);
        text.setText("Initialising plugin");

        add(text);

    }

    public void setMessage(String message) {
        text.setText(message);
    }
}
