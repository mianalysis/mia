package io.github.mianalysis.mia.module.objects.detect;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.objects.detect.RunTrackMate;

import static org.junit.jupiter.api.Assertions.*;


public class RunTrackMateTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RunTrackMate(null).getDescription());
    }
}