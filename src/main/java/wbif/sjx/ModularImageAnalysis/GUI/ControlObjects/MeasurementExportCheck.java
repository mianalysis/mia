package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.Object.MeasurementReference;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen Cross on 02/12/2017.
 */
public class MeasurementExportCheck extends JCheckBox implements ActionListener {
    private MeasurementReference measurementReference;

    public MeasurementExportCheck(MeasurementReference measurementReference) {
        this.measurementReference = measurementReference;

        this.setSelected(measurementReference.isExportable());
        this.setName("MeasurementExportCheck");

        addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        measurementReference.setExportable(isSelected());

    }
}