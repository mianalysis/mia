package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class MeasureImageTextureTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new MeasureImageTexture().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureImageTexture().getHelp());
    }
}