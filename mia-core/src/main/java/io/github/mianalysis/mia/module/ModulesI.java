package io.github.mianalysis.mia.module;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.refs.collections.Refs;

/**
 * Created by sc13967 on 03/05/2017.
 */
public interface ModulesI extends Refs<ModuleI>, Iterable<ModuleI> {
    public boolean execute(WorkspaceI workspace, boolean clearMemoryAtEnd);

    public ArrayList<ModuleI> getModules();

    public void removeAllModules();

    public String getAnalysisFilename();

    public void setAnalysisFilename(String analysisFilename);

    public ModuleI getModuleByID(String ID);

    public Parameter getImageSource(String imageName, ModuleI cutoffModule);

    public Parameter getObjectSource(String objectName, ModuleI cutoffModule);

    public boolean objectsExportMeasurements(String objectName);

    public boolean objectsExportMetadata(String objectName);

    public boolean hasModuleMatchingType(Class<? extends ModuleI> clazz);

    public ImageMeasurementRefs getImageMeasurementRefs(String imageName, @Nullable ModuleI cutoffModule);

    public ObjMeasurementRefs getObjectMeasurementRefs(String objectName, @Nullable ModuleI cutoffModule);
    
    public ObjMetadataRefs getObjectMetadataRefs(String objectName, @Nullable ModuleI cutoffModule);

    public MetadataRefs getMetadataRefs(@Nullable ModuleI cutoffModule);

    public ParentChildRefs getParentChildRefs(@Nullable ModuleI cutoffModule);

    public PartnerRefs getPartnerRefs(@Nullable ModuleI cutoffModule);

    public <T extends Parameter> LinkedHashSet<T> getParametersMatchingType(Class<T> type, @Nullable ModuleI cutoffModule);

    public <T extends OutputObjectsP> LinkedHashSet<OutputObjectsP> getAvailableObjectsMatchingClass(ModuleI cutoffModule,
            Class<T> objectClass, boolean ignoreRemoved);

    public LinkedHashSet<OutputObjectsP> getAvailableObjects(@Nullable ModuleI cutoffModule, boolean ignoreRemoved);

    public LinkedHashSet<OutputImageP> getAvailableImages(@Nullable ModuleI cutoffModule, boolean ignoreRemoved);

    public boolean hasVisibleParameters();

    public InputControl getInputControl();

    public void setInputControl(InputControl inputControl);

    public OutputControl getOutputControl();

    public void setOutputControl(OutputControl outputControl);

    public void reorder(int[] fromIndices, int toIndex);

    // public void reorderByModules(Module[] modulesToMove, ModuleI moduleToFollow);

    public void insert(ModulesI modulesToInsert, int toIndex);

    public ModulesI duplicate(boolean copyIDs);


    // From List

    public boolean addAll(ModulesI modules);

    public boolean removeAll(ModulesI modules);

    public boolean remove(ModuleI module);

    public int size();

    public void addAtIndex(int index, ModuleI module);

    public ModuleI getAtIndex(int idx);

    public ModuleI removeAtIndex(int idx);

    public void clear();

    public int indexOf(ModuleI module);

}
