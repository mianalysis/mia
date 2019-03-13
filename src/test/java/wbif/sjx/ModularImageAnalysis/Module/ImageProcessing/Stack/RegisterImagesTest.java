package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class RegisterImagesTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new RegisterImages().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new RegisterImages().getHelp());
    }
}