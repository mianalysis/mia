package io.github.mianalysis.mia.module.objects.detect;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.objects.detect.CircleHoughDetection;

import static org.junit.jupiter.api.Assertions.*;


public class HoughCircleDetectionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CircleHoughDetection(null).getDescription());
    }
}