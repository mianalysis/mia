package io.github.mianalysis.MIA.Module.Hidden;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.MIA.Module.ModuleTest;
import io.github.mianalysis.MIA.Module.Core.InputControl;

public class InputControlTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new InputControl(null).getDescription());
    }
}