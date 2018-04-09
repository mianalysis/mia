package wbif.sjx.ModularImageAnalysis.Object;

import org.junit.Test;

import static org.junit.Assert.*;

public class MeasurementReferenceTest {
    @Test
    public void testConstructor() {
        MeasurementReference measurementReference = new MeasurementReference("Test name");

        assertEquals("Test name",measurementReference.getName());
        assertEquals("",measurementReference.getImageObjName());
        assertTrue(measurementReference.isCalculated());
        assertTrue(measurementReference.isExportable());
    }

    @Test
    public void testToString() {
        MeasurementReference measurementReference = new MeasurementReference("Test name");

        String expected = "Measurement reference (Test name)";
        String actual = measurementReference.toString();

        assertEquals(expected,actual);
    }
}