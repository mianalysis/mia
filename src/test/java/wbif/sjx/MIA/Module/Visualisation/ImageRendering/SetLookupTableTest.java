package wbif.sjx.MIA.Module.Visualisation.ImageRendering;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class SetLookupTableTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new SetLookupTable(null).getDescription());
    }
}