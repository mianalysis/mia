package wbif.sjx.MIA.GUI.ControlObjects.ParameterList;

import wbif.sjx.MIA.GUI.ControlObjects.ModuleEnabledCheck;
import wbif.sjx.MIA.Object.References.Abstract.ExportableRef;
import wbif.sjx.MIA.GUI.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ExportEnableButton extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 6348882867766257110L;
    private ExportableRef ref;
    private static final ImageIcon blackIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/power_black_strike_12px.png"), "");
    private static final ImageIcon greenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/power_brightgreen_12px.png"), "");

    public ExportEnableButton(ExportableRef ref) {
        this.ref = ref;

        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0,0,0,0));
        setName("ModuleEnabled");
        setToolTipText("Enable/disable ref");
        setIcon();

        addActionListener(this);

    }

    public void setIcon() {
        if (ref.isExportGlobal()) setIcon(greenIcon);
        else setIcon(blackIcon);
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
