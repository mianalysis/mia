package io.github.mianalysis.mia.module.imageprocessing.stack;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.mia.module.ModuleTest;

public class RegisterImagesTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RegisterImages(null).getDescription());
    }
}