package io.github.mianalysis.mia.module.ObjectProcessing.Refinement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.mianalysis.mia.ExpectedObjects.ExpectedObjects;
import io.github.mianalysis.mia.ExpectedObjects.MergedObjects3D;
import io.github.mianalysis.mia.ExpectedObjects.Objects3D;
import io.github.mianalysis.mia.ExpectedObjects.Spots3D;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.Object.Obj;
import io.github.mianalysis.mia.Object.Objs;
import io.github.mianalysis.mia.Object.Workspace;
import io.github.mianalysis.mia.Object.Workspaces;
import io.github.sjcross.common.Exceptions.IntegerOverflowException;
import io.github.sjcross.common.Object.Volume.VolumeType;

public class CombineObjectSetsTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new CombineObjectSets(null).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWithoutObjectDeletion(VolumeType volumeType) throws IntegerOverflowException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        Objs inputObj1 = new Objects3D(volumeType).getObjects("Input_obj_1", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj1);
        Objs inputObj2 = new Spots3D(volumeType).getObjects("Input_obj_2",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj2);

        // Initialising FilterObjects module
        CombineObjectSets combineObjectSets = new CombineObjectSets(null);
        combineObjectSets.updateParameterValue(CombineObjectSets.INPUT_OBJECTS_1,"Input_obj_1");
        combineObjectSets.updateParameterValue(CombineObjectSets.INPUT_OBJECTS_2,"Input_obj_2");
        combineObjectSets.updateParameterValue(CombineObjectSets.OUTPUT_OBJECTS,"Output_obj");

        // Running the module
        combineObjectSets.execute(workspace);

        // Getting expected output objects
        Objs expectedOutputObj= new MergedObjects3D(volumeType).getObjects("Output_obj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(expectedOutputObj);

        // Getting actual output objects
        Objs actualOutputObj = workspace.getObjectSet("Output_obj");

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