package wbif.sjx.ModularImageAnalysis.Object;

import org.junit.Ignore;
import org.junit.Test;

import java.awt.*;
import java.util.HashMap;

import static org.junit.Assert.*;

public class ObjCollectionTest {
    private double tolerance = 1E-2;

    @Test
    public void testGetFirstPresent() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        ObjCollection collection = new ObjCollection("TestObj",false);

        Obj obj = new Obj("New obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("New obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("New obj",2,dppXY,dppZ,calibratedUnits);
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

        ObjCollection collection = new ObjCollection("TestObj",false);
        Obj firstObj = collection.getFirst();
        assertNull(firstObj);

    }

    @Test
    public void testGetSpatialLimits() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        obj.addCoord(3,1,6);
        obj.addCoord(2,2,8);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        obj.addCoord(3,2,2);
        obj.addCoord(2,2,9);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
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
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        obj.setT(9);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        obj.setT(3);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        obj.setT(12);
        collection.add(obj);

        int[] expected = new int[]{3,12};
        int[] actual = collection.getTimepointLimits();

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testGetLargestID() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",4,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",7,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
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
    public void testGetIDsIDScientific() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        String labelMode = ObjCollection.LabelModes.ID;
        HashMap<Integer, String> actual = collection.getIDs(labelMode, "", 2, true);

