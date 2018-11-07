package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementReference;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementReferenceCollection;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen Cross on 02/12/2017.
 */
public class MeasurementExportCheck extends JCheckBox implements ActionListener {
    public enum Statistic {GLOBAL,INDIVIDUAL, MEAN, MIN, MAX, SUM, STD}
    public enum Type {SINGLE_MEASUREMENT, ALL_MEASUREMENTS};


    private MeasurementReference measurementReference;
    private Statistic statistic;
    private Type type;


    public MeasurementExportCheck(MeasurementReference measurementReference, Statistic statistic, Type type) {
        this.measurementReference = measurementReference;
        this.statistic = statistic;
        this.type = type;

        this.setName("MeasurementExportCheck");

        addActionListener(this);

    }

    private void setStates(MeasurementReference measurementReference) {
        switch (statistic) {
            case GLOBAL:
                measurementReference.setExportIndividual(isSelected());
                measurementReference.setExportMean(isSelected());
                measurementReference.setExportMin(isSelected());
                measurementReference.setExportMax(isSelected());
                measurementReference.setExportSum(isSelected());
                measurementReference.setExportStd(isSelected());
                break;
            case INDIVIDUAL:
                measurementReference.setExportIndividual(isSelected());
                break;
            case MEAN:
                measurementReference.setExportMean(isSelected());
                break;
            case MIN:
                measurementReference.setExportMin(isSelected());
                break;
            case MAX:
                measurementReference.setExportMax(isSelected());
                break;
            case SUM:
                measurementReference.setExportSum(isSelected());
                break;
            case STD:
                measurementReference.setExportStd(isSelected());
                break;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (type) {
            case SINGLE_MEASUREMENT:
                setStates(measurementReference);
                break;
            case ALL_MEASUREMENTS:
                MeasurementReferenceCollection measurementReferences = new MeasurementReferenceCollection();
                measurementReferences.putAll(GUI.getModules().getObjectMeasurementReferences());
                measurementReferences.putAll(GUI.getModules().getImageMeasurementReferences());
                for (MeasurementReference measurementReference:measurementReferences.values()) setStates(measurementReference);

                setStates(measurementReference);
                
                break;
        }

        GUI.updateModules(false);

    }
}