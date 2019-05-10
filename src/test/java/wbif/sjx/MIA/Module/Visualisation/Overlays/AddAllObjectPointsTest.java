package wbif.sjx.MIA.Module.Visualisation.Overlays;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class AddAllObjectPointsTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new AddAllObjectPoints(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new AddAllObjectPoints(null).getHelp());
    }
}