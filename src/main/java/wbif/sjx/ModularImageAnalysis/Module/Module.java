// TODO: Module to plot histograms of measurements (e.g. mean intensity for objects)

package wbif.sjx.ModularImageAnalysis.Module;

import ij.Prefs;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;

/**
 * Created by sc13967 on 02/05/2017.
 */
public abstract class Module {
    protected ParameterCollection parameters = new ParameterCollection();
    protected MeasurementRefCollection imageMeasurementRefs = new MeasurementRefCollection();
    protected MeasurementRefCollection objectMeasurementRefs = new MeasurementRefCollection();

    private static boolean verbose = false;
    private String nickname;
    private String notes = "";
    private boolean enabled = true;
    private String moduleName;
    private String packageName;
    private boolean canBeDisabled = false;
    protected boolean showOutput = false;
    private boolean runnable = true;


    // CONSTRUCTOR

    public Module() {
        moduleName = getTitle();
        nickname = moduleName;

        initialiseParameters();

    }


    // PUBLIC METHODS

    public abstract String getTitle();

    public abstract String getPackageName();

    public abstract String getHelp();

    protected abstract boolean run(Workspace workspace);

    public boolean execute(Workspace workspace) {
        writeMessage("Processing");

        // By default all modules should use this format
        Prefs.blackBackground = false;

        // Running the main module code
        boolean status = run(workspace);

        if (status) {
            writeMessage("Completed");
        } else {
            writeMessage("Did not complete");
        }

        return status;

    }

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

    public abstract MeasurementRefCollection updateAndGetImageMeasurementRefs();

    public abstract MeasurementRefCollection updateAndGetObjectMeasurementRefs();

    public abstract MetadataRefCollection updateAndGetMetadataReferences();

    public MeasurementRef getImageMeasurementRef(String name) {
        return imageMeasurementRefs.getOrPut(name);
    }

    public MeasurementRef getObjectMeasurementRef(String name) {
        return objectMeasurementRefs.getOrPut(name);
    }

    /*
     * Returns a LinkedHashMap containing the parents (key) and their children (value)
     */
    public abstract void addRelationships(RelationshipCollection relationships);

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
        for (Parameter parameter:updateAndGetParameters()) {
            if (!parameter.isValid() && parameter.isVisible()) return true;
        }

        return false;

    }


    // PRIVATE METHODS

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    protected void writeMessage(String message) {
        if (verbose) new Thread(() -> System.out.println("[" + moduleName + "] "+message)).start();
    }

    protected static void writeMessage(String message, String moduleName) {
        if (verbose) new Thread(() -> System.out.println("[" + moduleName + "] "+message)).start();
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
}
