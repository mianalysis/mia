package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementRef;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ResetMeasurement extends JButton implements ActionListener {
    private MeasurementRef measurementReference;

    private static final ImageIcon refreshIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/refresh_black_12px.png"), "");

    public ResetMeasurement(MeasurementRef measurementReference) {
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
        measurementReference.setExportIndividual(true);
        measurementReference.setExportMean(true);
        measurementReference.setExportMin(true);
        measurementReference.setExportMax(true);
        measurementReference.setExportSum(true);
        measurementReference.setExportStd(true);

        GUI.populateModuleParameters();
        GUI.populateHelpNotes();

    }
}
