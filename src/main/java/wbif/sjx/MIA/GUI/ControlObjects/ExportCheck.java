package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.References.Abstract.ExportableRef;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputObjectsP;
import wbif.sjx.MIA.Object.References.Abstract.MeasurementRef;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen Cross on 02/12/2017.
 */
public class ExportCheck extends JCheckBox implements ActionListener {
    public enum Statistic {INDIVIDUAL, MEAN, MIN, MAX, SUM, STD}
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

    private void setStates(ExportableRef reference) {
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
                setStates(reference);
                break;
            case ALL:
                ModuleCollection modules = GUI.getModules();

                for (OutputObjectsP objectName:modules.getAvailableObjects(null)) {
                    ObjMeasurementRefCollection refs = modules.getObjectMeasurementRefs(objectName.getObjectsName());
                    for (MeasurementRef ref:refs.values()) setStates(ref);
                }

                for (OutputImageP imageName:modules.getAvailableImages(null)) {
                    ImageMeasurementRefCollection refs = modules.getImageMeasurementRefs(imageName.getImageName());
                    for (MeasurementRef ref:refs.values()) setStates(ref);
                }

                MetadataRefCollection metadataRefs = modules.getMetadataRefs();
                for (MetadataRef ref:metadataRefs.values()) setStates(ref);

                RelationshipRefCollection relationshipRefs = modules.getRelationshipRefs();
                for (RelationshipRef ref:relationshipRefs.values()) setStates(ref);


                setStates(reference);
                
                break;
        }

        GUI.populateModuleParameters();

    }
}