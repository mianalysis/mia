package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementReference;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementRefCollection;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.OutputImageP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.OutputObjectsP;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen Cross on 02/12/2017.
 */
public class MeasurementExportCheck extends JCheckBox implements ActionListener {
    public enum Statistic {INDIVIDUAL, MEAN, MIN, MAX, SUM, STD}
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
                ModuleCollection modules = GUI.getModules();

                for (OutputObjectsP objectName:modules.getAvailableObjects(null)) {
                    MeasurementRefCollection measurementReferences = modules.getObjectMeasurementReferences(objectName.getObjectsName());
                    for (MeasurementReference measurementReference:measurementReferences.values()) {
                        setStates(measurementReference);
                    }
                }

                for (OutputImageP imageName:modules.getAvailableImages(null)) {
                    MeasurementRefCollection measurementReferences = modules.getImageMeasurementRefs(imageName.getImageName());
                    for (MeasurementReference measurementReference:measurementReferences.values()) {
                        setStates(measurementReference);
                    }
                }

                setStates(measurementReference);
                
                break;
        }

        GUI.updateModules(false);

    }
}