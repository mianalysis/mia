package io.github.mianalysis.mia.gui.regions.parameterlist;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.abstrakt.ExportableRef;
import io.github.mianalysis.mia.object.refs.abstrakt.SummaryRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;

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
                Modules modules = GUI.getModules();

                for (OutputObjectsP objectName:modules.getAvailableObjects(null)) {
                    ObjMeasurementRefs refs = modules.getObjectMeasurementRefs(objectName.getObjectsName());
                    for (SummaryRef ref:refs.values()) setStates(ref);
                }

                for (OutputImageP imageName:modules.getAvailableImages(null)) {
                    ImageMeasurementRefs refs = modules.getImageMeasurementRefs(imageName.getImageName());
                    for (ExportableRef ref:refs.values()) ref.setExportIndividual(isSelected());
                }

                MetadataRefs metadataRefs = modules.getMetadataRefs();
                for (ExportableRef ref:metadataRefs.values()) ref.setExportIndividual(isSelected());

                break;
        }

        GUI.updateParameters();

    }
}