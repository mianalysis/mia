package io.github.mianalysis.mia.module.objects.measure.spatial;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.objects.measure.spatial.MeasureObjectLimits;

import static org.junit.jupiter.api.Assertions.*;


public class MeasureObjectLimitsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureObjectLimits(null).getDescription());
    }
}