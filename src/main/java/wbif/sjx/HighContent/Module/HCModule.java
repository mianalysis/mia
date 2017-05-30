// TODO: Module to save images and to save objects (could roll this into ShowImage and ShowObjects)
// TODO: Module to plot histograms of measurements (e.g. mean intensity for objects)
// TODO: Module to calculate size metrics of objects (can used Blob class)
// TODO: Module to calculate radial intensity distribution of objects
// TODO: Module to export the measurements from the current workspace (option to put file in current directory)

package wbif.sjx.HighContent.Module;

import wbif.sjx.HighContent.Object.*;

import java.io.Serializable;

/**
 * Created by sc13967 on 02/05/2017.
 */
public abstract class HCModule implements Serializable {
    public HCParameterCollection parameters = new HCParameterCollection();


    // CONSTRUCTOR

    public HCModule() {
        initialiseParameters();

    }


    // PUBLIC METHODS

    public abstract String getTitle();

    public abstract void execute(HCWorkspace workspace, boolean verbose);

    /**
     * Get a ParameterCollection of all the possible parameters this class requires (not all may be used).  This returns
     * the ParameterCollection, rather than just setting the local variable directly, which helps ensure the correct
     * operation is included in the method.
     * @return
     */
    public abstract void initialiseParameters();

    /**
     * Return a ParameterCollection of the currently active parameters.  This is run each time a parameter is changed.
     * For example, if "Export XML" is set to "false" a sub-parameter specifying the measurements to export won't be
     * included in the ParameterCollection.  A separate rendering class will take this ParameterCollection and generate
     * an appropriate GUI panel.
     * @return
     */
    public abstract HCParameterCollection getActiveParameters();

    /**
     * Takes an existing collection of measurements and adds any created
     * @param measurements
     * @return
     */
    public abstract void addMeasurements(HCMeasurementCollection measurements);

    /**
     * Returns a LinkedHashMap containing the parents (key) and their children (value)
     * @return
     */
    public abstract void addRelationships(HCRelationshipCollection relationships);

    public void updateParameterValue(String name, Object value) {
        parameters.updateValue(name,value);

    }

    public <T> T getParameterValue(String name) {
        return parameters.getParameter(name).getValue();

    }

    public void setParameterVisibility(String name, boolean visible) {
        parameters.updateVisible(name,visible);

    }


    // PRIVATE METHODS

    void execute(HCWorkspace workspace) {
        execute(workspace,false);

    }

}
