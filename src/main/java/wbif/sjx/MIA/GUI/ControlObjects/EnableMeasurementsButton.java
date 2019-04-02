package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.MeasurementRef;
import wbif.sjx.MIA.Object.MeasurementRefCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EnableMeasurementsButton extends JButton implements ActionListener {
    private static final ImageIcon icon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/check-mark_black_12px.png"), "");

    private MeasurementRefCollection measurementReferences;

    // CONSTRUCTOR

    public EnableMeasurementsButton(MeasurementRefCollection measurementReferences) {
        this.measurementReferences = measurementReferences;

        JButton enableButton = new JButton();
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
        for (MeasurementRef measurementReference:measurementReferences.values()) {
            measurementReference.setExportGlobal(true);
        }

        GUI.populateModuleParameters();

    }
}
