package io.github.mianalysis.mia.module.objectprocessing.refinement;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;


public class ExpandShrinkObjectsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ExpandShrinkObjects(null).getDescription());
    }
}