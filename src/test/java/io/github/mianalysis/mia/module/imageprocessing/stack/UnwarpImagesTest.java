package io.github.mianalysis.mia.module.imageprocessing.stack;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.imageprocessing.stack.registration.UnwarpAutomatic;

import static org.junit.jupiter.api.Assertions.*;

public class UnwarpImagesTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new UnwarpAutomatic(null).getDescription());
    }
}