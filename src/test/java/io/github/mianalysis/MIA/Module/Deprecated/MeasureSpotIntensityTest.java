package io.github.mianalysis.mia.module.Deprecated;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.ObjectMeasurements.Intensity.MeasureSpotIntensity;

import static org.junit.jupiter.api.Assertions.*;

public class MeasureSpotIntensityTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureSpotIntensity(null).getDescription());
    }
}