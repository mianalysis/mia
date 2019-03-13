package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class BleachingCorrectionTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new BleachingCorrection().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new BleachingCorrection().getHelp());
    }
}