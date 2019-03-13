package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class ManuallyEditImageTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ManuallyEditImage().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ManuallyEditImage().getHelp());
    }
}