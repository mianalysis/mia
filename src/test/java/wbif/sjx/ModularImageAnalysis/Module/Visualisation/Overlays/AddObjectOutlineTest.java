package wbif.sjx.ModularImageAnalysis.Module.Visualisation.Overlays;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class AddObjectOutlineTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new AddObjectOutline().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new AddObjectOutline().getNotes());
    }
}