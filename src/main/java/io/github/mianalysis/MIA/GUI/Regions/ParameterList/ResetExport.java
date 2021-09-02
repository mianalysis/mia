package io.github.mianalysis.MIA.GUI.Regions.ParameterList;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.github.mianalysis.MIA.GUI.GUI;
import io.github.mianalysis.MIA.Object.Refs.Abstract.ExportableRef;

public class ResetExport extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 2075096539899744698L;

    private ExportableRef ref;

    private static final ImageIcon refreshIcon = new ImageIcon(ResetExport.class.getResource("/Icons/refresh_black_12px.png"), "");

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
