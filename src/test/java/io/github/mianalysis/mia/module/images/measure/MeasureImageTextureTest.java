package io.github.mianalysis.mia.module.images.measure;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;


public class MeasureImageTextureTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureImageTexture(null).getDescription());
    }
}