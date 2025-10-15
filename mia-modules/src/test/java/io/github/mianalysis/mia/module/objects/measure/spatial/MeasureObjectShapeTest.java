package io.github.mianalysis.mia.module.objects.measure.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.mianalysis.mia.expectedobjects.ExpectedObjects;
import io.github.mianalysis.mia.expectedobjects.Objects3D;
import io.github.mianalysis.mia.expectedobjects.VolumeTypes;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.units.SpatialUnit;

/**
 * Created by Stephen Cross on 03/09/2017.
 */

public class MeasureObjectShapeTest extends ModuleTest {
    private double tolerance = 1E-2;


    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureObjectShape(null).getDescription());

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    @Disabled
    public void testRun(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjsI testObjects = new Objects3D(VolumeTypes.getFactory(volumeType)).getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising MeasureObjectShape
        MeasureObjectShape measureObjectShape = new MeasureObjectShape(null);
        measureObjectShape.initialiseParameters();
        measureObjectShape.updateParameterValue(MeasureObjectShape.INPUT_OBJECTS,inputObjectsName);


        // Running IdentifyObjects
        measureObjectShape.execute(workspace);

        // Checking the workspace contains a single object set
        assertEquals(1,workspace.getAllObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjects(inputObjectsName));
        assertEquals(8,workspace.getObjects(inputObjectsName).size());

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (ObjI testObject:testObjects.values()) {
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