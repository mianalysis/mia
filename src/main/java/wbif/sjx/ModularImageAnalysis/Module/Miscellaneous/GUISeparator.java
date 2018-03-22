package wbif.sjx.ModularImageAnalysis.Module.Miscellaneous;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementReferenceCollection;
import wbif.sjx.ModularImageAnalysis.Object.ParameterCollection;
import wbif.sjx.ModularImageAnalysis.Object.RelationshipCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

/**
 * Created by sc13967 on 14/03/2018.
 */
public class GUISeparator extends Module{
    @Override
    public String getTitle() {
        return "GUI separator";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {

    }

    @Override
    protected void initialiseParameters() {

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
