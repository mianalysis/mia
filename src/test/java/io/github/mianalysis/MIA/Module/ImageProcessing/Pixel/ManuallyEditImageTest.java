package io.github.mianalysis.MIA.Module.ImageProcessing.Pixel;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class ManuallyEditImageTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ManuallyEditImage(null).getDescription());
    }
}