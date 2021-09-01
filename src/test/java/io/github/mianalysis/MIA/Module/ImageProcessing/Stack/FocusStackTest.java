package io.github.mianalysis.MIA.Module.ImageProcessing.Stack;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class FocusStackTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new FocusStack(null).getDescription());
    }
}