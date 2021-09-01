package io.github.mianalysis.MIA.Module.Deprecated;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.MIA.Module.ModuleTest;
import io.github.mianalysis.MIA.Module.ImageProcessing.Stack.RegisterImages;

public class RegisterImagesTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RegisterImages(null).getDescription());
    }
}