package wbif.sjx.MIA.Module;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ij.Prefs;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.References.ImageMeasurementRef;
import wbif.sjx.MIA.Object.References.MetadataRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.ParentChildRef;
import wbif.sjx.MIA.Object.References.PartnerRef;
import wbif.sjx.MIA.Object.References.Abstract.Ref;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.MIA.Process.Logging.LogRenderer;

/**
 * Created by sc13967 on 02/05/2017.
 */
public abstract class Module extends Ref implements Comparable {
    protected ModuleCollection modules;

    protected ParameterCollection parameters = new ParameterCollection();
    protected ImageMeasurementRefCollection imageMeasurementRefs = new ImageMeasurementRefCollection();
    protected ObjMeasurementRefCollection objectMeasurementRefs = new ObjMeasurementRefCollection();
    protected MetadataRefCollection metadataRefs = new MetadataRefCollection();
    protected ParentChildRefCollection parentChildRefs = new ParentChildRefCollection();
    protected PartnerRefCollection partnerRefs = new PartnerRefCollection();

    private String moduleID = String.valueOf(System.currentTimeMillis()); // Using the system time to create a unique ID
    private static boolean verbose = false;
    private String notes = "";
    private boolean enabled = true;
    private boolean canBeDisabled = false;
    private boolean runnable = true;
    protected boolean showOutput = false;
    protected Module redirectModule = null; // After this module, can redirect to another module
    private boolean showBasicTitle = true;

    // CONSTRUCTOR

    public Module(String name, ModuleCollection modules) {
        super(name);
        this.modules = modules;
        initialiseParameters();
    }

    // ABSTRACT METHODS

    public abstract Category getCategory();

    protected abstract Status process(Workspace workspace);

    /*
     * Get a ParameterCollection of all the possible parameters this class requires
     * (not all may be used). This returns the ParameterCollection, rather than just
     * setting the local variable directly, which helps ensure the correct operation
     * is included in the method.
     */
    protected abstract void initialiseParameters();

    /*
     * Return a ParameterCollection of the currently active parameters. This is
     * generateModuleList each time a parameter is changed. For example, if
     * "Export XML" is set to "false" a sub-parameter specifying the measurements to
     * export won't be included in the ParameterCollection. A separate rendering
     * class will take this ParameterCollection and generate an appropriate GUI
     * panel.
     */
    public abstract ParameterCollection updateAndGetParameters();

    public abstract ImageMeasurementRefCollection updateAndGetImageMeasurementRefs();

    public abstract ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs();

    public abstract MetadataRefCollection updateAndGetMetadataReferences();

    public abstract ParentChildRefCollection updateAndGetParentChildRefs();

    public abstract PartnerRefCollection updateAndGetPartnerRefs();

    public abstract boolean verify();

    // PUBLIC METHODS

    public Status execute(Workspace workspace) {
        writeStatus("Processing");

        // By default all modules should use this format
        Prefs.blackBackground = false;

        // Running the main module code
        Status status = process(workspace);

        switch (status) {
            case PASS:
                writeStatus("Completed");
                break;
            case TERMINATE:
                writeStatus("Completed (ending analysis early)");
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

            DecimalFormat df = new DecimalFormat("#.0");

            String memoryMessage = df.format(usedMemory * 1E-6) + " MB of " + df.format(totalMemory * 1E-6) + " MB"
                    + ", MODULE = \"" + getName() + "\"" + ", DATE/TIME = " + dateTime + ", FILE = \""
                    + workspace.getMetadata().getFile() + "\"";

            MIA.log.writeMemory(memoryMessage);

        }

        return status;

    }

