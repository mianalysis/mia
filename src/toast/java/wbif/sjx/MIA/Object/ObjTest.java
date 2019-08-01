package wbif.sjx.MIA.Object;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.common.Exceptions.IntegerOverflowException;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

public class ObjTest {
    private double tolerance = 1E-2;

    @Test
    public void testAddMeasurementNormal() {
        Obj obj = new Obj("Test object",1,0.02,0.1,"µm",false);
        assertEquals(0,obj.getMeasurements().size());

        obj.addMeasurement(new Measurement("Meas",-12.4));

        assertEquals(1,obj.getMeasurements().size());
        assertNotNull(obj.getMeasurement("Meas"));
        assertNull(obj.getMeasurement("NotMeas"));
        assertEquals(-12.4,obj.getMeasurement("Meas").getValue(),tolerance);

    }

    @Test
    public void testAddMeasurementOverwrite() {
        Obj obj = new Obj("Test object",1,0.02,0.1,"µm",false);
        assertEquals(0,obj.getMeasurements().size());

        obj.addMeasurement(new Measurement("Meas",-12.4));
        obj.addMeasurement(new Measurement("Meas",3.2));

        assertEquals(1,obj.getMeasurements().size());
        assertNotNull(obj.getMeasurement("Meas"));
        assertNull(obj.getMeasurement("NotMeas"));
        assertEquals(3.2,obj.getMeasurement("Meas").getValue(),tolerance);

    }

    @Test
    public void testAddMeasurementNull() {
        Obj obj = new Obj("Test object",1,0.02,0.1,"µm",false);
        assertEquals(0,obj.getMeasurements().size());

        obj.addMeasurement(null);

        assertEquals(0,obj.getMeasurements().size());
        assertNull(obj.getMeasurement("Meas"));
        assertNull(obj.getMeasurement("NotMeas"));

    }

    @Test
    public void testToString() {
        Obj obj = new Obj("Test object",1,0.02,0.1,"µm",false);
        obj.setT(12);

        String expected = "Object Test object, ID = 1, frame = 12";
        String actual = obj.toString();

        assertEquals(expected,actual);

    }

    @Test
    public void testGetParentsLocalNone() {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        Obj obj1 = new Obj(childObjectsName,1,0.02,0.1,"µm",false);
        Obj obj2 = new Obj(parentObjectsName1,12,0.02,0.1,"µm",false);
        Obj obj3 = new Obj(parentObjectsName2,3,0.02,0.1,"µm",false);

        LinkedHashMap<String,Obj> parents = obj1.getParents(false);
        assertEquals(0,parents.size());

    }

    @Test
    public void testGetParentsLocal() {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        Obj obj1 = new Obj(childObjectsName,1,0.02,0.1,"µm",false);
        Obj obj2 = new Obj(parentObjectsName1,12,0.02,0.1,"µm",false);
        Obj obj3 = new Obj(parentObjectsName2,3,0.02,0.1,"µm",false);

        obj1.addParent(obj2);
        obj2.addChild(obj1);
        obj1.addParent(obj3);
        obj3.addChild(obj1);

        LinkedHashMap<String,Obj> parents = obj1.getParents(false);
        assertEquals(2,parents.size());
        assertNotNull(parents.get(parentObjectsName1));
        assertNotNull(parents.get(parentObjectsName2));

    }

    @Test
    public void testGetParentsLocalOverwrite() {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents1";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        Obj obj1 = new Obj(childObjectsName,1,0.02,0.1,"µm",false);
        Obj obj2 = new Obj(parentObjectsName1,12,0.02,0.1,"µm",false);
        Obj obj3 = new Obj(parentObjectsName2,3,0.02,0.1,"µm",false);

        obj1.addParent(obj2);
        obj2.addChild(obj1);
        obj1.addParent(obj3);
        obj3.addChild(obj1);

        LinkedHashMap<String,Obj> parents = obj1.getParents(false);
        assertEquals(1,parents.size());
        assertNotNull(parents.get(parentObjectsName1));

    }

    /**
     * Testing getting multiple parents from the relationship hierarchy.  In this case the relationships further away
     * are added last.
     */
    @Test
    public void testGetParentsFullHierarchyClosestFirst() {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";
        String parentObjectsName3 = "Parents3";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        Obj obj1 = new Obj(childObjectsName,1,0.02,0.1,"µm",false);
        Obj obj2 = new Obj(parentObjectsName1,12,0.02,0.1,"µm",false);
        Obj obj3 = new Obj(parentObjectsName2,3,0.02,0.1,"µm",false);
        Obj obj4 = new Obj(parentObjectsName3,42,0.02,0.1,"µm",false);

        obj1.addParent(obj2);
        obj2.addChild(obj1);

        obj2.addParent(obj3);
        obj3.addChild(obj2);

        obj3.addParent(obj4);
        obj4.addChild(obj3);

        LinkedHashMap<String,Obj> parents = obj1.getParents(true);
        assertEquals(3,parents.size());
        assertNotNull(parents.get(parentObjectsName1));
        assertNotNull(parents.get(parentObjectsName2));
        assertNotNull(parents.get(parentObjectsName3));

    }

