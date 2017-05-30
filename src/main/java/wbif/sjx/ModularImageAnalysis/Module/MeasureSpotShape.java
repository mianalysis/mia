// TODO: Add measurements to output

package wbif.sjx.ModularImageAnalysis.Module;

import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 26/05/2017.
 */
public class MeasureSpotShape extends HCModule {
    public static final String INPUT_OBJECTS = "Input spot objects";


    @Override
    public String getTitle() {
        return "Measure spot shape";
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
