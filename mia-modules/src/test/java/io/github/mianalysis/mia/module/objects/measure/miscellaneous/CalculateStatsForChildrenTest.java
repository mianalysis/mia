package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.objects.measure.miscellaneous.CalculateStatsForChildren;

import static org.junit.jupiter.api.Assertions.*;


public class CalculateStatsForChildrenTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CalculateStatsForChildren(null).getDescription());
    }
}