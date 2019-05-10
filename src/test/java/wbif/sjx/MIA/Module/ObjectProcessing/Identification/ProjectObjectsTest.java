package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Objects3D;
import wbif.sjx.MIA.ExpectedObjects.Objects2D;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.common.Object.Point;

import java.util.TreeSet;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 03/09/2017.
 */
public class ProjectObjectsTest extends ModuleTest {
    private double tolerance = 1E-2;

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetTitle() {
        assertNotNull(new ProjectObjects(null).getTitle());

    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ProjectObjects(null).getHelp());

    }

    @Test
    public void testRun() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Input objects";
        String outputObjectsName = "Output objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection inputObjects = new Objects3D().getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,false);
        workspace.addObjects(inputObjects);

        // Initialising ProjectObjects
        ProjectObjects projectObjects = new ProjectObjects(null);
        projectObjects.initialiseParameters();
        projectObjects.updateParameterValue(ProjectObjects.INPUT_OBJECTS,inputObjectsName);
        projectObjects.updateParameterValue(ProjectObjects.OUTPUT_OBJECTS,outputObjectsName);

        // Running ProjectObjects
        projectObjects.execute(workspace);

        // Testing there are now 2 sets of objects in the workspace and they have the expected names
        assertEquals(2,workspace.getObjects().size());
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertNotNull(workspace.getObjectSet(outputObjectsName));

        // Testing number of objects in projected set
        assertEquals(8,workspace.getObjectSet(outputObjectsName).size());

        // Getting expected and actual objects
        ObjCollection expectedObjects = new Objects2D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        ObjCollection actualObjects = workspace.getObjectSet(outputObjectsName);

        TreeSet<Point<Integer>> expPoints = expectedObjects.get(3).getPoints();

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }
}