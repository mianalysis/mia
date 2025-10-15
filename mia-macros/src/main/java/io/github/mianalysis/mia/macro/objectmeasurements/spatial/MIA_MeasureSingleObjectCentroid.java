package io.github.mianalysis.mia.macro.objectmeasurements.spatial;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.units.SpatialUnit;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_MeasureSingleObjectCentroid extends MacroOperation {
    public MIA_MeasureSingleObjectCentroid(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_STRING};
    }

    @Override
    public String action(Object[] objects, WorkspaceI workspace, Modules modules) {
        String inputObjectsName = (String) objects[0];
        int inputObjectsID = (int) Math.round((Double) objects[1]);

        String units = SpatialUnit.getOMEUnit().getSymbol();

        ObjI inputObject = workspace.getObjects(inputObjectsName).get(inputObjectsID);

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
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Calculates the centroid of the single object with ID matching the specified value.";
    }
}
