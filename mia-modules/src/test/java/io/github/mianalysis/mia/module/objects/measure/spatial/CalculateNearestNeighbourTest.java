package io.github.mianalysis.mia.module.objects.measure.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.mianalysis.mia.expectedobjects.VolumeTypes;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import ome.units.UNITS;


public class CalculateNearestNeighbourTest extends ModuleTest {
    private double tolerance = 1E-2;

    @Override
    public void testGetHelp() {
        assertNotNull(new CalculateNearestNeighbour(new Modules()).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetNearestNeighbour(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs("Objects 1", 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);

        ObjI obj1 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        obj1.addCoord(10, 20, 40);

        ObjI obj2 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        obj2.addCoord(20, 30, 10);

        ObjI obj3 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 3);
        obj3.addCoord(20, 20, 30);

        ObjI obj4 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 4);
        obj4.addCoord(50, 20, 10);

        // Testing against first object in set
        String referenceMode = CalculateNearestNeighbour.ReferenceModes.CENTROID_3D;
        CalculateNearestNeighbour calculateNearestNeighbour = new CalculateNearestNeighbour(new Modules());
        ObjI nearestNeighour = calculateNearestNeighbour.getNearestNeighbour(obj1, objects1, referenceMode,
                Double.MAX_VALUE, false, null);

        assertEquals(obj3, nearestNeighour);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetNearestNeighbourOverlapping(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs("Objects 1", 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);

        ObjI obj1 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        obj1.addCoord(10, 20, 40);

        ObjI obj2 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        obj2.addCoord(20, 30, 10);

        ObjI obj3 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 3);
        obj3.addCoord(20, 20, 30);

        ObjI obj4 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 4);
        obj4.addCoord(50, 20, 10);

