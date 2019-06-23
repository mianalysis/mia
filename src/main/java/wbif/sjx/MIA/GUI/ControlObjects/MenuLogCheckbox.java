package wbif.sjx.MIA.GUI.ControlObjects;

import org.apache.commons.lang.WordUtils;
import org.ojalgo.type.format.StringFormat;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Process.Logging.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuLogCheckbox extends JCheckBoxMenuItem implements ActionListener {
    private final Log.Level level;

    public MenuLogCheckbox(Log.Level level, boolean state) {
        this.level = level;
        String title = WordUtils.capitalizeFully(level.toString());
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setText(title);
        addActionListener(this);
        setSelected(state);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MIA.log.setWriteEnabled(level,isSelected());
    }
}
