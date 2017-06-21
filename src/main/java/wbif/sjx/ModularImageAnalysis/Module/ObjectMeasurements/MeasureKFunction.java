package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 21/06/2017.
 */
public class MeasureKFunction extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";

    @Override
    public String getTitle() {
        return "Measure K-function";
    }

    @Override
    public String getHelp() {
        return "INCOMPLETE" +
                "" +
                "Measures Ripley's K-function for each object";
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {

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
