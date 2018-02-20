package wbif.sjx.ModularImageAnalysis.Object;

import org.junit.Test;

import static org.junit.Assert.*;

public class ObjTest {
    @Test
    public void testHashCodeSameObject() {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj1.setT(1);
        obj1.addCoord(1,3,4);
        obj1.addCoord(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj2.setT(1);
        obj2.addCoord(1,3,4);
        obj2.addCoord(3,5,1);

        assertEquals(obj1.hashCode(),obj2.hashCode());

    }

    @Test
    public void testHashCodeDifferentOrder() {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj1.setT(1);
        obj1.addCoord(1,3,4);
        obj1.addCoord(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj2.setT(1);
        obj2.addCoord(3,5,1);
        obj2.addCoord(1,3,4);

        assertEquals(obj1.hashCode(),obj2.hashCode());

    }

    @Test
    public void testHashCodeDifferentNames() {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj1.setT(1);
        obj1.addCoord(1,3,4);
        obj1.addCoord(3,5,1);

        Obj obj2 = new Obj("Obj2",1,2.0,1.0,"PX");
        obj2.setT(1);
        obj2.addCoord(1,3,4);
        obj2.addCoord(3,5,1);

        assertEquals(obj1.hashCode(),obj2.hashCode());

    }

    @Test
    public void testHashCodeDifferentTimepoint() {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj1.setT(1);
        obj1.addCoord(1,1,1);
        obj1.addCoord(2,1,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj2.setT(2);
        obj2.addCoord(1,1,1);
        obj2.addCoord(1,2,1);

        assertNotEquals(obj1.hashCode(),obj2.hashCode());

    }

    @Test
    public void testHashCodeDifferentCoordinates() {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj1.setT(1);
        obj1.addCoord(1,3,4);
        obj1.addCoord(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj2.setT(1);
        obj2.addCoord(1,3,3);
        obj2.addCoord(3,5,1);

        assertNotEquals(obj1.hashCode(),obj2.hashCode());

    }

    @Test
    public void testHashCodeMissingCoordinates() {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj1.setT(1);
        obj1.addCoord(1,3,4);
        obj1.addCoord(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj2.setT(1);
        obj2.addCoord(1,3,4);

        assertNotEquals(obj1.hashCode(),obj2.hashCode());

    }

    @Test
    public void testEqualsSameObject() {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj1.setT(1);
        obj1.addCoord(1,3,4);
        obj1.addCoord(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj2.setT(1);
        obj2.addCoord(1,3,4);
        obj2.addCoord(3,5,1);

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));

    }

    @Test
    public void testEqualsDifferentOrder() {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj1.setT(1);
        obj1.addCoord(1,3,4);
        obj1.addCoord(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj2.setT(1);
        obj2.addCoord(3,5,1);
        obj2.addCoord(1,3,4);

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));

    }

    @Test
    public void testEqualsDifferentNames() {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj1.setT(1);
        obj1.addCoord(1,3,4);
        obj1.addCoord(3,5,1);

        Obj obj2 = new Obj("Obj2",1,2.0,1.0,"PX");
        obj2.setT(1);
        obj2.addCoord(1,3,4);
        obj2.addCoord(3,5,1);

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));

    }

    @Test
    public void testEqualsDifferentTimepoint() {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj1.setT(1);
        obj1.addCoord(1,1,1);
        obj1.addCoord(2,1,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj2.setT(2);
        obj2.addCoord(1,1,1);
        obj2.addCoord(1,2,1);

        assertFalse(obj1.equals(obj2));
        assertFalse(obj2.equals(obj1));

    }

    @Test
    public void testEqualsDifferentCoordinates() {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj1.setT(1);
        obj1.addCoord(1,3,4);
        obj1.addCoord(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj2.setT(1);
        obj2.addCoord(1,3,3);
        obj2.addCoord(3,5,1);

        assertFalse(obj1.equals(obj2));
        assertFalse(obj2.equals(obj1));

    }

    @Test
    public void testEqualsMissingCoordinates() {
        Obj obj1 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj1.setT(1);
        obj1.addCoord(1,3,4);
        obj1.addCoord(3,5,1);

        Obj obj2 = new Obj("Obj1",1,2.0,1.0,"PX");
        obj2.setT(1);
        obj2.addCoord(1,3,4);

        assertFalse(obj1.equals(obj2));
        assertFalse(obj2.equals(obj1));

    }
}