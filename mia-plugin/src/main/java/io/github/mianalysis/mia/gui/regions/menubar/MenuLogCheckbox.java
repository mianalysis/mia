package io.github.mianalysis.mia.gui.regions.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;

import org.apache.commons.lang.WordUtils;

import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.process.logging.LogRenderer.Level;

public class MenuLogCheckbox extends JCheckBoxMenuItem implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -114208262023653742L;
    private final Level level;

    public MenuLogCheckbox(Level level, boolean state) {
        this.level = level;
        String title = WordUtils.capitalizeFully(level.toString());
        setFont(GUI.getDefaultFont().deriveFont(14f));
        setText(title);
        addActionListener(this);
        setSelected(state);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (level) {
        case DEBUG:
            Prefs.set("MIA.Log.Debug", isSelected());
            break;
        case MEMORY:
            Prefs.set("MIA.Log.Memory", isSelected());
            break;
        case MESSAGE:
            Prefs.set("MIA.Log.Message", isSelected());
            break;
        case STATUS:
            Prefs.set("MIA.Log.Status", isSelected());
            break;
        case WARNING:
            Prefs.set("MIA.Log.Warning", isSelected());
            break;
        }
        
        Prefs.savePreferences();

        MIA.getMainRenderer().setWriteEnabled(level, isSelected());
    }
}
