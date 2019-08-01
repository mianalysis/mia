package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Objects3D;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;

import static org.junit.Assert.*;

public class BinObjectsByMeasurementTest extends ModuleTest {
    private double tolerance = 1E-2;

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new BinObjectsByMeasurement(null).getDescription());
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

        String measurement = Objects3D.Measures.EXP_N_VOXELS.name();

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D().getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising BinObjectsyMeasurement
        BinObjectsByMeasurement binObjectsByMeasurement = new BinObjectsByMeasurement(null);
        binObjectsByMeasurement.initialiseParameters();
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.INPUT_OBJECTS, inputObjectsName);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.MEASUREMENT,measurement);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.SMALLEST_BIN_CENTRE,100d);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.LARGEST_BIN_CENTRE,700d);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.NUMBER_OF_BINS,4);

        // Running IdentifyObjects
        binObjectsByMeasurement.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(Objects3D.Measures.EXP_BIN_N_VOXELS_4BINS_INRANGE.name()).getValue();
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

        String measurement = Objects3D.Measures.EXP_N_VOXELS.name();

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D().getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising BinObjectsyMeasurement
        BinObjectsByMeasurement binObjectsByMeasurement = new BinObjectsByMeasurement(null);
        binObjectsByMeasurement.initialiseParameters();
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.INPUT_OBJECTS, inputObjectsName);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.MEASUREMENT,measurement);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.SMALLEST_BIN_CENTRE,200d);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.LARGEST_BIN_CENTRE,800d);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.NUMBER_OF_BINS,4);

        // Running IdentifyObjects
        binObjectsByMeasurement.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(Objects3D.Measures.EXP_BIN_N_VOXELS_4BINS_SHORTRANGE.name()).getValue();
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

        String measurement = Objects3D.Measures.EXP_N_VOXELS.name();

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D().getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising BinObjectsyMeasurement
        BinObjectsByMeasurement binObjectsByMeasurement = new BinObjectsByMeasurement(null);
        binObjectsByMeasurement.initialiseParameters();
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.INPUT_OBJECTS, inputObjectsName);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.MEASUREMENT,measurement);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.SMALLEST_BIN_CENTRE,0d);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.LARGEST_BIN_CENTRE,150d);
        binObjectsByMeasurement.updateParameterValue(BinObjectsByMeasurement.NUMBER_OF_BINS,4);

        // Running IdentifyObjects
        binObjectsByMeasurement.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(Objects3D.Measures.EXP_BIN_N_VOXELS_4BINS_HIGHRANGE.name()).getValue();
            double actual = testObject.getMeasurement(BinObjectsByMeasurement.getFullName(measurement)).getValue();
            assertEquals("Measurement value", expected, actual, tolerance);

        }
    }
}