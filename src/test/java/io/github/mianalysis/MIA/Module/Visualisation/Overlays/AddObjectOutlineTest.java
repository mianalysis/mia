package io.github.mianalysis.MIA.Module.Visualisation.Overlays;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class AddObjectOutlineTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new AddObjectOutline(null).getDescription());
    }
}