    /**
     * Testing getting multiple parents from the relationship hierarchy.  In this case the relationships further away
     * are added first.
     */
    @Test
    public void testGetParentsFullHierarchyFurthestFirst() {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";
        String parentObjectsName3 = "Parents3";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        Obj obj1 = new Obj(childObjectsName,1,0.02,0.1,"µm",false);
        Obj obj2 = new Obj(parentObjectsName1,12,0.02,0.1,"µm",false);
        Obj obj3 = new Obj(parentObjectsName2,3,0.02,0.1,"µm",false);
        Obj obj4 = new Obj(parentObjectsName3,42,0.02,0.1,"µm",false);

        obj3.addParent(obj4);
        obj4.addChild(obj3);

        obj2.addParent(obj3);
        obj3.addChild(obj2);

        obj1.addParent(obj2);
        obj2.addChild(obj1);

        LinkedHashMap<String,Obj> parents = obj1.getParents(true);
        assertEquals(3,parents.size());
        assertNotNull(parents.get(parentObjectsName1));
        assertNotNull(parents.get(parentObjectsName2));
        assertNotNull(parents.get(parentObjectsName3));

    }

    /**
     * Here, the target object has a parent from the same collection as one of its parents; however, the parent objects
     * are different.  As parents are returned as a LinkedHashMap with String key it's not possible to have more than
     * one parent from a single collection.  This should preferentially return the direct parent of the target object.
     */
    @Test
    public void testGetParentsFullHierarchyCyclicClosestFirst() {
        String childObjectsName = "Children";
        String parentObjectsName1 = "Parents1";
        String parentObjectsName2 = "Parents2";
        String parentObjectsName3 = "Parents3";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        Obj obj1 = new Obj(childObjectsName,1,0.02,0.1,"µm",false);
        Obj obj2 = new Obj(parentObjectsName1,12,0.02,0.1,"µm",false);
        Obj obj3 = new Obj(parentObjectsName2,3,0.02,0.1,"µm",false);
        Obj obj4 = new Obj(parentObjectsName3,42,0.02,0.1,"µm",false);
        Obj obj5 = new Obj(parentObjectsName1,14,0.02,0.1,"µm",false);

        obj1.addParent(obj2);
        obj2.addChild(obj1);

        obj2.addParent(obj3);
        obj3.addChild(obj2);

        obj3.addParent(obj4);
        obj4.addChild(obj3);

        obj3.addParent(obj5);
        obj5.addChild(obj3);

        LinkedHashMap<String,Obj> parents = obj1.getParents(true);
        assertEquals(3,parents.size());
        assertNotNull(parents.get(parentObjectsName1));
        assertNotNull(parents.get(parentObjectsName2));
        assertNotNull(parents.get(parentObjectsName3));
        assertEquals(12,obj1.getParent(parentObjectsName1).getID());

    }

    @Test
    public void testAddChildren() {
        String parentObjectsName = "Parent";
        String childObjectsName = "Children";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        Obj obj1 = new Obj(parentObjectsName,1,0.02,0.1,"µm",false);
        Obj obj2 = new Obj(childObjectsName,12,0.02,0.1,"µm",false);
        Obj obj3 = new Obj(childObjectsName,2,0.02,0.1,"µm",false);
        Obj obj4 = new Obj(childObjectsName,32,0.02,0.1,"µm",false);

        assertEquals(0,obj1.getChildren().size());

        // Creating a new collection of child objects
        ObjCollection children = new ObjCollection(childObjectsName);
        children.add(obj2);
        children.add(obj3);
        children.add(obj4);

        // Assigning the new children
        obj1.addChildren(children);

        // Checking the children of the object
        assertEquals(1,obj1.getChildren().size());
        assertNotNull(obj1.getChildren(childObjectsName));
        assertEquals(0,obj1.getChildren("not children").size());
        assertEquals(3,obj1.getChildren(childObjectsName).size());

    }

    @Test @Ignore
    public void testAddChild() {

    }

    @Test @Ignore
    public void testRemoveChild() {

    }

