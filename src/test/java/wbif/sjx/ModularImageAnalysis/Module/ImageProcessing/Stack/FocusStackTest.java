package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class FocusStackTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new FocusStack().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new FocusStack().getHelp());
    }
}