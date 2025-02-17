package io.github.mianalysis.mia.module.objects.measure.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.mianalysis.mia.expectedobjects.VolumeTypes;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import ome.units.UNITS;


public class MeasureRelativeOrientationTest extends ModuleTest {
    private double tolerance = 1E-2;


    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureRelativeOrientation(null).getDescription());
    }


    //// ZERO TO NINETY DEGREE TESTS ////

    // POSITIVE X-AXIS
    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientation180ZeroNinety(VolumeTypes volumeType)
            throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientation135ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientation90ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientation45ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientation30ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = 30;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 30;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientation0ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientationMinus30ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = -30;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 30;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientationMinus45ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }




    // TOP RIGHT QUADRANT IN 2D

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientation180ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientation135ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientation90ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientation45ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {


        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientation0ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientationMinus45ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientationMinus90ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientationMinus135ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientationMinus180ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // BOTTOM RIGHT QUADRANT IN 2D

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientation180ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientation135ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientation90ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientation45ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientation0ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientationMinus45ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientationMinus90ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientationMinus135ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientationMinus180ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // BOTTOM LEFT QUADRANT IN 2D

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientation180ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientation135ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientation90ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientation45ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientation0ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientationMinus45ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientationMinus90ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientationMinus135ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientationMinus180ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // TOP LEFT QUADRANT IN 2D

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientation180ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientation135ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientation90ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientation45ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientation0ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientationMinus45ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientationMinus90ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientationMinus135ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientationMinus180ZeroNinety(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }



    //// ZERO TO ONE HUNDRED AND EIGHTY DEGREE TESTS ////

    // POSITIVE X-AXIS
    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientation180ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientation135ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientation90ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientation45ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientation30ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = 30;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 150;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientation0ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientationMinus30ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = -30;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 150;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAnglePosXOrientationMinus45ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,10,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }


    // TOP RIGHT QUADRANT IN 2D

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientation180ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientation135ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientation90ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientation45ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientation0ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientationMinus45ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientationMinus90ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientationMinus135ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopRightOrientationMinus180ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,15,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // BOTTOM RIGHT QUADRANT IN 2D

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientation180ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientation135ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientation90ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientation45ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientation0ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientationMinus45ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientationMinus90ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientationMinus135ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomRightOrientationMinus180ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(15,5,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // BOTTOM LEFT QUADRANT IN 2D

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientation180ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientation135ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientation90ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientation45ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientation0ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientationMinus45ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientationMinus90ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientationMinus135ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleBottomLeftOrientationMinus180ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,5,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }


    // TOP LEFT QUADRANT IN 2D

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientation180ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientation135ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientation90ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientation45ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientation0ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientationMinus45ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientationMinus90ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientationMinus135ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testGetXYAngleTopLeftOrientationMinus180ZeroOneEighty(VolumeTypes volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        SpatCal spatCal = new SpatCal(0.02,0.1,"um",30,30,1);
        Objs objects = new Objs("Objects",spatCal, 1, 0.02, UNITS.SECOND);
        Obj object = objects.createAndAddNewObject(VolumeTypes.getFactory(volumeType));
        object.add(5,15,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }
}