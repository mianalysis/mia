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


    // TOP RIGHT QUADRANT IN 2D

    @Test
    public void testGetXYAngleTopRightOrientation180() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,15,0);
        double xyOrientation = 180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation135() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,15,0);
        double xyOrientation = 135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation90() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,15,0);
        double xyOrientation = 90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation45() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,15,0);
        double xyOrientation = 45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientation0() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,15,0);
        double xyOrientation = 0;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus45() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,15,0);
        double xyOrientation = -45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus90() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,15,0);
        double xyOrientation = -90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus135() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,15,0);
        double xyOrientation = -135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopRightOrientationMinus180() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
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
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,5,0);
        double xyOrientation = 180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation135() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,5,0);
        double xyOrientation = 135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation90() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,5,0);
        double xyOrientation = 90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation45() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,5,0);
        double xyOrientation = 45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientation0() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,5,0);
        double xyOrientation = 0;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus45() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,5,0);
        double xyOrientation = -45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus90() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,5,0);
        double xyOrientation = -90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus135() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,5,0);
        double xyOrientation = -135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomRightOrientationMinus180() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(15,5,0);
        double xyOrientation = -180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation180() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(5,5,0);
        double xyOrientation = 180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation135() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(5,5,0);
        double xyOrientation = 135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation90() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(5,5,0);
        double xyOrientation = 90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation45() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(5,5,0);
        double xyOrientation = 45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientation0() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(5,5,0);
        double xyOrientation = 0;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus45() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(5,5,0);
        double xyOrientation = -45;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 90;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus90() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(5,5,0);
        double xyOrientation = -90;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus135() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(5,5,0);
        double xyOrientation = -135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleBottomLeftOrientationMinus180() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(5,5,0);
        double xyOrientation = -180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation180() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(5,15,0);
        double xyOrientation = 180;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 45;

        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testGetXYAngleTopLeftOrientation135() {
        double dppXY = 0.02;
        double dppZ = 0.1;
        String units = "um";

        Obj object = new Obj("Object",1,dppXY,dppZ,units, true);
        object.addCoord(5,15,0);
        double xyOrientation = 135;

        Point<Double> referencePoint = new Point<>(10d,10d,0d);

        double actual = MeasureRelativeOrientation.getXYAngle(object,xyOrientation,referencePoint);
        double expected = 0;

        assertEquals(expected,actual,tolerance);

    }

    
}