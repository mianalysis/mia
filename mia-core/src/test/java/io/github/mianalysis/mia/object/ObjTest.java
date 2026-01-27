package io.github.mianalysis.mia.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.mianalysis.mia.expectedobjects.VolumeTypes;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import ome.units.UNITS;

public class ObjTest {
    private double tolerance = 1E-2;

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testAddMeasurementNormal(VolumeTypes volumeType) {
        ObjsI objects = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);

        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = objects.createAndAddNewObject(factory);
        assertEquals(0, obj.getMeasurements().size());

        obj.addMeasurement(new MeasurementI("Meas", -12.4));

        assertEquals(1, obj.getMeasurements().size());
        assertNotNull(obj.getMeasurement("Meas"));
        assertNull(obj.getMeasurement("NotMeas"));
        assertEquals(-12.4, obj.getMeasurement("Meas").getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testAddMeasurementOverwrite(VolumeTypes volumeType) {
        ObjsI objects = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);

        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = objects.createAndAddNewObject(factory);
        assertEquals(0, obj.getMeasurements().size());

        obj.addMeasurement(new MeasurementI("Meas", -12.4));
        obj.addMeasurement(new MeasurementI("Meas", 3.2));

        assertEquals(1, obj.getMeasurements().size());
        assertNotNull(obj.getMeasurement("Meas"));
        assertNull(obj.getMeasurement("NotMeas"));
        assertEquals(3.2, obj.getMeasurement("Meas").getValue(), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testAddMeasurementNull(VolumeTypes volumeType) {
        ObjsI objects = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);

        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = objects.createAndAddNewObject(factory);
        assertEquals(0, obj.getMeasurements().size());

        obj.addMeasurement(null);

        assertEquals(0, obj.getMeasurements().size());
        assertNull(obj.getMeasurement("Meas"));
        assertNull(obj.getMeasurement("NotMeas"));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testToString(VolumeTypes volumeType) {
        ObjsI objects = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);

        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = objects.createAndAddNewObject(factory);
        obj.setT(12);

        String expected = "Object \"Obj\", ID = 1, frame = 12";
        String actual = obj.toString();

        assertEquals(expected, actual);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetParentsLocalNone(VolumeTypes volumeType) {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";

        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI children = ObjsFactories.getDefaultFactory().createObjs(childObjectsName, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj1 = children.createAndAddNewObjectWithID(factory, 1);
        ObjsI parents1 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName1, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj2 = parents1.createAndAddNewObjectWithID(factory, 12);
        ObjsI parents2 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName2, 1, 1, 1,0.02, 0.1, "µm", 3,
                0.02, UNITS.SECOND);
        ObjI obj3 = parents2.createAndAddNewObjectWithID(factory, 3);

        LinkedHashMap<String, ObjI> parents = obj1.getParents(false);
        assertEquals(0, parents.size());

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetParentsLocal(VolumeTypes volumeType) {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";

        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI children = ObjsFactories.getDefaultFactory().createObjs(childObjectsName, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj1 = children.createAndAddNewObjectWithID(factory, 1);
        ObjsI parents1 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName1, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj2 = parents1.createAndAddNewObjectWithID(factory, 12);
        ObjsI parents2 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName2, 1, 1, 1,0.02, 0.1, "µm", 3,
                0.02, UNITS.SECOND);
        ObjI obj3 = parents2.createAndAddNewObjectWithID(factory, 3);

        obj1.addParent(obj2);
        obj2.addChild(obj1);
        obj1.addParent(obj3);
        obj3.addChild(obj1);

        LinkedHashMap<String, ObjI> parents = obj1.getParents(false);
        assertEquals(2, parents.size());
        assertNotNull(parents.get(parentObjectsName1));
        assertNotNull(parents.get(parentObjectsName2));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetParentsLocalOverwrite(VolumeTypes volumeType) {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents1";

        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI children = ObjsFactories.getDefaultFactory().createObjs(childObjectsName, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj1 = children.createAndAddNewObjectWithID(factory, 1);
        ObjsI parents1 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName1, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj2 = parents1.createAndAddNewObjectWithID(factory, 12);
        ObjsI parents2 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName2, 1, 1, 1,0.02, 0.1, "µm", 3,
                0.02, UNITS.SECOND);
        ObjI obj3 = parents2.createAndAddNewObjectWithID(factory, 3);

        obj1.addParent(obj2);
        obj2.addChild(obj1);
        obj1.addParent(obj3);
        obj3.addChild(obj1);

        LinkedHashMap<String, ObjI> parents = obj1.getParents(false);
        assertEquals(1, parents.size());
        assertNotNull(parents.get(parentObjectsName1));

    }

    /**
     * Testing getting multiple parents from the relationship hierarchy. In this
     * case the relationships further away are added last.
     */
    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetParentsFullHierarchyClosestFirst(VolumeTypes volumeType) {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";
        String parentObjectsName3 = "Parents3";

        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI children = ObjsFactories.getDefaultFactory().createObjs(childObjectsName, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj1 = children.createAndAddNewObjectWithID(factory, 1);

        ObjsI parents1 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName1, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj2 = parents1.createAndAddNewObjectWithID(factory, 12);
        ObjsI parents2 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName2, 1, 1, 1,0.02, 0.1, "µm", 3,
                0.02, UNITS.SECOND);
        ObjI obj3 = parents2.createAndAddNewObjectWithID(factory, 3);
        ObjsI parents3 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName3, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj4 = parents3.createAndAddNewObjectWithID(factory, 42);

        obj1.addParent(obj2);
        obj2.addChild(obj1);

        obj2.addParent(obj3);
        obj3.addChild(obj2);

        obj3.addParent(obj4);
        obj4.addChild(obj3);

        LinkedHashMap<String, ObjI> parents = obj1.getParents(true);
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
    @EnumSource(VolumeTypes.class)
    public void testGetParentsFullHierarchyFurthestFirst(VolumeTypes volumeType) {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";
        String parentObjectsName3 = "Parents3";

        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI children = ObjsFactories.getDefaultFactory().createObjs(childObjectsName, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj1 = children.createAndAddNewObjectWithID(factory, 1);
        ObjsI parents1 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName1, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj2 = parents1.createAndAddNewObjectWithID(factory, 12);
        ObjsI parents2 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName2, 1, 1, 1,0.02, 0.1, "µm", 3,
                0.02, UNITS.SECOND);
        ObjI obj3 = parents2.createAndAddNewObjectWithID(factory, 3);
        ObjsI parents3 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName3, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj4 = parents3.createAndAddNewObjectWithID(factory, 42);

        obj3.addParent(obj4);
        obj4.addChild(obj3);

        obj2.addParent(obj3);
        obj3.addChild(obj2);

        obj1.addParent(obj2);
        obj2.addChild(obj1);

        LinkedHashMap<String, ObjI> parents = obj1.getParents(true);
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
    @EnumSource(VolumeTypes.class)
    public void testGetParentsFullHierarchyCyclicClosestFirst(VolumeTypes volumeType) {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";
        String parentObjectsName3 = "Parents3";

        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI children = ObjsFactories.getDefaultFactory().createObjs(childObjectsName, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj1 = children.createAndAddNewObjectWithID(factory, 1);
        ObjsI parents1 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName1, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj2 = parents1.createAndAddNewObjectWithID(factory, 12);
        ObjsI parents2 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName2, 1, 1, 1,0.02, 0.1, "µm", 3,
                0.02, UNITS.SECOND);
        ObjI obj3 = parents2.createAndAddNewObjectWithID(factory, 3);
        ObjsI parents3 = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName3, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj4 = parents3.createAndAddNewObjectWithID(factory, 42);
        ObjI obj5 = parents1.createAndAddNewObjectWithID(factory, 14);

        obj1.addParent(obj2);
        obj2.addChild(obj1);

        obj2.addParent(obj3);
        obj3.addChild(obj2);

        obj3.addParent(obj4);
        obj4.addChild(obj3);

        obj3.addParent(obj5);
        obj5.addChild(obj3);

        LinkedHashMap<String, ObjI> parents = obj1.getParents(true);
        assertEquals(3, parents.size());
        assertNotNull(parents.get(parentObjectsName1));
        assertNotNull(parents.get(parentObjectsName2));
        assertNotNull(parents.get(parentObjectsName3));
        assertEquals(12, obj1.getParent(parentObjectsName1).getID());

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testAddChildren(VolumeTypes volumeType) {
        String parentObjectsName = "Parent";
        String childObjectsName = "Children";

        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI parents = ObjsFactories.getDefaultFactory().createObjs(parentObjectsName, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        ObjI obj1 = parents.createAndAddNewObjectWithID(factory, 1);
        ObjI obj2 = parents.createAndAddNewObjectWithID(factory, 12);
        ObjI obj3 = parents.createAndAddNewObjectWithID(factory, 2);
        ObjI obj4 = parents.createAndAddNewObjectWithID(factory, 32);

        assertEquals(0, obj1.getAllChildren().size());

        // Creating a new collection of child objects
        ObjsI children = ObjsFactories.getDefaultFactory().createObjs(childObjectsName, 1, 1, 1,0.02, 0.1, "µm", 1,
                0.02, UNITS.SECOND);
        children.add(obj2);
        children.add(obj3);
        children.add(obj4);

        // Assigning the new children
        obj1.addChildren(children);

        // Checking the children of the object
        assertEquals(1, obj1.getAllChildren().size());
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
    @EnumSource(VolumeTypes.class)
    public void testHashCodeSameObject(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {

        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);

        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj1 = objects1.createAndAddNewObject(factory);
        obj1.setT(1);
        obj1.addCoord(1, 3, 4);
        obj1.addCoord(3, 5, 1);

        ObjsI objects2 = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj2 = objects2.createAndAddNewObject(factory);
        obj2.setT(1);
        obj2.addCoord(1, 3, 4);
        obj2.addCoord(3, 5, 1);

        assertEquals(obj1.hashCode(), obj2.hashCode());

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testHashCodeDifferentOrder(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);

        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj1 = objects1.createAndAddNewObject(factory);
        obj1.setT(1);
        obj1.addCoord(1, 3, 4);
        obj1.addCoord(3, 5, 1);

        ObjsI objects2 = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj2 = objects2.createAndAddNewObject(factory);
        obj2.setT(1);
        obj2.addCoord(3, 5, 1);
        obj2.addCoord(1, 3, 4);

        assertEquals(obj1.hashCode(), obj2.hashCode());

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testHashCodeDifferentNames(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);

        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj1 = objects1.createAndAddNewObject(factory);
        obj1.setT(1);
        obj1.addCoord(1, 3, 4);
        obj1.addCoord(3, 5, 1);

        ObjsI objects2 = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj2 = objects2.createAndAddNewObject(factory);
        obj2.setT(1);
        obj2.addCoord(1, 3, 4);
        obj2.addCoord(3, 5, 1);

        assertEquals(obj1.hashCode(), obj2.hashCode());

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testHashCodeDifferentTimepoint(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);

        ObjsI objects = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj1 = objects.createAndAddNewObject(factory);
        obj1.setT(1);
        obj1.addCoord(1, 1, 1);
        obj1.addCoord(2, 1, 1);

        ObjI obj2 = objects.createAndAddNewObject(factory);
        obj2.setT(2);
        obj2.addCoord(1, 1, 1);
        obj2.addCoord(1, 2, 1);

        assertNotEquals(obj1.hashCode(), obj2.hashCode());

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testHashCodeDifferentCoordinates(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);

        ObjsI objects = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj1 = objects.createAndAddNewObject(factory);
        obj1.setT(1);
        obj1.addCoord(1, 3, 4);
        obj1.addCoord(3, 5, 1);

        ObjI obj2 = objects.createAndAddNewObject(factory);
        obj2.setT(1);
        obj2.addCoord(1, 3, 3);
        obj2.addCoord(3, 5, 1);

        assertNotEquals(obj1.hashCode(), obj2.hashCode());

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testHashCodeMissingCoordinates(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);

        ObjsI objects = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj1 = objects.createAndAddNewObject(factory);
        obj1.setT(1);
        obj1.addCoord(1, 3, 4);
        obj1.addCoord(3, 5, 1);

        ObjI obj2 = objects.createAndAddNewObject(factory);
        obj2.setT(1);
        obj2.addCoord(1, 3, 4);

        assertNotEquals(obj1.hashCode(), obj2.hashCode());

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testEqualsSameObject(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);

        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj1 = objects1.createAndAddNewObject(factory);
        obj1.setT(1);
        obj1.addCoord(1, 3, 4);
        obj1.addCoord(3, 5, 1);

        ObjsI objects2 = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj2 = objects2.createAndAddNewObject(factory);
        obj2.setT(1);
        obj2.addCoord(1, 3, 4);
        obj2.addCoord(3, 5, 1);

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testEqualsDifferentOrder(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);

        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj1 = objects1.createAndAddNewObject(factory);
        obj1.setT(1);
        obj1.addCoord(1, 3, 4);
        obj1.addCoord(3, 5, 1);

        ObjsI objects2 = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj2 = objects2.createAndAddNewObject(factory);
        obj2.setT(1);
        obj2.addCoord(3, 5, 1);
        obj2.addCoord(1, 3, 4);

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testEqualsDifferentNames(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);

        ObjsI objects1 = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj1 = objects1.createAndAddNewObject(factory);
        obj1.setT(1);
        obj1.addCoord(1, 3, 4);
        obj1.addCoord(3, 5, 1);

        ObjsI objects2 = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj2 = objects2.createAndAddNewObject(factory);
        obj2.setT(1);
        obj2.addCoord(1, 3, 4);
        obj2.addCoord(3, 5, 1);

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testEqualsDifferentTimepoint(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);

        ObjsI objects = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj1 = objects.createAndAddNewObject(factory);
        obj1.setT(1);
        obj1.addCoord(1, 1, 1);
        obj1.addCoord(2, 1, 1);

        ObjI obj2 = objects.createAndAddNewObject(factory);
        obj2.setT(2);
        obj2.addCoord(1, 1, 1);
        obj2.addCoord(1, 2, 1);

        assertFalse(obj1.equals(obj2));
        assertFalse(obj2.equals(obj1));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testEqualsDifferentCoordinates(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);

        ObjsI objects = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj1 = objects.createAndAddNewObject(factory);
        obj1.setT(1);
        obj1.addCoord(1, 3, 4);
        obj1.addCoord(3, 5, 1);

        ObjI obj2 = objects.createAndAddNewObject(factory);
        obj2.setT(1);
        obj2.addCoord(1, 3, 3);
        obj2.addCoord(3, 5, 1);

        assertFalse(obj1.equals(obj2));
        assertFalse(obj2.equals(obj1));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testEqualsMissingCoordinates(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);

        ObjsI objects = ObjsFactories.getDefaultFactory().createObjs("Obj", 5, 7, 5, 2.0, 1.0, "PX", 1, 0.02,
                UNITS.SECOND);
        ObjI obj1 = objects.createAndAddNewObject(factory);
        obj1.setT(1);
        obj1.addCoord(1, 3, 4);
        obj1.addCoord(3, 5, 1);

        ObjI obj2 = objects.createAndAddNewObject(factory);
        obj2.setT(1);
        obj2.addCoord(1, 3, 4);

        assertFalse(obj1.equals(obj2));
        assertFalse(obj2.equals(obj1));

    }
}