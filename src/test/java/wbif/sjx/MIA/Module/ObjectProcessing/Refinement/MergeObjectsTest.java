package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.MIA.ExpectedObjects.MergedObjects3D;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Objects3D;
import wbif.sjx.MIA.ExpectedObjects.Spots3D;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.common.Exceptions.IntegerOverflowException;

import static org.junit.Assert.*;

public class MergeObjectsTest extends ModuleTest {
    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetTitle() {
        assertNotNull(new MergeObjects().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MergeObjects().getHelp());
    }

    @Test
    public void testRunWithoutObjectDeletion() throws IntegerOverflowException {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection inputObj1 = new Objects3D().getObjects("Input_obj_1", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj1);
        ObjCollection inputObj2 = new Spots3D().getObjects("Input_obj_2",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj2);

        // Initialising FilterObjectsMethods module
        MergeObjects mergeObjects = new MergeObjects();
        mergeObjects.updateParameterValue(MergeObjects.INPUT_OBJECTS_1,"Input_obj_1");
        mergeObjects.updateParameterValue(MergeObjects.INPUT_OBJECTS_2,"Input_obj_2");
        mergeObjects.updateParameterValue(MergeObjects.OUTPUT_OBJECTS,"Output_obj");

        // Running the module
        mergeObjects.execute(workspace);

        // Getting expected output objects
        ObjCollection expectedOutputObj= new MergedObjects3D().getObjects("Output_obj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(expectedOutputObj);

        // Getting actual output objects
        ObjCollection actualOutputObj = workspace.getObjectSet("Output_obj");

        // Checking the number of detected objects
        assertEquals(33,actualOutputObj.size());
        assertEquals(8,workspace.getObjectSet("Input_obj_1").size());
        assertEquals(25,workspace.getObjectSet("Input_obj_2").size());

        for (Obj object:actualOutputObj.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedOutputObj.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }
}