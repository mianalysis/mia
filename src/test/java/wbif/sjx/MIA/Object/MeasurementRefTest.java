package wbif.sjx.MIA.Object;

import org.junit.Test;
import wbif.sjx.MIA.Object.References.Abstract.MeasurementRef;

import static org.junit.Assert.*;

public class MeasurementRefTest {
    @Test
    public void testConstructor() {
        MeasurementRef measurementReference = new MeasurementRef("Test name", MeasurementRef.Type.OBJECT);

        assertEquals("Test name",measurementReference.getName());
        assertEquals("",measurementReference.getImageObjName());
        assertTrue(measurementReference.isAvailable());
        assertTrue(measurementReference.isExportIndividual());
    }

    @Test
    public void testToString() {
        MeasurementRef measurementReference = new MeasurementRef("Test name", MeasurementRef.Type.OBJECT);

        String expected = "Measurement reference (Test name)";
        String actual = measurementReference.toString();

        assertEquals(expected,actual);
    }
}