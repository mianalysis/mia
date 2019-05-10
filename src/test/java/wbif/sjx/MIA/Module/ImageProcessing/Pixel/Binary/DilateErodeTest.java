package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class DilateErodeTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new DilateErode(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new DilateErode(null).getHelp());
    }
}