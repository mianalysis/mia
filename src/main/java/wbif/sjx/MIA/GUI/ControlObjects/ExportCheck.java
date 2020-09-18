package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.References.Abstract.ExportableRef;
import wbif.sjx.MIA.Object.References.Abstract.SummaryRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen Cross on 02/12/2017.
 */
public class ExportCheck extends JCheckBox implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 3315897527281360540L;

    public enum Statistic {
        INDIVIDUAL, MEAN, MIN, MAX, SUM, STD
    }
    public enum Type {SINGLE, ALL};


    private ExportableRef reference;
    private Statistic statistic;
    private Type type;


    public ExportCheck(ExportableRef reference, Statistic statistic, Type type) {
        this.reference = reference;
        this.statistic = statistic;
        this.type = type;

        this.setName("ExportCheck");

        addActionListener(this);

    }

    private void setStates(SummaryRef reference) {
        switch (statistic) {
            case INDIVIDUAL:
                reference.setExportIndividual(isSelected());
                break;
            case MEAN:
                reference.setExportMean(isSelected());
                break;
            case MIN:
                reference.setExportMin(isSelected());
                break;
            case MAX:
                reference.setExportMax(isSelected());
                break;
            case SUM:
                reference.setExportSum(isSelected());
                break;
            case STD:
                reference.setExportStd(isSelected());
                break;
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        switch (type) {
            case SINGLE:
                if (reference instanceof SummaryRef) {
                    setStates((SummaryRef) reference);
                } else {
                    reference.setExportIndividual(isSelected());
                }
                break;
            case ALL:
                ModuleCollection modules = GUI.getModules();

                for (OutputObjectsP objectName:modules.getAvailableObjects(null)) {
                    ObjMeasurementRefCollection refs = modules.getObjectMeasurementRefs(objectName.getObjectsName());
                    for (SummaryRef ref:refs.values()) setStates(ref);
                }

                for (OutputImageP imageName:modules.getAvailableImages(null)) {
                    ImageMeasurementRefCollection refs = modules.getImageMeasurementRefs(imageName.getImageName());
                    for (ExportableRef ref:refs.values()) ref.setExportIndividual(isSelected());
                }

                MetadataRefCollection metadataRefs = modules.getMetadataRefs();
                for (ExportableRef ref:metadataRefs.values()) ref.setExportIndividual(isSelected());

                break;
        }

        GUI.updateParameters();

    }
}