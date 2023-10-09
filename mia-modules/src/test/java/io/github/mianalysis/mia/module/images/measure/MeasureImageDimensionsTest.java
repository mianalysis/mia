package io.github.mianalysis.mia.module.images.measure;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;


public class MeasureImageDimensionsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureImageDimensions(null).getDescription());
    }
}