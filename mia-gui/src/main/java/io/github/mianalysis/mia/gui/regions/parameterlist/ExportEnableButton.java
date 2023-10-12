package io.github.mianalysis.mia.gui.regions.parameterlist;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.object.refs.abstrakt.ExportableRef;
import io.github.mianalysis.mia.object.system.SwingPreferences;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ExportEnableButton extends JButton implements ActionListener {
    /**
     * s
     *
     */
    private static final long serialVersionUID = 6348882867766257110L;
    private ExportableRef ref;
    private static final ImageIcon blackIcon = new ImageIcon(
            ExportEnableButton.class.getResource("/icons/power_black_strike_12px.png"), "");
    private static final ImageIcon blackIconDM = new ImageIcon(
            ExportEnableButton.class.getResource("/icons/power_blackDM_strike_12px.png"), "");
    private static final ImageIcon greenIcon = new ImageIcon(
            ExportEnableButton.class.getResource("/icons/power_green_12px.png"), "");
    private static final ImageIcon greenIconDM = new ImageIcon(
            ExportEnableButton.class.getResource("/icons/power_greenDM_12px.png"), "");

    public ExportEnableButton(ExportableRef ref) {
        this.ref = ref;

        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0, 0, 0, 0));
        setName("ModuleEnabled");
        setToolTipText("Enable/disable ref");
        setIcon();

        addActionListener(this);

    }

    public void setIcon() {
        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        if (ref.isExportGlobal()) {
            if (isDark)
                setIcon(greenIconDM);
            else
                setIcon(greenIcon);
        } else {
            if (isDark)
                setIcon(blackIconDM);
            else
                setIcon(blackIcon);
        }
    }

    public ExportableRef getReference() {
        return ref;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        ref.setExportGlobal(!ref.isExportGlobal());

        setIcon();
        GUI.updateParameters();

    }
}
