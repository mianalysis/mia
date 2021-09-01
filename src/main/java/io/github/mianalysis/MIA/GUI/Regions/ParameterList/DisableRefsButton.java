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

public class DisableRefsButton extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -93923037075226123L;

    private static final ImageIcon icon = new ImageIcon(
        DisableRefsButton.class.getResource("/Icons/delete-2_black_12px.png"), "");

    private RefCollection<SummaryRef> refs;

    // CONSTRUCTOR

    public DisableRefsButton(RefCollection<SummaryRef> refs) {
        this.refs = refs;

        setMargin(new Insets(0,0,0,0));
        setFocusPainted(false);
        setSelected(false);
        setName("DisableAllMeasurements");
        setToolTipText("Disable all measurements");
        addActionListener(this);
        setIcon(icon);

    }


    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        for (ExportableRef ref: refs.values()) ref.setExportGlobal(false);

        GUI.updateParameters();

    }
}
