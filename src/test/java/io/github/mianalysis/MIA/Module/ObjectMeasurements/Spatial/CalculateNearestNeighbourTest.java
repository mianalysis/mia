package io.github.mianalysis.MIA.Module.ObjectMeasurements.Spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ome.units.UNITS;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Module.ModuleTest;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.ObjCollection;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.WorkspaceCollection;
import io.github.sjcross.common.Exceptions.IntegerOverflowException;
import io.github.sjcross.common.Object.Volume.PointOutOfRangeException;
import io.github.sjcross.common.Object.Volume.SpatCal;
import io.github.sjcross.common.Object.Volume.VolumeType;

public class CalculateNearestNeighbourTest extends ModuleTest {
    private double tolerance = 1E-2;

    @Override
    public void testGetHelp() {
        assertNotNull(new CalculateNearestNeighbour(new ModuleCollection()).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetNearestNeighbour(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 60, 50, 50);

        // Creating first object set
        ObjCollection objects1 = new ObjCollection("Objects 1", calibration, 1, 0.02, UNITS.SECOND);

        Obj obj1 = objects1.createAndAddNewObject(volumeType, 1);
        obj1.add(10, 20, 40);

        Obj obj2 = objects1.createAndAddNewObject(volumeType, 2);
        obj2.add(20, 30, 10);

        Obj obj3 = objects1.createAndAddNewObject(volumeType, 3);
        obj3.add(20, 20, 30);

        Obj obj4 = objects1.createAndAddNewObject(volumeType, 4);
        obj4.add(50, 20, 10);

        // Testing against first object in set
        String referenceMode = CalculateNearestNeighbour.ReferenceModes.CENTROID_3D;
        CalculateNearestNeighbour calculateNearestNeighbour = new CalculateNearestNeighbour(new ModuleCollection());
        Obj nearestNeighour = calculateNearestNeighbour.getNearestNeighbour(obj1, objects1, referenceMode,
                Double.MAX_VALUE, false, null);

        assertEquals(obj3, nearestNeighour);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetNearestNeighbourOverlapping(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 60, 50, 50);

        // Creating first object set
        ObjCollection objects1 = new ObjCollection("Objects 1", calibration, 1, 0.02, UNITS.SECOND);

        Obj obj1 = objects1.createAndAddNewObject(volumeType, 1);
        obj1.add(10, 20, 40);

        Obj obj2 = objects1.createAndAddNewObject(volumeType, 2);
        obj2.add(20, 30, 10);

        Obj obj3 = objects1.createAndAddNewObject(volumeType, 3);
        obj3.add(20, 20, 30);

        Obj obj4 = objects1.createAndAddNewObject(volumeType, 4);
        obj4.add(50, 20, 10);

        Obj obj5 = objects1.createAndAddNewObject(volumeType, 5);
        obj5.add(10, 20, 40);

        // Testing against first object in set
        String referenceMode = CalculateNearestNeighbour.ReferenceModes.CENTROID_3D;
        CalculateNearestNeighbour calculateNearestNeighbour = new CalculateNearestNeighbour(new ModuleCollection());
        Obj nearestNeighour = calculateNearestNeighbour.getNearestNeighbour(obj1, objects1, referenceMode,
                Double.MAX_VALUE, false, null);

        assertEquals(obj5, nearestNeighour);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetNearestNeighbourLinkingDistanceNNFound(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 60, 50, 50);

        // Creating first object set
        ObjCollection objects1 = new ObjCollection("Objects 1", calibration, 1, 0.02, UNITS.SECOND);

        Obj obj1 = objects1.createAndAddNewObject(volumeType, 1);
        obj1.add(10, 20, 40);

        Obj obj2 = objects1.createAndAddNewObject(volumeType, 2);
        obj2.add(20, 30, 10);

        Obj obj3 = objects1.createAndAddNewObject(volumeType, 3);
        obj3.add(20, 20, 30);

        Obj obj4 = objects1.createAndAddNewObject(volumeType, 4);
        obj4.add(50, 20, 10);

        // Testing against first object in set
        String referenceMode = CalculateNearestNeighbour.ReferenceModes.CENTROID_3D;
        CalculateNearestNeighbour calculateNearestNeighbour = new CalculateNearestNeighbour(new ModuleCollection());
        Obj nearestNeighour = calculateNearestNeighbour.getNearestNeighbour(obj1, objects1, referenceMode, 100d, false,
                null);

        assertEquals(obj3, nearestNeighour);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetNearestNeighbourLinkingDistanceNNNotFound(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 60, 50, 50);

        // Creating first object set
        ObjCollection objects1 = new ObjCollection("Objects 1", calibration, 1, 0.02, UNITS.SECOND);

        Obj obj1 = objects1.createAndAddNewObject(volumeType, 1);
        obj1.add(10, 20, 40);

        Obj obj2 = objects1.createAndAddNewObject(volumeType, 2);
        obj2.add(20, 30, 10);

        Obj obj3 = objects1.createAndAddNewObject(volumeType, 3);
        obj3.add(20, 20, 30);

        Obj obj4 = objects1.createAndAddNewObject(volumeType, 4);
        obj4.add(50, 20, 10);

        // Testing against first object in set
        String referenceMode = CalculateNearestNeighbour.ReferenceModes.CENTROID_3D;
        CalculateNearestNeighbour calculateNearestNeighbour = new CalculateNearestNeighbour(new ModuleCollection());
        Obj nearestNeighour = calculateNearestNeighbour.getNearestNeighbour(obj1, objects1, referenceMode, 50d, false,
                null);

        assertNull(nearestNeighour);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWithinSameSet(VolumeType volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 60, 50, 50);

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);
        Obj obj1 = objects1.createAndAddNewObject(volumeType, 1);
        obj1.add(10, 20, 40);
        Obj obj2 = objects1.createAndAddNewObject(volumeType, 2);
        obj2.add(20, 30, 10);
        Obj obj3 = objects1.createAndAddNewObject(volumeType, 3);
        obj3.add(20, 20, 30);
        Obj obj4 = objects1.createAndAddNewObject(volumeType, 4);
        obj4.add(50, 20, 10);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new ModuleCollection())
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
                inputObjectsName);
        assertEquals(3, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(50.99, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertEquals(4, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(31.62, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertEquals(1, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(50.99, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertEquals(2, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(31.62, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWithinSameSetMaxDistAllPass(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 60, 50, 50);

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);

        Obj obj1 = objects1.createAndAddNewObject(volumeType, 1);
        obj1.add(10, 20, 40);

        Obj obj2 = objects1.createAndAddNewObject(volumeType, 2);
        obj2.add(20, 30, 10);

        Obj obj3 = objects1.createAndAddNewObject(volumeType, 3);
        obj3.add(20, 20, 30);

        Obj obj4 = objects1.createAndAddNewObject(volumeType, 4);
        obj4.add(50, 20, 10);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new ModuleCollection())
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
        // ObjCollection objects2 = new ObjCollection("Objects 2");
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
                inputObjectsName);
        assertEquals(3, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(50.99, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertEquals(4, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(31.62, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertEquals(1, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(50.99, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertEquals(2, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(31.62, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWithinSameSetMaxDistSomeFail(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 60, 50, 50);

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);

        Obj obj1 = objects1.createAndAddNewObject(volumeType, 1);
        obj1.add(10, 20, 40);

        Obj obj2 = objects1.createAndAddNewObject(volumeType, 2);
        obj2.add(20, 30, 10);

        Obj obj3 = objects1.createAndAddNewObject(volumeType, 3);
        obj3.add(20, 20, 30);

        Obj obj4 = objects1.createAndAddNewObject(volumeType, 4);
        obj4.add(50, 20, 10);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new ModuleCollection())
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
        // ObjCollection objects2 = new ObjCollection("Objects 2");
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
                inputObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertEquals(4, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(31.62, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertEquals(2, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(31.62, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWithinSameSetMaxDistCalibratedSomeFail(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 60, 50, 50);

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);

        Obj obj1 = objects1.createAndAddNewObject(volumeType, 1);
        obj1.add(10, 20, 40);

        Obj obj2 = objects1.createAndAddNewObject(volumeType, 2);
        obj2.add(20, 30, 10);

        Obj obj3 = objects1.createAndAddNewObject(volumeType, 3);
        obj3.add(20, 20, 30);

        Obj obj4 = objects1.createAndAddNewObject(volumeType, 4);
        obj4.add(50, 20, 10);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new ModuleCollection())
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
        // ObjCollection objects2 = new ObjCollection("Objects 2");
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
                inputObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertEquals(4, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(31.62, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertEquals(2, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(31.62, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWithinSameSetWithinParent(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String parentObjectsName = "Parents";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 60, 50, 50);

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);
        Obj obj1 = objects1.createAndAddNewObject(volumeType, 1);
        obj1.add(10, 20, 40);
        Obj obj2 = objects1.createAndAddNewObject(volumeType, 2);
        obj2.add(20, 30, 10);
        Obj obj3 = objects1.createAndAddNewObject(volumeType, 3);
        obj3.add(20, 20, 30);
        Obj obj4 = objects1.createAndAddNewObject(volumeType, 4);
        obj4.add(50, 20, 10);

        // Creating the parent object set
        ObjCollection parents = new ObjCollection(parentObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(parents);

        Obj parent1 = parents.createAndAddNewObject(volumeType, 1);
        parents.add(parent1);
        parent1.addChild(obj1);
        obj1.addParent(parent1);
        parent1.addChild(obj2);
        obj2.addParent(parent1);
        parent1.addChild(obj4);
        obj4.addParent(parent1);

        Obj parent2 = parents.createAndAddNewObject(volumeType, 2);
        parents.add(parent2);
        parent2.addChild(obj3);
        obj3.addParent(parent2);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new ModuleCollection())
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
                inputObjectsName);
        assertEquals(2, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(150.67, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertEquals(4, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(31.62, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertEquals(2, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(31.62, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWithinSameSetWithinParentMaxDistSomeFail(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String parentObjectsName = "Parents";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 60, 50, 50);

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);
        Obj obj1 = objects1.createAndAddNewObject(volumeType, 1);
        obj1.add(10, 20, 40);
        Obj obj2 = objects1.createAndAddNewObject(volumeType, 2);
        obj2.add(20, 30, 10);
        Obj obj3 = objects1.createAndAddNewObject(volumeType, 3);
        obj3.add(20, 20, 30);
        Obj obj4 = objects1.createAndAddNewObject(volumeType, 4);
        obj4.add(50, 20, 10);

        // Creating the parent object set
        ObjCollection parents = new ObjCollection(parentObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(parents);

        Obj parent1 = parents.createAndAddNewObject(volumeType, 1);
        parents.add(parent1);
        parent1.addChild(obj1);
        obj1.addParent(parent1);
        parent1.addChild(obj2);
        obj2.addParent(parent1);
        parent1.addChild(obj4);
        obj4.addParent(parent1);

        Obj parent2 = parents.createAndAddNewObject(volumeType, 2);
        parents.add(parent2);
        parent2.addChild(obj3);
        obj3.addParent(parent2);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new ModuleCollection())
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
                inputObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertEquals(4, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(31.62, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, inputObjectsName);
        assertEquals(2, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                inputObjectsName);
        assertEquals(31.62, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunDifferentSets(VolumeType volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String secondObjectsName = "Objects 2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 60, 50, 50);

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);
        Obj obj1 = objects1.createAndAddNewObject(volumeType, 1);
        obj1.add(10, 20, 40);
        Obj obj2 = objects1.createAndAddNewObject(volumeType, 2);
        obj2.add(20, 30, 10);
        Obj obj3 = objects1.createAndAddNewObject(volumeType, 3);
        obj3.add(20, 20, 30);
        Obj obj4 = objects1.createAndAddNewObject(volumeType, 4);
        obj4.add(50, 20, 10);

        // Creating second object set
        ObjCollection objects2 = new ObjCollection(secondObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects2);
        Obj obj5 = objects2.createAndAddNewObject(volumeType, 5);
        obj5.add(12, 25, 40);
        Obj obj6 = objects2.createAndAddNewObject(volumeType, 6);
        obj6.add(20, 35, 10);
        Obj obj7 = objects2.createAndAddNewObject(volumeType, 7);
        obj7.add(35, 20, 20);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new ModuleCollection())
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
                secondObjectsName);
        assertEquals(5, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertEquals(5.39, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName);
        assertEquals(6, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertEquals(5, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName);
        assertEquals(5, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertEquals(50.88, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName);
        assertEquals(6, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertEquals(33.54, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunDifferentSetsMaxDistSomePass(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String secondObjectsName = "Objects 2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 60, 50, 50);

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);
        Obj obj1 = objects1.createAndAddNewObject(volumeType, 1);
        obj1.add(10, 20, 40);
        Obj obj2 = objects1.createAndAddNewObject(volumeType, 2);
        obj2.add(20, 30, 10);
        Obj obj3 = objects1.createAndAddNewObject(volumeType, 3);
        obj3.add(20, 20, 30);
        Obj obj4 = objects1.createAndAddNewObject(volumeType, 4);
        obj4.add(50, 20, 10);

        // Creating second object set
        ObjCollection objects2 = new ObjCollection(secondObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects2);
        Obj obj5 = objects2.createAndAddNewObject(volumeType, 5);
        obj5.add(12, 25, 40);
        Obj obj6 = objects2.createAndAddNewObject(volumeType, 6);
        obj6.add(20, 35, 10);
        Obj obj7 = objects2.createAndAddNewObject(volumeType, 7);
        obj7.add(35, 20, 20);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour(
                new ModuleCollection())
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
                secondObjectsName);
        assertEquals(5, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertEquals(5.39, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName);
        assertEquals(6, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertEquals(5, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName);
        assertEquals(6, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertEquals(33.54, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunDifferentSetsWithinParent(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String secondObjectsName = "Objects 2";
        String parentObjectsName = "Parents";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 60, 50, 50);

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);
        Obj obj1 = objects1.createAndAddNewObject(volumeType, 1);
        obj1.add(10, 20, 40);
        Obj obj2 = objects1.createAndAddNewObject(volumeType, 2);
        obj2.add(20, 30, 10);
        Obj obj3 = objects1.createAndAddNewObject(volumeType, 3);
        obj3.add(20, 20, 30);
        Obj obj4 = objects1.createAndAddNewObject(volumeType, 4);
        obj4.add(50, 20, 10);

        // Creating second object set
        ObjCollection objects2 = new ObjCollection(secondObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects2);
        Obj obj5 = objects2.createAndAddNewObject(volumeType, 5);
        obj5.add(12, 25, 40);
        Obj obj6 = objects2.createAndAddNewObject(volumeType, 6);
        obj6.add(20, 35, 10);
        Obj obj7 = objects2.createAndAddNewObject(volumeType, 7);
        obj7.add(35, 20, 20);

        // Creating the parent object set
        ObjCollection parents = new ObjCollection(parentObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(parents);

        Obj parent1 = parents.createAndAddNewObject(volumeType, 1);
        parents.add(parent1);
        parent1.addChild(obj1);
        obj1.addParent(parent1);
        parent1.addChild(obj2);
        obj2.addParent(parent1);
        parent1.addChild(obj7);
        obj7.addParent(parent1);

        Obj parent2 = parents.createAndAddNewObject(volumeType, 2);
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
                new ModuleCollection())
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
                secondObjectsName);
        assertEquals(7, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertEquals(103.08, obj1.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName);
        assertEquals(7, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertEquals(53.15, obj2.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName);
        assertEquals(5, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertEquals(50.88, obj3.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName);
        assertEquals(6, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertEquals(33.54, obj4.getMeasurement(name).getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunDifferentSetsWithinParentMaxDistSomeFail(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String secondObjectsName = "Objects 2";
        String parentObjectsName = "Parents";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 60, 50, 50);

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects1);
        Obj obj1 = objects1.createAndAddNewObject(volumeType, 1);
        obj1.add(10, 20, 40);
        Obj obj2 = objects1.createAndAddNewObject(volumeType, 2);
        obj2.add(20, 30, 10);
        Obj obj3 = objects1.createAndAddNewObject(volumeType, 3);
        obj3.add(20, 20, 30);
        Obj obj4 = objects1.createAndAddNewObject(volumeType, 4);
        obj4.add(50, 20, 10);

        // Creating second object set
        ObjCollection objects2 = new ObjCollection(secondObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(objects2);
        Obj obj5 = objects2.createAndAddNewObject(volumeType, 5);
        obj5.add(12, 25, 40);
        Obj obj6 = objects2.createAndAddNewObject(volumeType, 6);
        obj6.add(20, 35, 10);
        Obj obj7 = objects2.createAndAddNewObject(volumeType, 7);
        obj7.add(35, 20, 20);

        // Creating the parent object set
        ObjCollection parents = new ObjCollection(parentObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        workspace.addObjects(parents);

        Obj parent1 = parents.createAndAddNewObject(volumeType, 1);
        parents.add(parent1);
        parent1.addChild(obj1);
        obj1.addParent(parent1);
        parent1.addChild(obj2);
        obj2.addParent(parent1);
        parent1.addChild(obj7);
        obj7.addParent(parent1);

        Obj parent2 = parents.createAndAddNewObject(volumeType, 2);
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
                new ModuleCollection())
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
                secondObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID, secondObjectsName);
        assertEquals(6, obj4.getMeasurement(name).getValue(), tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,
                secondObjectsName);
        assertEquals(33.54, obj4.getMeasurement(name).getValue(), tolerance);

    }
}