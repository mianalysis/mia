package io.github.mianalysis.MIA.Module.ObjectProcessing.Identification;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class RidgeDetectionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RidgeDetection(null).getDescription());
    }
}