package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * Created by sc13967 on 19/01/2018.
 */
public class StatusTextField extends JLabel {
    @Override
    public String getToolTipText(MouseEvent event) {
        return getText();
    }
}
