package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Objects3D;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.common.Object.Volume.SpatCal;
import wbif.sjx.common.Object.Volume.VolumeType;

import static org.junit.jupiter.api.Assertions.*;

public class FilterWithWithoutParentTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new FilterWithWithoutParent(null).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunPresentParentDoNothing(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        ObjCollection parentObjects = new ObjCollection("Parents",calibration,1);
        ObjCollection expectedPassObjects = new ObjCollection("PassOutput",calibration,1);

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj(volumeType,"Parents",parentObjects.getAndIncrementID(),calibration,1);
                parentObjects.add(parentObject);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            }

            expectedPassObjects.add(testObject);

        }

        // Initialising FilterObjects module
        FilterWithWithoutParent filterWithWithoutParent = new FilterWithWithoutParent(null);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.INPUT_OBJECTS,"TestObj");
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_METHOD,FilterWithWithoutParent.FilterMethods.WITH_PARENT);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.FILTER_MODE,FilterWithWithoutParent.FilterModes.DO_NOTHING);
        filterWithWithoutParent.updateParameterValue(FilterWithWithoutParent.PARENT_OBJECT,"Parents");

        // Running the module
        filterWithWithoutParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(8,workspace.getObjectSet("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjectSet("TestObj"));

        String metadataName = FilterWithWithoutParent.getMetadataName("TestObj",FilterWithWithoutParent.FilterMethods.WITH_PARENT,"Parents");
        String metadata = workspace.getMetadata().getAsString(metadataName);
        assertEquals("3",metadata);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunPresentParentMove(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        ObjCollection parentObjects = new ObjCollection("Parents",calibration,1);
        ObjCollection expectedPassObjects = new ObjCollection("PassOutput",calibration,1);
        ObjCollection expectedFailObjects = new ObjCollection("FailOutput",calibration,1);

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj(volumeType,"Parents",parentObjects.getAndIncrementID(),calibration,1);
                parentObjects.add(parentObject);

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
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(5,workspace.getObjectSet("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjectSet("TestObj"));

        assertNotNull(workspace.getObjectSet("Output"));
        assertEquals(3,workspace.getObjectSet("Output").values().size());
        assertEquals(expectedFailObjects,workspace.getObjectSet("Output"));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunPresentParentRemove(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        ObjCollection parentObjects = new ObjCollection("Parents",calibration,1);
        ObjCollection expectedPassObjects = new ObjCollection("PassOutput",calibration,1);

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj(volumeType,"Parents",parentObjects.getAndIncrementID(),calibration,1);
                parentObjects.add(parentObject);

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
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(5,workspace.getObjectSet("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjectSet("TestObj"));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunMissingParentDoNothing(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        ObjCollection parentObjects = new ObjCollection("Parents",calibration,1);
        ObjCollection expectedPassObjects = new ObjCollection("PassOutput",calibration,1);

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj(volumeType,"Parents",parentObjects.getAndIncrementID(),calibration,1);
                parentObjects.add(parentObject);

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
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(8,workspace.getObjectSet("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjectSet("TestObj"));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunMissingParentMove(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        ObjCollection parentObjects = new ObjCollection("Parents",calibration,1);
        ObjCollection expectedPassObjects = new ObjCollection("PassOutput",calibration,1);
        ObjCollection expectedFailObjects = new ObjCollection("FailOutput",calibration,1);

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj(volumeType,"Parents",parentObjects.getAndIncrementID(),calibration,1);
                parentObjects.add(parentObject);

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
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(3,workspace.getObjectSet("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjectSet("TestObj"));

        assertNotNull(workspace.getObjectSet("Output"));
        assertEquals(5,workspace.getObjectSet("Output").values().size());
        assertEquals(expectedFailObjects,workspace.getObjectSet("Output"));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunMissingParentRemove(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,false};
        ObjCollection parentObjects = new ObjCollection("Parents",calibration,1);
        ObjCollection expectedPassObjects = new ObjCollection("PassOutput",calibration,1);

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj(volumeType,"Parents",parentObjects.getAndIncrementID(),calibration,1);
                parentObjects.add(parentObject);

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
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(3,workspace.getObjectSet("TestObj").values().size());
        assertEquals(expectedPassObjects,workspace.getObjectSet("TestObj"));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWithParent(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,true,true};
        ObjCollection parentObjects = new ObjCollection("Parents",calibration,1);

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj(volumeType,"Parents",parentObjects.getAndIncrementID(),calibration,1);
                parentObjects.add(parentObject);

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
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(3,workspace.getObjectSet("TestObj").values().size());

    }
}