    public void addObjectMeasurementRef(ObjMeasurementRef ref) {
        objectMeasurementRefs.add(ref);
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

    public <T> T getParameterValue(String name) {
        return parameters.getParameter(name).getValue();
    }

    public void setParameterVisibility(String name, boolean visible) {
        parameters.updateVisible(name, visible);
    }

    public ParameterCollection getAllParameters() {
        return parameters;
    }

    public boolean invalidParameterIsVisible() {
        return updateAndGetParameters().invalidParameterIsVisible();

    }

    public <T extends Parameter> LinkedHashSet<T> getParametersMatchingType(Class<T> type) {
        // If the current module is the cutoff the loop terminates. This prevents the
        // system offering measurements
        // that are created after this module or are currently unavailable.
        if (!isEnabled())
            return null;
        if (!isRunnable())
            return null;

        // Running through all parameters, adding all images to the list
        LinkedHashSet<T> parameters = new LinkedHashSet<>();
        ParameterCollection currParameters = updateAndGetParameters();
        for (Parameter currParameter : currParameters.values()) {
            if (type.isInstance(currParameter)) {
                parameters.add((T) currParameter);
            }
            if (currParameter instanceof ParameterGroup) {
                addParameterGroupParameters((ParameterGroup) currParameter, type, parameters);
            }
        }

        return parameters;

    }

    public static <T extends Parameter> void addParameterGroupParameters(ParameterGroup parameterGroup, Class<T> type,
            LinkedHashSet<T> parameters) {
        LinkedHashMap<Integer, ParameterCollection> collections = parameterGroup.getCollections(false);
        for (ParameterCollection collection : collections.values()) {
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

    public ModuleCollection getModules() {
        return modules;
    }

    public void setModules(ModuleCollection modules) {
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

    public boolean canShowBasicTitle() {
        return showBasicTitle;
    }

    public void setShowBasicTitle(boolean showBasicTitle) {
        this.showBasicTitle = showBasicTitle;
    }

    public static boolean isVerbose() {
        return verbose;
    }

    public static void setVerbose(boolean verbose) {
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

    public Module getRedirectModule() {
        return this.redirectModule;
    }

    public void setRedirectModule(Module module) {
        this.redirectModule = module;
    }

    public boolean hasVisibleParameters() {
        return updateAndGetParameters().hasVisibleParameters();

    }

    public Module duplicate(ModuleCollection newModules) {
        Constructor constructor;
        Module newModule;
        try {
            constructor = this.getClass().getDeclaredConstructor(ModuleCollection.class);
            newModule = (Module) constructor.newInstance(newModules);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException
                | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }

        newModule.setModuleID(getModuleID());
        newModule.setNickname(getNickname());
        newModule.setEnabled(enabled);
        newModule.setShowOutput(showOutput);
        newModule.setNotes(notes);
        newModule.setCanBeDisabled(canBeDisabled);

        ParameterCollection newParameters = newModule.getAllParameters();
        for (Parameter parameter : parameters.values()) {
            Parameter newParameter = parameter.duplicate(newModule);
            if (newParameter == null)
                continue;
            newParameter.setModule(newModule);
            newParameters.add(newParameter);
        }

        ObjMeasurementRefCollection newObjMeasurementRefs = newModule.objectMeasurementRefs;
        for (ObjMeasurementRef ref : objectMeasurementRefs.values()) {
            ObjMeasurementRef newRef = ref.duplicate();
            if (newRef == null)
                continue;
            newObjMeasurementRefs.add(newRef);
        }

        ImageMeasurementRefCollection newImageMeasurementRefs = newModule.imageMeasurementRefs;
        for (ImageMeasurementRef ref : imageMeasurementRefs.values()) {
            ImageMeasurementRef newRef = ref.duplicate();
            if (newRef == null)
                continue;
            newImageMeasurementRefs.add(newRef);
        }

        MetadataRefCollection newMetadataRefs = newModule.metadataRefs;
        for (MetadataRef ref : metadataRefs.values()) {
            MetadataRef newRef = ref.duplicate();
            if (newRef == null)
                continue;
            newMetadataRefs.add(newRef);
        }

        ParentChildRefCollection newParentChildRefs = newModule.parentChildRefs;
        for (ParentChildRef ref : parentChildRefs.values()) {
            ParentChildRef newRef = ref.duplicate();
            if (newRef == null)
                continue;
            newParentChildRefs.add(newRef);
        }

        PartnerRefCollection newPartnerRefs = newModule.partnerRefs;
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

    protected static void writeStatus(String message, String moduleName) {
        if (verbose)
            MIA.log.writeStatus("[" + moduleName + "] " + message);
    }

    protected void writeProgressStatus(int count, int total, String featureBeingProcessed) {
        writeProgressStatus(count, total, featureBeingProcessed, name);
    }

    protected static void writeProgressStatus(int count, int total, String featureBeingProcessed, String moduleName) {
        if (verbose)
            writeStatus("Processed " + count + " of " + total + " "+featureBeingProcessed+" ("
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
        element.setAttribute("SHOW_BASIC_TITLE", String.valueOf(showBasicTitle));
        element.setAttribute("SHOW_OUTPUT", String.valueOf(showOutput));
        element.setAttribute("NOTES", notes);

    }

    @Override
    public void setAttributesFromXML(Node node) {
        super.setAttributesFromXML(node);

        NamedNodeMap map = node.getAttributes();

        if (map.getNamedItem("ID") == null) {
            this.moduleID = String.valueOf(System.currentTimeMillis());
            try {
                Thread.sleep(5); // This prevents the next module ID clashing with this one
            } catch (InterruptedException e) {
            }
        } else {
            this.moduleID = map.getNamedItem("ID").getNodeValue();
        }
        this.enabled = Boolean.parseBoolean(map.getNamedItem("ENABLED").getNodeValue());
        this.canBeDisabled = Boolean.parseBoolean(map.getNamedItem("DISABLEABLE").getNodeValue());
        if (map.getNamedItem("SHOW_BASIC_TITLE") != null) {
            this.showBasicTitle = Boolean.parseBoolean(map.getNamedItem("SHOW_BASIC_TITLE").getNodeValue());
        }
        this.showOutput = Boolean.parseBoolean(map.getNamedItem("SHOW_OUTPUT").getNodeValue());
        this.notes = map.getNamedItem("NOTES").getNodeValue();

    }

    @Override
    public String toString() {
        return getNickname();
    }
}
