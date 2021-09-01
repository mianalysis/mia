package io.github.mianalysis.MIA.Module.ImageMeasurements;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class MeasureImageDimensionsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureImageDimensions(null).getDescription());
    }
}