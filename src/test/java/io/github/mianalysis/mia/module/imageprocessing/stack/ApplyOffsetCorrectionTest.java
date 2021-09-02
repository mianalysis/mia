package io.github.mianalysis.mia.module.imageprocessing.stack;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class ApplyOffsetCorrectionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ApplyOffsetCorrection<>(null).getDescription());
    }
}