package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Objects3D;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.*;

import static org.junit.Assert.*;

public class WithWithoutParentTest extends ModuleTest {
    @Override
    public void testGetTitle() {
        assertNotNull(new WithWithoutParent().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new WithWithoutParent().getHelp());
    }

    @Test
    public void testRunPresentParentDoNothing() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        ObjCollection parentObjects = new ObjCollection("Parents");
        ObjCollection expectedPassObjects = new ObjCollection("PassOutput");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj("Parents", parentObjects.getAndIncrementID(), dppXY, dppZ, calibratedUnits,false);
                parentObjects.add(parentObject);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            }

            expectedPassObjects.add(testObject);

        }

        // Initialising FilterObjects module
        WithWithoutParent withWithoutParent = new WithWithoutParent();
        withWithoutParent.updateParameterValue(WithWithoutParent.INPUT_OBJECTS,"TestObj");
        withWithoutParent.updateParameterValue(WithWithoutParent.FILTER_METHOD,WithWithoutParent.FilterMethods.WITH_PARENT);
        withWithoutParent.updateParameterValue(WithWithoutParent.FILTER_MODE,WithWithoutParent.FilterModes.DO_NOTHING);
        withWithoutParent.updateParameterValue(WithWithoutParent.PARENT_OBJECT,"Parents");

        // Running the module
        withWithoutParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(8,workspace.getObjectSet("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjectSet("TestObj"));

        String metadataName = WithWithoutParent.getMetadataName("TestObj",WithWithoutParent.FilterMethods.WITH_PARENT,"Parents");
        String metadata = workspace.getMetadata().getAsString(metadataName);
        assertEquals("3",metadata);

    }

    @Test
    public void testRunPresentParentMove() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        ObjCollection parentObjects = new ObjCollection("Parents");
        ObjCollection expectedPassObjects = new ObjCollection("PassOutput");
        ObjCollection expectedFailObjects = new ObjCollection("FailOutput");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj("Parents", parentObjects.getAndIncrementID(), dppXY, dppZ, calibratedUnits,false);
                parentObjects.add(parentObject);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

                expectedFailObjects.add(testObject);

            } else {
                expectedPassObjects.add(testObject);
            }
        }

        // Initialising FilterObjects module
        WithWithoutParent withWithoutParent = new WithWithoutParent();
        withWithoutParent.updateParameterValue(WithWithoutParent.INPUT_OBJECTS,"TestObj");
        withWithoutParent.updateParameterValue(WithWithoutParent.FILTER_METHOD,WithWithoutParent.FilterMethods.WITH_PARENT);
        withWithoutParent.updateParameterValue(WithWithoutParent.FILTER_MODE,WithWithoutParent.FilterModes.MOVE_FILTERED);
        withWithoutParent.updateParameterValue(WithWithoutParent.PARENT_OBJECT,"Parents");
        withWithoutParent.updateParameterValue(WithWithoutParent.OUTPUT_FILTERED_OBJECTS,"Output");

        // Running the module
        withWithoutParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(5,workspace.getObjectSet("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjectSet("TestObj"));

        assertNotNull(workspace.getObjectSet("Output"));
        assertEquals(3,workspace.getObjectSet("Output").values().size());
        assertEquals(expectedFailObjects,workspace.getObjectSet("Output"));

    }

    @Test
    public void testRunPresentParentRemove() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        ObjCollection parentObjects = new ObjCollection("Parents");
        ObjCollection expectedPassObjects = new ObjCollection("PassOutput");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj("Parents", parentObjects.getAndIncrementID(), dppXY, dppZ, calibratedUnits,false);
                parentObjects.add(parentObject);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            } else {
                expectedPassObjects.add(testObject);

            }
        }

        // Initialising FilterObjects module
        WithWithoutParent withWithoutParent = new WithWithoutParent();
        withWithoutParent.updateParameterValue(WithWithoutParent.INPUT_OBJECTS,"TestObj");
        withWithoutParent.updateParameterValue(WithWithoutParent.FILTER_METHOD,WithWithoutParent.FilterMethods.WITH_PARENT);
        withWithoutParent.updateParameterValue(WithWithoutParent.FILTER_MODE,WithWithoutParent.FilterModes.REMOVE_FILTERED);
        withWithoutParent.updateParameterValue(WithWithoutParent.PARENT_OBJECT,"Parents");

        // Running the module
        withWithoutParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(5,workspace.getObjectSet("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjectSet("TestObj"));

    }

    @Test
    public void testRunMissingParentDoNothing() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        ObjCollection parentObjects = new ObjCollection("Parents");
        ObjCollection expectedPassObjects = new ObjCollection("PassOutput");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj("Parents", parentObjects.getAndIncrementID(), dppXY, dppZ, calibratedUnits,false);
                parentObjects.add(parentObject);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            }

            expectedPassObjects.add(testObject);

        }

        // Initialising FilterObjects module
        WithWithoutParent withWithoutParent = new WithWithoutParent();
        withWithoutParent.updateParameterValue(WithWithoutParent.INPUT_OBJECTS,"TestObj");
        withWithoutParent.updateParameterValue(WithWithoutParent.FILTER_METHOD,WithWithoutParent.FilterMethods.WITHOUT_PARENT);
        withWithoutParent.updateParameterValue(WithWithoutParent.FILTER_MODE,WithWithoutParent.FilterModes.DO_NOTHING);
        withWithoutParent.updateParameterValue(WithWithoutParent.PARENT_OBJECT,"Parents");

        // Running the module
        withWithoutParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(8,workspace.getObjectSet("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjectSet("TestObj"));

    }

    @Test
    public void testRunMissingParentMove() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        ObjCollection parentObjects = new ObjCollection("Parents");
        ObjCollection expectedPassObjects = new ObjCollection("PassOutput");
        ObjCollection expectedFailObjects = new ObjCollection("FailOutput");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj("Parents", parentObjects.getAndIncrementID(), dppXY, dppZ, calibratedUnits,false);
                parentObjects.add(parentObject);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

                expectedPassObjects.add(testObject);

            } else {
                expectedFailObjects.add(testObject);
            }
        }

        // Initialising FilterObjects module
        WithWithoutParent withWithoutParent = new WithWithoutParent();
        withWithoutParent.updateParameterValue(WithWithoutParent.INPUT_OBJECTS,"TestObj");
        withWithoutParent.updateParameterValue(WithWithoutParent.FILTER_METHOD,WithWithoutParent.FilterMethods.WITHOUT_PARENT);
        withWithoutParent.updateParameterValue(WithWithoutParent.FILTER_MODE,WithWithoutParent.FilterModes.MOVE_FILTERED);
        withWithoutParent.updateParameterValue(WithWithoutParent.PARENT_OBJECT,"Parents");
        withWithoutParent.updateParameterValue(WithWithoutParent.OUTPUT_FILTERED_OBJECTS,"Output");

        // Running the module
        withWithoutParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(3,workspace.getObjectSet("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjectSet("TestObj"));

        assertNotNull(workspace.getObjectSet("Output"));
        assertEquals(5,workspace.getObjectSet("Output").values().size());
        assertEquals(expectedFailObjects,workspace.getObjectSet("Output"));

    }

    @Test
    public void testRunMissingParentRemove() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        ObjCollection parentObjects = new ObjCollection("Parents");
        ObjCollection expectedPassObjects = new ObjCollection("PassOutput");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj("Parents", parentObjects.getAndIncrementID(), dppXY, dppZ, calibratedUnits,false);
                parentObjects.add(parentObject);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

                expectedPassObjects.add(testObject);

            }
        }

        // Initialising FilterObjects module
        WithWithoutParent withWithoutParent = new WithWithoutParent();
        withWithoutParent.updateParameterValue(WithWithoutParent.INPUT_OBJECTS,"TestObj");
        withWithoutParent.updateParameterValue(WithWithoutParent.FILTER_METHOD,WithWithoutParent.FilterMethods.WITHOUT_PARENT);
        withWithoutParent.updateParameterValue(WithWithoutParent.FILTER_MODE,WithWithoutParent.FilterModes.REMOVE_FILTERED);
        withWithoutParent.updateParameterValue(WithWithoutParent.PARENT_OBJECT,"Parents");

        // Running the module
        withWithoutParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(3,workspace.getObjectSet("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjectSet("TestObj"));

    }

    @Test
    public void testRunWithParent() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,true,true};
        ObjCollection parentObjects = new ObjCollection("Parents");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj("Parents", parentObjects.getAndIncrementID(), dppXY, dppZ, calibratedUnits,false);
                parentObjects.add(parentObject);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            }
        }

        // Initialising FilterObjects module
        WithWithoutParent withWithoutParent = new WithWithoutParent();
        withWithoutParent.updateParameterValue(WithWithoutParent.INPUT_OBJECTS,"TestObj");
        withWithoutParent.updateParameterValue(WithWithoutParent.FILTER_METHOD,WithWithoutParent.FilterMethods.WITH_PARENT);
        withWithoutParent.updateParameterValue(WithWithoutParent.PARENT_OBJECT,"Parents");

        // Running the module
        withWithoutParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(3,workspace.getObjectSet("TestObj").values().size());

    }
}