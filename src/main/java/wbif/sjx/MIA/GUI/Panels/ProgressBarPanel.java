package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.Colours;

import javax.swing.*;
import java.awt.*;

public class ProgressBarPanel extends JProgressBar {
    public ProgressBarPanel() {
        super(0,100);

        setValue(0);
        setBorderPainted(false);
        setMinimumSize(new Dimension(0, 15));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 15));
        setStringPainted(true);
        setString("");
        setForeground(Colours.ORANGE);

    }

    @Override
    public void setValue(int value) {
        super.setValue(value);
        if (value == 100) setForeground(Colours.GREEN);
        else setForeground(Colours.BLUE);
    }
}
