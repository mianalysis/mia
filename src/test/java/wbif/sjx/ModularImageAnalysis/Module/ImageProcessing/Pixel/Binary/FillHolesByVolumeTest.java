package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.Binary;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class FillHolesByVolumeTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new FillHolesByVolume().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new FillHolesByVolume().getHelp());
    }
}