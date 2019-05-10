package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class FocusStackTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new FocusStack(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new FocusStack(null).getHelp());
    }
}