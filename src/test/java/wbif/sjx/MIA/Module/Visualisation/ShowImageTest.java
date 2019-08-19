package wbif.sjx.MIA.Module.Visualisation;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class ShowImageTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ShowImage(null).getDescription());
    }
}