package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class SetLookupTableTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new SetLookupTable().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new SetLookupTable().getHelp());
    }
}