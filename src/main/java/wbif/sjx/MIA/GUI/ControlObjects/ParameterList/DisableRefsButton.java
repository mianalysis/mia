package wbif.sjx.MIA.GUI.ControlObjects.ParameterList;

import wbif.sjx.MIA.GUI.ControlObjects.ModuleEnabledCheck;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.References.Abstract.ExportableRef;
import wbif.sjx.MIA.Object.References.Abstract.SummaryRef;
import wbif.sjx.MIA.Object.References.Collections.RefCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DisableRefsButton extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -93923037075226123L;

    private static final ImageIcon icon = new ImageIcon(
            ModuleEnabledCheck.class.getResource("/Icons/delete-2_black_12px.png"), "");

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
