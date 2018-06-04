package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Miscellaneous;

import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import static org.junit.Assert.*;

public class BinObjectsByMeasurementTest {
    private double tolerance = 1E-2;

    @Test
    public void testGetTitle() {
        assertNotNull(new BinObjectsByMeasurement().getTitle());
    }

    @Test
    public void testRunAllInRange() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        String measurement = ExpectedObjects3D.Measures.EXP_N_VOXELS.name();

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising BinObjectsyMeasurement
        BinObjectsByMeasurement binObjectsByMeasurement = new BinObjectsByMeasurement();
        binObjectsByMeasurement.initialiseParameters();
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.INPUT_OBJECTS, inputObjectsName);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.MEASUREMENT,measurement);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.SMALLEST_BIN_CENTRE,100d);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.LARGEST_BIN_CENTRE,700d);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.NUMBER_OF_BINS,4);

        // Running IdentifyObjects
        binObjectsByMeasurement.run(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_BIN_N_VOXELS_4BINS_INRANGE.name()).getValue();
            double actual = testObject.getMeasurement(BinObjectsByMeasurement.getFullName(measurement)).getValue();
            assertEquals("Measurement value", expected, actual, tolerance);

        }
    }

    @Test
    public void testRunSomeBelowRange() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        String measurement = ExpectedObjects3D.Measures.EXP_N_VOXELS.name();

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising BinObjectsyMeasurement
        BinObjectsByMeasurement binObjectsByMeasurement = new BinObjectsByMeasurement();
        binObjectsByMeasurement.initialiseParameters();
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.INPUT_OBJECTS, inputObjectsName);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.MEASUREMENT,measurement);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.SMALLEST_BIN_CENTRE,200d);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.LARGEST_BIN_CENTRE,800d);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.NUMBER_OF_BINS,4);

        // Running IdentifyObjects
        binObjectsByMeasurement.run(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_BIN_N_VOXELS_4BINS_SHORTRANGE.name()).getValue();
            double actual = testObject.getMeasurement(BinObjectsByMeasurement.getFullName(measurement)).getValue();
            assertEquals("Measurement value", expected, actual, tolerance);

        }
    }

    @Test
    public void testRunSomeAboveRange() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        String measurement = ExpectedObjects3D.Measures.EXP_N_VOXELS.name();

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising BinObjectsyMeasurement
        BinObjectsByMeasurement binObjectsByMeasurement = new BinObjectsByMeasurement();
        binObjectsByMeasurement.initialiseParameters();
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.INPUT_OBJECTS, inputObjectsName);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.MEASUREMENT,measurement);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.SMALLEST_BIN_CENTRE,0d);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.LARGEST_BIN_CENTRE,150d);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.NUMBER_OF_BINS,4);

        // Running IdentifyObjects
        binObjectsByMeasurement.run(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_BIN_N_VOXELS_4BINS_HIGHRANGE.name()).getValue();
            double actual = testObject.getMeasurement(BinObjectsByMeasurement.getFullName(measurement)).getValue();
            assertEquals("Measurement value", expected, actual, tolerance);

        }
    }
}