package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.Binary;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class FillHolesTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new FillHoles().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new FillHoles().getHelp());
    }
}