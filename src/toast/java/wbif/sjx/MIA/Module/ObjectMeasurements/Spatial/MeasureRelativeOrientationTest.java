package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Point;

import static org.junit.Assert.*;

public class MeasureRelativeOrientationTest extends ModuleTest {
    private double tolerance = 1E-2;

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureRelativeOrientation(null).getDescription());
    }


    //// ZERO TO NINETY DEGREE TESTS ////

    // POSITIVE X-AXIS
    @Test
    public void testGetXYAnglePosXOrientation180ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation135ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation90ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation45ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation30ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = 30;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 30;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation0ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientationMinus30ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = -30;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 30;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientationMinus45ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }




    // TOP RIGHT QUADRANT IN 2D

    @Test
    public void testGetXYAngleTopRightOrientation180ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation135ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation90ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation45ZeroNinety() throws IntegerOverflowException {


        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation0ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus45ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus90ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus135ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus180ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // BOTTOM RIGHT QUADRANT IN 2D

    @Test
    public void testGetXYAngleBottomRightOrientation180ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation135ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation90ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation45ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation0ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus45ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus90ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus135ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus180ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // BOTTOM LEFT QUADRANT IN 2D

    @Test
    public void testGetXYAngleBottomLeftOrientation180ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation135ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation90ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation45ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation0ZeroNinety()throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus45ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus90ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus135ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus180ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // TOP LEFT QUADRANT IN 2D

    @Test
    public void testGetXYAngleTopLeftOrientation180ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation135ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation90ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation45ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation0ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus45ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus90ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus135ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus180ZeroNinety() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
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
    @Test
    public void testGetXYAnglePosXOrientation180ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation135ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation90ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation45ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation30ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = 30;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 150;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation0ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientationMinus30ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = -30;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 150;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientationMinus45ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,10,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }


    // TOP RIGHT QUADRANT IN 2D

    @Test
    public void testGetXYAngleTopRightOrientation180ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation135ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation90ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation45ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation0ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus45ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus90ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus135ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus180ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,15,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // BOTTOM RIGHT QUADRANT IN 2D

    @Test
    public void testGetXYAngleBottomRightOrientation180ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation135ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation90ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation45ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation0ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus45ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus90ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus135ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus180ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(15,5,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // BOTTOM LEFT QUADRANT IN 2D

    @Test
    public void testGetXYAngleBottomLeftOrientation180ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation135ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation90ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation45ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation0ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus45ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus90ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus135ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus180ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,5,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }


    // TOP LEFT QUADRANT IN 2D

    @Test
    public void testGetXYAngleTopLeftOrientation180ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation135ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation90ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation45ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation0ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus45ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus90ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus135ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus180ZeroOneEighty() throws IntegerOverflowException {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.add(5,15,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }
}