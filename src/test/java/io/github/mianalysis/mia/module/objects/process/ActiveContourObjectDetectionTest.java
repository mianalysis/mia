package io.github.mianalysis.mia.module.objects.process;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;


public class ActiveContourObjectDetectionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new FitActiveContours(null).getDescription());
    }
}