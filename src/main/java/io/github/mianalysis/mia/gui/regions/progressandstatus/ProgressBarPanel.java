package io.github.mianalysis.mia.gui.regions.progressandstatus;

import java.awt.Dimension;

import javax.swing.JProgressBar;

import io.github.mianalysis.mia.object.Colours;

public class ProgressBarPanel extends JProgressBar {
    /**
     *
     */
    private static final long serialVersionUID = 8268143348160546845L;

    public ProgressBarPanel() {
        super(0,100);

        setValue(0);
        setBorderPainted(false);
        setMinimumSize(new Dimension(1, 15));
        setMaximumSize(new Dimension(1, 15));
        setStringPainted(true);
        setString("");
        setForeground(Colours.ORANGE);
        
    }

    @Override
    public void setValue(int value) {
        super.setValue(value);
        if (value == 100)
            setForeground(Colours.GREEN);
        else
            setForeground(Colours.BLUE);
    }
}
