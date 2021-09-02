package io.github.mianalysis.mia.object;

import org.junit.jupiter.api.Test;
import io.github.mianalysis.mia.module.imageprocessing.pixel.FilterImage;

import static org.junit.jupiter.api.Assertions.*;

public class MeasurementTest {
    @Test
    public void testConstructionNameOnly() {
        Measurement measurement = new Measurement("Meas 1");

        assertEquals("Meas 1", measurement.getName());
        assertTrue(Double.isNaN(measurement.getValue()));
    }

    @Test
    public void testConstructionNameSource() {
        FilterImage filterImage = new FilterImage(null);
        Measurement measurement = new Measurement("Meas 1");

        assertEquals("Meas 1", measurement.getName());
        assertTrue(Double.isNaN(measurement.getValue()));
    }

    @Test
    public void testConstructionNameValue() {
        Measurement measurement = new Measurement("Meas 1",-4.2);

        assertEquals("Meas 1", measurement.getName());
        assertEquals(-4.2,measurement.getValue(),0);
    }

    @Test
    public void testConstructionNameValueSource() {
        FilterImage filterImage = new FilterImage(null);
        Measurement measurement = new Measurement("Meas 1",-4.2);

        assertEquals("Meas 1", measurement.getName());
        assertEquals(-4.2,measurement.getValue(),0);
    }
}