package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class CombingCorrectionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CombingCorrection(null).getDescription());
    }
}