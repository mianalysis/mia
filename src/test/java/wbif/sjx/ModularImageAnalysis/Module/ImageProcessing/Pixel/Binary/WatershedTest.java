package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.Binary;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class WatershedTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new Watershed().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new Watershed().getHelp());
    }
}