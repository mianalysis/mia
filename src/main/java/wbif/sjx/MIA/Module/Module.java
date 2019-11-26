// TODO: Module to plot histograms of measurements (e.g. mean intensity for objects)

package wbif.sjx.MIA.Module;

import ij.Prefs;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.References.Abstract.Ref;
import wbif.sjx.MIA.Process.Logging.LogRenderer;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 02/05/2017.
 */
public abstract class Module extends Ref implements Comparable, Serializable {
    protected ModuleCollection modules;

    protected ParameterCollection parameters = new ParameterCollection();
    protected ImageMeasurementRefCollection imageMeasurementRefs = new ImageMeasurementRefCollection();
    protected ObjMeasurementRefCollection objectMeasurementRefs = new ObjMeasurementRefCollection();
    protected MetadataRefCollection metadataRefs = new MetadataRefCollection();
    protected RelationshipRefCollection relationshipRefs = new RelationshipRefCollection();

    private static boolean verbose = false;
    private String notes = "";
    private boolean enabled = true;
    private String packageName;
    private boolean canBeDisabled = false;
    protected boolean showOutput = false;
    private boolean runnable = true;


    // CONSTRUCTOR

    public Module(String name, ModuleCollection modules) {
        super(name);
        this.modules = modules;
        initialiseParameters();
    }


    // ABSTRACT METHODS

    public abstract String getPackageName();

    protected abstract boolean process(Workspace workspace);

    /*
     * Get a ParameterCollection of all the possible parameters this class requires (not all may be used).  This returns
     * the ParameterCollection, rather than just setting the local variable directly, which helps ensure the correct
     * operation is included in the method.
     */
    protected abstract void initialiseParameters();

    /*
     * Return a ParameterCollection of the currently active parameters.  This is generateModuleList each time a parameter is changed.
     * For example, if "Export XML" is set to "false" a sub-parameter specifying the measurements to export won't be
     * included in the ParameterCollection.  A separate rendering class will take this ParameterCollection and generate
     * an appropriate GUI panel.
     */
    public abstract ParameterCollection updateAndGetParameters();

    public abstract ImageMeasurementRefCollection updateAndGetImageMeasurementRefs();

    public abstract ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs();

    public abstract MetadataRefCollection updateAndGetMetadataReferences();

    public abstract RelationshipRefCollection updateAndGetRelationships();

    public abstract boolean verify();


    // PUBLIC METHODS

