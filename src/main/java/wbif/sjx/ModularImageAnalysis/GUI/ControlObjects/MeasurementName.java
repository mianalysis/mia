package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementRef;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by sc13967 on 06/09/2017.
 */
public class MeasurementName extends JTextField implements FocusListener {
    private MeasurementRef measurementReference;

    public MeasurementName(MeasurementRef measurementReference) {
        this.measurementReference = measurementReference;

        setText(measurementReference.getNickname());
        addFocusListener(this);

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        measurementReference.setNickname(getText());
        GUI.updateModules(true);
        GUI.populateModuleList();

    }
}
