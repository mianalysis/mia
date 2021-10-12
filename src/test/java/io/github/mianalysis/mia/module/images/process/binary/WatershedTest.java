package io.github.mianalysis.mia.module.images.process.binary;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.mia.module.ModuleTest;


public class WatershedTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new Watershed(null).getDescription());
    }
}