        ObjI obj5 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 5);
        obj5.addCoord(10, 20, 40);

        // Testing against first object in set
        String referenceMode = CalculateNearestNeighbour.ReferenceModes.CENTROID_3D;
        CalculateNearestNeighbour calculateNearestNeighbour = new CalculateNearestNeighbour(new Modules());
        ObjI nearestNeighour = calculateNearestNeighbour.getNearestNeighbour(obj1, objects1, referenceMode,
                Double.MAX_VALUE, false, null);

        assertEquals(obj5, nearestNeighour);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetNearestNeighbourLinkingDistanceNNFound(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs("Objects 1", 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);

        ObjI obj1 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        obj1.addCoord(10, 20, 40);

        ObjI obj2 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        obj2.addCoord(20, 30, 10);

        ObjI obj3 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 3);
        obj3.addCoord(20, 20, 30);

        ObjI obj4 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 4);
        obj4.addCoord(50, 20, 10);

        // Testing against first object in set
        String referenceMode = CalculateNearestNeighbour.ReferenceModes.CENTROID_3D;
        CalculateNearestNeighbour calculateNearestNeighbour = new CalculateNearestNeighbour(new Modules());
        ObjI nearestNeighour = calculateNearestNeighbour.getNearestNeighbour(obj1, objects1, referenceMode, 100d, false,
                null);

        assertEquals(obj3, nearestNeighour);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetNearestNeighbourLinkingDistanceNNNotFound(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs("Objects 1", 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);

        ObjI obj1 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        obj1.addCoord(10, 20, 40);

        ObjI obj2 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        obj2.addCoord(20, 30, 10);

        ObjI obj3 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 3);
        obj3.addCoord(20, 20, 30);

        ObjI obj4 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 4);
        obj4.addCoord(50, 20, 10);

        // Testing against first object in set
        String referenceMode = CalculateNearestNeighbour.ReferenceModes.CENTROID_3D;
        CalculateNearestNeighbour calculateNearestNeighbour = new CalculateNearestNeighbour(new Modules());
        ObjI nearestNeighour = calculateNearestNeighbour.getNearestNeighbour(obj1, objects1, referenceMode, 50d, false,
                null);

        assertNull(nearestNeighour);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunWithinSameSet(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs(inputObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);
        ObjI obj1 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        obj1.addCoord(10, 20, 40);
        ObjI obj2 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        obj2.addCoord(20, 30, 10);
        ObjI obj3 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 3);
        obj3.addCoord(20, 20, 30);
        ObjI obj4 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 4);
        obj4.addCoord(50, 20, 10);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new Modules())
                        .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS, inputObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE,
                                CalculateNearestNeighbour.RelationshipModes.WITHIN_SAME_SET)
                        .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS, null)
                        .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT, false)
                        .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS, null)
                        .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE, false)
                        .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE, Double.MAX_VALUE)
                        .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE, false);

        // Run module
        calculateNearestNeighbour.execute(workspace);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(3, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(50.99, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(4, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(31.62, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(1, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(50.99, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(2, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(31.62, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunWithinSameSetMaxDistAllPass(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs(inputObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);

        ObjI obj1 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        obj1.addCoord(10, 20, 40);

        ObjI obj2 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        obj2.addCoord(20, 30, 10);

        ObjI obj3 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 3);
        obj3.addCoord(20, 20, 30);

        ObjI obj4 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 4);
        obj4.addCoord(50, 20, 10);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new Modules())
                        .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS, inputObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE,
                                CalculateNearestNeighbour.RelationshipModes.WITHIN_SAME_SET)
                        .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS, null)
                        .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT, false)
                        .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS, null)
                        .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE, true)
                        .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE, 100d)
                        .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE, false);

        // Run module
        calculateNearestNeighbour.execute(workspace);

        // // Creating second object set
        // Objs objects2 = new Objs("Objects 2");
        //
        // Obj obj5 = new Obj(volumeType,"Objects
        // 2",1,dppXY,dppZ,calibratedUnits,false);
        // obj5.add(12,25,40);
        // objects2.addRef(obj5);
        //
        // Obj obj6 = new Obj(volumeType,"Objects
        // 2",2,dppXY,dppZ,calibratedUnits,false);
        // obj6.add(20,35,10);
        // objects2.addRef(obj6);
        //
        // Obj obj7 = new Obj(volumeType,"Objects
        // 2",3,dppXY,dppZ,calibratedUnits,false);
        // obj7.add(35,20,20);
        // objects2.addRef(obj7);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(3, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(50.99, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(4, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(31.62, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(1, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(50.99, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(2, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(31.62, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunWithinSameSetMaxDistSomeFail(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs(inputObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);

        ObjI obj1 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        obj1.addCoord(10, 20, 40);

        ObjI obj2 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        obj2.addCoord(20, 30, 10);

        ObjI obj3 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 3);
        obj3.addCoord(20, 20, 30);

        ObjI obj4 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 4);
        obj4.addCoord(50, 20, 10);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new Modules())
                        .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS, inputObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE,
                                CalculateNearestNeighbour.RelationshipModes.WITHIN_SAME_SET)
                        .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS, null)
                        .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT, false)
                        .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS, null)
                        .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE, true)
                        .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE, 50d)
                        .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE, false);

        // Run module
        calculateNearestNeighbour.execute(workspace);

        // // Creating second object set
        // Objs objects2 = new Objs("Objects 2");
        //
        // Obj obj5 = new Obj(volumeType,"Objects
        // 2",1,dppXY,dppZ,calibratedUnits,false);
        // obj5.add(12,25,40);
        // objects2.addRef(obj5);
        //
        // Obj obj6 = new Obj(volumeType,"Objects
        // 2",2,dppXY,dppZ,calibratedUnits,false);
        // obj6.add(20,35,10);
        // objects2.addRef(obj6);
        //
        // Obj obj7 = new Obj(volumeType,"Objects
        // 2",3,dppXY,dppZ,calibratedUnits,false);
        // obj7.add(35,20,20);
        // objects2.addRef(obj7);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(4, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(31.62, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(2, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(31.62, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunWithinSameSetMaxDistCalibratedSomeFail(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs(inputObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);

        ObjI obj1 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        obj1.addCoord(10, 20, 40);

        ObjI obj2 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        obj2.addCoord(20, 30, 10);

        ObjI obj3 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 3);
        obj3.addCoord(20, 20, 30);

        ObjI obj4 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 4);
        obj4.addCoord(50, 20, 10);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new Modules())
                        .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS, inputObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE,
                                CalculateNearestNeighbour.RelationshipModes.WITHIN_SAME_SET)
                        .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS, null)
                        .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT, false)
                        .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS, null)
                        .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE, true)
                        .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE, 1d)
                        .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE, true);

        // Run module
        calculateNearestNeighbour.execute(workspace);

        // // Creating second object set
        // Objs objects2 = new Objs("Objects 2");
        //
        // Obj obj5 = new Obj(volumeType,"Objects
        // 2",1,dppXY,dppZ,calibratedUnits,false);
        // obj5.add(12,25,40);
        // objects2.addRef(obj5);
        //
        // Obj obj6 = new Obj(volumeType,"Objects
        // 2",2,dppXY,dppZ,calibratedUnits,false);
        // obj6.add(20,35,10);
        // objects2.addRef(obj6);
        //
        // Obj obj7 = new Obj(volumeType,"Objects
        // 2",3,dppXY,dppZ,calibratedUnits,false);
        // obj7.add(35,20,20);
        // objects2.addRef(obj7);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(4, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(31.62, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(2, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(31.62, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunWithinSameSetWithinParent(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String parentObjectsName = "Parents";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs(inputObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);
        ObjI obj1 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        obj1.addCoord(10, 20, 40);
        ObjI obj2 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        obj2.addCoord(20, 30, 10);
        ObjI obj3 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 3);
        obj3.addCoord(20, 20, 30);
        ObjI obj4 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 4);
        obj4.addCoord(50, 20, 10);

        // Creating the parent object set
        ObjsI parents = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(parents);

        ObjI parent1 = parents.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        parents.add(parent1);
        parent1.addChild(obj1);
        obj1.addParent(parent1);
        parent1.addChild(obj2);
        obj2.addParent(parent1);
        parent1.addChild(obj4);
        obj4.addParent(parent1);

        ObjI parent2 = parents.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        parents.add(parent2);
        parent2.addChild(obj3);
        obj3.addParent(parent2);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new Modules())
                        .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS, inputObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE,
                                CalculateNearestNeighbour.RelationshipModes.WITHIN_SAME_SET)
                        .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS, null)
                        .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT, true)
                        .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS, parentObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE, false)
                        .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE, Double.MAX_VALUE)
                        .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE, false);

        // Run module
        calculateNearestNeighbour.execute(workspace);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(2, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(150.67, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(4, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(31.62, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(2, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(31.62, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunWithinSameSetWithinParentMaxDistSomeFail(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String parentObjectsName = "Parents";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs(inputObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);
        ObjI obj1 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        obj1.addCoord(10, 20, 40);
        ObjI obj2 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        obj2.addCoord(20, 30, 10);
        ObjI obj3 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 3);
        obj3.addCoord(20, 20, 30);
        ObjI obj4 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 4);
        obj4.addCoord(50, 20, 10);

        // Creating the parent object set
        ObjsI parents = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(parents);

        ObjI parent1 = parents.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        parents.add(parent1);
        parent1.addChild(obj1);
        obj1.addParent(parent1);
        parent1.addChild(obj2);
        obj2.addParent(parent1);
        parent1.addChild(obj4);
        obj4.addParent(parent1);

        ObjI parent2 = parents.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        parents.add(parent2);
        parent2.addChild(obj3);
        obj3.addParent(parent2);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new Modules())
                        .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS, inputObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE,
                                CalculateNearestNeighbour.RelationshipModes.WITHIN_SAME_SET)
                        .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS, null)
                        .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT, true)
                        .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS, parentObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE, true)
                        .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE, 100d)
                        .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE, false);

        // Run module
        calculateNearestNeighbour.execute(workspace);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(4, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(31.62, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(2, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(31.62, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunDifferentSets(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String secondObjectsName = "Objects 2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs(inputObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);
        ObjI obj1 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        obj1.addCoord(10, 20, 40);
        ObjI obj2 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        obj2.addCoord(20, 30, 10);
        ObjI obj3 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 3);
        obj3.addCoord(20, 20, 30);
        ObjI obj4 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 4);
        obj4.addCoord(50, 20, 10);

        // Creating second object set
        ObjsI objects2 = ObjsFactories.getDefaultFactory().createObjs(secondObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects2);
        ObjI obj5 = objects2.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 5);
        obj5.addCoord(12, 25, 40);
        ObjI obj6 = objects2.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 6);
        obj6.addCoord(20, 35, 10);
        ObjI obj7 = objects2.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 7);
        obj7.addCoord(35, 20, 20);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new Modules())
                        .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS, inputObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE,
                                CalculateNearestNeighbour.RelationshipModes.DIFFERENT_SET)
                        .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS, secondObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT, false)
                        .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS, null)
                        .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE, false)
                        .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE, Double.MAX_VALUE)
                        .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE, false);

        // Run module
        calculateNearestNeighbour.execute(workspace);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(5, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(5.39, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(6, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(5, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(5, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(50.88, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(6, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(33.54, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunDifferentSetsMaxDistSomePass(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String secondObjectsName = "Objects 2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs(inputObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);
        ObjI obj1 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        obj1.addCoord(10, 20, 40);
        ObjI obj2 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        obj2.addCoord(20, 30, 10);
        ObjI obj3 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 3);
        obj3.addCoord(20, 20, 30);
        ObjI obj4 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 4);
        obj4.addCoord(50, 20, 10);

        // Creating second object set
        ObjsI objects2 = ObjsFactories.getDefaultFactory().createObjs(secondObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects2);
        ObjI obj5 = objects2.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 5);
        obj5.addCoord(12, 25, 40);
        ObjI obj6 = objects2.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 6);
        obj6.addCoord(20, 35, 10);
        ObjI obj7 = objects2.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 7);
        obj7.addCoord(35, 20, 20);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new Modules())
                        .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS, inputObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE,
                                CalculateNearestNeighbour.RelationshipModes.DIFFERENT_SET)
                        .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS, secondObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT, false)
                        .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS, null)
                        .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE, true)
                        .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE, 50d)
                        .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE, false);

        // Run module
        calculateNearestNeighbour.execute(workspace);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(5, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(5.39, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(6, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(5, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(6, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(33.54, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunDifferentSetsWithinParent(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String secondObjectsName = "Objects 2";
        String parentObjectsName = "Parents";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs(inputObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);
        ObjI obj1 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        obj1.addCoord(10, 20, 40);
        ObjI obj2 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        obj2.addCoord(20, 30, 10);
        ObjI obj3 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 3);
        obj3.addCoord(20, 20, 30);
        ObjI obj4 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 4);
        obj4.addCoord(50, 20, 10);

        // Creating second object set
        ObjsI objects2 = ObjsFactories.getDefaultFactory().createObjs(secondObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects2);
        ObjI obj5 = objects2.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 5);
        obj5.addCoord(12, 25, 40);
        ObjI obj6 = objects2.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 6);
        obj6.addCoord(20, 35, 10);
        ObjI obj7 = objects2.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 7);
        obj7.addCoord(35, 20, 20);

        // Creating the parent object set
        ObjsI parents = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(parents);

        ObjI parent1 = parents.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        parents.add(parent1);
        parent1.addChild(obj1);
        obj1.addParent(parent1);
        parent1.addChild(obj2);
        obj2.addParent(parent1);
        parent1.addChild(obj7);
        obj7.addParent(parent1);

        ObjI parent2 = parents.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        parents.add(parent2);
        parent2.addChild(obj3);
        obj3.addParent(parent2);
        parent2.addChild(obj4);
        obj4.addParent(parent2);
        parent2.addChild(obj5);
        obj5.addParent(parent2);
        parent2.addChild(obj6);
        obj6.addParent(parent2);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new Modules())
                        .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS, inputObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE,
                                CalculateNearestNeighbour.RelationshipModes.DIFFERENT_SET)
                        .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS, secondObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT, true)
                        .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS, parentObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE, false)
                        .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE, Double.MAX_VALUE)
                        .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE, false);

        // Run module
        calculateNearestNeighbour.execute(workspace);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(7, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(103.08, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(7, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(53.15, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(5, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(50.88, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(6, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(33.54, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunDifferentSetsWithinParentMaxDistSomeFail(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String secondObjectsName = "Objects 2";
        String parentObjectsName = "Parents";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs(inputObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);
        ObjI obj1 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        obj1.addCoord(10, 20, 40);
        ObjI obj2 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        obj2.addCoord(20, 30, 10);
        ObjI obj3 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 3);
        obj3.addCoord(20, 20, 30);
        ObjI obj4 = objects1.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 4);
        obj4.addCoord(50, 20, 10);

        // Creating second object set
        ObjsI objects2 = ObjsFactories.getDefaultFactory().createObjs(secondObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects2);
        ObjI obj5 = objects2.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 5);
        obj5.addCoord(12, 25, 40);
        ObjI obj6 = objects2.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 6);
        obj6.addCoord(20, 35, 10);
        ObjI obj7 = objects2.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 7);
        obj7.addCoord(35, 20, 20);

        // Creating the parent object set
        ObjsI parents = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName, 60, 50, 50, dppXY, dppZ, calibratedUnits, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(parents);

        ObjI parent1 = parents.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 1);
        parents.add(parent1);
        parent1.addChild(obj1);
        obj1.addParent(parent1);
        parent1.addChild(obj2);
        obj2.addParent(parent1);
        parent1.addChild(obj7);
        obj7.addParent(parent1);

        ObjI parent2 = parents.createAndAddNewObjectWithID(VolumeTypes.getFactory(volumeType), 2);
        parents.add(parent2);
        parent2.addChild(obj3);
        obj3.addParent(parent2);
        parent2.addChild(obj4);
        obj4.addParent(parent2);
        parent2.addChild(obj5);
        obj5.addParent(parent2);
        parent2.addChild(obj6);
        obj6.addParent(parent2);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new Modules())
                        .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS, inputObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE,
                                CalculateNearestNeighbour.RelationshipModes.DIFFERENT_SET)
                        .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS, secondObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT, true)
                        .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS, parentObjectsName)
                        .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE, true)
                        .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE, 50d)
                        .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE, false);

        // Run module
        calculateNearestNeighbour.execute(workspace);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(6, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName, CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        assertEquals(33.54, obj4.getMeasurement(name).getValue(), tolerance);

    }
}