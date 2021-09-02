package io.github.mianalysis.mia.object;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ome.units.UNITS;
import io.github.sjcross.common.exceptions.IntegerOverflowException;
import io.github.sjcross.common.object.volume.PointOutOfRangeException;
import io.github.sjcross.common.object.volume.SpatCal;
import io.github.sjcross.common.object.volume.VolumeType;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class ObjTest {
    private double tolerance = 1E-2;

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testAddMeasurementNormal(VolumeType volumeType) {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj = objects.createAndAddNewObject(volumeType);
        assertEquals(0, obj.getMeasurements().size());

        obj.addMeasurement(new Measurement("Meas", -12.4));

        assertEquals(1, obj.getMeasurements().size());
        assertNotNull(obj.getMeasurement("Meas"));
        assertNull(obj.getMeasurement("NotMeas"));
        assertEquals(-12.4, obj.getMeasurement("Meas").getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testAddMeasurementOverwrite(VolumeType volumeType) {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj = objects.createAndAddNewObject(volumeType);
        assertEquals(0, obj.getMeasurements().size());

        obj.addMeasurement(new Measurement("Meas", -12.4));
        obj.addMeasurement(new Measurement("Meas", 3.2));

        assertEquals(1, obj.getMeasurements().size());
        assertNotNull(obj.getMeasurement("Meas"));
        assertNull(obj.getMeasurement("NotMeas"));
        assertEquals(3.2, obj.getMeasurement("Meas").getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testAddMeasurementNull(VolumeType volumeType) {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj = objects.createAndAddNewObject(volumeType);
        assertEquals(0, obj.getMeasurements().size());

        obj.addMeasurement(null);

        assertEquals(0, obj.getMeasurements().size());
        assertNull(obj.getMeasurement("Meas"));
        assertNull(obj.getMeasurement("NotMeas"));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testToString(VolumeType volumeType) {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj = objects.createAndAddNewObject(volumeType);
        obj.setT(12);

        String expected = "Object \"Obj\", ID = 1, frame = 12";
        String actual = obj.toString();

        assertEquals(expected, actual);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetParentsLocalNone(VolumeType volumeType) {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";

        SpatCal calibration = new SpatCal(0.02, 0.1, "µm", 1, 1, 1);

        Objs children = new Objs(childObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = children.createAndAddNewObject(volumeType, 1);
        Objs parents1 = new Objs(parentObjectsName1, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj2 = parents1.createAndAddNewObject(volumeType, 12);
        Objs parents2 = new Objs(parentObjectsName2, calibration, 3, 0.02, UNITS.SECOND);
        Obj obj3 = parents2.createAndAddNewObject(volumeType, 3);

        LinkedHashMap<String, Obj> parents = obj1.getParents(false);
        assertEquals(0, parents.size());

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetParentsLocal(VolumeType volumeType) {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";

        SpatCal calibration = new SpatCal(0.02, 0.1, "µm", 1, 1, 1);

        Objs children = new Objs(childObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = children.createAndAddNewObject(volumeType, 1);
        Objs parents1 = new Objs(parentObjectsName1, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj2 = parents1.createAndAddNewObject(volumeType, 12);
        Objs parents2 = new Objs(parentObjectsName2, calibration, 3, 0.02, UNITS.SECOND);
        Obj obj3 = parents2.createAndAddNewObject(volumeType, 3);

        obj1.addParent(obj2);
        obj2.addChild(obj1);
        obj1.addParent(obj3);
        obj3.addChild(obj1);

        LinkedHashMap<String, Obj> parents = obj1.getParents(false);
        assertEquals(2, parents.size());
        assertNotNull(parents.get(parentObjectsName1));
        assertNotNull(parents.get(parentObjectsName2));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetParentsLocalOverwrite(VolumeType volumeType) {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents1";

        SpatCal calibration = new SpatCal(0.02, 0.1, "µm", 1, 1, 1);

        Objs children = new Objs(childObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = children.createAndAddNewObject(volumeType, 1);
        Objs parents1 = new Objs(parentObjectsName1, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj2 = parents1.createAndAddNewObject(volumeType, 12);
        Objs parents2 = new Objs(parentObjectsName2, calibration, 3, 0.02, UNITS.SECOND);
        Obj obj3 = parents2.createAndAddNewObject(volumeType, 3);

        obj1.addParent(obj2);
        obj2.addChild(obj1);
        obj1.addParent(obj3);
        obj3.addChild(obj1);

        LinkedHashMap<String, Obj> parents = obj1.getParents(false);
        assertEquals(1, parents.size());
        assertNotNull(parents.get(parentObjectsName1));

    }

    /**
     * Testing getting multiple parents from the relationship hierarchy. In this
     * case the relationships further away are added last.
     */
    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetParentsFullHierarchyClosestFirst(VolumeType volumeType) {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";
        String parentObjectsName3 = "Parents3";

        SpatCal calibration = new SpatCal(0.02, 0.1, "µm", 1, 1, 1);

        Objs children = new Objs(childObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = children.createAndAddNewObject(volumeType, 1);

        Objs parents1 = new Objs(parentObjectsName1, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj2 = parents1.createAndAddNewObject(volumeType, 12);
        Objs parents2 = new Objs(parentObjectsName2, calibration, 3, 0.02, UNITS.SECOND);
        Obj obj3 = parents2.createAndAddNewObject(volumeType, 3);
        Objs parents3 = new Objs(parentObjectsName3, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj4 = parents3.createAndAddNewObject(volumeType, 42);

        obj1.addParent(obj2);
        obj2.addChild(obj1);

        obj2.addParent(obj3);
        obj3.addChild(obj2);

        obj3.addParent(obj4);
        obj4.addChild(obj3);

        LinkedHashMap<String, Obj> parents = obj1.getParents(true);
        assertEquals(3, parents.size());
        assertNotNull(parents.get(parentObjectsName1));
        assertNotNull(parents.get(parentObjectsName2));
        assertNotNull(parents.get(parentObjectsName3));

    }

    /**
     * Testing getting multiple parents from the relationship hierarchy. In this
     * case the relationships further away are added first.
     */
    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetParentsFullHierarchyFurthestFirst(VolumeType volumeType) {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";
        String parentObjectsName3 = "Parents3";

        SpatCal calibration = new SpatCal(0.02, 0.1, "µm", 1, 1, 1);

        Objs children = new Objs(childObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = children.createAndAddNewObject(volumeType, 1);
        Objs parents1 = new Objs(parentObjectsName1, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj2 = parents1.createAndAddNewObject(volumeType, 12);
        Objs parents2 = new Objs(parentObjectsName2, calibration, 3, 0.02, UNITS.SECOND);
        Obj obj3 = parents2.createAndAddNewObject(volumeType, 3);
        Objs parents3 = new Objs(parentObjectsName3, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj4 = parents3.createAndAddNewObject(volumeType, 42);

        obj3.addParent(obj4);
        obj4.addChild(obj3);

        obj2.addParent(obj3);
        obj3.addChild(obj2);

        obj1.addParent(obj2);
        obj2.addChild(obj1);

        LinkedHashMap<String, Obj> parents = obj1.getParents(true);
        assertEquals(3, parents.size());
        assertNotNull(parents.get(parentObjectsName1));
        assertNotNull(parents.get(parentObjectsName2));
        assertNotNull(parents.get(parentObjectsName3));

    }

    /**
     * Here, the target object has a parent from the same collection as one of its
     * parents; however, the parent objects are different. As parents are returned
     * as a LinkedHashMap with String key it's not possible to have more than one
     * parent from a single collection. This should preferentially return the direct
     * parent of the target object.
     */
    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetParentsFullHierarchyCyclicClosestFirst(VolumeType volumeType) {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";
        String parentObjectsName3 = "Parents3";

        SpatCal calibration = new SpatCal(0.02, 0.1, "µm", 1, 1, 1);

        Objs children = new Objs(childObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = children.createAndAddNewObject(volumeType, 1);
        Objs parents1 = new Objs(parentObjectsName1, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj2 = parents1.createAndAddNewObject(volumeType, 12);
        Objs parents2 = new Objs(parentObjectsName2, calibration, 3, 0.02, UNITS.SECOND);
        Obj obj3 = parents2.createAndAddNewObject(volumeType, 3);
        Objs parents3 = new Objs(parentObjectsName3, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj4 = parents3.createAndAddNewObject(volumeType, 42);
        Obj obj5 = parents1.createAndAddNewObject(volumeType, 14);

        obj1.addParent(obj2);
        obj2.addChild(obj1);

        obj2.addParent(obj3);
        obj3.addChild(obj2);

        obj3.addParent(obj4);
        obj4.addChild(obj3);

        obj3.addParent(obj5);
        obj5.addChild(obj3);

        LinkedHashMap<String, Obj> parents = obj1.getParents(true);
        assertEquals(3, parents.size());
        assertNotNull(parents.get(parentObjectsName1));
        assertNotNull(parents.get(parentObjectsName2));
        assertNotNull(parents.get(parentObjectsName3));
        assertEquals(12, obj1.getParent(parentObjectsName1).getID());

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testAddChildren(VolumeType volumeType) {
        String parentObjectsName = "Parent";
        String childObjectsName = "Children";

        SpatCal calibration = new SpatCal(0.02, 0.1, "µm", 1, 1, 1);
        Objs parents = new Objs(parentObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = parents.createAndAddNewObject(volumeType, 1);
        Obj obj2 = parents.createAndAddNewObject(volumeType, 12);
        Obj obj3 = parents.createAndAddNewObject(volumeType, 2);
        Obj obj4 = parents.createAndAddNewObject(volumeType, 32);

        assertEquals(0, obj1.getChildren().size());

        // Creating a new collection of child objects
        Objs children = new Objs(childObjectsName, calibration, 1, 0.02, UNITS.SECOND);
        children.add(obj2);
        children.add(obj3);
        children.add(obj4);

        // Assigning the new children
        obj1.addChildren(children);

        // Checking the children of the object
        assertEquals(1, obj1.getChildren().size());
        assertNotNull(obj1.getChildren(childObjectsName));
        assertEquals(0, obj1.getChildren("not children").size());
        assertEquals(3, obj1.getChildren(childObjectsName).size());

    }

    @Test
    @Disabled
    public void testAddChild() {

    }

    @Test
    @Disabled
    public void testRemoveChild() {

    }

    @Test
    @Disabled
    public void testRemoveRelationships() {

    }

    @Test
    @Disabled
    public void testGetRoi() {

    }

    @Test
    @Disabled
    public void testAddPointsFromRoi() {

    }

    @Test
    @Disabled
    public void testGetAsImage() {

    }

    @Test
    @Disabled
    public void testConvertObjToImage() {

    }

    @Test
    @Disabled
    public void testCropToImageSize() {

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testHashCodeSameObject(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {

        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = objects.createAndAddNewObject(volumeType);
        obj1.setT(1);
        obj1.add(1, 3, 4);
        obj1.add(3, 5, 1);

        Obj obj2 = objects.createAndAddNewObject(volumeType);
        obj2.setT(1);
        obj2.add(1, 3, 4);
        obj2.add(3, 5, 1);

        assertEquals(obj1.hashCode(), obj2.hashCode());

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testHashCodeDifferentOrder(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = objects.createAndAddNewObject(volumeType);
        obj1.setT(1);
        obj1.add(1, 3, 4);
        obj1.add(3, 5, 1);

        Obj obj2 = objects.createAndAddNewObject(volumeType);
        obj2.setT(1);
        obj2.add(3, 5, 1);
        obj2.add(1, 3, 4);

        assertEquals(obj1.hashCode(), obj2.hashCode());

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testHashCodeDifferentNames(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = objects.createAndAddNewObject(volumeType);
        obj1.setT(1);
        obj1.add(1, 3, 4);
        obj1.add(3, 5, 1);

        Obj obj2 = objects.createAndAddNewObject(volumeType);
        obj2.setT(1);
        obj2.add(1, 3, 4);
        obj2.add(3, 5, 1);

        assertEquals(obj1.hashCode(), obj2.hashCode());

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testHashCodeDifferentTimepoint(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = objects.createAndAddNewObject(volumeType);
        obj1.setT(1);
        obj1.add(1, 1, 1);
        obj1.add(2, 1, 1);

        Obj obj2 = objects.createAndAddNewObject(volumeType);
        obj2.setT(2);
        obj2.add(1, 1, 1);
        obj2.add(1, 2, 1);

        assertNotEquals(obj1.hashCode(), obj2.hashCode());

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testHashCodeDifferentCoordinates(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = objects.createAndAddNewObject(volumeType);
        obj1.setT(1);
        obj1.add(1, 3, 4);
        obj1.add(3, 5, 1);

        Obj obj2 = objects.createAndAddNewObject(volumeType);
        obj2.setT(1);
        obj2.add(1, 3, 3);
        obj2.add(3, 5, 1);

        assertNotEquals(obj1.hashCode(), obj2.hashCode());

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testHashCodeMissingCoordinates(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = objects.createAndAddNewObject(volumeType);
        obj1.setT(1);
        obj1.add(1, 3, 4);
        obj1.add(3, 5, 1);

        Obj obj2 = objects.createAndAddNewObject(volumeType);
        obj2.setT(1);
        obj2.add(1, 3, 4);

        assertNotEquals(obj1.hashCode(), obj2.hashCode());

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testEqualsSameObject(VolumeType volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = objects.createAndAddNewObject(volumeType);
        obj1.setT(1);
        obj1.add(1, 3, 4);
        obj1.add(3, 5, 1);

        Obj obj2 = objects.createAndAddNewObject(volumeType);
        obj2.setT(1);
        obj2.add(1, 3, 4);
        obj2.add(3, 5, 1);

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testEqualsDifferentOrder(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = objects.createAndAddNewObject(volumeType);
        obj1.setT(1);
        obj1.add(1, 3, 4);
        obj1.add(3, 5, 1);

        Obj obj2 = objects.createAndAddNewObject(volumeType);
        obj2.setT(1);
        obj2.add(3, 5, 1);
        obj2.add(1, 3, 4);

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testEqualsDifferentNames(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = objects.createAndAddNewObject(volumeType);
        obj1.setT(1);
        obj1.add(1, 3, 4);
        obj1.add(3, 5, 1);

        Obj obj2 = objects.createAndAddNewObject(volumeType);
        obj2.setT(1);
        obj2.add(1, 3, 4);
        obj2.add(3, 5, 1);

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testEqualsDifferentTimepoint(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = objects.createAndAddNewObject(volumeType);
        obj1.setT(1);
        obj1.add(1, 1, 1);
        obj1.add(2, 1, 1);

        Obj obj2 = objects.createAndAddNewObject(volumeType);
        obj2.setT(2);
        obj2.add(1, 1, 1);
        obj2.add(1, 2, 1);

        assertFalse(obj1.equals(obj2));
        assertFalse(obj2.equals(obj1));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testEqualsDifferentCoordinates(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = objects.createAndAddNewObject(volumeType);
        obj1.setT(1);
        obj1.add(1, 3, 4);
        obj1.add(3, 5, 1);

        Obj obj2 = objects.createAndAddNewObject(volumeType);
        obj2.setT(1);
        obj2.add(1, 3, 3);
        obj2.add(3, 5, 1);

        assertFalse(obj1.equals(obj2));
        assertFalse(obj2.equals(obj1));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testEqualsMissingCoordinates(VolumeType volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal calibration = new SpatCal(2.0, 1.0, "PX", 5, 7, 5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj1 = objects.createAndAddNewObject(volumeType);
        obj1.setT(1);
        obj1.add(1, 3, 4);
        obj1.add(3, 5, 1);

        Obj obj2 = objects.createAndAddNewObject(volumeType);
        obj2.setT(1);
        obj2.add(1, 3, 4);

        assertFalse(obj1.equals(obj2));
        assertFalse(obj2.equals(obj1));

    }
}