    @Test @Ignore
    public void testRemoveRelationships() {

    }

    @Test @Ignore
    public void testGetRoi() {

    }

    @Test @Ignore
    public void testAddPointsFromRoi() {

    }

    @Test @Ignore
    public void testGetAsImage() {

    }

    @Test @Ignore
    public void testGetSlicePoints() {

    }

    @Test @Ignore
    public void testConvertObjToImage() {

    }

    @Test @Ignore
    public void testCropToImageSize() {

    }

    @Test
    public void testHashCodeSameObject() throws IntegerOverflowException {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj1.setT(1);
        obj1.add(1,3,4);
        obj1.add(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj2.setT(1);
        obj2.add(1,3,4);
        obj2.add(3,5,1);

        assertEquals(obj1.hashCode(),obj2.hashCode());

    }

    @Test
    public void testHashCodeDifferentOrder() throws IntegerOverflowException {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj1.setT(1);
        obj1.add(1,3,4);
        obj1.add(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj2.setT(1);
        obj2.add(3,5,1);
        obj2.add(1,3,4);

        assertEquals(obj1.hashCode(),obj2.hashCode());

    }

    @Test
    public void testHashCodeDifferentNames() throws IntegerOverflowException {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj1.setT(1);
        obj1.add(1,3,4);
        obj1.add(3,5,1);

        Obj obj2 = new Obj("Obj2",1,2.0,1.0,"PX",false);
        obj2.setT(1);
        obj2.add(1,3,4);
        obj2.add(3,5,1);

        assertEquals(obj1.hashCode(),obj2.hashCode());

    }

    @Test
    public void testHashCodeDifferentTimepoint() throws IntegerOverflowException {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj1.setT(1);
        obj1.add(1,1,1);
        obj1.add(2,1,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj2.setT(2);
        obj2.add(1,1,1);
        obj2.add(1,2,1);

        assertNotEquals(obj1.hashCode(),obj2.hashCode());

    }

    @Test
    public void testHashCodeDifferentCoordinates() throws IntegerOverflowException {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj1.setT(1);
        obj1.add(1,3,4);
        obj1.add(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj2.setT(1);
        obj2.add(1,3,3);
        obj2.add(3,5,1);

        assertNotEquals(obj1.hashCode(),obj2.hashCode());

    }

    @Test
    public void testHashCodeMissingCoordinates() throws IntegerOverflowException {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj1.setT(1);
        obj1.add(1,3,4);
        obj1.add(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj2.setT(1);
        obj2.add(1,3,4);

        assertNotEquals(obj1.hashCode(),obj2.hashCode());

    }

    @Test
    public void testEqualsSameObject() throws IntegerOverflowException {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj1.setT(1);
        obj1.add(1,3,4);
        obj1.add(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj2.setT(1);
        obj2.add(1,3,4);
        obj2.add(3,5,1);

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));

    }

    @Test
    public void testEqualsDifferentOrder() throws IntegerOverflowException {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj1.setT(1);
        obj1.add(1,3,4);
        obj1.add(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj2.setT(1);
        obj2.add(3,5,1);
        obj2.add(1,3,4);

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));

    }

    @Test
    public void testEqualsDifferentNames() throws IntegerOverflowException {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj1.setT(1);
        obj1.add(1,3,4);
        obj1.add(3,5,1);

        Obj obj2 = new Obj("Obj2",1,2.0,1.0,"PX",false);
        obj2.setT(1);
        obj2.add(1,3,4);
        obj2.add(3,5,1);

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));

    }

    @Test
    public void testEqualsDifferentTimepoint() throws IntegerOverflowException {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj1.setT(1);
        obj1.add(1,1,1);
        obj1.add(2,1,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj2.setT(2);
        obj2.add(1,1,1);
        obj2.add(1,2,1);

        assertFalse(obj1.equals(obj2));
        assertFalse(obj2.equals(obj1));

    }

    @Test
    public void testEqualsDifferentCoordinates() throws IntegerOverflowException {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj1.setT(1);
        obj1.add(1,3,4);
        obj1.add(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj2.setT(1);
        obj2.add(1,3,3);
        obj2.add(3,5,1);

        assertFalse(obj1.equals(obj2));
        assertFalse(obj2.equals(obj1));

    }

    @Test
    public void testEqualsMissingCoordinates() throws IntegerOverflowException {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj1.setT(1);
        obj1.add(1,3,4);
        obj1.add(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX",false);
        obj2.setT(1);
        obj2.add(1,3,4);

        assertFalse(obj1.equals(obj2));
        assertFalse(obj2.equals(obj1));

    }
}