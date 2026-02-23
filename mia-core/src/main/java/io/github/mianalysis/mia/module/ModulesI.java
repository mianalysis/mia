package io.github.mianalysis.mia.module;

import java.util.ArrayList;
import java.util.Collection;
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
public interface ModulesI extends Refs<Module>, Iterable<Module> {
    public boolean execute(WorkspaceI workspace, boolean clearMemoryAtEnd);

    public ArrayList<Module> getModules();

    public void removeAllModules();

    public String getAnalysisFilename();

    public void setAnalysisFilename(String analysisFilename);

    public Module getModuleByID(String ID);

    public Parameter getImageSource(String imageName, Module cutoffModule);

    public Parameter getObjectSource(String objectName, Module cutoffModule);

    public boolean objectsExportMeasurements(String objectName);

    public boolean objectsExportMetadata(String objectName);

    public boolean hasModuleMatchingType(Class<? extends Module> clazz);

    public ImageMeasurementRefs getImageMeasurementRefs(String imageName, @Nullable Module cutoffModule);

    public ObjMeasurementRefs getObjectMeasurementRefs(String objectName, @Nullable Module cutoffModule);
    
    public ObjMetadataRefs getObjectMetadataRefs(String objectName, @Nullable Module cutoffModule);

    public MetadataRefs getMetadataRefs(@Nullable Module cutoffModule);

    public ParentChildRefs getParentChildRefs(@Nullable Module cutoffModule);

    public PartnerRefs getPartnerRefs(@Nullable Module cutoffModule);

    public <T extends Parameter> LinkedHashSet<T> getParametersMatchingType(Class<T> type, @Nullable Module cutoffModule);

    public <T extends OutputObjectsP> LinkedHashSet<OutputObjectsP> getAvailableObjectsMatchingClass(Module cutoffModule,
            Class<T> objectClass, boolean ignoreRemoved);

    public LinkedHashSet<OutputObjectsP> getAvailableObjects(@Nullable Module cutoffModule, boolean ignoreRemoved);

    public LinkedHashSet<OutputImageP> getAvailableImages(@Nullable Module cutoffModule, boolean ignoreRemoved);

    public boolean hasVisibleParameters();

    public InputControl getInputControl();

    public void setInputControl(InputControl inputControl);

    public OutputControl getOutputControl();

    public void setOutputControl(OutputControl outputControl);

    public void reorder(int[] fromIndices, int toIndex);

    // public void reorderByModules(Module[] modulesToMove, Module moduleToFollow);

    public void insert(ModulesI modulesToInsert, int toIndex);

    public ModulesI duplicate(boolean copyIDs);


    // From List

    public boolean addAll(ModulesI modules);

    public boolean removeAll(ModulesI modules);

    public boolean remove(Module module);

    public int size();

    public void addAtIndex(int index, Module module);

    public Module getAtIndex(int idx);

    public Module removeAtIndex(int idx);

    public void clear();

    public int indexOf(Module module);

}
