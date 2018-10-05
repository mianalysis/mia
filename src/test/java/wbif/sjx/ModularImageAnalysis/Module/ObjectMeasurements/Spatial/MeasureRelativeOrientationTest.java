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


    // POSITIVE X-AXIS
    @Test
    public void testGetXYAnglePosXOrientation180() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation135() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation90() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation45() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation30() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 30;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 30;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientation0() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = 0;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientationMinus30() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = -30;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 30;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAnglePosXOrientationMinus45() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,10,0);
        double xyOrientation = -45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }




    // TOP RIGHT QUADRANT IN 2D

    @Test
    public void testGetXYAngleTopRightOrientation180() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation135() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation90() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation45() {


        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation0() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = 0;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus45() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = -45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus90() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = -90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus135() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = -135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus180() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,15,0);
        double xyOrientation = -180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // BOTTOM RIGHT QUADRANT IN 2D

    @Test
    public void testGetXYAngleBottomRightOrientation180() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation135() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation90() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation45() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation0() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = 0;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus45() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = -45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus90() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = -90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus135() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = -135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus180() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(15,5,0);
        double xyOrientation = -180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // BOTTOM LEFT QUADRANT IN 2D

    @Test
    public void testGetXYAngleBottomLeftOrientation180() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation135() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation90() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation45() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation0() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = 0;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus45() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = -45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus90() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = -90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus135() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = -135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus180() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,5,0);
        double xyOrientation = -180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }


    // TOP LEFT QUADRANT IN 2D

    @Test
    public void testGetXYAngleTopLeftOrientation180() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation135() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation90() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation45() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation0() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = 0;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus45() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = -45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus90() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = -90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus135() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = -135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientationMinus180() {
        Obj object = new Obj("Object",1,0.02,0.1,"um", true);
        object.addCoord(5,15,0);
        double xyOrientation = -180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }
}