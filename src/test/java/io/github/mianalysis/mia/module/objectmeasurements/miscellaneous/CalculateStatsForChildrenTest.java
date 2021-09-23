package io.github.mianalysis.mia.module.objectmeasurements.miscellaneous;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;


public class CalculateStatsForChildrenTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CalculateStatsForChildren(null).getDescription());
    }
}