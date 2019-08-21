package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class FocusStackTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new FocusStack(null).getDescription());
    }
}