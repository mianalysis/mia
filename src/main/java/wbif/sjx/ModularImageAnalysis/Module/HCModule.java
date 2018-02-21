// TODO: Module to save images and to save objects (could roll this into ShowImage and ShowObjects)
// TODO: Module to plot histograms of measurements (e.g. mean intensity for objects)
// TODO: Module to calculate size metrics of objects (can used Blob class)
// TODO: Module to calculate radial intensity distribution of objects
// TODO: Modules creating new images should pass spatial calibrations across in case new images are used to get objects

package wbif.sjx.ModularImageAnalysis.Module;

import ij.Prefs;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by sc13967 on 02/05/2017.
 */
public abstract class HCModule implements Serializable {
    protected ParameterCollection parameters = new ParameterCollection();
    protected MeasurementReferenceCollection imageMeasurementReferences = new MeasurementReferenceCollection();
    protected MeasurementReferenceCollection objectMeasurementReferences = new MeasurementReferenceCollection();

    private String nickname;
    private String notes = "";
    private boolean enabled = true;
    protected String moduleName = "";


    // CONSTRUCTOR

    public HCModule() {
        moduleName = getTitle();
        nickname = moduleName;

        initialiseParameters();
        initialiseMeasurementReferences();

    }


    // PUBLIC METHODS

    public abstract String getTitle();

    public abstract String getHelp();

    protected abstract void run(Workspace workspace, boolean verbose) throws GenericMIAException;

    public void execute(Workspace workspace, boolean verbose) throws GenericMIAException {
        String moduleName = getTitle();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // By default all modules should use this format
        Prefs.blackBackground = false;

        // Running the main module code
        run(workspace,verbose);

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    /**
     * Get a ParameterCollection of all the possible parameters this class requires (not all may be used).  This returns
     * the ParameterCollection, rather than just setting the local variable directly, which helps ensure the correct
     * operation is included in the method.
     * @return
     */
    protected abstract void initialiseParameters();

    protected abstract void initialiseMeasurementReferences();

    /**
     * Return a ParameterCollection of the currently active parameters.  This is run each time a parameter is changed.
     * For example, if "Export XML" is set to "false" a sub-parameter specifying the measurements to export won't be
     * included in the ParameterCollection.  A separate rendering class will take this ParameterCollection and generate
     * an appropriate GUI panel.
     * @return
     */
    public abstract ParameterCollection updateAndGetParameters();

    public abstract MeasurementReferenceCollection updateAndGetImageMeasurementReferences();

    public abstract MeasurementReferenceCollection updateAndGetObjectMeasurementReferences();

    public MeasurementReference getImageMeasurementReference(String name) {
        for (MeasurementReference measurementReference : imageMeasurementReferences) {
            if (measurementReference.getName().equals(name)) return measurementReference;
        }

        return null;

    }

    public MeasurementReference getObjectMeasurementReference(String name) {
        for (MeasurementReference measurementReference : objectMeasurementReferences) {
            if (measurementReference.getName().equals(name)) return measurementReference;
        }

        return null;

    }

    /**
     * Returns a LinkedHashMap containing the parents (key) and their children (value)
     * @return
     */
    public abstract void addRelationships(RelationshipCollection relationships);

    public void updateParameterValue(String name, Object value) {
        parameters.updateValue(name,value);

    }

    public <T> T getParameterValue(String name) {
        return parameters.getParameter(name).getValue();

    }

    public int getParameterType(String name) {
        return parameters.get(name).getType();

    }

    public void setParameterVisibility(String name, boolean visible) {
        parameters.updateVisible(name,visible);

    }

    public ParameterCollection getAllParameters() {
        return parameters;
    }


    // PRIVATE METHODS

    void run(Workspace workspace) throws GenericMIAException {
        run(workspace,false);

    }

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

    protected void writeMessage(String message, boolean verbose) {
        if (verbose) System.out.println("[" + moduleName + "] "+message);
    }
}
