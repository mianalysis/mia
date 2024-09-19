package io.github.mianalysis.mia.gui.regions.parameterlist;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.object.refs.abstrakt.ExportableRef;
import io.github.mianalysis.mia.object.refs.abstrakt.SummaryRef;
import io.github.mianalysis.mia.object.refs.collections.Refs;
import io.github.mianalysis.mia.object.system.SwingPreferences;

public class DisableRefsButton extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -93923037075226123L;

    private static final ImageIcon icon = new ImageIcon(
            DisableRefsButton.class.getResource("/icons/delete-2_black_12px.png"), "");
    private static final ImageIcon iconDM = new ImageIcon(
            DisableRefsButton.class.getResource("/icons/delete-2_blackDM_12px.png"), "");

    private Refs<SummaryRef> refs;

    // CONSTRUCTOR

    public DisableRefsButton(Refs<SummaryRef> refs) {
        this.refs = refs;

        setMargin(new Insets(0, 0, 0, 0));
        setFocusPainted(false);
        setSelected(false);
        setName("DisableAllMeasurements");
        setToolTipText("Disable all measurements");
        addActionListener(this);

        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();
        if (isDark)
            setIcon(iconDM);
        else
            setIcon(icon);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        for (ExportableRef ref : refs.values())
            ref.setExportGlobal(false);

        GUI.updateParameters(false, null);

    }
}
