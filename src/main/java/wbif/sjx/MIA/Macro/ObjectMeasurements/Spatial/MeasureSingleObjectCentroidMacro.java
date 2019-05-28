package wbif.sjx.MIA.Macro.ObjectMeasurements.Spatial;

import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ObjectMeasurements.Spatial.MeasureObjectCentroid;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.Units;
import wbif.sjx.MIA.Object.Workspace;

public class MeasureSingleObjectCentroidMacro extends MacroOperation {
    public MeasureSingleObjectCentroidMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_MeasureSingleObjectCentroid";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        String inputObjectsName = (String) objects[0];
        int inputObjectsID = (int) Math.round((Double) objects[1]);
        String centroidMethod = (String) objects[2];

        String units = Units.getOMEUnits().getSymbol();

        Obj inputObject = workspace.getObjectSet(inputObjectsName).get(inputObjectsID);

        ResultsTable resultsTable = new ResultsTable();
        switch (centroidMethod) {
            case "Mean":
            case "mean":
            case "MEAN":
                resultsTable.setValue("X-mean (px)",0,inputObject.getXMean(true));
                resultsTable.setValue("X-mean ("+units+")",0,inputObject.getXMean(false));
                resultsTable.setValue("Y-mean (px)",0,inputObject.getYMean(true));
                resultsTable.setValue("Y-mean ("+units+")",0,inputObject.getYMean(false));
                resultsTable.setValue("Z-mean (slice)",0,inputObject.getZMean(true,false));
                resultsTable.setValue("Z-mean ("+units+")",0,inputObject.getZMean(false,false));
                break;
            case "Median":
            case "median":
            case "MEDIAN":
                resultsTable.setValue("X-median (px)",0,inputObject.getXMedian(true));
                resultsTable.setValue("X-median ("+units+")",0,inputObject.getXMedian(false));
                resultsTable.setValue("Y-median (px)",0,inputObject.getYMedian(true));
                resultsTable.setValue("Y-median ("+units+")",0,inputObject.getYMedian(false));
                resultsTable.setValue("Z-median (slice)",0,inputObject.getZMedian(true,false));
                resultsTable.setValue("Z-median ("+units+")",0,inputObject.getZMedian(false,false));
                break;
            default:
                System.err.println("Error in \"\"MIA_MeasureSingleObjectCentroid\"\".  " +
                        "\"centroidMethod\" must be either \"Mean\" or \"Median\".");
                return null;
        }

        resultsTable.show("Results");
        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String objectName, Integer objectID, String centroidMethod";
    }

    @Override
    public String getDescription() {
        return "Calculates the centroid of the single object with ID matching the specified value.  " +
                "Centroid method must be set to either \"Mean\" or \"Median\".";
    }
}
