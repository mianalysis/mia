package io.github.mianalysis.MIA.Module.ImageProcessing.Pixel.Binary;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class ExtendedMinimaTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ExtendedMinima(null).getDescription());
    }
}