package wbif.sjx.MIA.Process;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ome.units.UNITS;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.common.Object.Volume.SpatCal;
import wbif.sjx.common.Object.Volume.VolumeType;

import java.awt.*;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class ColourFactoryTest {
    private double tolerance = 1E-2;

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetHuesSingleColour(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getSingleColourHues(collection,ColourFactory.SingleColours.WHITE);

        assertEquals(3,actual.size());
        assertEquals(Float.MAX_VALUE,actual.get(0),tolerance);
        assertEquals(Float.MAX_VALUE,actual.get(1),tolerance);
        assertEquals(Float.MAX_VALUE,actual.get(2),tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetHuesRandomColour(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getRandomHues(collection);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertNotNull(actual.get(0));
        assertNotNull(actual.get(1));
        assertNotNull(actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetHuesMeasurementColour(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
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

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetHuesMeasurementColourNormalised(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
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

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetHuesIDColour(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
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

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetHuesIDColourNormalised(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
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

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetHuesParentIDColour(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj", calibration, 1, 0.02, UNITS.SECOND);
        ObjCollection parents = new ObjCollection("Parents", calibration, 1, 0.02, UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        Obj parent = parents.createAndAddNewObject(volumeType, 6);
        obj.addParent(parent);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
        parent = parents.createAndAddNewObject(volumeType,5);
        obj.addParent(parent);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getParentIDHues(collection,"Parents",false);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(6f,actual.get(0),tolerance);
        assertEquals(-1f,actual.get(1),tolerance);
        assertEquals(5f,actual.get(2),tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetHuesParentIDColourNormalised(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1,0.02,UNITS.SECOND);
        ObjCollection parents = new ObjCollection("Parents", calibration, 1, 0.02, UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        Obj parent = parents.createAndAddNewObject(volumeType, 6);
        obj.addParent(parent);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
        parent = parents.createAndAddNewObject(volumeType, 5);
        obj.addParent(parent);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getParentIDHues(collection,"Parents",true);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(0.38f,actual.get(0),tolerance);
        assertEquals(-1f,actual.get(1),tolerance);
        assertEquals(0.31f,actual.get(2),tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetColoursSingleColour(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(collection,ColourFactory.SingleColours.WHITE);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues,100);

        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(1f,0f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(1f,0f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(1f,0f,1f),actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetColoursRandomColour(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getRandomHues(collection);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues,100);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertNotNull(actual.get(0));
        assertNotNull(actual.get(1));
        assertNotNull(actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetColoursMeasurementColour(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getMeasurementValueHues(collection,"Meas",false);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues,100);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(3.2f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(-0.1f,1f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(1f,1f,1f),actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetColoursMeasurementColourNormalised(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getMeasurementValueHues(collection,"Meas",true);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues,100);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(0.4706f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(0f,1f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(1f,1f,1f),actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetColoursIDColour(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getIDHues(collection,false);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues,100);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(0f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(1f,1f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(2f,1f,1f),actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetColoursIDColourNormalised(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getIDHues(collection,true);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues,100);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(0f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(0.0627f,1f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(0.1255f,1f,1f),actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetColoursParentIDColour(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj", calibration, 1, 0.02, UNITS.SECOND);
        ObjCollection parents = new ObjCollection("Parents", calibration, 1, 0.02, UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        Obj parent = parents.createAndAddNewObject(volumeType, 6);
        obj.addParent(parent);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
        parent = parents.createAndAddNewObject(volumeType, 5);
        obj.addParent(parent);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getParentIDHues(collection,"Parents",false);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues,100);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(6f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(0f,0f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(5f,1f,1f),actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetColoursParentIDColourNormalised(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj", calibration, 1, 0.02, UNITS.SECOND);
        ObjCollection parents = new ObjCollection("Parents", calibration, 1, 0.02, UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        Obj parent = parents.createAndAddNewObject(volumeType, 6);
        obj.addParent(parent);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObject(volumeType, 2);
        parent = parents.createAndAddNewObject(volumeType, 5);
        obj.addParent(parent);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getParentIDHues(collection,"Parents",true);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues,100);

        // For random numbers we don't know what value they will have
        assertEquals(3,actual.size());
        assertEquals(Color.getHSBColor(0.3765f,1f,1f),actual.get(0));
        assertEquals(Color.getHSBColor(0f,0f,1f),actual.get(1));
        assertEquals(Color.getHSBColor(0.3137f,1f,1f),actual.get(2));

    }
}