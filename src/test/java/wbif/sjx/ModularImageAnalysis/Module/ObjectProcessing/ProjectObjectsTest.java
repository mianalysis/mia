package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects2D;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;
import wbif.sjx.common.Object.Point;

import java.util.TreeSet;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 03/09/2017.
 */
public class ProjectObjectsTest {
    private double tolerance = 1E-2;

    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new ProjectObjects().getTitle());

    }

    @Test
    public void testRun() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting object parameters
        String inputObjectsName = "Input objects";
        String outputObjectsName = "Output objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection inputObjects = new ExpectedObjects3D().getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits,false);
        workspace.addObjects(inputObjects);

        // Initialising ProjectObjects
        ProjectObjects projectObjects = new ProjectObjects();
        projectObjects.initialiseParameters();
        projectObjects.updateParameterValue(ProjectObjects.INPUT_OBJECTS,inputObjectsName);
        projectObjects.updateParameterValue(ProjectObjects.OUTPUT_OBJECTS,outputObjectsName);

        // Running ProjectObjects
        projectObjects.run(workspace,false);

        // Testing there are now 2 sets of objects in the workspace and they have the expected names
        assertEquals(2,workspace.getObjects().size());
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertNotNull(workspace.getObjectSet(outputObjectsName));

        // Testing number of objects in projected set
        assertEquals(8,workspace.getObjectSet(outputObjectsName).size());

        // Getting expected and actual objects
        ObjCollection expectedObjects = new ExpectedObjects2D().getObjects("Expected",true,dppXY,dppZ,calibratedUnits,true);
        ObjCollection actualObjects = workspace.getObjectSet(outputObjectsName);

        TreeSet<Point<Integer>> expPoints = expectedObjects.get(3).getPoints();

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }
}