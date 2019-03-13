package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class ReplaceImageTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ReplaceImage().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ReplaceImage().getHelp());
    }
}