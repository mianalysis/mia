package wbif.sjx.ModularImageAnalysis.Process;

import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Object.Measurement;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;

import java.awt.*;
import java.util.HashMap;

import static org.junit.Assert.*;

public class ColourFactoryTest {
    private double tolerance = 1E-2;

    @Test
    public void testGetHuesSingleColour() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getSingleColourHues(collection,ColourFactory.SingleColours.WHITE);

        assertEquals(3,actual.size());
        assertEquals(Float.MAX_VALUE,actual.get(0),tolerance);
        assertEquals(Float.MAX_VALUE,actual.get(1),tolerance);
        assertEquals(Float.MAX_VALUE,actual.get(2),tolerance);

    }

    @Test
    public void testGetHuesRandomColour() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getRandomHues(collection);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getMeasurementValueHues(collection,"Meas",false);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getMeasurementValueHues(collection,"Meas",true);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getIDHues(collection,false);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getIDHues(collection,true);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        Obj parent = new Obj("Parent",6,dppXY,dppZ,calibratedUnits,false);
        obj.addParent(parent);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        parent = new Obj("Parent",5,dppXY,dppZ,calibratedUnits,false);
        obj.addParent(parent);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getParentIDHues(collection,"Parent",false);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(6f,actual.get(0),tolerance);
        assertEquals(-1f,actual.get(1),tolerance);
        assertEquals(5f,actual.get(2),tolerance);

    }

    @Test
    public void testGetHuesParentIDColourNormalised() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        Obj parent = new Obj("Parent",6,dppXY,dppZ,calibratedUnits,false);
        obj.addParent(parent);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        parent = new Obj("Parent",5,dppXY,dppZ,calibratedUnits,false);
        obj.addParent(parent);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getParentIDHues(collection,"Parent",true);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(0.38f,actual.get(0),tolerance);
        assertEquals(-1f,actual.get(1),tolerance);
        assertEquals(0.31f,actual.get(2),tolerance);

    }

    @Test
    public void testGetColoursSingleColour() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(collection,ColourFactory.SingleColours.WHITE);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues);

        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(1f,0f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(1f,0f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(1f,0f,1f),actual.get(2));

    }

    @Test
    public void testGetColoursRandomColour() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getRandomHues(collection);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getMeasurementValueHues(collection,"Meas",false);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getMeasurementValueHues(collection,"Meas",true);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getIDHues(collection,false);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getIDHues(collection,true);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        Obj parent = new Obj("Parent",6,dppXY,dppZ,calibratedUnits,false);
        obj.addParent(parent);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        parent = new Obj("Parent",5,dppXY,dppZ,calibratedUnits,false);
        obj.addParent(parent);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getParentIDHues(collection,"Parent",false);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(6f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(0f,0f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(5f,1f,1f),actual.get(2));

    }

    @Test
    public void testGetColoursParentIDColourNormalised() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,dppXY,dppZ,calibratedUnits,false);
        Obj parent = new Obj("Parent",6,dppXY,dppZ,calibratedUnits,false);
        obj.addParent(parent);
        collection.add(obj);

        obj = new Obj("Obj",1,dppXY,dppZ,calibratedUnits,false);
        collection.add(obj);

        obj = new Obj("Obj",2,dppXY,dppZ,calibratedUnits,false);
        parent = new Obj("Parent",5,dppXY,dppZ,calibratedUnits,false);
        obj.addParent(parent);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getParentIDHues(collection,"Parent",true);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(0.3765f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(0f,0f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(0.3137f,1f,1f),actual.get(2));

    }
}