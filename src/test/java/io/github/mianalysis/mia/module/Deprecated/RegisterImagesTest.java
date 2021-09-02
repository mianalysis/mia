package io.github.mianalysis.mia.module.Deprecated;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.imageprocessing.Stack.RegisterImages;

public class RegisterImagesTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RegisterImages(null).getDescription());
    }
}