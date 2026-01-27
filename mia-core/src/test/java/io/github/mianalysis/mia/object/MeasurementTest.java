package io.github.mianalysis.mia.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.mianalysis.mia.module.testmodules.FilterImage;
import io.github.mianalysis.mia.object.measurements.MeasurementFactories;
import io.github.mianalysis.mia.object.measurements.MeasurementI;

public class MeasurementTest {

    @Test
    public void testConstructionNameValue() {
        MeasurementI measurement = MeasurementFactories.getDefaultFactory().createMeasurement("Meas 1",-4.2);

        assertEquals("Meas 1", measurement.getName());
        assertEquals(-4.2,measurement.getValue(),0);
    }

    @Test
    public void testConstructionNameValueSource() {
        FilterImage filterImage = new FilterImage(null);
        MeasurementI measurement = MeasurementFactories.getDefaultFactory().createMeasurement("Meas 1",-4.2);

        assertEquals("Meas 1", measurement.getName());
        assertEquals(-4.2,measurement.getValue(),0);
    }
}