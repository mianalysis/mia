package wbif.sjx.MIA.Module.ImageMeasurements;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class MeasureImageTextureTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new MeasureImageTexture(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureImageTexture(null).getHelp());
    }
}