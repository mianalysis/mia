package wbif.sjx.MIA.GUI.ControlObjects.SearchPanel;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JCheckBox;

public class SearchIncludeCheckbox extends JCheckBox {
    public SearchIncludeCheckbox(String text) {
        setText(text);
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setPreferredSize(new Dimension(0, 26));
        setMinimumSize(new Dimension(0, 26));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
    }
}
