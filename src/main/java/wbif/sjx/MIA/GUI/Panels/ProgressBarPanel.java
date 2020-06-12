package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.GUI.Colours;

import javax.swing.*;
import java.awt.*;

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
        MIA.log.writeDebug("Progress "+value);
        if (value == 100)
            setForeground(Colours.GREEN);
        else
            setForeground(Colours.BLUE);
    }
}
