package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Objects3D;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Units;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 03/09/2017.
 */
public class MeasureObjectShapeTest {
    private double tolerance = 1E-2;

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new MeasureObjectShape().getTitle());

    }

    @Test @Ignore
    public void testRun() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D().getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising MeasureObjectShape
        MeasureObjectShape measureObjectShape = new MeasureObjectShape();
        measureObjectShape.initialiseParameters();
        measureObjectShape.updateParameterValue(MeasureObjectShape.INPUT_OBJECTS,inputObjectsName);
        measureObjectShape.updateParameterValue(MeasureObjectShape.VOLUMETRIC_MEASURES,true);
        measureObjectShape.updateParameterValue(MeasureObjectShape.PROJECTED_MEASURES,false);

        // Running IdentifyObjects
        measureObjectShape.run(workspace);

        // Checking the workspace contains a single object set
        assertEquals("Number of ObjSets in Workspace",1,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expectedNVoxels = testObject.getMeasurement(Objects3D.Measures.EXP_N_VOXELS.name()).getValue();
            double actualNVoxels = testObject.getMeasurement(MeasureObjectShape.Measurements.N_VOXELS).getValue();
            assertEquals("Measurement value", expectedNVoxels, actualNVoxels,tolerance);

            double expectedProjDiaPX = testObject.getMeasurement(Objects3D.Measures.EXP_PROJ_DIA_PX.name()).getValue();
            double actualProjDiaPX = testObject.getMeasurement(MeasureObjectShape.Measurements.PROJ_DIA_PX).getValue();
            assertEquals("Measurement value", expectedProjDiaPX, actualProjDiaPX, tolerance);

            double expectedProjDiaCal = testObject.getMeasurement(Objects3D.Measures.EXP_PROJ_DIA_CAL.name()).getValue();
            double actualProjDiaCal = testObject.getMeasurement(Units.replace(MeasureObjectShape.Measurements.PROJ_DIA_CAL)).getValue();
            assertEquals("Measurement value", expectedProjDiaCal, actualProjDiaCal, tolerance);

        }
    }

}