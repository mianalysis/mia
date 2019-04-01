package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class PlotKymographTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new PlotKymograph().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new PlotKymograph().getHelp());
    }
}