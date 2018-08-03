package wbif.sjx.ModularImageAnalysis.Module.Miscellaneous;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 14/03/2018.
 */
public class GUISeparator extends Module{
    public static final String TITLE = "Title";
    public static final String EXPANDED = "Expanded";

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
        parameters.add(new Parameter(TITLE,Parameter.STRING,""));
        parameters.add(new Parameter(EXPANDED,Parameter.BOOLEAN,true));
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
