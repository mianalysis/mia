package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementReference;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementRefCollection;

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
        for (MeasurementReference measurementReference:measurementReferences.values()) {
            measurementReference.setExportGlobal(true);
        }

        GUI.updateModules(false);

    }
}
