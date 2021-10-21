package io.github.mianalysis.mia.module.objects.detect;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.objects.detect.RidgeDetection;

import static org.junit.jupiter.api.Assertions.*;


public class RidgeDetectionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RidgeDetection(null).getDescription());
    }
}