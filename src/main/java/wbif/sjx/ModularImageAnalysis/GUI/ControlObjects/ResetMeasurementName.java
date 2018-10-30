package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementReference;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ResetMeasurementName extends JButton implements ActionListener {
    private MeasurementReference measurementReference;

    public ResetMeasurementName(MeasurementReference measurementReference) {
        this.measurementReference = measurementReference;

        setText("Reset name");
        setFocusPainted(false);
        setPreferredSize(new Dimension(100, 25));
        addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        measurementReference.setNickname(measurementReference.getName());

    }
}
