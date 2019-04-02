package wbif.sjx.MIA.Module.Visualisation;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class CreateObjectDensityMapTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new CreateObjectDensityMap().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new CreateObjectDensityMap().getHelp());
    }
}