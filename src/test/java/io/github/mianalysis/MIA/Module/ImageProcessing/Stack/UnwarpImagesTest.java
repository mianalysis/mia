package io.github.mianalysis.MIA.Module.ImageProcessing.Stack;

import io.github.mianalysis.MIA.Module.ModuleTest;
import io.github.mianalysis.MIA.Module.ImageProcessing.Stack.Registration.UnwarpAutomatic;

import static org.junit.jupiter.api.Assertions.*;

public class UnwarpImagesTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new UnwarpAutomatic(null).getDescription());
    }
}