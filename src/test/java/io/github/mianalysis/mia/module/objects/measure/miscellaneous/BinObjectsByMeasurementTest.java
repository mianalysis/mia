package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.mianalysis.mia.expectedobjects.ExpectedObjects;
import io.github.mianalysis.mia.expectedobjects.Objects3D;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.measure.miscellaneous.BinObjectsByMeasurement;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.sjcross.common.object.volume.VolumeType;


public class BinObjectsByMeasurementTest extends ModuleTest {
    private double tolerance = 1E-2;

    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new BinObjectsByMeasurement(null).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunAllInRange(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        String measurement = Objects3D.Measures.EXP_N_VOXELS.name();

        // Creating objects and adding to workspace
        Objs testObjects = new Objects3D(volumeType).getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising BinObjectsyMeasurement
        BinObjectsByMeasurement binObjectsByMeasurement = new BinObjectsByMeasurement(new Modules());
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
            assertEquals(expected, actual, tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunSomeBelowRange(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        String measurement = Objects3D.Measures.EXP_N_VOXELS.name();

        // Creating objects and adding to workspace
        Objs testObjects = new Objects3D(volumeType).getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising BinObjectsyMeasurement
        BinObjectsByMeasurement binObjectsByMeasurement = new BinObjectsByMeasurement(new Modules());
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
            assertEquals(expected, actual, tolerance);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunSomeAboveRange(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        String measurement = Objects3D.Measures.EXP_N_VOXELS.name();

        // Creating objects and adding to workspace
        Objs testObjects = new Objects3D(volumeType).getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising BinObjectsyMeasurement
        BinObjectsByMeasurement binObjectsByMeasurement = new BinObjectsByMeasurement(new Modules());
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
            assertEquals(expected, actual, tolerance);

        }
    }
}