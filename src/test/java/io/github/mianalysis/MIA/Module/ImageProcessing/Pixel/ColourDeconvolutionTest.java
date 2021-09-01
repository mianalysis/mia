package io.github.mianalysis.MIA.Module.ImageProcessing.Pixel;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.MIA.Module.ModuleTest;

public class ColourDeconvolutionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ColourDeconvolution(null).getDescription());
    }
}