package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.UnwarpAutomatic;

import static org.junit.jupiter.api.Assertions.*;

public class UnwarpImagesTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new UnwarpAutomatic(null).getDescription());
    }
}