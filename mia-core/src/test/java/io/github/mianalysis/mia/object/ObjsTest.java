package io.github.mianalysis.mia.object;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ome.units.UNITS;
import io.github.mianalysis.mia.expectedobjects.VolumeTypes;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;

import static org.junit.jupiter.api.Assertions.*;

public class ObjsTest {
    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetFirstPresent(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 1, 1, 1);

        Objs collection = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);

        collection.createAndAddNewObject(factory, 0);
        collection.createAndAddNewObject(factory, 1);
        collection.createAndAddNewObject(factory, 2);

        Obj firstObj = collection.getFirst();
        assertNotNull(firstObj);
        assertEquals(0, firstObj.getID());

    }

    @Test
    public void testGetFirstAbsent() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 1, 1, 1);

        Objs collection = new Objs("TestObj", calibration, 1, 0.02, UNITS.SECOND);
        Obj firstObj = collection.getFirst();
        assertNull(firstObj);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetSpatialLimits(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 10, 3, 12);

        // Creating the Objs
        Objs collection = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        Obj obj = collection.createAndAddNewObject(factory, 0);
        obj.add(3, 1, 6);
        obj.add(2, 2, 8);

        obj = collection.createAndAddNewObject(factory, 1);
        obj.add(3, 2, 2);
        obj.add(2, 2, 9);

        obj = collection.createAndAddNewObject(factory, 2);
        obj.add(4, 1, 2);
        obj.add(6, 2, 10);

        // Getting expected spatial limits
        int[][] expected = new int[][] { { 0, 9 }, { 0, 2 }, { 0, 11 } };

        // Checking actual values
        int[][] actual = collection.getSpatialLimits();

        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i]);
        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetTimepointLimits(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 1, 1, 1);

        // Creating the Objs
        Objs collection = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        Obj obj = collection.createAndAddNewObject(factory, 0);
        obj.setT(9);

        obj = collection.createAndAddNewObject(factory, 1);
        obj.setT(3);

        obj = collection.createAndAddNewObject(factory, 2);
        obj.setT(12);

        int[] expected = new int[] { 3, 12 };
        int[] actual = collection.getTemporalLimits();

        assertArrayEquals(expected, actual);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetLargestID(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 1, 1, 1);

        // Creating the Objs
        Objs collection = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        collection.createAndAddNewObject(factory, 4);
        collection.createAndAddNewObject(factory, 7);
        collection.createAndAddNewObject(factory, 2);

        assertEquals(7, collection.getLargestID());

    }

    @Test
    @Disabled
    public void testConvertObjectsToImageSingleColour() {
    }

    @Test
    @Disabled
    public void testConvertObjectsToImageSingleColourNoTemplateImage() {
    }

    @Test
    @Disabled
    public void testConvertObjectsToImageRandomColour() {
    }

    @Test
    @Disabled
    public void testConvertObjectsToImageMeasurementColour() {
    }

    @Test
    @Disabled
    public void testConvertObjectsToImageIDColour() {
    }

    @Test
    @Disabled
    public void testConvertObjectsToImageParentIDColour() {
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetByEquals(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 10, 4, 12);

        // Creating the Objs
        Objs collection = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        Obj obj1 = collection.createAndAddNewObject(factory, 1);
        obj1.add(3, 2, 2);
        obj1.add(2, 2, 9);

        Obj obj2 = collection.createAndAddNewObject(factory, 0);
        obj2.add(3, 2, 2);
        obj2.add(2, 2, 9);
        obj2.add(3, 1, 6);
        obj2.add(2, 2, 8);

        Obj obj3 = collection.createAndAddNewObject(factory, 2);
        obj3.add(4, 1, 2);
        obj3.add(6, 2, 10);

        Obj oj4 = collection.createAndAddNewObject(factory, 2);
        oj4.add(4, 1, 2);
        oj4.add(6, 2, 10);
        oj4.add(3, 2, 2);
        oj4.add(2, 2, 9);

        // Creating a test object with the same coordinates as one of the other objects
        Objs testObjects = new Objs("Test", calibration, 1, 0.02, UNITS.SECOND);
        Obj testObj = testObjects.createAndAddNewObject(factory, 5);
        testObj.add(3, 1, 6);
        testObj.add(2, 2, 8);
        testObj.add(3, 2, 2);
        testObj.add(2, 2, 9);

        Obj actual = collection.getByEqualsIgnoreNameAndID(testObj);

        assertEquals(obj2, actual);
        assertEquals(obj2.getID(), actual.getID());

    }
}