package wbif.sjx.MIA.Macro.ObjectMeasurements.Spatial;

import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.Units;
import wbif.sjx.MIA.Object.Workspace;

public class MIA_MeasureSingleObjectCentroid extends MacroOperation {
    public MIA_MeasureSingleObjectCentroid(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        String inputObjectsName = (String) objects[0];
        int inputObjectsID = (int) Math.round((Double) objects[1]);

        String units = Units.getOMEUnits().getSymbol();

        Obj inputObject = workspace.getObjectSet(inputObjectsName).get(inputObjectsID);

        ResultsTable resultsTable = new ResultsTable();
        resultsTable.setValue("X-mean (px)",0,inputObject.getXMean(true));
        resultsTable.setValue("X-mean ("+units+")",0,inputObject.getXMean(false));
        resultsTable.setValue("Y-mean (px)",0,inputObject.getYMean(true));
        resultsTable.setValue("Y-mean ("+units+")",0,inputObject.getYMean(false));
        resultsTable.setValue("Z-mean (slice)",0,inputObject.getZMean(true,false));
        resultsTable.setValue("Z-mean ("+units+")",0,inputObject.getZMean(false,false));

        resultsTable.show("Results");
        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String objectName, Integer objectID";
    }

    @Override
    public String getDescription() {
        return "Calculates the centroid of the single object with ID matching the specified value.";
    }
}
