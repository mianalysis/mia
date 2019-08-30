package wbif.sjx.MIA.Module.ImageMeasurements;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class MeasureImageTextureTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureImageTexture(null).getDescription());
    }
}