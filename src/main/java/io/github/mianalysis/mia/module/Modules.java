// TODO: Could addRef optional argument to getParametersMatchingType for the removal type (i.e. if it matches type 1 addRef
// to the list, but if it matches type 2 remove the same parameter from the list.  Would need to compare Parameters for
// value.

package io.github.mianalysis.mia.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.RemovedImageP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.objects.RemovedObjectsP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.refs.collections.Refs;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class Modules extends ArrayList<Module> implements Refs<Module> {
    /**
     *
     */
    private static final long serialVersionUID = -2862089290555674650L;
    private InputControl inputControl = new InputControl(this);
    private OutputControl outputControl = new OutputControl(this);

    public Module getModuleByID(String ID) {
        for (Module module : this) {
            if (module.getModuleID().equals(ID))
                return module;
        }

        // Return null if no module found
        return null;

    }

    public ImageMeasurementRefs getImageMeasurementRefs(String imageName) {
        return getImageMeasurementRefs(imageName, null);
    }

    public ImageMeasurementRefs getImageMeasurementRefs(String imageName, Module cutoffModule) {
        ImageMeasurementRefs measurementRefs = new ImageMeasurementRefs();

        addImageMeasurementRefs(inputControl, measurementRefs, imageName);
        addImageMeasurementRefs(outputControl, measurementRefs, imageName);

        // Iterating over all modules, collecting any measurements for the current image
        for (Module module : this) {
            if (module == cutoffModule)
                break;
            if (!module.isEnabled() | !module.isRunnable())
                continue;
            addImageMeasurementRefs(module, measurementRefs, imageName);
        }

        return measurementRefs;

    }

    void addImageMeasurementRefs(Module module, ImageMeasurementRefs measurementRefs, String imageName) {
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

    public ObjMeasurementRefs getObjectMeasurementRefs(String objectName) {
        return getObjectMeasurementRefs(objectName, null);

    }

    public ObjMeasurementRefs getObjectMeasurementRefs(String objectName, Module cutoffModule) {
        ObjMeasurementRefs measurementRefs = new ObjMeasurementRefs();
        
        // If this is a distant relative there will be "//" in the name that need to be
        // removed
        if (objectName.contains("//"))
            objectName = objectName.substring(objectName.lastIndexOf("//") + 3);

        addObjectMeasurementRefs(inputControl, measurementRefs, objectName);
        addObjectMeasurementRefs(outputControl, measurementRefs, objectName);

        // Iterating over all modules, collecting any measurements for the current
        // objects
        for (Module module : this) {
            if (module == cutoffModule)
                break;
            if (!module.isEnabled() | !module.isRunnable())
                continue;
            addObjectMeasurementRefs(module, measurementRefs, objectName);
        }

        return measurementRefs;

    }

    public boolean objectsExportMeasurements(String objectName) {
        ObjMeasurementRefs refCollection = getObjectMeasurementRefs(objectName);

        for (ObjMeasurementRef ref : refCollection.values()) {
            if (ref.isExportIndividual() && ref.isExportGlobal())
                return true;
        }

        return false;

    }

    void addObjectMeasurementRefs(Module module, ObjMeasurementRefs measurementRefs, String objectName) {
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

    public MetadataRefs getMetadataRefs() {
        return getMetadataRefs(null);

    }

    public MetadataRefs getMetadataRefs(Module cutoffModule) {
        MetadataRefs metadataRefs = new MetadataRefs();

        addMetadataRefs(inputControl, metadataRefs);
        addMetadataRefs(outputControl, metadataRefs);

        // Iterating over all modules, collecting any measurements for the current
        // objects
        for (Module module : this) {
            if (module == cutoffModule)
                break;
            addMetadataRefs(module, metadataRefs);
        }

        return metadataRefs;

    }

    void addMetadataRefs(Module module, MetadataRefs metadataRefs) {
        if (!module.isEnabled())
            return;

        MetadataRefs currentMetadataReferences = module.updateAndGetMetadataReferences();
        if (currentMetadataReferences == null)
            return;

        metadataRefs.putAll(currentMetadataReferences);

    }

    public ParentChildRefs getParentChildRefs() {
        return getParentChildRefs(null);

    }

    public ParentChildRefs getParentChildRefs(Module cutoffModule) {
        ParentChildRefs parentChildRefs = new ParentChildRefs();

        addParentChildRefs(inputControl, parentChildRefs);
        addParentChildRefs(outputControl, parentChildRefs);

        for (Module module : this) {
            if (module == cutoffModule)
                break;
            if (!module.isEnabled() | !module.isRunnable())
                continue;

            addParentChildRefs(module, parentChildRefs);

        }

        return parentChildRefs;

    }

    void addParentChildRefs(Module module, ParentChildRefs parentChildRefs) {
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

    public PartnerRefs getPartnerRefs(Module cutoffModule) {
        PartnerRefs partnerRefs = new PartnerRefs();

        addPartnerRefs(inputControl, partnerRefs);
        addPartnerRefs(outputControl, partnerRefs);

        for (Module module : this) {
            if (module == cutoffModule)
                break;
            if (!module.isEnabled() | !module.isRunnable())
                continue;

            addPartnerRefs(module, partnerRefs);

        }

        return partnerRefs;

    }

    void addPartnerRefs(Module module, PartnerRefs partnerRefs) {
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
    public <T extends Parameter> LinkedHashSet<T> getParametersMatchingType(Class<T> type, Module cutoffModule) {
        LinkedHashSet<T> parameters = new LinkedHashSet<>();

        for (Module module : this) {
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

    public Parameter getObjectSource(String objectName, Module cutoffModule) {
        Parameter sourceParameter = null;

        for (Module module : this) {
            if (module == cutoffModule)
                break;

            // Get the added and removed images
            LinkedHashSet<OutputObjectsP> addedObjects = module.getParametersMatchingType(OutputObjectsP.class);
            if (addedObjects == null)
                continue;
                
            // Find most recent instance of this object being created
            for (OutputObjectsP addedObject : addedObjects) {
                if (addedObject.getValue().equals(objectName))
                    sourceParameter = addedObject;
            }
        }

        return sourceParameter;

    }

    public <T extends OutputObjectsP> LinkedHashSet<OutputObjectsP> getAvailableObjects(Module cutoffModule,
            Class<T> objectClass, boolean ignoreRemoved) {
        LinkedHashSet<OutputObjectsP> objects = new LinkedHashSet<>();

        for (Module module : this) {
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

            for (Parameter removedImage : removedObjects) {
                String removeObjectName = removedImage.getRawStringValue();
                objects.removeIf(outputObjectP -> outputObjectP.getObjectsName().equals(removeObjectName));
            }
        }

        return objects;

    }

    public <T extends OutputObjectsP> LinkedHashSet<OutputObjectsP> getAvailableObjects(Module cutoffModule,
            Class<T> objectClass) {
        return getAvailableObjects(cutoffModule, objectClass, true);
    }

    public LinkedHashSet<OutputObjectsP> getAvailableObjects(Module cutoffModule, boolean ignoreRemoved) {
        return getAvailableObjects(cutoffModule, OutputObjectsP.class, ignoreRemoved);
    }

    public LinkedHashSet<OutputObjectsP> getAvailableObjects(Module cutoffModule) {
        return getAvailableObjects(cutoffModule, OutputObjectsP.class, true);
    }

    public Parameter getImageSource(String imageName, Module cutoffModule) {
        Parameter sourceParameter = null;

        for (Module module : this) {
            if (module == cutoffModule)
                break;

            // Get the added and removed images
            LinkedHashSet<OutputImageP> addedImages = module.getParametersMatchingType(OutputImageP.class);

            // Find most recent instance of this object being created
            for (OutputImageP addedImage : addedImages) {
                if (addedImage.getValue().equals(imageName))
                    sourceParameter = addedImage;
            }
        }

        return sourceParameter;

    }

    public LinkedHashSet<OutputImageP> getAvailableImages(Module cutoffModule) {
        return getAvailableImages(cutoffModule, true);
    }

    public LinkedHashSet<OutputImageP> getAvailableImages(Module cutoffModule, boolean ignoreRemoved) {
        LinkedHashSet<OutputImageP> images = new LinkedHashSet<>();

        for (Module module : this) {
            if (module == cutoffModule)
                break;

            // Get the added and removed images
            LinkedHashSet<OutputImageP> addedImages = module.getParametersMatchingType(OutputImageP.class);
            LinkedHashSet<RemovedImageP> removedImages = module.getParametersMatchingType(RemovedImageP.class);

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
        }

        return images;

    }

    public boolean hasModuleMatchingType(Class<? extends Module> clazz) {
        for (Module module : this) {
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

        for (Module module : this) {
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
        Modules newModules = new Modules();
        for (int idx = 0; idx < inIdx.size() + fromIndices.length + 1; idx++) {
            // If this is the target, move the relevant indices, else move the current value
            if (idx == toIndex) {
                for (int toMoveIdx : toMove)
                    newModules.add(get(toMoveIdx));
            }

            if (idx < size() & !toMove.contains(idx)) {
                newModules.add(get(idx));
            }
        }

        removeAll(this);
        addAll(newModules);

    }

    public void reorder(Module[] modulesToMove, Module moduleToFollow) {
        int[] fromIndices = new int[modulesToMove.length];
        for (int i = 0; i < modulesToMove.length; i++) {
            fromIndices[i] = indexOf(modulesToMove[i]);
        }

        int toIndex;
        if (moduleToFollow == null)
            toIndex = 0;
        else
            toIndex = indexOf(moduleToFollow) + 1;

        reorder(fromIndices, toIndex);

    }

    public void insert(Modules modulesToInsert, int toIndex) {
        // Iterating over all input indices, when we get to the target index, add the
        // moved values
        Modules newModules = new Modules();
        for (Module module : this) {
            int idx = indexOf(module);

            // Adding in the module at this location
            newModules.add(module);

            // If this is where the modules should go, add them in
            if (idx == toIndex)
                newModules.addAll(modulesToInsert);

        }

        removeAll(this);
        addAll(newModules);
    }

    public Modules duplicate() {
        Modules copyModules = new Modules();

        copyModules.setInputControl((InputControl) inputControl.duplicate(copyModules));
        copyModules.setOutputControl((OutputControl) outputControl.duplicate(copyModules));

        for (Module module : values())
            copyModules.add(module.duplicate(copyModules));

        return copyModules;

    }

    @Override
    public Collection<Module> values() {
        return this;
    }
}
