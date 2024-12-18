package io.github.mianalysis.mia.module;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.scijava.plugin.SciJavaPlugin;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ij.Prefs;
import io.github.mianalysis.mia.MIA;
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
import io.github.mianalysis.mia.object.refs.abstrakt.Ref;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.logging.LogRenderer;

/**
 * Abstract MIA module. Each module extending this class should perform a
 * defined action such as image filtering, object detection or adding a
 * component to an overlay
 */
public abstract class Module extends Ref implements Comparable, ModuleI {
    protected Modules modules;

    protected Parameters parameters = new Parameters();
    protected ImageMeasurementRefs imageMeasurementRefs = new ImageMeasurementRefs();
    protected ObjMeasurementRefs objectMeasurementRefs = new ObjMeasurementRefs();
    protected ObjMetadataRefs objectMetadataRefs = new ObjMetadataRefs();
    protected MetadataRefs metadataRefs = new MetadataRefs();
    protected ParentChildRefs parentChildRefs = new ParentChildRefs();
    protected PartnerRefs partnerRefs = new PartnerRefs();

    private String moduleID = String.valueOf(hashCode());
    private static boolean verbose = false;
    private String notes = "";
    private boolean enabled = true;
    private boolean canBeDisabled = false;
    private boolean runnable = true;
    private boolean reachable = true;
    protected boolean showOutput = false;
    protected String redirectModuleID = null; // After this module, can redirect to another module
    private boolean showProcessingViewTitle = true;
    protected boolean deprecated = false; // When set to true, this module is marked for future removal
    protected IL2Support il2Support = IL2Support.NONE;

    // CONSTRUCTOR

    /**
     * The module constructor requires us to provide the name of this module.
     * 
     * @param name    The name of this module as seen from within the MIA GUI.
     * @param modules The module constructor, when called from within MIA, provides
     *                all the modules currently in the workflow as an argument.
     */
    public Module(String name, Modules modules) {        
        super(name);
        this.modules = modules;
        initialiseParameters();
    }

    // PUBLIC METHODS

    public Status execute(WorkspaceI workspace) {
        double t1 = System.currentTimeMillis();

        writeStatus("Processing");

        // By default all modules should use this format
        Prefs.blackBackground = true;

        // Running the main module code
        Status status = process(workspace);

        switch (status) {
            case PASS:
                writeStatus("Completed");
                break;
            case TERMINATE:
                writeStatus("Completed (ending analysis early)");
                break;
            case TERMINATE_SILENT:
                break;
            case FAIL:
                writeStatus("Did not complete");
                break;
        }

        // If enabled, write the current memory usage to the console
        if (MIA.getMainRenderer().isWriteEnabled(LogRenderer.Level.MEMORY)) {
            double totalMemory = Runtime.getRuntime().totalMemory();
            double usedMemory = totalMemory - Runtime.getRuntime().freeMemory();
            String dateTime = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss").format(new Date());

            DecimalFormat df1 = new DecimalFormat("#.0");
            DecimalFormat df3 = new DecimalFormat("0.000");

            double t2 = System.currentTimeMillis();
            double elapsed = (t2-t1)/1000;

            String memoryMessage = df1.format(usedMemory * 1E-6) + "/" + df1.format(totalMemory * 1E-6) + " MB"
                    + ", MODULE: \"" + getName() + "\"" + ", ELAPSED: "+df3.format(elapsed)+" s, DATE/TIME: " + dateTime + ", FILE: \""
                    + workspace.getMetadata().getFile() + "\"";

            MIA.log.writeMemory(memoryMessage);

        }

        return status;

    }

    public void addObjectMeasurementRef(ObjMeasurementRef ref) {
        objectMeasurementRefs.add(ref);
    }

    public void addObjectMetadataRef(ObjMetadataRef ref) {
        objectMetadataRefs.add(ref);
    }

    public ImageMeasurementRef getImageMeasurementRef(String name) {
        return imageMeasurementRefs.getOrPut(name);
    }

    public void addImageMeasurementRef(ImageMeasurementRef ref) {
        imageMeasurementRefs.add(ref);
    }

