package wbif.sjx.ModularImageAnalysis.Module.Visualisation.Overlays;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class AddAllObjectPointsTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new AddAllObjectPoints().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new AddAllObjectPoints().getHelp());
    }
}