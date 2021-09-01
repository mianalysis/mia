package io.github.mianalysis.MIA.Object;

import org.junit.jupiter.api.Test;

import io.github.mianalysis.MIA.Object.References.ImageMeasurementRef;

import static org.junit.jupiter.api.Assertions.*;

public class MeasurementRefTest {
    @Test
    public void testConstructor() {
        ImageMeasurementRef measurementReference = new ImageMeasurementRef("Test name");

        assertEquals("Test name",measurementReference.getName());
        assertEquals("",measurementReference.getImageName());
        assertTrue(measurementReference.isExportIndividual());
    }
}