    public ObjMeasurementRef getObjectMeasurementRef(String name) {
        return objectMeasurementRefs.getOrPut(name);
    }

    public ObjMetadataRef getObjectMetadataRef(String name) {
        return objectMetadataRefs.getOrPut(name);
    }

    public MetadataRef getMetadataRef(String name) {
        return metadataRefs.getOrPut(name);
    }

    public void addMetadataRef(MetadataRef ref) {
        metadataRefs.add(ref);
    }

    public ParentChildRef getParentChildRef(String parentName, String childName) {
        return parentChildRefs.getOrPut(parentName, childName);
    }

    public void addParentChildRef(ParentChildRef ref) {
        parentChildRefs.add(ref);
    }

    public void addPartnerRef(PartnerRef ref) {
        partnerRefs.add(ref);
    }

    public <T extends Parameter> T getParameter(String name) {
        return parameters.getParameter(name);
    }

    public Module updateParameterValue(String name, Object value) {
        parameters.updateValue(name, value);
        return this;

    }

    public <T> T getParameterValue(String name, WorkspaceI workspace) {
        return parameters.getParameter(name).getValue(workspace);
    }

    public void setParameterVisibility(String name, boolean visible) {
        parameters.updateVisible(name, visible);
    }

    public Parameters getAllParameters() {
        return parameters;
    }

    public boolean invalidParameterIsVisible() {
        return updateAndGetParameters().invalidParameterIsVisible();

    }

    public <T extends Parameter> LinkedHashSet<T> getParametersMatchingType(Class<T> type) {
        if (!isEnabled())
            return null;
        if (!isRunnable())
            return null;

        // Running through all parameters, adding all images to the list
        LinkedHashSet<T> parameters = new LinkedHashSet<>();
        Parameters currParameters = updateAndGetParameters();
        for (Parameter currParameter : currParameters.values()) {
            if (type.isInstance(currParameter))
                parameters.add((T) currParameter);

            if (currParameter instanceof ParameterGroup)
                addParameterGroupParameters((ParameterGroup) currParameter, type, parameters);

        }

        return parameters;

    }

    public <T extends Parameter> void addParameterGroupParameters(ParameterGroup parameterGroup, Class<T> type,
            LinkedHashSet<T> parameters) {
        LinkedHashMap<Integer, Parameters> collections = parameterGroup.getCollections(true);
        for (Parameters collection : collections.values()) {
            for (Parameter currParameter : collection.values()) {
                if (type.isInstance(currParameter)) {
                    parameters.add((T) currParameter);
                }
                if (currParameter instanceof ParameterGroup) {
                    addParameterGroupParameters((ParameterGroup) currParameter, type, parameters);
                }
            }
        }
    }

    public Modules getModules() {
        return modules;
    }

    public void setModules(Modules modules) {
        this.modules = modules;
    }

    public boolean hasParameter(String parameterName) {
        return parameters.keySet().contains(parameterName);
    }

