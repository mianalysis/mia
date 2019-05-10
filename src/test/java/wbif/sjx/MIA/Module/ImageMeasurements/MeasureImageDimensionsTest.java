package wbif.sjx.MIA.Module.ImageMeasurements;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class MeasureImageDimensionsTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new MeasureImageDimensions(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureImageDimensions(null).getHelp());
    }
}