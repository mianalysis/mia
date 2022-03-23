package io.github.mianalysis.mia.module.visualisation.overlays;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.visualise.overlays.AddObjectOutline;

import static org.junit.jupiter.api.Assertions.*;


public class AddObjectOutlineTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new AddObjectOutline(null).getDescription());
    }
}