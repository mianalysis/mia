package io.github.mianalysis.MIA.Module.ObjectProcessing.Refinement;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class ExpandShrinkObjectsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ExpandShrinkObjects(null).getDescription());
    }
}