package io.github.mianalysis.mia.module.objects.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.mianalysis.mia.expectedobjects.ExpectedObjects;
import io.github.mianalysis.mia.expectedobjects.Objects2D;
import io.github.mianalysis.mia.expectedobjects.Objects3D;
import io.github.mianalysis.mia.expectedobjects.VolumeTypes;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.coordinates.ObjI;

/**
 * Created by Stephen Cross on 03/09/2017.
 */

public class ProjectObjectsTest extends ModuleTest {
    private double tolerance = 1E-2;


    @Override
    public void testGetHelp() {
        assertNotNull(new ProjectObjects(null).getDescription());

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRun(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String inputObjectsName = "Input objects";
        String outputObjectsName = "Output objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjsI inputObjects = new Objects3D(VolumeTypes.getFactory(volumeType)).getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,false);
        workspace.addObjects(inputObjects);

        // Initialising ProjectObjects
        ProjectObjects projectObjects = new ProjectObjects(null);
        projectObjects.initialiseParameters();
        projectObjects.updateParameterValue(ProjectObjects.INPUT_OBJECTS,inputObjectsName);
        projectObjects.updateParameterValue(ProjectObjects.OUTPUT_OBJECTS,outputObjectsName);

        // Running ProjectObjects
        projectObjects.execute(workspace);

        // Testing there are now 2 sets of objects in the workspace and they have the expected names
        assertEquals(2,workspace.getAllObjects().size());
        assertNotNull(workspace.getObjects(inputObjectsName));
        assertNotNull(workspace.getObjects(outputObjectsName));

        // Testing number of objects in projected set
        assertEquals(8,workspace.getObjects(outputObjectsName).size());

        // Getting expected and actual objects
        ObjsI expectedObjects = new Objects2D(VolumeTypes.getFactory(volumeType)).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        ObjsI actualObjects = workspace.getObjects(outputObjectsName);

        for (ObjI object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }
}