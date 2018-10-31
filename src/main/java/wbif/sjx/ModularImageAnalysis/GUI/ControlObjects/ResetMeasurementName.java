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

    private static final ImageIcon refreshIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/refresh_black_12px.png"), "");

    public ResetMeasurementName(MeasurementReference measurementReference) {
        this.measurementReference = measurementReference;

        setMargin(new Insets(0,0,0,0));
        setFocusPainted(false);
        setSelected(false);
        setName("Refresh measurement name");
        setToolTipText("Refresh measurement name");
        addActionListener(this);
        setIcon(refreshIcon);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        measurementReference.setNickname(measurementReference.getName());

        GUI.populateModuleParameters();

    }
}
