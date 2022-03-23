package io.github.mianalysis.mia.module.images.process.binary;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;


public class FillHolesByVolumeTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new FillHolesByVolume(null).getDescription());
    }
}