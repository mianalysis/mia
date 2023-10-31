package io.github.mianalysis.mia.process;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ome.units.UNITS;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;

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

        // Creating the Objs
        Objs collection = new Objs("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        obj = collection.createAndAddNewObject(volumeType, 1);
        obj = collection.createAndAddNewObject(volumeType, 2);

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

        // Creating the Objs
        Objs collection = new Objs("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        obj = collection.createAndAddNewObject(volumeType, 1);
        obj = collection.createAndAddNewObject(volumeType, 2);

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

        // Creating the Objs
        Objs collection = new Objs("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        obj = collection.createAndAddNewObject(volumeType, 1);
        obj = collection.createAndAddNewObject(volumeType, 2);

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

        // Creating the Objs
        Objs collection = new Objs("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        obj = collection.createAndAddNewObject(volumeType, 1);
        obj = collection.createAndAddNewObject(volumeType, 2);

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
        String calibratedUnits = "m*10-6";

        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,1,1,1);

        // Creating the Objs
        Objs collection = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Objs parents = new Objs("Parents", calibration, 1, 0.02, UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        Obj parent = parents.createAndAddNewObject(volumeType, 6);
        obj.addParent(parent);

        obj = collection.createAndAddNewObject(volumeType, 1);

        obj = collection.createAndAddNewObject(volumeType, 2);
        parent = parents.createAndAddNewObject(volumeType, 5);
        obj.addParent(parent);

        DecimalFormat df = LabelFactory.getDecimalFormat(0,false);
        HashMap<Integer, String> actual = LabelFactory.getParentIDLabels(collection,"Parents",df);

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

        // Creating the Objs
        Objs collection = new Objs("Obj",calibration,1,0.02,UNITS.SECOND);

        // Adding objects
        Obj obj = collection.createAndAddNewObject(volumeType, 0);
        Measurement meas = new Measurement("Meas",3.2);
        obj.addMeasurement(meas);

        obj = collection.createAndAddNewObject(volumeType, 1);
        meas = new Measurement("Meas",-0.1);
        obj.addMeasurement(meas);

        obj = collection.createAndAddNewObject(volumeType, 2);
        meas = new Measurement("Meas",Double.NaN);
        obj.addMeasurement(meas);

        DecimalFormat df = LabelFactory.getDecimalFormat(2,false);
        HashMap<Integer, String> actual = LabelFactory.getMeasurementLabels(collection,"Meas",df);

        assertEquals(3,actual.size());
        assertEquals("3.20",actual.get(0));
        assertEquals("-0.10",actual.get(1));
        assertEquals("NA",actual.get(2));
    }
} 