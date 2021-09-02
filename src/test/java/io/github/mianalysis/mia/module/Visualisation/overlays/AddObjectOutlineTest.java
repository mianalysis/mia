package io.github.mianalysis.mia.module.Visualisation.overlays;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class AddObjectOutlineTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new AddObjectOutline(null).getDescription());
    }
}