package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Objects3D;
import wbif.sjx.MIA.ExpectedObjects.Spots3D;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;

import static org.junit.Assert.*;

public class RemoveObjectsTest extends ModuleTest {
    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new RemoveObjects(null).getHelp());
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
        RemoveObjects removeObjects = new RemoveObjects(null);
        removeObjects.initialiseParameters();
        removeObjects.updateParameterValue(RemoveObjects.INPUT_OBJECTS,"TestObj");

        // Running the module
        removeObjects.execute(workspace);

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
        RemoveObjects removeObjects = new RemoveObjects(null);
        removeObjects.initialiseParameters();
        removeObjects.updateParameterValue(RemoveObjects.INPUT_OBJECTS,"TestObj");

        // Running the module
        removeObjects.execute(workspace);

        // Checking the objects have been removed
        assertEquals(1,workspace.getObjects().size());

    }
}