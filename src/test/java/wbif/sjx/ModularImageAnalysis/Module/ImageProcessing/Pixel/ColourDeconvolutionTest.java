package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class ColourDeconvolutionTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ColourDeconvolution().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ColourDeconvolution().getHelp());
    }
}