package io.github.mianalysis.mia.module.objects.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.mianalysis.mia.expectedobjects.ExpectedObjects;
import io.github.mianalysis.mia.expectedobjects.Objects3D;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import ome.units.UNITS;


public class FilterWithWithoutParentTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new FilterWithWithoutParent(null).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunPresentParentDoNothing(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 1, 1, 1);

        // Getting test objects
        Objs testObjects = new Objects3D(volumeType).getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[] { true, true, false, true, false, false, false, false };
        Objs parentObjects = new Objs("Parents", calibration, 1, 0.02, UNITS.SECOND);
        Objs expectedPassObjects = new Objs("PassOutput", calibration, 1, 0.02, UNITS.SECOND);

        int counter = 0;
        for (Obj testObject : testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = parentObjects.createAndAddNewObject(volumeType);                
                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            }
            expectedPassObjects.add(testObject);
        }

        // Initialising FilterObjects module
        FilterWithWithoutParent filterWithWithoutParent = new FilterWithWithoutParent(null);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.INPUT_OBJECTS, "TestObj");
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_METHOD,
                FilterWithWithoutParent.FilterMethods.WITH_PARENT);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_MODE,
                FilterWithWithoutParent.FilterModes.DO_NOTHING);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.PARENT_OBJECT, "Parents");
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.STORE_RESULTS, true);

        // Running the module
        filterWithWithoutParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjects("TestObj"));
        assertEquals(8, workspace.getObjects("TestObj").values().size());
        assertEquals(expectedPassObjects, workspace.getObjects("TestObj"));

        String metadataName = FilterWithWithoutParent.getMetadataName("TestObj",
                FilterWithWithoutParent.FilterMethods.WITH_PARENT, "Parents");
        String metadata = workspace.getMetadata().getAsString(metadataName);

        assertEquals("3",metadata);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunPresentParentMove(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Getting test objects
        Objs testObjects = new Objects3D(volumeType).getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        Objs parentObjects = new Objs("Parents",calibration,1,0.02,UNITS.SECOND);
        Objs expectedPassObjects = new Objs("PassOutput",calibration,1,0.02,UNITS.SECOND);
        Objs expectedFailObjects = new Objs("FailOutput",calibration,1,0.02,UNITS.SECOND);

        int counter = 0;
        for (Obj testObject : testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = parentObjects.createAndAddNewObject(volumeType);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

                expectedFailObjects.add(testObject);

            } else {
                expectedPassObjects.add(testObject);
            }
        }

        // Initialising FilterObjects module
        FilterWithWithoutParent filterWithWithoutParent = new FilterWithWithoutParent(null);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.INPUT_OBJECTS,"TestObj");
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_METHOD,FilterWithWithoutParent.FilterMethods.WITH_PARENT);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_MODE,FilterWithWithoutParent.FilterModes.MOVE_FILTERED);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.PARENT_OBJECT,"Parents");
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.OUTPUT_FILTERED_OBJECTS,"Output");

        // Running the module
        filterWithWithoutParent.execute(workspace);

        // Checking basic facts                
        assertNotNull(workspace.getObjects("TestObj"));
        assertEquals(5, workspace.getObjects("TestObj").values().size());
        Objs actualPassObjects = workspace.getObjects("TestObj");
        for (Obj expectedPassObject : expectedPassObjects.values()) {
            Obj actualPassObject = actualPassObjects.get(expectedPassObject.getID());
            assertEquals(expectedPassObject, actualPassObject);
        }
        // assertEquals(expectedPassObjects,workspace.getObjects("TestObj"));

        assertNotNull(workspace.getObjects("Output"));
        assertEquals(3, workspace.getObjects("Output").values().size());
        Objs actualFailObjects = workspace.getObjects("Output");
        for (Obj expectedFailObject : expectedFailObjects.values()) {
            Obj actualFailObject = actualFailObjects.get(expectedFailObject.getID());
            assertEquals(expectedFailObject, actualFailObject);
        }
        // assertEquals(expectedFailObjects,workspace.getObjects("Output"));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunPresentParentRemove(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Getting test objects
        Objs testObjects = new Objects3D(volumeType).getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        Objs parentObjects = new Objs("Parents",calibration,1,0.02,UNITS.SECOND);
        Objs expectedPassObjects = new Objs("PassOutput",calibration,1,0.02,UNITS.SECOND);

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = parentObjects.createAndAddNewObject(volumeType);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            } else {
                expectedPassObjects.add(testObject);

            }
        }

        // Initialising FilterObjects module
        FilterWithWithoutParent filterWithWithoutParent = new FilterWithWithoutParent(null);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.INPUT_OBJECTS,"TestObj");
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_METHOD,FilterWithWithoutParent.FilterMethods.WITH_PARENT);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_MODE,FilterWithWithoutParent.FilterModes.REMOVE_FILTERED);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.PARENT_OBJECT,"Parents");

        // Running the module
        filterWithWithoutParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjects("TestObj"));
        assertEquals(5,workspace.getObjects("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjects("TestObj"));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunMissingParentDoNothing(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Getting test objects
        Objs testObjects = new Objects3D(volumeType).getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        Objs parentObjects = new Objs("Parents",calibration,1,0.02,UNITS.SECOND);
        Objs expectedPassObjects = new Objs("PassOutput",calibration,1,0.02,UNITS.SECOND);

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = parentObjects.createAndAddNewObject(volumeType);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            }

            expectedPassObjects.add(testObject);

        }

        // Initialising FilterObjects module
        FilterWithWithoutParent filterWithWithoutParent = new FilterWithWithoutParent(null);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.INPUT_OBJECTS,"TestObj");
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_METHOD,FilterWithWithoutParent.FilterMethods.WITHOUT_PARENT);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_MODE,FilterWithWithoutParent.FilterModes.DO_NOTHING);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.PARENT_OBJECT,"Parents");

        // Running the module
        filterWithWithoutParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjects("TestObj"));
        assertEquals(8,workspace.getObjects("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjects("TestObj"));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunMissingParentMove(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Getting test objects
        Objs testObjects = new Objects3D(volumeType).getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        Objs parentObjects = new Objs("Parents",calibration,1,0.02,UNITS.SECOND);
        Objs expectedPassObjects = new Objs("PassOutput",calibration,1,0.02,UNITS.SECOND);
        Objs expectedFailObjects = new Objs("FailOutput",calibration,1,0.02,UNITS.SECOND);

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = parentObjects.createAndAddNewObject(volumeType);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

                expectedPassObjects.add(testObject);

            } else {
                expectedFailObjects.add(testObject);
            }
        }

        // Initialising FilterObjects module
        FilterWithWithoutParent filterWithWithoutParent = new FilterWithWithoutParent(null);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.INPUT_OBJECTS,"TestObj");
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_METHOD,FilterWithWithoutParent.FilterMethods.WITHOUT_PARENT);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_MODE,FilterWithWithoutParent.FilterModes.MOVE_FILTERED);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.PARENT_OBJECT,"Parents");
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.OUTPUT_FILTERED_OBJECTS,"Output");

        // Running the module
        filterWithWithoutParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjects("TestObj"));
        assertEquals(3,workspace.getObjects("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjects("TestObj"));

        assertNotNull(workspace.getObjects("Output"));
        assertEquals(5,workspace.getObjects("Output").values().size());
        assertEquals(expectedFailObjects,workspace.getObjects("Output"));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunMissingParentRemove(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Getting test objects
        Objs testObjects = new Objects3D(volumeType).getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        Objs parentObjects = new Objs("Parents",calibration,1,0.02,UNITS.SECOND);
        Objs expectedPassObjects = new Objs("PassOutput",calibration,1,0.02,UNITS.SECOND);

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = parentObjects.createAndAddNewObject(volumeType);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

                expectedPassObjects.add(testObject);

            }
        }

        // Initialising FilterObjects module
        FilterWithWithoutParent filterWithWithoutParent = new FilterWithWithoutParent(null);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.INPUT_OBJECTS,"TestObj");
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_METHOD,FilterWithWithoutParent.FilterMethods.WITHOUT_PARENT);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_MODE,FilterWithWithoutParent.FilterModes.REMOVE_FILTERED);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.PARENT_OBJECT,"Parents");

        // Running the module
        filterWithWithoutParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjects("TestObj"));
        assertEquals(3,workspace.getObjects("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjects("TestObj"));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWithParent(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Getting test objects
        Objs testObjects = new Objects3D(volumeType).getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,true,true};
        Objs parentObjects = new Objs("Parents",calibration,1,0.02,UNITS.SECOND);

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = parentObjects.createAndAddNewObject(volumeType);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            }
        }

        // Initialising FilterObjects module
        FilterWithWithoutParent filterWithWithoutParent = new FilterWithWithoutParent(null);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.INPUT_OBJECTS,"TestObj");
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_METHOD,FilterWithWithoutParent.FilterMethods.WITH_PARENT);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.PARENT_OBJECT,"Parents");

        // Running the module
        filterWithWithoutParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjects("TestObj"));
        assertEquals(3,workspace.getObjects("TestObj").values().size());

    }
}