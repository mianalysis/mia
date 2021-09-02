package io.github.mianalysis.mia.module.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.mia.module.ModuleTest;

public class InputControlTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new InputControl(null).getDescription());
    }
}