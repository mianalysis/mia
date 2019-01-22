package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Objects3D;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Spots3D;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import static org.junit.Assert.*;

public class RemoveObjectsTest {
    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Test
    public void testGetTitle() {
        assertNotNull(new RemoveObjects().getTitle());
    }

    @Test
    public void testRunSingleObjCollection() throws Exception{
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising the module
        RemoveObjects removeObjects = new RemoveObjects();
        removeObjects.initialiseParameters();
        removeObjects.updateParameterValue(RemoveObjects.INPUT_OBJECTS,"TestObj");

        // Running the module
        removeObjects.run(workspace);

        // Checking the objects have been removed
        assertEquals(0,workspace.getObjects().size());

    }

    @Test
    public void testRunMultipleObjCollections() throws Exception{
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        ObjCollection spotObjects = new Spots3D().getObjects("SpotObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(spotObjects);

        // Initialising the module
        RemoveObjects removeObjects = new RemoveObjects();
        removeObjects.initialiseParameters();
        removeObjects.updateParameterValue(RemoveObjects.INPUT_OBJECTS,"TestObj");

        // Running the module
        removeObjects.run(workspace);

        // Checking the objects have been removed
        assertEquals(1,workspace.getObjects().size());

    }
}