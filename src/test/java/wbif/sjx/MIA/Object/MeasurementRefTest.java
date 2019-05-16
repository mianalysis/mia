package wbif.sjx.MIA.Object;

import org.junit.Test;
import wbif.sjx.MIA.Object.References.ImageMeasurementRef;

import static org.junit.Assert.*;

public class MeasurementRefTest {
    @Test
    public void testConstructor() {
        ImageMeasurementRef measurementReference = new ImageMeasurementRef("Test name");

        assertEquals("Test name",measurementReference.getName());
        assertEquals("",measurementReference.getImageName());
        assertTrue(measurementReference.isAvailable());
        assertTrue(measurementReference.isExportIndividual());
    }

    @Test
    public void testToString() {
        ImageMeasurementRef measurementReference = new ImageMeasurementRef("Test name");

        String expected = "Measurement reference (Test name)";
        String actual = measurementReference.toString();

        assertEquals(expected,actual);
    }
}