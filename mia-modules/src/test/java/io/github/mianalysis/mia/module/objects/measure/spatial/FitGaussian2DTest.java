package io.github.mianalysis.mia.module.objects.measure.spatial;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.objects.process.FitGaussian2D;

import static org.junit.jupiter.api.Assertions.*;


public class FitGaussian2DTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new FitGaussian2D(null).getDescription());
    }
}