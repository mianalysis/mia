package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.References.MeasurementRef;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class MeasurementEnabledButton extends JButton implements ActionListener {
    private MeasurementRef measurementReference;
    private boolean state = true;
    private static final ImageIcon blackIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/power_black_strike_12px.png"), "");
    private static final ImageIcon redIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/power_red_12px.png"), "");
    private static final ImageIcon greenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/power_brightgreen_12px.png"), "");

    public MeasurementEnabledButton(MeasurementRef measurementReference) {
        this.measurementReference = measurementReference;

        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0,0,0,0));
        setName("ModuleEnabled");
        setToolTipText("Enable/disable measurementReference");
        setIcon();

        addActionListener(this);

    }

    public void setIcon() {
        if (measurementReference.isExportGlobal()) setIcon(greenIcon);
        else setIcon(blackIcon);
    }

    public MeasurementRef getMeasurementRef() {
        return measurementReference;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        measurementReference.setExportGlobal(!measurementReference.isExportGlobal());

        setIcon();
        GUI.populateModuleParameters();

    }
}
