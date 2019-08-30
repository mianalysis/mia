package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterImagesTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RegisterImages(null).getDescription());
    }
}