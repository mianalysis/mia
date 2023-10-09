package io.github.mianalysis.mia.gui.regions.progressandstatus;

import java.awt.Dimension;

import javax.swing.JProgressBar;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.Preferences;

public class ProgressBarPanel extends JProgressBar {
    /**
     *
     */
    private static final long serialVersionUID = 8268143348160546845L;

    public ProgressBarPanel() {
        super(0, 100);

        Preferences preferences = MIA.getPreferences();
        boolean darkMode = preferences == null ? false : preferences.darkThemeEnabled();

        setValue(0);
        setBorderPainted(false);
        setMinimumSize(new Dimension(1, 15));
        setMaximumSize(new Dimension(1, 15));
        setStringPainted(true);
        setString("");
        setForeground(Colours.getOrange(darkMode));

    }

    @Override
    public void setValue(int value) {
        super.setValue(value);

        Preferences preferences = MIA.getPreferences();
        boolean darkMode = preferences == null ? false : preferences.darkThemeEnabled();

        if (value == 100)
            setForeground(Colours.getGreen(darkMode));
        else
            setForeground(Colours.getBlue(darkMode));
    }
}
