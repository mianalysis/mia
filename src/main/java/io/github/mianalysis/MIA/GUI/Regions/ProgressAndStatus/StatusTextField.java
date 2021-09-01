package io.github.mianalysis.MIA.GUI.Regions.ProgressAndStatus;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;

import io.github.mianalysis.MIA.MIA;
import io.github.mianalysis.MIA.GUI.GUI;
import io.github.mianalysis.MIA.Module.Module;

/**
 * Created by sc13967 on 19/01/2018.
 */
public class StatusTextField extends JLabel implements MouseListener{
    /**
     *
     */
    private static final long serialVersionUID = 5602369416510484361L;

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
                    GUI.updateProgressBar();
                } else {
                    setText("Verbose output enabled");
                    TimeUnit.SECONDS.sleep(1);
                }

                Module.setVerbose(!state);

            } catch (InterruptedException e1) {
                MIA.log.writeError(e1);
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
