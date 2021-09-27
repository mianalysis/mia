package io.github.mianalysis.mia.module.imageprocessing.pixel;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.mia.module.ModuleTest;


public class ColourDeconvolutionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ColourDeconvolution(null).getDescription());
    }
}