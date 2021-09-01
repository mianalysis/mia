package io.github.mianalysis.MIA.Module.ObjectMeasurements.Spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.mianalysis.MIA.ExpectedObjects.ExpectedObjects;
import io.github.mianalysis.MIA.ExpectedObjects.Objects3D;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleTest;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.ObjCollection;
import io.github.mianalysis.MIA.Object.Units.SpatialUnit;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.WorkspaceCollection;
import io.github.sjcross.common.Object.Volume.VolumeType;

/**
 * Created by Stephen Cross on 03/09/2017.
 */
public class MeasureObjectShapeTest extends ModuleTest {
    private double tolerance = 1E-2;

    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureObjectShape(null).getDescription());

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    @Disabled
    public void testRun(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D(volumeType).getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising MeasureObjectShape
        MeasureObjectShape measureObjectShape = new MeasureObjectShape(null);
        measureObjectShape.initialiseParameters();
        measureObjectShape.updateParameterValue(MeasureObjectShape.INPUT_OBJECTS,inputObjectsName);


        // Running IdentifyObjects
        measureObjectShape.execute(workspace);

        // Checking the workspace contains a single object set
        assertEquals(1,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expectedNVoxels = testObject.getMeasurement(Objects3D.Measures.EXP_N_VOXELS.name()).getValue();
            double actualNVoxels = testObject.getMeasurement(MeasureObjectShape.Measurements.N_VOXELS).getValue();
            assertEquals(expectedNVoxels, actualNVoxels,tolerance);

            double expectedProjDiaPX = testObject.getMeasurement(Objects3D.Measures.EXP_PROJ_DIA_PX.name()).getValue();
            double actualProjDiaPX = testObject.getMeasurement(MeasureObjectShape.Measurements.PROJ_DIA_PX).getValue();
            assertEquals(expectedProjDiaPX, actualProjDiaPX, tolerance);

            double expectedProjDiaCal = testObject.getMeasurement(Objects3D.Measures.EXP_PROJ_DIA_CAL.name()).getValue();
            double actualProjDiaCal = testObject.getMeasurement(SpatialUnit.replace(MeasureObjectShape.Measurements.PROJ_DIA_CAL)).getValue();
            assertEquals(expectedProjDiaCal, actualProjDiaCal, tolerance);

        }
    }

}