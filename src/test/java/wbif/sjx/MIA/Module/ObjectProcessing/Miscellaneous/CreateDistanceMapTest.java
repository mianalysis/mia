package wbif.sjx.MIA.Module.ObjectProcessing.Miscellaneous;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class CreateDistanceMapTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CreateDistanceMap(null).getDescription());
    }
}