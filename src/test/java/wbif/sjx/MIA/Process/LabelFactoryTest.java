package wbif.sjx.MIA.Process;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.common.Object.Volume.SpatCal;
import wbif.sjx.common.Object.Volume.VolumeType;

import java.text.DecimalFormat;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class LabelFactoryTest {
    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetIDsIDScientific(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1);

        // Adding objects
        Obj obj = new Obj(volumeType,"Obj",0,calibration,1);
        collection.add(obj);

        obj = new Obj(volumeType,"Obj",1,calibration,1);
        collection.add(obj);

        obj = new Obj(volumeType,"Obj",2,calibration,1);
        collection.add(obj);

        DecimalFormat df = LabelFactory.getDecimalFormat(2,true);
        HashMap<Integer, String> actual = LabelFactory.getIDLabels(collection,df);

        assertEquals(3,actual.size());
        assertEquals("0.00E0",actual.get(0));
        assertEquals("1.00E0",actual.get(1));
        assertEquals("2.00E0",actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetIDsIDZeroDecimalPlaces(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1);

        // Adding objects
        Obj obj = new Obj(volumeType,"Obj",0,calibration,1);
        collection.add(obj);

        obj = new Obj(volumeType,"Obj",1,calibration,1);
        collection.add(obj);

        obj = new Obj(volumeType,"Obj",2,calibration,1);
        collection.add(obj);

        DecimalFormat df = LabelFactory.getDecimalFormat(0,false);
        HashMap<Integer, String> actual = LabelFactory.getIDLabels(collection,df);

        assertEquals(3,actual.size());
        assertEquals("0",actual.get(0));
        assertEquals("1",actual.get(1));
        assertEquals("2",actual.get(2));
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetIDsIDOneDecimalPlaces(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1);

        // Adding objects
        Obj obj = new Obj(volumeType,"Obj",0,calibration,1);
        collection.add(obj);

        obj = new Obj(volumeType,"Obj",1,calibration,1);
        collection.add(obj);

        obj = new Obj(volumeType,"Obj",2,calibration,1);
        collection.add(obj);

        DecimalFormat df = LabelFactory.getDecimalFormat(1,false);
        HashMap<Integer, String> actual = LabelFactory.getIDLabels(collection,df);

        assertEquals(3,actual.size());
        assertEquals("0.0",actual.get(0));
        assertEquals("1.0",actual.get(1));
        assertEquals("2.0",actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetIDsIDTwoDecimalPlaces(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1);

        // Adding objects
        Obj obj = new Obj(volumeType,"Obj",0,calibration,1);
        collection.add(obj);

        obj = new Obj(volumeType,"Obj",1,calibration,1);
        collection.add(obj);

        obj = new Obj(volumeType,"Obj",2,calibration,1);
        collection.add(obj);

        DecimalFormat df = LabelFactory.getDecimalFormat(2,false);
        HashMap<Integer, String> actual = LabelFactory.getIDLabels(collection,df);

        assertEquals(3,actual.size());
        assertEquals("0.00",actual.get(0));
        assertEquals("1.00",actual.get(1));
        assertEquals("2.00",actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetIDsParentID(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1);

        // Adding objects
        Obj obj = new Obj(volumeType,"Obj",0,calibration,1);
        Obj parent = new Obj(volumeType,"Parent",6,calibration,1);
        obj.addParent(parent);
        collection.add(obj);

        obj = new Obj(volumeType,"Obj",1,calibration,1);
        collection.add(obj);

        obj = new Obj(volumeType,"Obj",2,calibration,1);
        parent = new Obj(volumeType,"Parent",5,calibration,1);
        obj.addParent(parent);
        collection.add(obj);

        DecimalFormat df = LabelFactory.getDecimalFormat(0,false);
        HashMap<Integer, String> actual = LabelFactory.getParentIDLabels(collection,"Parent",df);

        assertEquals(3,actual.size());
        assertEquals("6",actual.get(0));
        assertEquals("NA",actual.get(1));
        assertEquals("5",actual.get(2));
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetIDsMeasurement(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the ObjCollection
        ObjCollection collection = new ObjCollection("Obj",calibration,1);

        // Adding objects
        Obj obj = new Obj(volumeType,"Obj",0,calibration,1);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj(volumeType,"Obj",1,calibration,1);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = new Obj(volumeType,"Obj",2,calibration,1);
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