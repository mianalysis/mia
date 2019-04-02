package wbif.sjx.MIA.Object;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.common.Exceptions.IntegerOverflowException;

import static org.junit.Assert.*;

public class ObjCollectionTest {
    private double tolerance = 1E-2;

    @Test
    public void testGetFirstPresent() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        ObjCollection collection = new ObjCollection("TestObj");

        Obj obj = new Obj("New obj",0,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("New obj",1,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("New obj",2,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        Obj firstObj = collection.getFirst();
        assertNotNull(firstObj);
        assertEquals(0,firstObj.getID());

    }

    @Test
    public void testGetFirstAbsent() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        ObjCollection collection = new ObjCollection("TestObj");
        Obj firstObj = collection.getFirst();
        assertNull(firstObj);

    }

    @Test
    public void testGetSpatialLimits() throws IntegerOverflowException {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        obj.addCoord(3,1,6);
        obj.addCoord(2,2,8);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        obj.addCoord(3,2,2);
        obj.addCoord(2,2,9);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        obj.addCoord(4,1,2);
        obj.addCoord(6,2,10);
        collection.add(obj);

        // Getting expected spatial limits
        int[][] expected = new int[][]{{2,6},{1,2},{2,10}};

        // Checking actual values
        int[][] actual = collection.getSpatialLimits();

        for (int i=0;i<expected.length;i++) {
            assertArrayEquals(expected[i],actual[i]);
        }
    }

    @Test
    public void testGetTimepointLimits() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        obj.setT(9);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        obj.setT(3);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        obj.setT(12);
        collection.add(obj);

        int[] expected = new int[]{3,12};
        int[] actual = collection.getTemporalLimits();

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testGetLargestID() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",4,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",7,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        assertEquals(7,collection.getLargestID());

    }

    @Test @Ignore
    public void testConvertObjectsToImageSingleColour() {
    }

    @Test @Ignore
    public void testConvertObjectsToImageSingleColourNoTemplateImage() {
    }

    @Test @Ignore
    public void testConvertObjectsToImageRandomColour() {
    }

    @Test @Ignore
    public void testConvertObjectsToImageMeasurementColour() {
    }

    @Test @Ignore
    public void testConvertObjectsToImageIDColour() {
    }

    @Test @Ignore
    public void testConvertObjectsToImageParentIDColour() {
    }

    @Test
    public void testGetByEquals() throws IntegerOverflowException {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj1 = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        obj1.addCoord(3,2,2);
        obj1.addCoord(2,2,9);
        collection.add(obj1);

        Obj obj2 = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        obj2.addCoord(3,2,2);
        obj2.addCoord(2,2,9);
        obj2.addCoord(3,1,6);
        obj2.addCoord(2,2,8);
        collection.add(obj2);

        Obj obj3 = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        obj3.addCoord(4,1,2);
        obj3.addCoord(6,2,10);
        collection.add(obj3);

        Obj oj4 = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        oj4.addCoord(4,1,2);
        oj4.addCoord(6,2,10);
        oj4.addCoord(3,2,2);
        oj4.addCoord(2,2,9);
        collection.add(oj4);

        // Creating a test object with the same coordinates as one of the other objects
        Obj testObj = new Obj("Obj",5,dppXY,dppZ,calibratedUnits,false);
        testObj.addCoord(3,1,6);
        testObj.addCoord(2,2,8);
        testObj.addCoord(3,2,2);
        testObj.addCoord(2,2,9);
        collection.add(testObj);

        Obj actual = collection.getByEquals(testObj);

        assertEquals(obj2,actual);
        assertEquals(obj2.getID(),actual.getID());

    }
}