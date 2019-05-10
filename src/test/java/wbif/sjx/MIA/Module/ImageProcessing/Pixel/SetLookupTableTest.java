package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class SetLookupTableTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new SetLookupTable(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new SetLookupTable(null).getHelp());
    }
}