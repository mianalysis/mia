package wbif.sjx.MIA.GUI.ControlObjects.ParameterList;

import wbif.sjx.MIA.GUI.ControlObjects.ModuleEnabledCheck;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.References.Abstract.ExportableRef;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ResetExport extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 2075096539899744698L;

    private ExportableRef ref;

    private static final ImageIcon refreshIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/refresh_black_12px.png"), "");

    public ResetExport(ExportableRef ref) {
        this.ref = ref;

        setMargin(new Insets(0,0,0,0));
        setFocusPainted(false);
        setSelected(false);
        addActionListener(this);
        setIcon(refreshIcon);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        ref.setAllExport(true);

        GUI.updateParameters();
        GUI.updateHelpNotes();

    }
}
