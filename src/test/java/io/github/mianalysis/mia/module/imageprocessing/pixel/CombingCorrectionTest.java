package io.github.mianalysis.mia.module.imageprocessing.pixel;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;


public class CombingCorrectionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CombingCorrection(null).getDescription());
    }
}