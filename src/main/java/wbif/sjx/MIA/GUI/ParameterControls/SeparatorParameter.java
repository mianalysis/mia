package wbif.sjx.MIA.GUI.ParameterControls;

import javax.swing.*;
import java.awt.*;

public class SeparatorParameter extends ParameterControl {
    @Override
    public JComponent getComponent() {
        JSeparator separator = new JSeparator();

        separator.setPreferredSize(new Dimension(0,15));

        return separator;
    }

    @Override
    public void updateControl() {

    }
}
