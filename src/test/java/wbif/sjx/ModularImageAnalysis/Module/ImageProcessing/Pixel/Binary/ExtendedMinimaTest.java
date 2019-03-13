package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.Binary;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class ExtendedMinimaTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ExtendedMinima().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ExtendedMinima().getHelp());
    }
}