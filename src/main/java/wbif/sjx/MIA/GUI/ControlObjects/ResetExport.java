package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.References.Abstract.Ref;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ResetExport extends JButton implements ActionListener {
    private Ref ref;

    private static final ImageIcon refreshIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/refresh_black_12px.png"), "");

    public ResetExport(Ref ref) {
        this.ref = ref;

        setMargin(new Insets(0,0,0,0));
        setFocusPainted(false);
        setSelected(false);
        setName("Refresh name");
        setToolTipText("Refresh name");
        addActionListener(this);
        setIcon(refreshIcon);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ref.setNickname(ref.getName());
        ref.setAllExport(true);

        GUI.populateModuleParameters();
        GUI.populateHelpNotes();

    }
}
