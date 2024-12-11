package io.github.mianalysis.mia.module;

import java.util.LinkedHashSet;

import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.MetadataRef;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.refs.ParentChildRef;
import io.github.mianalysis.mia.object.refs.PartnerRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

public interface ModuleI {
    // ABSTRACT METHODS

    /**
     * The module category within MIA in which this module will be placed. We can
     * choose any of the default categories available in
     * io.github.mianalysis.mia.module.Categories or use one created along with this
     * module.
     * 
     * @return The category for this module to be placed in
     */
    Category getCategory();

    /**
     * The version number for this module specified using standard x.x.x semantic
     * versioning format.
     * 
     * @return Version number
     */
    String getVersionNumber();

    /**
     * The method which is run as part of a workflow. This method contains all the
     * code for loading items from the MIA workspace, performing the action of this
     * module and exporting any new items to the workspace.
     * 
     * @param workspace The current workspace containing all available images and
     *                  objects (i.e. those previously output by earlier modules in
     *                  the workflow).
     * @return Exit status
     */
    Status process(WorkspaceI workspace);

    /**
     * Creates an instance of each parameter, each of which is stored in the
     * "parameters" variable of the module. Each new instance of the module will
     * have a new set of parameters. This method runs once, when the module is first
     * created.
     */
    void initialiseParameters();

    /**
     * Returns the currently-active parameters for this module. The returned
     * parameters will change depending on what other parameters are set to. The
     * output of this module determines the parameters that are displayed in the
     * GUI.
     * 
     * @return Currently-active parameters for this module
     */
    Parameters updateAndGetParameters();

    /**
     * Measurements added to any images by this module are reported by adding their
     * reference to an ImageMeasurementRefs collection. When no measurements are
     * added by this module, this method can simply return "null". These references
     * tell downstream modules what measurements are available for each image.
     * Returned references should be the original copies stored in the local
     * "imageMeasurementRefs" object.
     * 
     * @return Image measurement references currently active for this module
     */
    ImageMeasurementRefs updateAndGetImageMeasurementRefs();

    /**
     * Measurements added to any objects by this module are reported by adding their
     * reference to an ObjMeasurementRefs collection. When no measurements are added
     * by this module, this method can simply return "null". These references tell
     * downstream modules what measurements are available for each object of a
     * specific object collection. Returned references should be the original copies
     * stored in the local "objectMeasurementRefs" object.
     * 
     * @return Object measurement references currently active for this module
     */
    ObjMeasurementRefs updateAndGetObjectMeasurementRefs();

    /**
     * Metadata added to any objects by this module are reported by adding their
     * reference to an ObjMetadataRefs collection. When no metadata items are added
     * by this module, this method can simply return "null". These references tell
     * downstream modules what metadata items are available for each object of a
     * specific object collection. Returned references should be the original copies
     * stored in the local "objectMetadataRefs" object.
     * 
     * @return Object metadata references currently active for this module
     */
    ObjMetadataRefs updateAndGetObjectMetadataRefs();

    /**
     * Values added to the workspace's metadata collection by this module are
     * reported by adding their reference to a MetadataRefs collection. When no
     * metadata values are added by this module, this method can simply return
     * "null". Metadata values are single values within a workspace that specify
     * information such as the root filename or series number. These references tell
     * downstream modules what metadata is available. Returned references should be
     * the original copies stored in the local "metadataRefs" object.
     * 
     * @return Metadata references currently active for this module
     */
    MetadataRefs updateAndGetMetadataReferences();

    /**
     * Any parent-child relationships established between objects by this module are
     * reported by adding their reference to a ParentChildRefs collection. When no
     * parent-child relationships are added by this module, this method can simply
     * return "null". These references tell downstream modules what parent-child
     * relationships are available. Returned references should be the original
     * copies stored in the local "parentChildRefs" object.
     * 
     * @return Parent-child relationship references currently active for this module
     */
    ParentChildRefs updateAndGetParentChildRefs();

    /**
     * Any partner-partner relationships established between objects by this module
     * are reported by adding their reference to a PartnerRefs collection. When no
     * partner-partner relationships are added by this module, this method can
     * simply return "null". These references tell downstream modules what
     * partner-partner relationships are available. Returned references should be
     * the original copies stored in the local "partnerRefs" object.
     * 
     * @return Partner relationship references currently active for this module
     */
    PartnerRefs updateAndGetPartnerRefs();

    /**
     * Can be used to perform checks on parameters or other conditions to ensure the
     * module is configured correctly. This runs whenever a workflow is updated
     * (e.g. a parameter in any module is changed).
     * 
     * @return returns true if module checks pass
     */
    boolean verify();

    // PUBLIC METHODS

    Status execute(WorkspaceI workspace);

    void addObjectMeasurementRef(ObjMeasurementRef ref);

    public void addObjectMetadataRef(ObjMetadataRef ref);

    public ImageMeasurementRef getImageMeasurementRef(String name);

    public void addImageMeasurementRef(ImageMeasurementRef ref);

    public ObjMeasurementRef getObjectMeasurementRef(String name);

    public ObjMetadataRef getObjectMetadataRef(String name);

    public MetadataRef getMetadataRef(String name);

    public void addMetadataRef(MetadataRef ref);

    public ParentChildRef getParentChildRef(String parentName, String childName);

    public void addParentChildRef(ParentChildRef ref);

    public void addPartnerRef(PartnerRef ref);

    public <T extends Parameter> T getParameter(String name);

    public Module updateParameterValue(String name, Object value);

    public <T> T getParameterValue(String name, WorkspaceI workspace);

    public void setParameterVisibility(String name, boolean visible);

    public Parameters getAllParameters();

    public boolean invalidParameterIsVisible();

    public <T extends Parameter> LinkedHashSet<T> getParametersMatchingType(Class<T> type);

    public <T extends Parameter> void addParameterGroupParameters(ParameterGroup parameterGroup, Class<T> type,
            LinkedHashSet<T> parameters);

    public Modules getModules();

    public void setModules(Modules modules);

    public boolean hasParameter(String parameterName);

    public String getModuleID();

    public void setModuleID(String moduleID);

    public String getShortDescription();

    public String getNotes();

    public void setNotes(String notes);

    public boolean isEnabled();

    public void setEnabled(boolean enabled);

    public boolean canBeDisabled();

    public void setCanBeDisabled(boolean canBeDisabled);

    public boolean canShowProcessingTitle();

    public void setShowProcessingViewTitle(boolean showProcessingViewTitle);

    public boolean isVerbose();

    public void setVerbose(boolean verboseIn);

    public boolean canShowOutput();

    public void setShowOutput(boolean showOutput);

    public boolean isRunnable();

    public void setRunnable(boolean runnable);

    public boolean isReachable();

    public void setReachable(boolean reachable);

    public boolean isDeprecated();

    public void setDeprecated(boolean deprecated);

    public IL2Support getIL2Support();

    public String getRedirectModuleID(WorkspaceI workspace);

    public void setRedirectModuleID(String redirectModuleID);

    public boolean hasVisibleParameters();

    public Module duplicate(Modules newModules, boolean copyID);

    // PROTECTED METHODS

    public void writeStatus(String message);

    public void writeProgressStatus(int count, int total, String featureBeingProcessed);

}
