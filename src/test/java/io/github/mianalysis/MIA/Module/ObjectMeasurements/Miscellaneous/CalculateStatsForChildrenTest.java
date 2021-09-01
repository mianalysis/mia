package io.github.mianalysis.MIA.Module.ObjectMeasurements.Miscellaneous;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class CalculateStatsForChildrenTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CalculateStatsForChildren(null).getDescription());
    }
}