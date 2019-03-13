package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class CreateDistanceMapTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new CreateDistanceMap().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new CreateDistanceMap().getHelp());
    }
}