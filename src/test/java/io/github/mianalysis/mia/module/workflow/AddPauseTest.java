package io.github.mianalysis.mia.module.workflow;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;


public class AddPauseTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new AddPause(null).getDescription());
    }
}