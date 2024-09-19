package io.github.mianalysis.mia.module.objects.detect;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.mia.module.ModuleTest;


public class RunTrackMateTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RunTrackMate(null).getDescription());
    }
}