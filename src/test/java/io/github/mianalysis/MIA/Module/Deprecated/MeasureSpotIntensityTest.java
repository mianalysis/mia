package io.github.mianalysis.MIA.Module.Deprecated;

import io.github.mianalysis.MIA.Module.ModuleTest;
import io.github.mianalysis.MIA.Module.ObjectMeasurements.Intensity.MeasureSpotIntensity;

import static org.junit.jupiter.api.Assertions.*;

public class MeasureSpotIntensityTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureSpotIntensity(null).getDescription());
    }
}