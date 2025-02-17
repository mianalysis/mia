package io.github.mianalysis.mia.module.objects.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.mianalysis.mia.expectedobjects.ExpectedObjects;
import io.github.mianalysis.mia.expectedobjects.MergedObjects3D;
import io.github.mianalysis.mia.expectedobjects.Objects3D;
import io.github.mianalysis.mia.expectedobjects.Spots3D;
import io.github.mianalysis.mia.expectedobjects.VolumeTypes;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.objects.relate.mergeobjects.CombineObjectSets;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;


public class CombineObjectSetsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CombineObjectSets(null).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunWithoutObjectDeletion(VolumeTypes volumeType) throws IntegerOverflowException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        Objs inputObj1 = new Objects3D(VolumeTypes.getFactory(volumeType)).getObjects("Input_obj_1", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj1);
        Objs inputObj2 = new Spots3D(VolumeTypes.getFactory(volumeType)).getObjects("Input_obj_2",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(inputObj2);

        // Initialising FilterObjects module
        CombineObjectSets combineObjectSets = new CombineObjectSets(null);
        combineObjectSets.updateParameterValue(CombineObjectSets.INPUT_OBJECTS_1,"Input_obj_1");
        combineObjectSets.updateParameterValue(CombineObjectSets.INPUT_OBJECTS_2,"Input_obj_2");
        combineObjectSets.updateParameterValue(CombineObjectSets.OUTPUT_OBJECTS,"Output_obj");

        // Running the module
        combineObjectSets.execute(workspace);

        // Getting expected output objects
        Objs expectedOutputObj= new MergedObjects3D(VolumeTypes.getFactory(volumeType)).getObjects("Output_obj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(expectedOutputObj);

        // Getting actual output objects
        Objs actualOutputObj = workspace.getObjects("Output_obj");

        // Checking the number of detected objects
        assertEquals(33,actualOutputObj.size());
        assertEquals(8,workspace.getObjects("Input_obj_1").size());
        assertEquals(25,workspace.getObjects("Input_obj_2").size());

        for (Obj object:actualOutputObj.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedOutputObj.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }
}