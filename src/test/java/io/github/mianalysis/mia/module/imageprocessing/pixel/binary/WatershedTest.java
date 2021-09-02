package io.github.mianalysis.mia.module.imageprocessing.pixel.binary;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class WatershedTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new Watershed(null).getDescription());
    }
}