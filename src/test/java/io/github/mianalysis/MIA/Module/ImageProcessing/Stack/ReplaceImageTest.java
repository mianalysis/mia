package io.github.mianalysis.MIA.Module.ImageProcessing.Stack;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class ReplaceImageTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ReplaceImage(null).getDescription());
    }
}