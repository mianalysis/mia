package io.github.mianalysis.mia.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Color;
import java.util.HashMap;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.mianalysis.mia.expectedobjects.VolumeTypes;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import ome.units.UNITS;

public class ColourFactoryTest {
    private double tolerance = 1E-2;

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetHuesSingleColour(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getSingleColourValues(collection,
                ColourFactory.SingleColours.WHITE);

        assertEquals(3, actual.size());
        assertEquals(Float.MAX_VALUE, actual.get(0), tolerance);
        assertEquals(Float.MAX_VALUE, actual.get(1), tolerance);
        assertEquals(Float.MAX_VALUE, actual.get(2), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetHuesRandomColour(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getRandomHues(collection);

        // For random numbers we don't know what value they will have
        assertEquals(3, actual.size());
        assertNotNull(actual.get(0));
        assertNotNull(actual.get(1));
        assertNotNull(actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetHuesMeasurementColour(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        MeasurementI meas = new MeasurementI("Meas", 3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        meas = new MeasurementI("Meas", -0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        meas = new MeasurementI("Meas", Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getMeasurementValueHues(collection, "Meas", false,
                new double[] { Double.NaN, Double.NaN });

        // For random numbers we don't know what value they will have
        assertEquals(3, actual.size());
        assertEquals(3.2, actual.get(0), tolerance);
        assertEquals(-0.1, actual.get(1), tolerance);
        assertEquals(Double.NaN, actual.get(2), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetHuesMeasurementColourNormalised(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        MeasurementI meas = new MeasurementI("Meas", 3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        meas = new MeasurementI("Meas", -0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        meas = new MeasurementI("Meas", Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getMeasurementValueHues(collection, "Meas", true,
                new double[] { Double.NaN, Double.NaN });

        // For random numbers we don't know what value they will have
        assertEquals(3, actual.size());
        assertEquals(0.47f, actual.get(0), tolerance);
        assertEquals(0f, actual.get(1), tolerance);
        assertEquals(Double.NaN, actual.get(2), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetHuesIDColour(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        MeasurementI meas = new MeasurementI("Meas", 3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        meas = new MeasurementI("Meas", -0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        meas = new MeasurementI("Meas", Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getIDHues(collection, false);

        // For random numbers we don't know what value they will have
        assertEquals(3, actual.size());
        assertEquals(0f, actual.get(0), tolerance);
        assertEquals(1f, actual.get(1), tolerance);
        assertEquals(2f, actual.get(2), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetHuesIDColourNormalised(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        MeasurementI meas = new MeasurementI("Meas", 3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        meas = new MeasurementI("Meas", -0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        meas = new MeasurementI("Meas", Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getIDHues(collection, true);

        // For random numbers we don't know what value they will have
        assertEquals(3, actual.size());
        assertEquals(0f, actual.get(0), tolerance);
        assertEquals(0.06f, actual.get(1), tolerance);
        assertEquals(0.13f, actual.get(2), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetHuesParentIDColour(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);
        ObjsI parents = ObjsFactories.getDefaultFactory().createObjs("Parents", 1, 1, 1, dppXY, dppZ, calibratedUnits,
                1, 0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        ObjI parent = parents.createAndAddNewObjectWithID(factory, 6);
        obj.addParent(parent);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        parent = parents.createAndAddNewObjectWithID(factory, 5);
        obj.addParent(parent);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getParentIDHues(collection, "Parents", false);

        // For random numbers we don't know what value they will have
        assertEquals(3, actual.size());
        assertEquals(6f, actual.get(0), tolerance);
        assertEquals(-1f, actual.get(1), tolerance);
        assertEquals(5f, actual.get(2), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetHuesParentIDColourNormalised(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);
        ObjsI parents = ObjsFactories.getDefaultFactory().createObjs("Parents", 1, 1, 1, dppXY, dppZ, calibratedUnits,
                1, 0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        ObjI parent = parents.createAndAddNewObjectWithID(factory, 6);
        obj.addParent(parent);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        parent = parents.createAndAddNewObjectWithID(factory, 5);
        obj.addParent(parent);
        collection.add(obj);

        HashMap<Integer, Float> actual = ColourFactory.getParentIDHues(collection, "Parents", true);

        // For random numbers we don't know what value they will have
        assertEquals(3, actual.size());
        assertEquals(0.38f, actual.get(0), tolerance);
        assertEquals(-1f, actual.get(1), tolerance);
        assertEquals(0.31f, actual.get(2), tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetColoursSingleColour(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getSingleColourValues(collection,
                ColourFactory.SingleColours.WHITE);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues, 100);

        assertEquals(3, actual.size());
        assertEquals(Color.getHSBColor(1f, 0f, 1f), actual.get(0));
        assertEquals(Color.getHSBColor(1f, 0f, 1f), actual.get(1));
        assertEquals(Color.getHSBColor(1f, 0f, 1f), actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetColoursRandomColour(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getRandomHues(collection);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues, 100);

        // For random numbers we don't know what value they will have
        assertEquals(3, actual.size());
        assertNotNull(actual.get(0));
        assertNotNull(actual.get(1));
        assertNotNull(actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetColoursMeasurementColour(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        MeasurementI meas = new MeasurementI("Meas", 3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        meas = new MeasurementI("Meas", -0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        meas = new MeasurementI("Meas", Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getMeasurementValueHues(collection, "Meas", false,
                new double[] { Double.NaN, Double.NaN });
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues, 100);

        // For random numbers we don't know what value they will have
        assertEquals(3, actual.size());
        assertEquals(Color.getHSBColor(3.2f, 1f, 1f), actual.get(0));
        assertEquals(Color.getHSBColor(-0.1f, 1f, 1f), actual.get(1));
        assertEquals(Color.getHSBColor(1f, 1f, 1f), actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetColoursMeasurementColourNormalised(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        MeasurementI meas = new MeasurementI("Meas", 3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        meas = new MeasurementI("Meas", -0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        meas = new MeasurementI("Meas", Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getMeasurementValueHues(collection, "Meas", true,
                new double[] { Double.NaN, Double.NaN });
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues, 100);

        // For random numbers we don't know what value they will have
        assertEquals(3, actual.size());
        assertEquals(Color.getHSBColor(0.4706f, 1f, 1f), actual.get(0));
        assertEquals(Color.getHSBColor(0f, 1f, 1f), actual.get(1));
        assertEquals(Color.getHSBColor(1f, 1f, 1f), actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetColoursIDColour(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        MeasurementI meas = new MeasurementI("Meas", 3.2);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        meas = new MeasurementI("Meas", -0.1);
        obj.addMeasurement(meas);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        meas = new MeasurementI("Meas", Double.NaN);
        obj.addMeasurement(meas);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getIDHues(collection, false);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues, 100);

        // For random numbers we don't know what value they will have
        assertEquals(3, actual.size());
        assertEquals(Color.getHSBColor(0f, 1f, 1f), actual.get(0));
        assertEquals(Color.getHSBColor(1f, 1f, 1f), actual.get(1));
        assertEquals(Color.getHSBColor(2f, 1f, 1f), actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetColoursIDColourNormalised(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getIDHues(collection, true);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues, 100);

        // For random numbers we don't know what value they will have
        assertEquals(3, actual.size());
        assertEquals(Color.getHSBColor(0f, 1f, 1f), actual.get(0));
        assertEquals(Color.getHSBColor(0.0627f, 1f, 1f), actual.get(1));
        assertEquals(Color.getHSBColor(0.1255f, 1f, 1f), actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetColoursParentIDColour(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);
        ObjsI parents = ObjsFactories.getDefaultFactory().createObjs("Parents", 1, 1, 1, dppXY, dppZ, calibratedUnits,
                1, 0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        ObjI parent = parents.createAndAddNewObjectWithID(factory, 6);
        obj.addParent(parent);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        parent = parents.createAndAddNewObjectWithID(factory, 5);
        obj.addParent(parent);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getParentIDHues(collection, "Parents", false);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues, 100);

        // For random numbers we don't know what value they will have
        assertEquals(3, actual.size());
        assertEquals(Color.getHSBColor(6f, 1f, 1f), actual.get(0));
        assertEquals(Color.getHSBColor(0f, 0f, 1f), actual.get(1));
        assertEquals(Color.getHSBColor(5f, 1f, 1f), actual.get(2));

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetColoursParentIDColourNormalised(VolumeTypes volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the Objs
        ObjsI collection = ObjsFactories.getDefaultFactory().createObjs("Obj", 1, 1, 1, dppXY, dppZ, calibratedUnits, 1,
                0.02, UNITS.SECOND);
        ObjsI parents = ObjsFactories.getDefaultFactory().createObjs("Parents", 1, 1, 1, dppXY, dppZ, calibratedUnits,
                1, 0.02, UNITS.SECOND);

        // Adding objects
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjI obj = collection.createAndAddNewObjectWithID(factory, 0);
        ObjI parent = parents.createAndAddNewObjectWithID(factory, 6);
        obj.addParent(parent);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 1);
        collection.add(obj);

        obj = collection.createAndAddNewObjectWithID(factory, 2);
        parent = parents.createAndAddNewObjectWithID(factory, 5);
        obj.addParent(parent);
        collection.add(obj);

        HashMap<Integer, Float> hues = ColourFactory.getParentIDHues(collection, "Parents", true);
        HashMap<Integer, Color> actual = ColourFactory.getColours(hues, 100);

        // For random numbers we don't know what value they will have
        assertEquals(3, actual.size());
        assertEquals(Color.getHSBColor(0.3765f, 1f, 1f), actual.get(0));
        assertEquals(Color.getHSBColor(0f, 0f, 1f), actual.get(1));
        assertEquals(Color.getHSBColor(0.3137f, 1f, 1f), actual.get(2));

    }
}