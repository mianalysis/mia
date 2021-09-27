package io.github.mianalysis.mia.module.objectmeasurements.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.mianalysis.mia.expectedobjects.Tracks3D;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.sjcross.common.exceptions.IntegerOverflowException;
import io.github.sjcross.common.object.tracks.Track;
import io.github.sjcross.common.object.volume.VolumeType;

/**
 * Created by Stephen Cross on 09/08/2018.
 */

public class MeasureTrackMotionTest extends ModuleTest {
    private double tolerance = 1E-2;


    // GENERAL TRACK TESTS

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureTrackMotion(null).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testCreateTrack(VolumeType volumeType) throws IntegerOverflowException {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        double zScaling = dppZ/dppXY;
        String calibratedUnits = "µm";

        String trackObjectsName = "Tracks";
        String spotObjectsName = "Spots";

        // Getting input objects and expected values
        Objs trackObjects = new Tracks3D().getObjects(volumeType,trackObjectsName,spotObjectsName,dppXY,dppZ,calibratedUnits);
        TreeMap<Integer,Track> expectedObjects = new Tracks3D().getRawTracks(zScaling);

        // Comparing actual values
        for (Obj trackObject:trackObjects.values()) {
            Track expected = expectedObjects.get(trackObject.getID());
            Track actual = MeasureTrackMotion.createTrack(trackObject,spotObjectsName);

            assertEquals(expected,actual);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testCreateAverageTrack(VolumeType volumeType) throws IntegerOverflowException {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        double zScaling = dppZ/dppXY;
        String calibratedUnits = "µm";

        String trackObjectsName = "Tracks";
        String spotObjectsName = "Spots";

        // Getting input objects and expected values
        Objs trackObjects = new Tracks3D().getObjects(volumeType,trackObjectsName,spotObjectsName,dppXY,dppZ,calibratedUnits);
        TreeMap<Integer,Track> expectedObjects = new Tracks3D().getAverageTrack(zScaling);

        Track expected = expectedObjects.get(0);
        Track actual = MeasureTrackMotion.createAverageTrack(trackObjects, spotObjectsName);

        assertEquals(expectedObjects.get(0),actual);

    }

    @Test
    public void testSubtractAverageMotion() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        double zScaling = dppZ/dppXY;

        // Getting input objects and expected values
        TreeMap<Integer,Track> rawTracks = new Tracks3D().getRawTracks(zScaling);
        TreeMap<Integer,Track> averageTracks = new Tracks3D().getAverageTrack(zScaling);
        TreeMap<Integer,Track> expectedObjects = new Tracks3D().getSubtractedTracks(zScaling);

        // Comparing actual values
        for (int ID:expectedObjects.keySet()) {
            Track rawTrack = rawTracks.get(ID);
            Track expected = expectedObjects.get(ID);

            MeasureTrackMotion.subtractAverageMotion(rawTrack,averageTracks.get(0));

            assertEquals(expected,rawTrack);

        }
    }


    // SPECIFIC MEASUREMENT TESTS (Currently disabled due to negative coordinates - no longer supported)

//    @Test @Disabled
//    public void testCalculateTemporalMeasurements() {
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        boolean subtractAverage = false;
//
//        // The general object doesn't need to include coordinates, as calculations are done on the specific Track object
//        String trackObjectsName = "Tracks";
//        Obj trackObject = new Obj(trackObjectsName,1,dppXY,dppZ,calibratedUnits);
//
//        int[] f = new int[]{0,1,2,3,4,5,6,7};
//        double[] x = new double[]{3,-56,23.3,-16.2,62.4,23.8,55.3,-76.3};
//        double[] y = new double[]{12,54.2,43.7,99.6,34.6,12.2,-21,-12};
//        double[] z = new double[]{-2.2,45.8,-2.4,24,12.1,44.5,76.6,34.6};
//        Track track = new Track(x,y,z,f,"px");
//
//        // Calculating the measurements
//        MeasureTrackMotion.calculateTemporalMeasurements(trackObject,track,subtractAverage);
//
//        // Checking the measurements are correct
//        String name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.DURATION,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(7,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.FIRST_FRAME,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(0,trackObject.getMeasurement(name).getValue(),tolerance);
//
//    }
//
//    @Test @Disabled
//    public void testCalculateTemporalMeasurementsMissingFrame() {
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        boolean subtractAverage = false;
//
//        // The general object doesn't need to include coordinates, as calculations are done on the specific Track object
//        String trackObjectsName = "Tracks";
//        Obj trackObject = new Obj(trackObjectsName,1,dppXY,dppZ,calibratedUnits);
//
//        int[] f = new int[]{0,1,2,3,4,6,7};
//        double[] x = new double[]{3,-56,23.3,-16.2,62.4,55.3,-76.3};
//        double[] y = new double[]{12,54.2,43.7,99.6,34.6,-21,-12};
//        double[] z = new double[]{-2.2,45.8,-2.4,24,12.1,76.6,34.6};
//        Track track = new Track(x,y,z,f,"px");
//
//        // Calculating the measurements
//        MeasureTrackMotion.calculateTemporalMeasurements(trackObject,track,subtractAverage);
//
//        // Checking the measurements are correct
//        String name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.DURATION,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(7,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.FIRST_FRAME,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(0,trackObject.getMeasurement(name).getValue(),tolerance);
//
//    }
//
//    @Test
//    public void testCalculateVelocity() {
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        boolean subtractAverage = false;
//
//        // The general object doesn't need to include coordinates, as calculations are done on the specific Track object
//        String trackObjectsName = "Tracks";
//        Obj trackObject = new Obj(trackObjectsName,1,dppXY,dppZ,calibratedUnits,false);
//
//        int[] f = new int[]{0,1,2,3,4,5,6,7};
//        double[] x = new double[]{3,-56,23.3,-16.2,62.4,23.8,55.3,-76.3};
//        double[] y = new double[]{12,54.2,43.7,99.6,34.6,12.2,-21,-12};
//        double[] z = new double[]{-2.2,45.8,-2.4,24,12.1,44.5,76.6,34.6};
//        Track track = new Track(x,y,z,f,"px");
//
//        // Calculating the measurements
//        MeasureTrackMotion.calculateVelocity(trackObject,track,subtractAverage);
//
//        // Checking the measurements are correct
//        String name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.MEAN_X_VELOCITY_PX,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(-11.33,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.MEAN_X_VELOCITY_CAL,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(-0.23,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.MEAN_Y_VELOCITY_PX,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(-3.43,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.MEAN_Y_VELOCITY_CAL,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(-0.07,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.MEAN_Z_VELOCITY_SLICES,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(1.05,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.MEAN_Z_VELOCITY_CAL,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(0.11,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.MEAN_INSTANTANEOUS_SPEED_CAL,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(1.73,trackObject.getMeasurement(name).getValue(),tolerance);
//
//    }
//
//    @Test
//    public void testCalculateVelocityMissingFrame() {
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        boolean subtractAverage = false;
//
//        // The general object doesn't need to include coordinates, as calculations are done on the specific Track object
//        String trackObjectsName = "Tracks";
//        Obj trackObject = new Obj(trackObjectsName,1,dppXY,dppZ,calibratedUnits,false);
//
//        int[] f = new int[]{0,1,2,3,4,6,7};
//        double[] x = new double[]{3,-56,23.3,-16.2,62.4,55.3,-76.3};
//        double[] y = new double[]{12,54.2,43.7,99.6,34.6,-21,-12};
//        double[] z = new double[]{-2.2,45.8,-2.4,24,12.1,76.6,34.6};
//        Track track = new Track(x,y,z,f,"px");
//
//        // Calculating the measurements
//        MeasureTrackMotion.calculateVelocity(trackObject,track,subtractAverage);
//
//        // Checking the measurements are correct
//        String name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.MEAN_X_VELOCITY_PX,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(-12.63,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.MEAN_X_VELOCITY_CAL,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(-0.25,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.MEAN_Y_VELOCITY_PX,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(0.63,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.MEAN_Y_VELOCITY_CAL,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(0.01,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.MEAN_Z_VELOCITY_SLICES,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(0.15,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.MEAN_Z_VELOCITY_CAL,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(0.02,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.MEAN_INSTANTANEOUS_SPEED_CAL,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(1.79,trackObject.getMeasurement(name).getValue(),tolerance);
//
//    }
//
//    @Test
//    public void testCalculateSpatialMeasurements() {
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        boolean subtractAverage = false;
//
//        // The general object doesn't need to include coordinates, as calculations are done on the specific Track object
//        String trackObjectsName = "Tracks";
//        Obj trackObject = new Obj(trackObjectsName,1,dppXY,dppZ,calibratedUnits,false);
//
//        int[] f = new int[]{0,1,2,3,4,5,6,7};
//        double[] x = new double[]{3,-56,23.3,-16.2,62.4,23.8,55.3,-76.3};
//        double[] y = new double[]{12,54.2,43.7,99.6,34.6,12.2,-21,-12};
//        double[] z = new double[]{-2.2,45.8,-2.4,24,12.1,44.5,76.6,34.6};
//        Track track = new Track(x,y,z,f,"px");
//
//        // Calculating the measurements
//        MeasureTrackMotion.calculateSpatialMeasurements(trackObject,track,subtractAverage);
//
//        // Checking the measurements are correct
//        String name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.EUCLIDEAN_DISTANCE_PX,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(90.66,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.EUCLIDEAN_DISTANCE_CAL,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(1.81,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.TOTAL_PATH_LENGTH_PX,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(605.91,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.TOTAL_PATH_LENGTH_CAL,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(12.12,trackObject.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.DIRECTIONALITY_RATIO,subtractAverage);
//        assertNotNull(trackObject.getMeasurement(name));
//        assertEquals(0.15,trackObject.getMeasurement(name).getValue(),tolerance);
//
//    }
//
//    @Test
//    public void testCalculateInstantaneousVelocity() {
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        boolean subtractAverage = false;
//
//        // The general object doesn't need to include coordinates, as calculations are done on the specific Track object
//        String trackObjectsName = "Tracks";
//        String spotObjectsName = "Spots";
//        Obj trackObject = new Obj(trackObjectsName,1,dppXY,dppZ,calibratedUnits,false);
//
//        // Creating spot objects
//        Obj spot1 = new Obj(spotObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(0);
//        spot1.addParent(trackObject);
//        trackObject.addChild(spot1);
//
//        Obj spot2 = new Obj(spotObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(1);
//        spot2.addParent(trackObject);
//        trackObject.addChild(spot2);
//
//        Obj spot3 = new Obj(spotObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(2);
//        spot3.addParent(trackObject);
//        trackObject.addChild(spot3);
//
//        Obj spot4 = new Obj(spotObjectsName,4,dppXY,dppZ,calibratedUnits,false).setT(3);
//        spot4.addParent(trackObject);
//        trackObject.addChild(spot4);
//
//        Obj spot5 = new Obj(spotObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(4);
//        spot5.addParent(trackObject);
//        trackObject.addChild(spot5);
//
//        Obj spot6 = new Obj(spotObjectsName,6,dppXY,dppZ,calibratedUnits,false).setT(5);
//        spot6.addParent(trackObject);
//        trackObject.addChild(spot6);
//
//        Obj spot7 = new Obj(spotObjectsName,7,dppXY,dppZ,calibratedUnits,false).setT(6);
//        spot7.addParent(trackObject);
//        trackObject.addChild(spot7);
//
//        Obj spot8 = new Obj(spotObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(7);
//        spot8.addParent(trackObject);
//        trackObject.addChild(spot8);
//
//
//        int[] f = new int[]{0,1,2,3,4,5,6,7};
//        double[] x = new double[]{3,-56,23.3,-16.2,62.4,23.8,55.3,-76.3};
//        double[] y = new double[]{12,54.2,43.7,99.6,34.6,12.2,-21,-12};
//        double[] z = new double[]{-2.2,45.8,-2.4,24,12.1,44.5,76.6,34.6};
//        Track track = new Track(x,y,z,f,"px");
//
//        // Calculating the measurements
//        MeasureTrackMotion.calculateInstantaneousVelocity(trackObject,track,spotObjectsName,subtractAverage);
//
//        // Checking the measurements are correct
//        String name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.X_VELOCITY_PX,subtractAverage);
//        assertTrue(Double.isNaN(spot1.getMeasurement(name).getValue()));
//        assertEquals(-59,spot2.getMeasurement(name).getValue(),tolerance);
//        assertEquals(79.3,spot3.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-39.5,spot4.getMeasurement(name).getValue(),tolerance);
//        assertEquals(78.6,spot5.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-38.6,spot6.getMeasurement(name).getValue(),tolerance);
//        assertEquals(31.5,spot7.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-131.6,spot8.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.X_VELOCITY_CAL,subtractAverage);
//        assertTrue(Double.isNaN(spot1.getMeasurement(name).getValue()));
//        assertEquals(-1.18,spot2.getMeasurement(name).getValue(),tolerance);
//        assertEquals(1.59,spot3.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-0.79,spot4.getMeasurement(name).getValue(),tolerance);
//        assertEquals(1.57,spot5.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-0.77,spot6.getMeasurement(name).getValue(),tolerance);
//        assertEquals(0.63,spot7.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-2.63,spot8.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.Y_VELOCITY_PX,subtractAverage);
//        assertTrue(Double.isNaN(spot1.getMeasurement(name).getValue()));
//        assertEquals(42.2,spot2.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-10.5,spot3.getMeasurement(name).getValue(),tolerance);
//        assertEquals(55.9,spot4.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-65,spot5.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-22.4,spot6.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-33.2,spot7.getMeasurement(name).getValue(),tolerance);
//        assertEquals(9,spot8.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.Y_VELOCITY_CAL,subtractAverage);
//        assertTrue(Double.isNaN(spot1.getMeasurement(name).getValue()));
//        assertEquals(0.844,spot2.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-0.21,spot3.getMeasurement(name).getValue(),tolerance);
//        assertEquals(1.12,spot4.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-1.3,spot5.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-0.45,spot6.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-0.66,spot7.getMeasurement(name).getValue(),tolerance);
//        assertEquals(0.18,spot8.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.Z_VELOCITY_SLICES,subtractAverage);
//        assertTrue(Double.isNaN(spot1.getMeasurement(name).getValue()));
//        assertEquals(9.6,spot2.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-9.64,spot3.getMeasurement(name).getValue(),tolerance);
//        assertEquals(5.28,spot4.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-2.38,spot5.getMeasurement(name).getValue(),tolerance);
//        assertEquals(6.48,spot6.getMeasurement(name).getValue(),tolerance);
//        assertEquals(6.42,spot7.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-8.4,spot8.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.Z_VELOCITY_CAL,subtractAverage);
//        assertTrue(Double.isNaN(spot1.getMeasurement(name).getValue()));
//        assertEquals(0.96,spot2.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-0.96,spot3.getMeasurement(name).getValue(),tolerance);
//        assertEquals(0.53,spot4.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-0.24,spot5.getMeasurement(name).getValue(),tolerance);
//        assertEquals(0.65,spot6.getMeasurement(name).getValue(),tolerance);
//        assertEquals(0.64,spot7.getMeasurement(name).getValue(),tolerance);
//        assertEquals(-0.84,spot8.getMeasurement(name).getValue(),tolerance);
//
//    }
//
//    @Test
//    public void testCalculateInstantaneousSpatialMeasurements() {
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        boolean subtractAverage = false;
//
//        // The general object doesn't need to include coordinates, as calculations are done on the specific Track object
//        String trackObjectsName = "Tracks";
//        String spotObjectsName = "Spots";
//        Obj trackObject = new Obj(trackObjectsName,1,dppXY,dppZ,calibratedUnits,false);
//
//        // Creating spot objects
//        Obj spot1 = new Obj(spotObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(0);
//        spot1.addParent(trackObject);
//        trackObject.addChild(spot1);
//
//        Obj spot2 = new Obj(spotObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(1);
//        spot2.addParent(trackObject);
//        trackObject.addChild(spot2);
//
//        Obj spot3 = new Obj(spotObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(2);
//        spot3.addParent(trackObject);
//        trackObject.addChild(spot3);
//
//        Obj spot4 = new Obj(spotObjectsName,4,dppXY,dppZ,calibratedUnits,false).setT(3);
//        spot4.addParent(trackObject);
//        trackObject.addChild(spot4);
//
//        Obj spot5 = new Obj(spotObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(4);
//        spot5.addParent(trackObject);
//        trackObject.addChild(spot5);
//
//        Obj spot6 = new Obj(spotObjectsName,6,dppXY,dppZ,calibratedUnits,false).setT(5);
//        spot6.addParent(trackObject);
//        trackObject.addChild(spot6);
//
//        Obj spot7 = new Obj(spotObjectsName,7,dppXY,dppZ,calibratedUnits,false).setT(6);
//        spot7.addParent(trackObject);
//        trackObject.addChild(spot7);
//
//        Obj spot8 = new Obj(spotObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(7);
//        spot8.addParent(trackObject);
//        trackObject.addChild(spot8);
//
//
//        int[] f = new int[]{0,1,2,3,4,5,6,7};
//        double[] x = new double[]{3,-56,23.3,-16.2,62.4,23.8,55.3,-76.3};
//        double[] y = new double[]{12,54.2,43.7,99.6,34.6,12.2,-21,-12};
//        double[] z = new double[]{-2.2,45.8,-2.4,24,12.1,44.5,76.6,34.6};
//        Track track = new Track(x,y,z,f,"px");
//
//        // Calculating the measurements
//        MeasureTrackMotion.calculateInstantaneousSpatialMeasurements(trackObject,track,spotObjectsName,subtractAverage);
//
//        // Checking the measurements are correct
//        String name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.CUMULATIVE_PATH_LENGTH_PX,subtractAverage);
//        assertEquals(0,spot1.getMeasurement(name).getValue(),tolerance);
//        assertEquals(86.98,spot2.getMeasurement(name).getValue(),tolerance);
//        assertEquals(180.37,spot3.getMeasurement(name).getValue(),tolerance);
//        assertEquals(253.74,spot4.getMeasurement(name).getValue(),tolerance);
//        assertEquals(356.42,spot5.getMeasurement(name).getValue(),tolerance);
//        assertEquals(411.57,spot6.getMeasurement(name).getValue(),tolerance);
//        assertEquals(467.47,spot7.getMeasurement(name).getValue(),tolerance);
//        assertEquals(605.91,spot8.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.CUMULATIVE_PATH_LENGTH_CAL,subtractAverage);
//        assertEquals(0,spot1.getMeasurement(name).getValue(),tolerance);
//        assertEquals(1.7396,spot2.getMeasurement(name).getValue(),tolerance);
//        assertEquals(3.6074,spot3.getMeasurement(name).getValue(),tolerance);
//        assertEquals(5.0748,spot4.getMeasurement(name).getValue(),tolerance);
//        assertEquals(7.1284,spot5.getMeasurement(name).getValue(),tolerance);
//        assertEquals(8.2314,spot6.getMeasurement(name).getValue(),tolerance);
//        assertEquals(9.3494,spot7.getMeasurement(name).getValue(),tolerance);
//        assertEquals(12.1182,spot8.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.ROLLING_EUCLIDEAN_DISTANCE_PX,subtractAverage);
//        assertEquals(0,spot1.getMeasurement(name).getValue(),tolerance);
//        assertEquals(86.98,spot2.getMeasurement(name).getValue(),tolerance);
//        assertEquals(37.64,spot3.getMeasurement(name).getValue(),tolerance);
//        assertEquals(93.43,spot4.getMeasurement(name).getValue(),tolerance);
//        assertEquals(65.14,spot5.getMeasurement(name).getValue(),tolerance);
//        assertEquals(51.12,spot6.getMeasurement(name).getValue(),tolerance);
//        assertEquals(100.17,spot7.getMeasurement(name).getValue(),tolerance);
//        assertEquals(90.66,spot8.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.ROLLING_EUCLIDEAN_DISTANCE_CAL,subtractAverage);
//        assertEquals(0,spot1.getMeasurement(name).getValue(),tolerance);
//        assertEquals(1.7396,spot2.getMeasurement(name).getValue(),tolerance);
//        assertEquals(0.7528,spot3.getMeasurement(name).getValue(),tolerance);
//        assertEquals(1.8686,spot4.getMeasurement(name).getValue(),tolerance);
//        assertEquals(1.3028,spot5.getMeasurement(name).getValue(),tolerance);
//        assertEquals(1.0224,spot6.getMeasurement(name).getValue(),tolerance);
//        assertEquals(2.0034,spot7.getMeasurement(name).getValue(),tolerance);
//        assertEquals(1.8132,spot8.getMeasurement(name).getValue(),tolerance);
//
//        name = MeasureTrackMotion.getFullName(MeasureTrackMotion.Measurements.ROLLING_DIRECTIONALITY_RATIO,subtractAverage);
//        assertTrue(Double.isNaN(spot1.getMeasurement(name).getValue()));
//        assertEquals(1,spot2.getMeasurement(name).getValue(),tolerance);
//        assertEquals(0.21,spot3.getMeasurement(name).getValue(),tolerance);
//        assertEquals(0.37,spot4.getMeasurement(name).getValue(),tolerance);
//        assertEquals(0.18,spot5.getMeasurement(name).getValue(),tolerance);
//        assertEquals(0.12,spot6.getMeasurement(name).getValue(),tolerance);
//        assertEquals(0.21,spot7.getMeasurement(name).getValue(),tolerance);
//        assertEquals(0.15,spot8.getMeasurement(name).getValue(),tolerance);
//
//    }
}