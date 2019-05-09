package wbif.sjx.MIA.Module.ObjectProcessing.Miscellaneous;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class CreateDistanceMapTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new CreateDistanceMap(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new CreateDistanceMap(null).getHelp());
    }
}