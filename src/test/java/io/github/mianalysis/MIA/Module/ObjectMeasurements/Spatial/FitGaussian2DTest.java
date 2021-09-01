package io.github.mianalysis.MIA.Module.ObjectMeasurements.Spatial;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class FitGaussian2DTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new FitGaussian2D(null).getDescription());
    }
}