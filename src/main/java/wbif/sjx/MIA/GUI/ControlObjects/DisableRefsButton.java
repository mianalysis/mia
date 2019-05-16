package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.References.Abstract.RefCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DisableRefsButton extends JButton implements ActionListener {
    private static final ImageIcon icon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/delete-2_black_12px.png"), "");

    private RefCollection<? extends ExportableRef> refs;

    // CONSTRUCTOR

    public DisableRefsButton(RefCollection<? extends ExportableRef> refs) {
        this.refs = refs;

        JButton enableButton = new JButton();
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
        for (ExportableRef measurementReference: refs.values()) {
            measurementReference.setExportGlobal(false);
        }

        GUI.populateModuleParameters();

    }
}
