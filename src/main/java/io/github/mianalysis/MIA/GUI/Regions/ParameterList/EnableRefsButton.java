package io.github.mianalysis.MIA.GUI.Regions.ParameterList;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.github.mianalysis.MIA.GUI.GUI;
import io.github.mianalysis.MIA.Object.References.Abstract.ExportableRef;
import io.github.mianalysis.MIA.Object.References.Abstract.SummaryRef;
import io.github.mianalysis.MIA.Object.References.Collections.RefCollection;

public class EnableRefsButton extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -1146487404468628869L;

    private static final ImageIcon icon = new ImageIcon(
        EnableRefsButton.class.getResource("/Icons/check-mark_black_12px.png"), "");

    private RefCollection<SummaryRef> refs;

    // CONSTRUCTOR

    public EnableRefsButton(RefCollection<SummaryRef> refs) {
        this.refs = refs;

        setMargin(new Insets(0,0,0,0));
        setFocusPainted(false);
        setSelected(false);
        setName("EnableAllMeasurements");
        setToolTipText("Enable all measurements");
        addActionListener(this);
        setIcon(icon);

    }


    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        for (ExportableRef ref: refs.values()) ref.setExportGlobal(true);

        GUI.updateParameters();

    }
}
