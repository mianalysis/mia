package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume;

import static org.junit.Assert.*;

public class MeasureRelativeOrientationTest {
    private double tolerance = 1E-2;

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Test
    public void testGetTitle() {
        assertNotNull(new MeasureRelativeOrientation().getTitle());
    }


    //// ZERO TO NINETY DEGREE TESTS ////

    // POSITIVE X-AXIS
    @Test
    public void testGetXYAnglePosXOrientation180ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation135ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation90ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation45ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation30ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 30;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 30;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation0ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientationMinus30ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = -30;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 30;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientationMinus45ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }




    // TOP RIGHT QUADRANT IN 2D

    @Test
    public void testGetXYAngleTopRightOrientation180ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation135ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation90ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation45ZeroNinety() {


        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation0ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus45ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus90ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus135ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus180ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // BOTTOM RIGHT QUADRANT IN 2D

    @Test
    public void testGetXYAngleBottomRightOrientation180ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation135ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation90ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation45ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation0ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus45ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus90ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus135ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus180ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // BOTTOM LEFT QUADRANT IN 2D

    @Test
    public void testGetXYAngleBottomLeftOrientation180ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation135ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation90ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation45ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation0ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus45ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus90ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus135ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus180ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // TOP LEFT QUADRANT IN 2D

    @Test
    public void testGetXYAngleTopLeftOrientation180ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation135ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation90ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation45ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation0ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus45ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus90ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus135ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_NINETY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus180ZeroNinety() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
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
    public void testGetXYAnglePosXOrientation180ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation135ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation90ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation45ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation30ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 30;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 150;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation0ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientationMinus30ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = -30;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 150;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientationMinus45ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }


    // TOP RIGHT QUADRANT IN 2D

    @Test
    public void testGetXYAngleTopRightOrientation180ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation135ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation90ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation45ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation0ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus45ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus90ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus135ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus180ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // BOTTOM RIGHT QUADRANT IN 2D

    @Test
    public void testGetXYAngleBottomRightOrientation180ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation135ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation90ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation45ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation0ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus45ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus90ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus135ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus180ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // BOTTOM LEFT QUADRANT IN 2D

    @Test
    public void testGetXYAngleBottomLeftOrientation180ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation135ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation90ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation45ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation0ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus45ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus90ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus135ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus180ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }


    // TOP LEFT QUADRANT IN 2D

    @Test
    public void testGetXYAngleTopLeftOrientation180ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation135ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 180;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation90ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation45ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation0ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 0;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus45ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = -45;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus90ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = -90;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus135ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = -135;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus180ZeroOneEighty() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = -180;
        String measurementRange = MeasureRelativeOrientation.MeasurementRanges.ZERO_ONE_EIGHTY;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,measurementRange,referencePoint);
        double expected = 135;

        assertEquals(expected,actual,tolerance);

    }
}