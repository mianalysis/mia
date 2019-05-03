package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.TimeUnit;

/**
 * Created by sc13967 on 19/01/2018.
 */
public class StatusTextField extends JLabel implements MouseListener{
    public StatusTextField() {
        addMouseListener(this);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        return getText();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Thread t = new Thread(() -> {
            try {
                boolean state = Module.isVerbose();
                Module.setVerbose(false);

                if (state) {
                    setText("Verbose output disabled");
                    TimeUnit.SECONDS.sleep(1);
                    setText("");
                } else {
                    setText("Verbose output enabled");
                    TimeUnit.SECONDS.sleep(1);
                }

                Module.setVerbose(!state);

            } catch (InterruptedException e1) {
                System.err.println(e1);
            }
        });
        t.start();

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
