package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Objects3D;
import wbif.sjx.MIA.ExpectedObjects.Spots3D;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.WorkspaceCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.common.Object.Volume.VolumeType;

public class RemoveObjectsTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new RemoveObjects(null).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunSingleObjCollection(VolumeType volumeType) throws Exception{
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising the module
        RemoveObjects removeObjects = new RemoveObjects(null);
        removeObjects.initialiseParameters();
        ParameterGroup group = removeObjects.getParameter(RemoveObjects.REMOVE_ANOTHER_OBJECT_SET);
        ParameterCollection collection = group.addParameters();
        collection.updateValue(RemoveObjects.INPUT_OBJECTS, "TestObj");
        
        // Running the module
        removeObjects.execute(workspace);

        // Checking the objects have been removed
        assertEquals(0,workspace.getObjects().size());

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunMultipleObjCollections(VolumeType volumeType) throws Exception{
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        ObjCollection spotObjects = new Spots3D(volumeType).getObjects("SpotObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(spotObjects);

        // Initialising the module
        RemoveObjects removeObjects = new RemoveObjects(null);
        removeObjects.initialiseParameters();
        ParameterGroup group = removeObjects.getParameter(RemoveObjects.REMOVE_ANOTHER_OBJECT_SET);
        ParameterCollection collection = group.addParameters();
        collection.updateValue(RemoveObjects.INPUT_OBJECTS, "TestObj");

        // Running the module
        removeObjects.execute(workspace);

        // Checking the objects have been removed
        assertEquals(1,workspace.getObjects().size());

    }
}