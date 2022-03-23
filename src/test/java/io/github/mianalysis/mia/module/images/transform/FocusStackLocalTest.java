package io.github.mianalysis.mia.module.images.transform;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;


public class FocusStackLocalTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new FocusStackLocal(null).getDescription());
    }
}