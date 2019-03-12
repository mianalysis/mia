package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class ShowImageTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ShowImage().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ShowImage().getHelp());
    }
}