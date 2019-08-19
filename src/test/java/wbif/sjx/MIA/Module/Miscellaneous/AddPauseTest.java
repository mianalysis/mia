package wbif.sjx.MIA.Module.Miscellaneous;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class AddPauseTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new AddPause(null).getDescription());
    }
}