        assertEquals(3,actual.size());
        assertEquals("0.00E0",actual.get(0));
        assertEquals("1.00E0",actual.get(1));
        assertEquals("2.00E0",actual.get(2));

    }

    @Test
    public void testGetIDsIDZeroDecimalPlaces() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        String labelMode = ObjCollection.LabelModes.ID;
        HashMap<Integer, String> actual = collection.getIDs(labelMode, "", 0, false);

        assertEquals(3,actual.size());
        assertEquals("0",actual.get(0));
        assertEquals("1",actual.get(1));
        assertEquals("2",actual.get(2));
    }

    @Test
    public void testGetIDsIDOneDecimalPlaces() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        String labelMode = ObjCollection.LabelModes.ID;
        HashMap<Integer, String> actual = collection.getIDs(labelMode, "", 1, false);

        assertEquals(3,actual.size());
        assertEquals("0.0",actual.get(0));
        assertEquals("1.0",actual.get(1));
        assertEquals("2.0",actual.get(2));

    }

    @Test
    public void testGetIDsIDTwoDecimalPlaces() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        String labelMode = ObjCollection.LabelModes.ID;
        HashMap<Integer, String> actual = collection.getIDs(labelMode, "", 2, false);

        assertEquals(3,actual.size());
        assertEquals("0.00",actual.get(0));
        assertEquals("1.00",actual.get(1));
        assertEquals("2.00",actual.get(2));

    }

    @Test
    public void testGetIDsParentID() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        Obj parent = new Obj("Parent",6,dppXY,dppZ,calibratedUnits);
        obj.addParent(parent);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        parent = new Obj("Parent",5,dppXY,dppZ,calibratedUnits);
        obj.addParent(parent);
        collection.add(obj);

        String labelMode = ObjCollection.LabelModes.PARENT_ID;
        HashMap<Integer, String> actual = collection.getIDs(labelMode, "Parent", 0, false);

        assertEquals(3,actual.size());
        assertEquals("6",actual.get(0));
        assertEquals("NA",actual.get(1));
        assertEquals("5",actual.get(2));
    }

    @Test
    public void testGetIDsMeasurement() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        String labelMode = ObjCollection.LabelModes.MEASUREMENT_VALUE;
        HashMap<Integer, String> actual = collection.getIDs(labelMode, "Meas", 2, false);

        assertEquals(3,actual.size());
        assertEquals("3.20",actual.get(0));
        assertEquals("-0.10",actual.get(1));
        assertEquals("NA",actual.get(2));
    }

    @Test
    public void testGetHuesSingleColour() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.SINGLE_COLOUR;
        HashMap<Integer, Float> actual = collection.getHues(colourMode, "", false);

        assertEquals(3,actual.size());
        assertEquals(1f,actual.get(0),tolerance);
        assertEquals(1f,actual.get(1),tolerance);
        assertEquals(1f,actual.get(2),tolerance);

    }

    @Test
    public void testGetHuesSingleColourNormalised() {
        // For "single colour", normalisation shouldn't do anything
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.SINGLE_COLOUR;
        HashMap<Integer, Float> actual = collection.getHues(colourMode, "", true);

        assertEquals(3,actual.size());
        assertEquals(1f,actual.get(0),tolerance);
        assertEquals(1f,actual.get(1),tolerance);
        assertEquals(1f,actual.get(2),tolerance);

    }

    @Test
    public void testGetHuesRandomColour() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.RANDOM_COLOUR;
        HashMap<Integer, Float> actual = collection.getHues(colourMode, "", false);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertNotNull(actual.get(0));
        assertNotNull(actual.get(1));
        assertNotNull(actual.get(2));

    }

    @Test
    public void testGetHuesRandomColourNormalised() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.RANDOM_COLOUR;
        HashMap<Integer, Float> actual = collection.getHues(colourMode, "", true);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertNotNull(actual.get(0));
        assertNotNull(actual.get(1));
        assertNotNull(actual.get(2));

    }

    @Test
    public void testGetHuesMeasurementColour() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.MEASUREMENT_VALUE;
        HashMap<Integer, Float> actual = collection.getHues(colourMode, "Meas", false);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(3.2,actual.get(0),tolerance);
        assertEquals(-0.1,actual.get(1),tolerance);
        assertEquals(Double.NaN,actual.get(2),tolerance);

    }

    @Test
    public void testGetHuesMeasurementColourNormalised() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.MEASUREMENT_VALUE;
        HashMap<Integer, Float> actual = collection.getHues(colourMode, "Meas", true);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(0.47f,actual.get(0),tolerance);
        assertEquals(0f,actual.get(1),tolerance);
        assertEquals(Double.NaN,actual.get(2),tolerance);

    }

    @Test
    public void testGetHuesIDColour() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.ID;
        HashMap<Integer, Float> actual = collection.getHues(colourMode, "", false);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(0f,actual.get(0),tolerance);
        assertEquals(1f,actual.get(1),tolerance);
        assertEquals(2f,actual.get(2),tolerance);

    }

    @Test
    public void testGetHuesIDColourNormalised() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.ID;
        HashMap<Integer, Float> actual = collection.getHues(colourMode, "", true);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(0f,actual.get(0),tolerance);
        assertEquals(0.06f,actual.get(1),tolerance);
        assertEquals(0.13f,actual.get(2),tolerance);

    }

    @Test
    public void testGetHuesParentIDColour() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        Obj parent = new Obj("Parent",6,dppXY,dppZ,calibratedUnits);
        obj.addParent(parent);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        parent = new Obj("Parent",5,dppXY,dppZ,calibratedUnits);
        obj.addParent(parent);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.PARENT_ID;
        HashMap<Integer, Float> actual = collection.getHues(colourMode, "Parent", false);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(6f,actual.get(0),tolerance);
        assertEquals(0.2f,actual.get(1),tolerance);
        assertEquals(5f,actual.get(2),tolerance);

    }

    @Test
    public void testGetHuesParentIDColourNormalised() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        Obj parent = new Obj("Parent",6,dppXY,dppZ,calibratedUnits);
        obj.addParent(parent);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        parent = new Obj("Parent",5,dppXY,dppZ,calibratedUnits);
        obj.addParent(parent);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.PARENT_ID;
        HashMap<Integer, Float> actual = collection.getHues(colourMode, "Parent", true);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(0.38f,actual.get(0),tolerance);
        assertEquals(0.41f,actual.get(1),tolerance);
        assertEquals(0.31f,actual.get(2),tolerance);

    }

    @Test
    public void testGetColoursSingleColour() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.SINGLE_COLOUR;
        HashMap<Integer, Color> actual = collection.getColours(colourMode, "", false);

        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(1f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(1f,1f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(1f,1f,1f),actual.get(2));

    }

    @Test
    public void testGetColoursSingleColourNormalised() {
// For "single colour", normalisation shouldn't do anything
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.SINGLE_COLOUR;
        HashMap<Integer, Color> actual = collection.getColours(colourMode, "", true);

        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(1f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(1f,1f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(1f,1f,1f),actual.get(2));

    }

    @Test
    public void testGetColoursRandomColour() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.RANDOM_COLOUR;
        HashMap<Integer, Color> actual = collection.getColours(colourMode, "", false);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertNotNull(actual.get(0));
        assertNotNull(actual.get(1));
        assertNotNull(actual.get(2));

    }

    @Test
    public void testGetColoursRandomColourNormalised() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.RANDOM_COLOUR;
        HashMap<Integer, Color> actual = collection.getColours(colourMode, "", true);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertNotNull(actual.get(0));
        assertNotNull(actual.get(1));
        assertNotNull(actual.get(2));
    }

    @Test
    public void testGetColoursMeasurementColour() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.MEASUREMENT_VALUE;
        HashMap<Integer, Color> actual = collection.getColours(colourMode, "Meas", false);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(3.2f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(-0.1f,1f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(1f,1f,1f),actual.get(2));

    }

    @Test
    public void testGetColoursMeasurementColourNormalised() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.MEASUREMENT_VALUE;
        HashMap<Integer, Color> actual = collection.getColours(colourMode, "Meas", true);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(0.4706f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(0f,1f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(1f,1f,1f),actual.get(2));

    }

    @Test
    public void testGetColoursIDColour() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.ID;
        HashMap<Integer, Color> actual = collection.getColours(colourMode, "", false);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(0f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(1f,1f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(2f,1f,1f),actual.get(2));

    }

    @Test
    public void testGetColoursIDColourNormalised() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.ID;
        HashMap<Integer, Color> actual = collection.getColours(colourMode, "", true);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(0f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(0.0627f,1f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(0.1255f,1f,1f),actual.get(2));

    }

    @Test
    public void testGetColoursParentIDColour() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        Obj parent = new Obj("Parent",6,dppXY,dppZ,calibratedUnits);
        obj.addParent(parent);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        parent = new Obj("Parent",5,dppXY,dppZ,calibratedUnits);
        obj.addParent(parent);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.PARENT_ID;
        HashMap<Integer, Color> actual = collection.getColours(colourMode, "Parent", false);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(6f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(0.2f,1f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(5f,1f,1f),actual.get(2));

    }

    @Test
    public void testGetColoursParentIDColourNormalised() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        Obj parent = new Obj("Parent",6,dppXY,dppZ,calibratedUnits);
        obj.addParent(parent);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        parent = new Obj("Parent",5,dppXY,dppZ,calibratedUnits);
        obj.addParent(parent);
        collection.add(obj);

        String colourMode = ObjCollection.ColourModes.PARENT_ID;
        HashMap<Integer, Color> actual = collection.getColours(colourMode, "Parent", true);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(0.3765f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(0.4125f,1f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(0.3137f,1f,1f),actual.get(2));

    }

    @Test
    public void testGetByEquals() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",false);

        // Adding objects
        Obj obj1 = new Obj("Obj",1,dppXY,dppZ,calibratedUnits);
        obj1.addCoord(3,2,2);
        obj1.addCoord(2,2,9);
        collection.add(obj1);

        Obj obj2 = new Obj("Obj",0,dppXY,dppZ,calibratedUnits);
        obj2.addCoord(3,2,2);
        obj2.addCoord(2,2,9);
        obj2.addCoord(3,1,6);
        obj2.addCoord(2,2,8);
        collection.add(obj2);

        Obj obj3 = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        obj3.addCoord(4,1,2);
        obj3.addCoord(6,2,10);
        collection.add(obj3);

        Obj oj4 = new Obj("Obj",2,dppXY,dppZ,calibratedUnits);
        oj4.addCoord(4,1,2);
        oj4.addCoord(6,2,10);
        oj4.addCoord(3,2,2);
        oj4.addCoord(2,2,9);
        collection.add(oj4);

        // Creating a test object with the same coordinates as one of the other objects
        Obj testObj = new Obj("Obj",5,dppXY,dppZ,calibratedUnits);
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