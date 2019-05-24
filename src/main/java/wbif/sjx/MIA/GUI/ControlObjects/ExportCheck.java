package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ImageTypeConverter;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputObjectsP;
import wbif.sjx.MIA.Object.References.Abstract.Ref;
import wbif.sjx.MIA.Object.References.Abstract.SummaryRef;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen Cross on 02/12/2017.
 */
public class ExportCheck extends JCheckBox implements ActionListener {
    public enum Statistic {INDIVIDUAL, MEAN, MIN, MAX, SUM, STD}
    public enum Type {SINGLE, ALL};


    private Ref reference;
    private Statistic statistic;
    private Type type;


    public ExportCheck(Ref reference, Statistic statistic, Type type) {
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
                    for (Ref ref:refs.values()) ref.setExportIndividual(isSelected());
                }

                MetadataRefCollection metadataRefs = modules.getMetadataRefs();
                for (Ref ref:metadataRefs.values()) ref.setExportIndividual(isSelected());

                RelationshipRefCollection relationshipRefs = modules.getRelationshipRefs();
                for (SummaryRef ref:relationshipRefs.values()) setStates(ref);

                break;
        }

        GUI.populateModuleParameters();

    }
}