package wbif.sjx.MIA.Object;

import org.junit.Test;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.FilterImage;

import static org.junit.Assert.*;

public class MeasurementTest {
    @Test
    public void testConstructionNameOnly() {
        Measurement measurement = new Measurement("Meas 1");

        assertEquals("Meas 1", measurement.getName());
        assertTrue(Double.isNaN(measurement.getValue()));
        assertNull(measurement.getSource());
    }

    @Test
    public void testConstructionNameSource() {
        FilterImage filterImage = new FilterImage();
        Measurement measurement = new Measurement("Meas 1",filterImage);

        assertEquals("Meas 1", measurement.getName());
        assertTrue(Double.isNaN(measurement.getValue()));
        assertEquals(filterImage,measurement.getSource());
    }

    @Test
    public void testConstructionNameValue() {
        Measurement measurement = new Measurement("Meas 1",-4.2);

        assertEquals("Meas 1", measurement.getName());
        assertEquals(-4.2,measurement.getValue(),0);
        assertNull(measurement.getSource());
    }

    @Test
    public void testConstructionNameValueSource() {
        FilterImage filterImage = new FilterImage();
        Measurement measurement = new Measurement("Meas 1",-4.2,filterImage);

        assertEquals("Meas 1", measurement.getName());
        assertEquals(-4.2,measurement.getValue(),0);
        assertEquals(filterImage,measurement.getSource());
    }
}