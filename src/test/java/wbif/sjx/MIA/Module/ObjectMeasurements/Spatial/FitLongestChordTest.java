package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.HorizontalCylinderR22;
import wbif.sjx.MIA.ExpectedObjects.VerticalCylinderR5;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Units;
import wbif.sjx.common.Exceptions.IntegerOverflowException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class FitLongestChordTest extends ModuleTest {
    private double tolerance = 1E-2;

    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Test
    public void testProcessObjectHorizontalCylinderR22() throws IntegerOverflowException {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection inputObjs = new HorizontalCylinderR22().getObjects("Input_obj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        Obj inputObj = inputObjs.getFirst();

        // Processing object
        FitLongestChord fitLongestChord = new FitLongestChord(null);
        fitLongestChord.processObject(inputObj);

        // Testing single value measurements
        double actual = inputObj.getMeasurement(FitLongestChord.Measurements.LENGTH_PX).getValue();
        double expected = inputObj.getMeasurement(HorizontalCylinderR22.Measures.LC_LENGTH_PX.name()).getValue();
        assertEquals(expected,actual,tolerance);

        actual = inputObj.getMeasurement(Units.replace(FitLongestChord.Measurements.LENGTH_CAL)).getValue();
        expected = inputObj.getMeasurement(HorizontalCylinderR22.Measures.LC_LENGTH_CAL.name()).getValue();
        assertEquals(expected,actual,tolerance);

        actual = inputObj.getMeasurement(FitLongestChord.Measurements.MEAN_SURF_DIST_PX).getValue();
        expected = inputObj.getMeasurement(HorizontalCylinderR22.Measures.MEAN_DIST_PX.name()).getValue();
        assertEquals(expected,actual,expected*0.2);

        actual = inputObj.getMeasurement(Units.replace(FitLongestChord.Measurements.MEAN_SURF_DIST_CAL)).getValue();
        expected = inputObj.getMeasurement(HorizontalCylinderR22.Measures.MEAN_DIST_CAL.name()).getValue();
        assertEquals(expected,actual,expected*0.2);

        actual = inputObj.getMeasurement(FitLongestChord.Measurements.MAX_SURF_DIST_PX).getValue();
        expected = inputObj.getMeasurement(HorizontalCylinderR22.Measures.MAX_DIST_PX.name()).getValue();
        assertEquals(expected,actual,expected*0.1);

        actual = inputObj.getMeasurement(Units.replace(FitLongestChord.Measurements.MAX_SURF_DIST_CAL)).getValue();
        expected = inputObj.getMeasurement(HorizontalCylinderR22.Measures.MAX_DIST_CAL.name()).getValue();
        assertEquals(expected,actual,expected*0.1);

        // Testing coordinates of the end points.  There's no guarantee which will be point 1 or 2
        double actualX1 = inputObj.getMeasurement(FitLongestChord.Measurements.X1_PX).getValue();
        double actualX2 = inputObj.getMeasurement(FitLongestChord.Measurements.X2_PX).getValue();
        double actualY1 = inputObj.getMeasurement(FitLongestChord.Measurements.Y1_PX).getValue();
        double actualY2 = inputObj.getMeasurement(FitLongestChord.Measurements.Y2_PX).getValue();
        double actualZ1 = inputObj.getMeasurement(FitLongestChord.Measurements.Z1_SLICE).getValue();
        double actualZ2 = inputObj.getMeasurement(FitLongestChord.Measurements.Z2_SLICE).getValue();

        double expectedX1 = inputObj.getMeasurement(HorizontalCylinderR22.Measures.LC_X1_PX.name()).getValue();
        double expectedX2 = inputObj.getMeasurement(HorizontalCylinderR22.Measures.LC_X2_PX.name()).getValue();
        double expectedY1 = inputObj.getMeasurement(HorizontalCylinderR22.Measures.LC_Y1_PX.name()).getValue();
        double expectedY2 = inputObj.getMeasurement(HorizontalCylinderR22.Measures.LC_Y2_PX.name()).getValue();
        double expectedZ1 = inputObj.getMeasurement(HorizontalCylinderR22.Measures.LC_Z1_SLICE.name()).getValue();
        double expectedZ2 = inputObj.getMeasurement(HorizontalCylinderR22.Measures.LC_Z2_SLICE.name()).getValue();

        double[] actual1 = new double[]{actualX1,actualY1,actualZ1};
        double[] actual2 = new double[]{actualX2,actualY2,actualZ2};

        double[] expected1 = new double[]{expectedX1,expectedY1,expectedZ1};
        double[] expected2 = new double[]{expectedX2,expectedY2,expectedZ2};

        boolean same = Arrays.equals(actual1,expected1) && Arrays.equals(actual2,expected2);
        boolean other = Arrays.equals(actual1,expected2) && Arrays.equals(actual2,expected1);

        assertTrue(same || other);

    }

    @Test
    public void testProcessObjectVerticalCylinderR5() throws IntegerOverflowException {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection inputObjs = new VerticalCylinderR5().getObjects("Input_obj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        Obj inputObj = inputObjs.getFirst();

        // Processing object
        FitLongestChord fitLongestChord = new FitLongestChord(null);
        fitLongestChord.processObject(inputObj);

        // Testing single value measurements
        double actual = inputObj.getMeasurement(FitLongestChord.Measurements.LENGTH_PX).getValue();
        double expected = inputObj.getMeasurement(VerticalCylinderR5.Measures.LC_LENGTH_PX.name()).getValue();
        assertEquals(expected,actual,tolerance);

        actual = inputObj.getMeasurement(Units.replace(FitLongestChord.Measurements.LENGTH_CAL)).getValue();
        expected = inputObj.getMeasurement(VerticalCylinderR5.Measures.LC_LENGTH_CAL.name()).getValue();
        assertEquals(expected,actual,tolerance);

        actual = inputObj.getMeasurement(FitLongestChord.Measurements.MEAN_SURF_DIST_PX).getValue();
        expected = inputObj.getMeasurement(VerticalCylinderR5.Measures.MEAN_DIST_PX.name()).getValue();
        assertEquals(expected,actual,expected*0.2);

        actual = inputObj.getMeasurement(Units.replace(FitLongestChord.Measurements.MEAN_SURF_DIST_CAL)).getValue();
        expected = inputObj.getMeasurement(VerticalCylinderR5.Measures.MEAN_DIST_CAL.name()).getValue();
        assertEquals(expected,actual,expected*0.2);

        actual = inputObj.getMeasurement(FitLongestChord.Measurements.MAX_SURF_DIST_PX).getValue();
        expected = inputObj.getMeasurement(VerticalCylinderR5.Measures.MAX_DIST_PX.name()).getValue();
        assertEquals(expected,actual,expected*0.1);

        actual = inputObj.getMeasurement(Units.replace(FitLongestChord.Measurements.MAX_SURF_DIST_CAL)).getValue();
        expected = inputObj.getMeasurement(VerticalCylinderR5.Measures.MAX_DIST_CAL.name()).getValue();
        assertEquals(expected,actual,expected*0.1);

        // Testing coordinates of the end points.  There's no guarantee which will be point 1 or 2
        double actualX1 = inputObj.getMeasurement(FitLongestChord.Measurements.X1_PX).getValue();
        double actualX2 = inputObj.getMeasurement(FitLongestChord.Measurements.X2_PX).getValue();
        double actualY1 = inputObj.getMeasurement(FitLongestChord.Measurements.Y1_PX).getValue();
        double actualY2 = inputObj.getMeasurement(FitLongestChord.Measurements.Y2_PX).getValue();
        double actualZ1 = inputObj.getMeasurement(FitLongestChord.Measurements.Z1_SLICE).getValue();
        double actualZ2 = inputObj.getMeasurement(FitLongestChord.Measurements.Z2_SLICE).getValue();

        double expectedX1 = inputObj.getMeasurement(VerticalCylinderR5.Measures.LC_X1_PX.name()).getValue();
        double expectedX2 = inputObj.getMeasurement(VerticalCylinderR5.Measures.LC_X2_PX.name()).getValue();
        double expectedY1 = inputObj.getMeasurement(VerticalCylinderR5.Measures.LC_Y1_PX.name()).getValue();
        double expectedY2 = inputObj.getMeasurement(VerticalCylinderR5.Measures.LC_Y2_PX.name()).getValue();
        double expectedZ1 = inputObj.getMeasurement(VerticalCylinderR5.Measures.LC_Z1_SLICE.name()).getValue();
        double expectedZ2 = inputObj.getMeasurement(VerticalCylinderR5.Measures.LC_Z2_SLICE.name()).getValue();

        double[] actual1 = new double[]{actualX1,actualY1,actualZ1};
        double[] actual2 = new double[]{actualX2,actualY2,actualZ2};

        double[] expected1 = new double[]{expectedX1,expectedY1,expectedZ1};
        double[] expected2 = new double[]{expectedX2,expectedY2,expectedZ2};

        boolean same = Arrays.equals(actual1,expected1) && Arrays.equals(actual2,expected2);
        boolean other = Arrays.equals(actual1,expected2) && Arrays.equals(actual2,expected1);

        assertTrue(same || other);

    }

    @Override
    public void testGetHelp() {
        assertNotNull(new FitLongestChord(null).getDescription());
    }
}