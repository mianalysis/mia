package io.github.mianalysis.mia.module;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.macro.MacroHandler;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.module.script.AbstractMacroRunner;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.RemovableInputImageP;
import io.github.mianalysis.mia.object.parameters.RemovedImageP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.objects.RemovedObjectsP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.logging.LogRenderer;
import io.github.mianalysis.mia.process.logging.ProgressBar;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class Modules implements ModulesI {
    private InputControl inputControl = new InputControl(this);
    private OutputControl outputControl = new OutputControl(this);
    private String analysisFilename = "";
    private ArrayList<ModuleI> modules = new ArrayList<>();

    /*
     * The method that gets called by the AnalysisRunner. This shouldn't have any
     * user interaction elements
     */
    public boolean execute(WorkspaceI workspace, boolean clearMemoryAtEnd) {
        double t1 = System.currentTimeMillis();

        MIA.log.writeDebug("Processing file \"" + workspace.getMetadata().getFile().getAbsolutePath() + "\"");

        // Setting the MacroHandler to the current workspace (only if macro modules are
        // present)
        if (hasModuleMatchingType(AbstractMacroRunner.class)) {
            MacroHandler.setWorkspace(workspace);
            MacroHandler.setModules(this);
        }

        // Running through modules
        Status status = Status.PASS;

        for (int i = 0; i < size(); i++) {
            ModuleI module = getAtIndex(i);

            if (Thread.currentThread().isInterrupted())
                break;

            if (status == Status.PASS && module.isEnabled() && module.isRunnable()) {
                status = module.execute(workspace);
                workspace.setStatus(status);

                switch (status) {
                    case PASS:
                        break;
                    case FAIL:
                        MIA.log.writeWarning("Analysis failed for file \"" + workspace.getMetadata().getFile()
                                + "\" (series " + workspace.getMetadata().getSeriesNumber() + ") by module \""
                                + module.getName() + "\" (\"" + module.getNickname() + "\").");
                        break;
                    case REDIRECT:
                        // Getting index of module before one to move to
                        ModuleI redirectModule = getModuleByID(module.getRedirectModuleID(workspace));
                        if (redirectModule == null)
                            break;
                        i = indexOf(redirectModule) - 1;
                        status = Status.PASS;
                        break;
                    case TERMINATE:
                        MIA.log.writeWarning("Analysis terminated early for file \"" + workspace.getMetadata().getFile()
                                + "\" (series " + workspace.getMetadata().getSeriesNumber() + ") by module \""
                                + module.getName() + "\" (\"" + module.getNickname() + "\").");
                        break;
                    case TERMINATE_SILENT:
                        break;
                }
            }

            // Updating progress bar
            double fractionComplete = ((double) (i + 1)) / ((double) size());
            workspace.setProgress(fractionComplete);

            if (MIA.isHeadless())
                LogRenderer.setProgress(workspace.getWorkspaces());
            else
                ProgressBar.update(workspace.getWorkspaces().getOverallProgress());

        }

        // We're only interested in the measurements now, so clearing images and object
        // coordinates
        if (clearMemoryAtEnd) {
            workspace.clearAllImages(true);
            workspace.clearAllObjects(true);
        }
        // If enabled, write the current memory usage to the console
        if (MIA.getMainRenderer().isWriteEnabled(LogRenderer.Level.MEMORY)) {
            double totalMemory = Runtime.getRuntime().totalMemory();
            double usedMemory = totalMemory - Runtime.getRuntime().freeMemory();
            String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

            DecimalFormat df1 = new DecimalFormat("#.0");
            DecimalFormat df3 = new DecimalFormat("0.000");

            double t2 = System.currentTimeMillis();
            double elapsed = (t2-t1)/1000;

            String memoryMessage = df1.format(usedMemory * 1E-6) + "/" + df1.format(totalMemory * 1E-6) + " MB"
                    + ", ANALYSIS COMPLETE , ELAPSED: "+df3.format(elapsed)+" s, DATE/TIME: " + dateTime + ", FILE: \""
                    + workspace.getMetadata().getFile() + "\"";

            MIA.log.write(memoryMessage, LogRenderer.Level.MEMORY);

        }

        return true;

    }

    public void removeAllModules() {
        clear();

    }

    public String getAnalysisFilename() {
        return analysisFilename;
    }

    public void setAnalysisFilename(String analysisFilename) {
        this.analysisFilename = analysisFilename;
    }

    public ModuleI getModuleByID(String ID) {
        for (ModuleI module : this) {
            if (module.getModuleID().equals(ID))
                return module;
        }

        // Return null if no module found
        return null;

    }

    public ImageMeasurementRefs getImageMeasurementRefs(String imageName, ModuleI cutoffModule) {
        ImageMeasurementRefs measurementRefs = new ImageMeasurementRefs();

        addImageMeasurementRefs(inputControl, measurementRefs, imageName);
        addImageMeasurementRefs(outputControl, measurementRefs, imageName);

        // Iterating over all modules, collecting any measurements for the current image
        for (ModuleI module : this) {
            if (module == cutoffModule)
                break;
            if (!module.isEnabled() | !module.isRunnable())
                continue;
            addImageMeasurementRefs(module, measurementRefs, imageName);
        }

        return measurementRefs;

    }

    void addImageMeasurementRefs(ModuleI module, ImageMeasurementRefs measurementRefs, String imageName) {
        if (!module.isEnabled())
            return;
        ImageMeasurementRefs currentMeasurementRefs = module.updateAndGetImageMeasurementRefs();

        if (currentMeasurementRefs == null)
            return;

        for (ImageMeasurementRef measurementRef : currentMeasurementRefs.values()) {
            if (measurementRef.getImageName() == null)
                continue;
            if (measurementRef.getImageName().equals(imageName))
                measurementRefs.put(measurementRef.getName(), measurementRef);

        }
    }

    public ObjMeasurementRefs getObjectMeasurementRefs(String objectName, ModuleI cutoffModule) {
        ObjMeasurementRefs measurementRefs = new ObjMeasurementRefs();

        // If this is a distant relative there will be "//" in the name that need to be
        // removed
        if (objectName.contains("//"))
            objectName = objectName.substring(objectName.lastIndexOf("//") + 3);

        addObjectMeasurementRefs(inputControl, measurementRefs, objectName);
        addObjectMeasurementRefs(outputControl, measurementRefs, objectName);

        // Iterating over all modules, collecting any measurements for the current
        // objects
        for (ModuleI module : this) {
            if (module == cutoffModule)
                break;
            if (!module.isEnabled() | !module.isRunnable())
                continue;
            addObjectMeasurementRefs(module, measurementRefs, objectName);
        }

        return measurementRefs;

    }

    public boolean objectsExportMeasurements(String objectName) {
        ObjMeasurementRefs refCollection = getObjectMeasurementRefs(objectName, null);

        for (ObjMeasurementRef ref : refCollection.values()) {
            if (ref.isExportIndividual() && ref.isExportGlobal())
                return true;
        }

        return false;

    }

    void addObjectMeasurementRefs(ModuleI module, ObjMeasurementRefs measurementRefs, String objectName) {
        if (!module.isEnabled())
            return;
        ObjMeasurementRefs currentMeasurementRefs = module.updateAndGetObjectMeasurementRefs();
        if (currentMeasurementRefs == null)
            return;

        for (ObjMeasurementRef ref : currentMeasurementRefs.values()) {
            if (ref.getObjectsName() == null)
                continue;
            if (ref.getObjectsName().equals(objectName))
                measurementRefs.put(ref.getName(), ref);

        }
    }

    public ObjMetadataRefs getObjectMetadataRefs(String objectName, ModuleI cutoffModule) {
        ObjMetadataRefs metadataRefs = new ObjMetadataRefs();

        // If this is a distant relative there will be "//" in the name that need to be
        // removed
        if (objectName.contains("//"))
            objectName = objectName.substring(objectName.lastIndexOf("//") + 3);

        addObjectMetadataRefs(inputControl, metadataRefs, objectName);
        addObjectMetadataRefs(outputControl, metadataRefs, objectName);

        // Iterating over all modules, collecting any measurements for the current
        // objects
        for (ModuleI module : this) {
            if (module == cutoffModule)
                break;
            if (!module.isEnabled() | !module.isRunnable())
                continue;
            addObjectMetadataRefs(module, metadataRefs, objectName);
        }

        return metadataRefs;

    }

    public boolean objectsExportMetadata(String objectName) {
        ObjMetadataRefs refCollection = getObjectMetadataRefs(objectName, null);

        for (ObjMetadataRef ref : refCollection.values()) {
            if (ref.isExportIndividual() && ref.isExportGlobal())
                return true;
        }

        return false;

    }

    void addObjectMetadataRefs(ModuleI module, ObjMetadataRefs metadataRefs, String objectName) {
        if (!module.isEnabled())
            return;
        ObjMetadataRefs currentMetadataRefs = module.updateAndGetObjectMetadataRefs();
        if (currentMetadataRefs == null)
            return;

        for (ObjMetadataRef ref : currentMetadataRefs.values()) {
            if (ref.getObjectsName() == null)
                continue;
            if (ref.getObjectsName().equals(objectName))
                metadataRefs.put(ref.getName(), ref);

        }
    }

    public MetadataRefs getMetadataRefs() {
        return getMetadataRefs(null);

    }

    public MetadataRefs getMetadataRefs(ModuleI cutoffModule) {
        MetadataRefs metadataRefs = new MetadataRefs();

        addMetadataRefs(inputControl, metadataRefs);
        addMetadataRefs(outputControl, metadataRefs);

        // Iterating over all modules, collecting any measurements for the current
        // objects
        for (ModuleI module : this) {
            if (module == cutoffModule)
                break;
            addMetadataRefs(module, metadataRefs);
        }

        return metadataRefs;

    }

    void addMetadataRefs(ModuleI module, MetadataRefs metadataRefs) {
        if (!module.isEnabled())
            return;

        MetadataRefs currentMetadataReferences = module.updateAndGetMetadataReferences();
        if (currentMetadataReferences == null)
            return;

        metadataRefs.putAll(currentMetadataReferences);

    }

    public ParentChildRefs getParentChildRefs(@Nullable ModuleI cutoffModule) {
        ParentChildRefs parentChildRefs = new ParentChildRefs();

        addParentChildRefs(inputControl, parentChildRefs);
        addParentChildRefs(outputControl, parentChildRefs);

        for (ModuleI module : this) {
            if (module == cutoffModule)
                break;
            if (!module.isEnabled() | !module.isRunnable())
                continue;

            addParentChildRefs(module, parentChildRefs);

        }

        return parentChildRefs;

    }

    void addParentChildRefs(ModuleI module, ParentChildRefs parentChildRefs) {
        if (!module.isEnabled())
            return;

        ParentChildRefs currentParentChildRefs = module.updateAndGetParentChildRefs();
        if (currentParentChildRefs == null)
            return;

        parentChildRefs.putAll(currentParentChildRefs);

    }

    public PartnerRefs getPartnerRefs() {
        return getPartnerRefs(null);

    }

    public PartnerRefs getPartnerRefs(ModuleI cutoffModule) {
        PartnerRefs partnerRefs = new PartnerRefs();

        addPartnerRefs(inputControl, partnerRefs);
        addPartnerRefs(outputControl, partnerRefs);

        for (ModuleI module : this) {
            if (module == cutoffModule)
                break;
            if (!module.isEnabled() | !module.isRunnable())
                continue;

            addPartnerRefs(module, partnerRefs);

        }

        return partnerRefs;

    }

    void addPartnerRefs(ModuleI module, PartnerRefs partnerRefs) {
        if (!module.isEnabled())
            return;

        PartnerRefs currentPartnerRefs = module.updateAndGetPartnerRefs();
        if (currentPartnerRefs == null)
            return;

        partnerRefs.addAll(currentPartnerRefs);

    }

    /*
     * Returns an LinkedHashSet of all parameters of a specific type
     */
    public <T extends Parameter> LinkedHashSet<T> getParametersMatchingType(Class<T> type, ModuleI cutoffModule) {
        LinkedHashSet<T> parameters = new LinkedHashSet<>();

        for (ModuleI module : this) {
            // If the current module is the cutoff the loop terminates. This prevents the
            // system offering measurements
            // that are created after this module or are currently unavailable.
            if (module == cutoffModule)
                break;
            if (!module.isEnabled())
                continue;
            if (!module.isRunnable())
                continue;

            // Running through all parameters, adding all images to the list
            Parameters currParameters = module.updateAndGetParameters();
            for (Parameter currParameter : currParameters.values()) {
                if (type.isInstance(currParameter)) {
                    parameters.add((T) currParameter);
                }
            }
        }

        return parameters;

    }

    public <T extends Parameter> LinkedHashSet<T> getParametersMatchingType(Class<T> type) {
        return getParametersMatchingType(type, null);
    }

    public Parameter getObjectSource(String objectName, ModuleI cutoffModule) {
        Parameter sourceParameter = null;

        for (ModuleI module : this) {
            if (module == cutoffModule)
                break;

            // Get the added and removed images
            LinkedHashSet<OutputObjectsP> addedObjects = module.getParametersMatchingType(OutputObjectsP.class);
            if (addedObjects == null)
                continue;

            // Find most recent instance of this object being created
            for (OutputObjectsP addedObject : addedObjects)
                if (addedObject.getValue(null).equals(objectName))
                    sourceParameter = addedObject;

        }

        return sourceParameter;

    }

    public <T extends OutputObjectsP> LinkedHashSet<OutputObjectsP> getAvailableObjectsMatchingClass(ModuleI cutoffModule,
            Class<T> objectClass, boolean ignoreRemoved) {
        LinkedHashSet<OutputObjectsP> objects = new LinkedHashSet<>();

        for (ModuleI module : this) {
            if (module == cutoffModule)
                break;

            // Get the added and removed objects
            LinkedHashSet<T> addedObjects = module.getParametersMatchingType(objectClass);
            LinkedHashSet<RemovedObjectsP> removedObjects = module.getParametersMatchingType(RemovedObjectsP.class);

            // Adding new objects
            if (addedObjects != null)
                objects.addAll(addedObjects);

            // Removing objects
            if (!ignoreRemoved || removedObjects == null)
                continue;

            for (Parameter removedObject : removedObjects) {
                String removeObjectName = removedObject.getRawStringValue();
                objects.removeIf(outputObjectP -> outputObjectP.getObjectsName().equals(removeObjectName));
            }
        }

        return objects;

    }

    public LinkedHashSet<OutputObjectsP> getAvailableObjects(ModuleI cutoffModule, boolean ignoreRemoved) {
        return getAvailableObjectsMatchingClass(cutoffModule, OutputObjectsP.class, ignoreRemoved);
    }

    public Parameter getImageSource(String imageName, ModuleI cutoffModule) {
        Parameter sourceParameter = null;

        for (ModuleI module : this) {
            if (module == cutoffModule)
                break;

            // Get the added and removed images
            LinkedHashSet<OutputImageP> addedImages = module.getParametersMatchingType(OutputImageP.class);

            // Find most recent instance of this object being created
            for (OutputImageP addedImage : addedImages)
                if (addedImage.getValue(null).equals(imageName))
                    sourceParameter = addedImage;

        }

        return sourceParameter;

    }

    public LinkedHashSet<OutputImageP> getAvailableImages(ModuleI cutoffModule, boolean ignoreRemoved) {
        LinkedHashSet<OutputImageP> images = new LinkedHashSet<>();

        for (ModuleI module : this) {
            if (module == cutoffModule)
                break;

            // Get the added and removed images
            LinkedHashSet<OutputImageP> addedImages = module.getParametersMatchingType(OutputImageP.class);
            LinkedHashSet<RemovedImageP> removedImages = module.getParametersMatchingType(RemovedImageP.class);
            LinkedHashSet<RemovableInputImageP> removableImages = module
                    .getParametersMatchingType(RemovableInputImageP.class);

            // Adding new images
            if (addedImages != null)
                images.addAll(addedImages);

            // Removing images
            if (!ignoreRemoved || removedImages == null)
                continue;

            for (Parameter removedImage : removedImages) {
                String removeImageName = removedImage.getRawStringValue();
                images.removeIf(outputImageP -> outputImageP.getImageName().equals(removeImageName));
            }

            for (RemovableInputImageP removeableImage : removableImages) {
                if (!removeableImage.isRemoveInputImages())
                    continue;

                String removeImageName = removeableImage.getRawStringValue();
                images.removeIf(outputImageP -> outputImageP.getImageName().equals(removeImageName));
            }
        }

        return images;

    }

    public boolean hasModuleMatchingType(Class<? extends ModuleI> clazz) {
        for (ModuleI module : this) {
            if (clazz.isAssignableFrom(module.getClass()))
                return true;
        }

        return false;

    }

    public boolean hasVisibleParameters() {
        if (inputControl.hasVisibleParameters())
            return true;
        if (outputControl.hasVisibleParameters())
            return true;

        for (ModuleI module : this) {
            if (module.hasVisibleParameters())
                return true;
        }

        return false;

    }

    public InputControl getInputControl() {
        return inputControl;
    }

    public void setInputControl(InputControl inputControl) {
        this.inputControl = inputControl;
    }

    public OutputControl getOutputControl() {
        return outputControl;
    }

    public void setOutputControl(OutputControl outputControl) {
        this.outputControl = outputControl;
    }

    public void reorder(int[] fromIndices, int toIndex) {
        // Creating a list of initial indices
        ArrayList<Integer> inIdx = new ArrayList<>();
        for (int i = 0; i < size(); i++)
            inIdx.add(i);

        // Creating a list of the indices to move
        ArrayList<Integer> toMove = new ArrayList<>();
        for (int fromIndex : fromIndices)
            toMove.add(fromIndex);

        // Removing the indices to be moved
        inIdx.removeAll(toMove);

        // Iterating over all input indices, when we get to the target index, add the
        // moved values
        ModulesI newModules = new Modules();
        for (int idx = 0; idx < inIdx.size() + fromIndices.length + 1; idx++) {
            // If this is the target, move the relevant indices, else move the current value
            if (idx == toIndex) {
                for (int toMoveIdx : toMove)
                    newModules.add(getAtIndex(toMoveIdx));
            }

            if (idx < size() & !toMove.contains(idx)) {
                newModules.add(getAtIndex(idx));
            }
        }

        removeAll(this);
        addAll(newModules);

    }

    // public void reorderByModules(Module[] modulesToMove, ModuleI moduleToFollow) {
    //     int[] fromIndices = new int[modulesToMove.length];
    //     for (int i = 0; i < modulesToMove.length; i++) {
    //         fromIndices[i] = indexOf(modulesToMove[i]);
    //     }

    //     int toIndex;
    //     if (moduleToFollow == null)
    //         toIndex = 0;
    //     else
    //         toIndex = indexOf(moduleToFollow) + 1;

    //     reorder(fromIndices, toIndex);

    // }

    public void insert(ModulesI modulesToInsert, int toIndex) {
        // Iterating over all input indices, when we get to the target index, add the
        // moved values
        ModulesI newModules = new Modules();
        for (ModuleI module : this) {
            int idx = indexOf(module);

            // Adding in the module at this location
            newModules.add((Module) module);

            // If this is where the modules should go, add them in
            if (idx == toIndex)
                newModules.addAll(modulesToInsert);

        }

        removeAll(this);
        addAll(newModules);

    }

    public ModulesI duplicate(boolean copyIDs) {
        ModulesI copyModules = new Modules();

        copyModules.setInputControl((InputControl) inputControl.duplicate(copyModules, copyIDs));
        copyModules.setOutputControl((OutputControl) outputControl.duplicate(copyModules, copyIDs));
        copyModules.setAnalysisFilename(analysisFilename);

        for (ModuleI module : values())
            copyModules.add((Module) module.duplicate(copyModules, copyIDs));
        
        return copyModules;

    }

    public Collection<ModuleI> values() {
        return modules;
    }

    public ArrayList<ModuleI> getModules() {
        return modules;
    }

    public boolean add(ModuleI module) {
        return modules.add(module);
    }

    public Iterator<ModuleI> iterator() {
        return modules.iterator();
    }

    public int size() {
        return modules.size();
    }

    public ModuleI getAtindex(int idx) {
        return modules.get(idx);
    }

    public int indexOf(ModuleI module) {
        return modules.indexOf(module);
    }

    public boolean addAll(ModulesI modulesToAdd) {
        return modules.addAll(modulesToAdd.getModules());
    }

    public boolean removeAll(ModulesI modulesToRemove) {
        return modules.removeAll(modulesToRemove.getModules());
    }

    public ModuleI getAtIndex(int idx) {
        return modules.get(idx);
    }

    public ModuleI removeAtIndex(int idx) {
        return modules.remove(idx);
    }

    public void clear() {
        modules.clear();
    }

    @Override
    public boolean remove(ModuleI module) {
        return modules.remove(module);
    }

    @Override
    public void addAtIndex(int index, ModuleI module) {
        modules.add(index, module);
    }
}
