package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class UnwarpImagesTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new UnwarpImages().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new UnwarpImages().getHelp());
    }
}