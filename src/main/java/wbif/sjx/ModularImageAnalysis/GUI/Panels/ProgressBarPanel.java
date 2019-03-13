package wbif.sjx.ModularImageAnalysis.GUI.Panels;

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
        setForeground(new Color(86,190,253));

    }
}
