package io.github.mianalysis.MIA.Module.ObjectMeasurements.Spatial;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class MeasureObjectLimitsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureObjectLimits(null).getDescription());
    }
}