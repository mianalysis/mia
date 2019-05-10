package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class UnwarpImagesTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new UnwarpImages(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new UnwarpImages(null).getHelp());
    }
}