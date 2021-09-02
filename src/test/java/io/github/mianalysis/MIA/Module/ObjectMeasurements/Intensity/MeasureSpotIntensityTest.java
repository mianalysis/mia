package io.github.mianalysis.mia.module.objectmeasurements.intensity;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class MeasureSpotIntensityTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureSpotIntensity(null).getDescription());
    }
}