package wbif.sjx.MIA.Process;

import org.junit.jupiter.api.Test;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;

import java.text.DecimalFormat;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class LabelFactoryTest {
    @Test
    public void testGetIDsIDScientific() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,1,1,1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,1,1,1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,1,1,1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        DecimalFormat df = LabelFactory.getDecimalFormat(2,true);
        HashMap<Integer, String> actual = LabelFactory.getIDLabels(collection,df);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,1,1,1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,1,1,1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,1,1,1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        DecimalFormat df = LabelFactory.getDecimalFormat(0,false);
        HashMap<Integer, String> actual = LabelFactory.getIDLabels(collection,df);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,1,1,1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,1,1,1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,1,1,1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        DecimalFormat df = LabelFactory.getDecimalFormat(1,false);
        HashMap<Integer, String> actual = LabelFactory.getIDLabels(collection,df);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,1,1,1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",1,1,1,1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,1,1,1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        DecimalFormat df = LabelFactory.getDecimalFormat(2,false);
        HashMap<Integer, String> actual = LabelFactory.getIDLabels(collection,df);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,1,1,1,dppXY,dppZ,calibratedUnits);
        Obj parent = new Obj("Parent",6,1,1,1,dppXY,dppZ,calibratedUnits);
        obj.addParent(parent);
        collection.add(obj);

        obj = new Obj("Obj",1,1,1,1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("Obj",2,1,1,1,dppXY,dppZ,calibratedUnits);
        parent = new Obj("Parent",5,1,1,1,dppXY,dppZ,calibratedUnits);
        obj.addParent(parent);
        collection.add(obj);

        DecimalFormat df = LabelFactory.getDecimalFormat(0,false);
        HashMap<Integer, String> actual = LabelFactory.getParentIDLabels(collection,"Parent",df);

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
        ObjCollection collection = new ObjCollection("Obj");

        // Adding objects
        Obj obj = new Obj("Obj",0,1,1,1,dppXY,dppZ,calibratedUnits);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",1,1,1,1,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj("Obj",2,1,1,1,dppXY,dppZ,calibratedUnits);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        DecimalFormat df = LabelFactory.getDecimalFormat(2,false);
        HashMap<Integer, String> actual = LabelFactory.getMeasurementLabels(collection,"Meas",df);

        assertEquals(3,actual.size());
        assertEquals("3.20",actual.get(0));
        assertEquals("-0.10",actual.get(1));
        assertEquals("NA",actual.get(2));
    }
}