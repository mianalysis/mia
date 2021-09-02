package io.github.mianalysis.mia.module.imageprocessing.Stack;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.imageprocessing.Stack.Registration.UnwarpAutomatic;

import static org.junit.jupiter.api.Assertions.*;

public class UnwarpImagesTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new UnwarpAutomatic(null).getDescription());
    }
}