    public String getModuleID() {
        return moduleID;
    }

    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }

    public String getShortDescription() {
        String des = getDescription();
        if (des.length() == 0)
            return "";

        if (!des.contains("."))
            return des;

        return des.substring(0, des.indexOf(".")) + ".";

    }

    public String getNotes() {
        return notes;

    }

    public void setNotes(String notes) {
        this.notes = notes;

    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean canBeDisabled() {
        return canBeDisabled;
    }

    public void setCanBeDisabled(boolean canBeDisabled) {
        this.canBeDisabled = canBeDisabled;
    }

    public boolean canShowProcessingTitle() {
        return showProcessingViewTitle;
    }

    public void setShowProcessingViewTitle(boolean showProcessingViewTitle) {
        this.showProcessingViewTitle = showProcessingViewTitle;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        Module.verbose = verbose;
    }

    public boolean canShowOutput() {
        return showOutput;
    }

    public void setShowOutput(boolean showOutput) {
        this.showOutput = showOutput;
    }

    public boolean isRunnable() {
        return runnable;
    }

    public void setRunnable(boolean runnable) {
        this.runnable = runnable;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public IL2Support getIL2Support() {
        return il2Support;
    }

    public String getRedirectModuleID(WorkspaceI workspace) {
        return this.redirectModuleID;
    }

    public void setRedirectModuleID(String redirectModuleID) {
        this.redirectModuleID = redirectModuleID;
    }

    public boolean hasVisibleParameters() {
        return updateAndGetParameters().hasVisibleParameters();

    }

    public Module duplicate(Modules newModules, boolean copyID) {
        Constructor constructor;
        Module newModule;
        try {
            constructor = this.getClass().getDeclaredConstructor(Modules.class);
            newModule = (Module) constructor.newInstance(newModules);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException
                | InvocationTargetException e) {
            MIA.log.writeError(e);
            return null;
        }

        if (copyID)
            newModule.setModuleID(getModuleID());
        else
            newModule.setModuleID(String.valueOf(hashCode()));

        newModule.setNickname(getNickname());
        newModule.setEnabled(enabled);
        newModule.setShowOutput(showOutput);
        newModule.setNotes(notes);
        newModule.setCanBeDisabled(canBeDisabled);
        newModule.setShowProcessingViewTitle(showProcessingViewTitle);
        newModule.setRedirectModuleID(redirectModuleID);

        Parameters newParameters = newModule.getAllParameters();
        for (Parameter parameter : parameters.values()) {
            Parameter newParameter = parameter.duplicate(newModule);
            if (newParameter == null)
                continue;
            newParameter.setModule(newModule);
            newParameters.add(newParameter);
        }

        ObjMeasurementRefs newObjMeasurementRefs = newModule.objectMeasurementRefs;
        for (ObjMeasurementRef ref : objectMeasurementRefs.values()) {
            ObjMeasurementRef newRef = ref.duplicate();
            if (newRef == null)
                continue;
            newObjMeasurementRefs.add(newRef);
        }

        ObjMetadataRefs newObjMetadataRefs = newModule.objectMetadataRefs;
        for (ObjMetadataRef ref : objectMetadataRefs.values()) {
            ObjMetadataRef newRef = ref.duplicate();
            if (newRef == null)
                continue;
            newObjMetadataRefs.add(newRef);
        }

        ImageMeasurementRefs newImageMeasurementRefs = newModule.imageMeasurementRefs;
        for (ImageMeasurementRef ref : imageMeasurementRefs.values()) {
            ImageMeasurementRef newRef = ref.duplicate();
            if (newRef == null)
                continue;
            newImageMeasurementRefs.add(newRef);
        }

        MetadataRefs newMetadataRefs = newModule.metadataRefs;
        for (MetadataRef ref : metadataRefs.values()) {
            MetadataRef newRef = ref.duplicate();
            if (newRef == null)
                continue;
            newMetadataRefs.add(newRef);
        }

        ParentChildRefs newParentChildRefs = newModule.parentChildRefs;
        for (ParentChildRef ref : parentChildRefs.values()) {
            ParentChildRef newRef = ref.duplicate();
            if (newRef == null)
                continue;
            newParentChildRefs.add(newRef);
        }

        PartnerRefs newPartnerRefs = newModule.partnerRefs;
        for (PartnerRef ref : newPartnerRefs.values()) {
            PartnerRef newRef = ref.duplicate();
            if (newRef == null)
                continue;
            newPartnerRefs.add(newRef);
        }

        return newModule;

    }

    // PROTECTED METHODS

    public void writeStatus(String message) {
        writeStatus(message, name);
    }

    public static void writeStatus(String message, String moduleName) {
        if (verbose)
            MIA.log.writeStatus(moduleName + ": " + message);
    }

    public void writeProgressStatus(int count, int total, String featureBeingProcessed) {
        writeProgressStatus(count, total, featureBeingProcessed, name);
    }

    public static void writeProgressStatus(int count, int total, String featureBeingProcessed, String moduleName) {
        if (verbose)
            writeStatus(count + "/" + total + " " + featureBeingProcessed + " ("
                    + Math.floorDiv(100 * count, total) + "%)", moduleName);
    }

    // OVER-RIDDEN METHODS

    @Override
    public int compareTo(Object o) {
        return getName().compareTo(((Module) o).getName());

    }

    @Override
    public void appendXMLAttributes(Element element) {
        super.appendXMLAttributes(element);

        element.setAttribute("ID", moduleID);
        element.setAttribute("CLASSNAME", getClass().getName());
        element.setAttribute("ENABLED", String.valueOf(enabled));
        element.setAttribute("DISABLEABLE", String.valueOf(canBeDisabled));
        element.setAttribute("SHOW_BASIC_TITLE", String.valueOf(showProcessingViewTitle));
        element.setAttribute("SHOW_OUTPUT", String.valueOf(showOutput));
        element.setAttribute("NOTES", notes);
        element.setAttribute("VERSION", getVersionNumber());

    }

    @Override
    public void setAttributesFromXML(Node node) {
        super.setAttributesFromXML(node);

        NamedNodeMap map = node.getAttributes();

        if (map.getNamedItem("ID") == null) {
            this.moduleID = String.valueOf(hashCode());
        } else {
            this.moduleID = map.getNamedItem("ID").getNodeValue();
        }
        this.enabled = Boolean.parseBoolean(map.getNamedItem("ENABLED").getNodeValue());
        this.canBeDisabled = Boolean.parseBoolean(map.getNamedItem("DISABLEABLE").getNodeValue());
        if (map.getNamedItem("SHOW_BASIC_TITLE") != null) {
            this.showProcessingViewTitle = Boolean.parseBoolean(map.getNamedItem("SHOW_BASIC_TITLE").getNodeValue());
        }
        this.showOutput = Boolean.parseBoolean(map.getNamedItem("SHOW_OUTPUT").getNodeValue());
        this.notes = map.getNamedItem("NOTES").getNodeValue();

        if (map.getNamedItem("VERSION") != null) {
            String workflowVersion = map.getNamedItem("VERSION").getNodeValue();
            int comparison = compareVersions(getVersionNumber(), workflowVersion);

            if (comparison != 0) {
                String resultsWarning = "";
                if (Math.abs(comparison) == 1)
                    resultsWarning = "Differences in results possible, but unlikely";
                if (Math.abs(comparison) == 2)
                    resultsWarning = "Differences in results possible";
                if (Math.abs(comparison) == 3)
                    resultsWarning = "Differences in results likely";

                MIA.log.writeWarning("Module version mismatch:");
                MIA.log.writeWarning("    Module name: \"" + name + "\"");
                MIA.log.writeWarning("    Workflow version: " + workflowVersion);
                MIA.log.writeWarning("    Plugin version: " + getVersionNumber());
                MIA.log.writeWarning("    Status: " + resultsWarning + ".");

            }

        }
    }

    /**
     * Compares two version strings and returns a number indicating the location of
     * the most significant version difference
     * 
     * @param v1 Version string 1
     * @param v2 Version string 2
     * @return Comparison number for version strings. A difference in major version
     *         will return a value of 3, difference in minor version will return a
     *         value of 2, difference in patch version will return 1 and matching
     *         versions will return 0. Returned values are positive if the second
     *         version is greater than the first and a negative value if the second
     *         version is smaller.
     */
    public static int compareVersions(String v1, String v2) {
        String[] elementStrings1 = v1.split("\\.");
        String[] elementStrings2 = v2.split("\\.");

        int[] elements1 = new int[] { Integer.parseInt(elementStrings1[0]), Integer.parseInt(elementStrings1[1]),
                Integer.parseInt(elementStrings1[2]) };
        int[] elements2 = new int[] { Integer.parseInt(elementStrings2[0]), Integer.parseInt(elementStrings2[1]),
                Integer.parseInt(elementStrings2[2]) };

        if (elements1[0] < elements2[0])
            // Second version newer in major version
            return 3;

        else if (elements1[0] > elements2[0])
            // Second version older in major version
            return -3;

        else if (elements1[1] < elements2[1])
            // Second version newer in minor version
            return 2;

        else if (elements1[1] > elements2[1])
            // Second version older in minor version
            return -2;

        else if (elements1[2] < elements2[2])
            // Second version newer in patch version
            return 1;

        else if (elements1[2] > elements2[2])
            // Second version older in patch version
            return -1;

        else
            return 0;

    }

    @Override
    public String toString() {
        return getNickname();
    }
}
