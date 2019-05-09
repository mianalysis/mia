package wbif.sjx.MIA.Module.Visualisation.Overlays;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class AddObjectOutlineTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new AddObjectOutline(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new AddObjectOutline(null).getHelp());
    }
}