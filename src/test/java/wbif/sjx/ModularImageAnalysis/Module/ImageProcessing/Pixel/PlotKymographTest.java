package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

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