    public boolean execute(Workspace workspace) {
        writeMessage("Processing");

        // By default all modules should use this format
        Prefs.blackBackground = false;

        // Running the main module code
        boolean status = process(workspace);

        if (status) {
            writeMessage("Completed");
        } else {
            writeMessage("Did not complete");
        }

        // If enabled, write the current memory usage to the console
        if (MIA.getMainRenderer().isWriteEnabled(LogRenderer.Level.MEMORY)) {
            double totalMemory = Runtime.getRuntime().totalMemory();
            double usedMemory = totalMemory - Runtime.getRuntime().freeMemory();
            ZonedDateTime zonedDateTime = ZonedDateTime.now();
            String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

            DecimalFormat df = new DecimalFormat("#.0");

            String memoryMessage = df.format(usedMemory*1E-6)+" MB of "+df.format(totalMemory*1E-6)+" MB" +
                    ", module \""+getName()+"\"" +
                    ", file \""+workspace.getMetadata().getFile() +
                    ", time "+dateTime;

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

    public RelationshipRef getRelationshipRef(String parentName, String childName) {
        return relationshipRefs.getOrPut(parentName,childName);
    }

    public void addRelationshipRef(RelationshipRef ref) {
        relationshipRefs.add(ref);
    }

    public <T extends Parameter> T getParameter(String name) {
        return parameters.getParameter(name);
    }

    public Module updateParameterValue(String name, Object value) {
        parameters.updateValue(name,value);
        return this;

    }

    public <T> T getParameterValue(String name) {
        return parameters.getParameter(name).getValue();
    }

    public void setParameterVisibility(String name, boolean visible) {
        parameters.updateVisible(name,visible);
    }

    public ParameterCollection getAllParameters() {
        return parameters;
    }

    public boolean invalidParameterIsVisible() {
        for (Parameter parameter:updateAndGetParameters().values()) {
            if (!parameter.isValid() && parameter.isVisible()) return true;
        }

        return false;

    }

    public <T extends Parameter> LinkedHashSet<T> getParametersMatchingType(Class<T> type) {
        // If the current module is the cutoff the loop terminates.  This prevents the system offering measurements
        // that are created after this module or are currently unavailable.
        if (!isEnabled()) return null;
        if (!isRunnable()) return null;

        // Running through all parameters, adding all images to the list
        LinkedHashSet<T> parameters = new LinkedHashSet<>();
        ParameterCollection currParameters = updateAndGetParameters();
        for (Parameter currParameter : currParameters.values()) {
            if (type.isInstance(currParameter)) {
                parameters.add((T) currParameter);
            }
            if (currParameter instanceof ParameterGroup) {
                addParameterGroupParameters((ParameterGroup) currParameter,type,parameters);
            }
        }

        return parameters;

    }

    public static <T extends Parameter> void addParameterGroupParameters(ParameterGroup parameterGroup, Class<T> type, LinkedHashSet<T> parameters) {
        LinkedHashSet<ParameterCollection> collections = parameterGroup.getCollections();
        for (ParameterCollection collection:collections) {
            for (Parameter currParameter : collection.values()) {
                if (type.isInstance(currParameter)) {
                    parameters.add((T) currParameter);
                }
                if (currParameter instanceof ParameterGroup) {
                    addParameterGroupParameters((ParameterGroup) currParameter,type,parameters);
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

    public boolean hasVisibleParameters() {
        return updateAndGetParameters().hasVisibleParameters();

    }

    public Module duplicate(ModuleCollection newModules) {
        Constructor constructor;
        Module newModule;
        try {
            constructor = this.getClass().getDeclaredConstructor(ModuleCollection.class);
            newModule = (Module) constructor.newInstance(newModules);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }

        newModule.setNickname(getNickname());
        newModule.setEnabled(enabled);
        newModule.setShowOutput(showOutput);
        newModule.setNotes(notes);
        newModule.setCanBeDisabled(canBeDisabled);

        ParameterCollection newParameters = newModule.getAllParameters();
        for (Parameter parameter:parameters.values()) {
            Parameter newParameter = parameter.duplicate(newModule);
            if (newParameter == null) continue;
            newParameter.setModule(newModule);
            newParameters.add(newParameter);
        }

        ObjMeasurementRefCollection newObjMeasurementRefs = newModule.objectMeasurementRefs;
        for (ObjMeasurementRef ref:objectMeasurementRefs.values()) {
            ObjMeasurementRef newRef = ref.duplicate();
            if (newRef == null) continue;
            newObjMeasurementRefs.add(newRef);
        }

        ImageMeasurementRefCollection newImageMeasurementRefs = newModule.imageMeasurementRefs;
        for (ImageMeasurementRef ref:imageMeasurementRefs.values()) {
            ImageMeasurementRef newRef = ref.duplicate();
            if (newRef == null) continue;
            newImageMeasurementRefs.add(newRef);
        }

        MetadataRefCollection newMetadataRefs = newModule.metadataRefs;
        for (MetadataRef ref:metadataRefs.values()) {
            MetadataRef newRef = ref.duplicate();
            if (newRef == null) continue;
            newMetadataRefs.add(newRef);
        }

        RelationshipRefCollection newRelationshipRefs = newModule.relationshipRefs;
        for (RelationshipRef ref:relationshipRefs.values()) {
            RelationshipRef newRef = ref.duplicate();
            if (newRef == null) continue;
            newRelationshipRefs.add(newRef);
        }

        return newModule;

    }


    // PROTECTED METHODS

    public void writeMessage(String message) {
        if (verbose) MIA.log.writeStatus("[" + name + "] "+message);
    }

    protected static void writeMessage(String message, String name) {
        if (verbose) MIA.log.writeStatus("[" + name + "] "+message);
    }


    // OVER-RIDDEN METHODS

    @Override
    public int compareTo(Object o) {
        return getName().compareTo(((Module) o).getNotes());

    }

    @Override
    public void appendXMLAttributes(Element element) {
        super.appendXMLAttributes(element);

        element.setAttribute("CLASSNAME",getClass().getName());
        element.setAttribute("ENABLED",String.valueOf(enabled));
        element.setAttribute("DISABLEABLE",String.valueOf(canBeDisabled));
        element.setAttribute("SHOW_OUTPUT",String.valueOf(showOutput));
        element.setAttribute("NOTES",notes);

    }

    @Override
    public void setAttributesFromXML(Node node) {
        super.setAttributesFromXML(node);

        NamedNodeMap map = node.getAttributes();

        this.enabled = Boolean.parseBoolean(map.getNamedItem("ENABLED").getNodeValue());
        this.canBeDisabled = Boolean.parseBoolean(map.getNamedItem("DISABLEABLE").getNodeValue());
        this.showOutput = Boolean.parseBoolean(map.getNamedItem("SHOW_OUTPUT").getNodeValue());
        this.notes = map.getNamedItem("NOTES").getNodeValue();

    }

    @Override
    public String toString() {
        return getNickname();
    }
}
