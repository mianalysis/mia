package io.github.mianalysis.MIA.Module.Visualisation.ImageRendering;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class SetLookupTableTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new SetLookupTable(null).getDescription());
    }
}