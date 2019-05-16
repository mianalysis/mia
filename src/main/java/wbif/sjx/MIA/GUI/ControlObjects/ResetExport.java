package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ResetExport extends JButton implements ActionListener {
    private ExportableRef ref;

    private static final ImageIcon refreshIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/refresh_black_12px.png"), "");

    public ResetExport(ExportableRef ref) {
        this.ref = ref;

        setMargin(new Insets(0,0,0,0));
        setFocusPainted(false);
        setSelected(false);
        setName("Refresh measurement name");
        setToolTipText("Refresh measurement name");
        addActionListener(this);
        setIcon(refreshIcon);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ref.setNickname(ref.getName());
        ref.setExportIndividual(true);
        ref.setExportMean(true);
        ref.setExportMin(true);
        ref.setExportMax(true);
        ref.setExportSum(true);
        ref.setExportStd(true);

        GUI.populateModuleParameters();
        GUI.populateHelpNotes();

    }
}
