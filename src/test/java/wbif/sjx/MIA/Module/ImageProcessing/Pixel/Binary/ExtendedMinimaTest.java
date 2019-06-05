package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class ExtendedMinimaTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ExtendedMinima(null).getDescription());
    }
}