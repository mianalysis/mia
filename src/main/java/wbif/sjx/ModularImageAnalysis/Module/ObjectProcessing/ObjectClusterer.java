package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 21/06/2017.
 */
public class ObjectClusterer extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";

    @Override
    public String getTitle() {
        return "Object clustering";

    }

    @Override
    public String getHelp() {
        return "INCOMPLETE" +
                "" +
                "Clusters objects using K-Means and/or DB-SCAN algorithms.  The group is assigned as a parent";

    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting objects to measure
        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        HCObjectSet inputObjects = workspace.getObjects().get(inputObjectsName);

        ////// CODE GOES HERE!! (This can be done using Apache Commons Math - offers K-Means, but also DB-SCAN)

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_OBJECTS,HCParameter.INPUT_OBJECTS,null));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
