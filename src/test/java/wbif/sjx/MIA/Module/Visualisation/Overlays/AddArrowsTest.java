package wbif.sjx.MIA.Module.Visualisation.Overlays;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 29/03/2019.
 */
public class AddArrowsTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new AddArrows(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new AddArrows(null).getHelp());
    }
}