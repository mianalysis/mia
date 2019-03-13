package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class ExpandShrinkObjectsTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ExpandShrinkObjects().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ExpandShrinkObjects().getHelp());
    }
}