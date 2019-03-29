package wbif.sjx.ModularImageAnalysis.Module.Visualisation.Overlays;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 29/03/2019.
 */
public class AddArrowsTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new AddArrows().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new AddArrows().getHelp());
    }
}