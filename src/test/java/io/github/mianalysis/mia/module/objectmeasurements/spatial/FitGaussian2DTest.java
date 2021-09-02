package io.github.mianalysis.mia.module.objectmeasurements.spatial;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class FitGaussian2DTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new FitGaussian2D(null).getDescription());
    }
}