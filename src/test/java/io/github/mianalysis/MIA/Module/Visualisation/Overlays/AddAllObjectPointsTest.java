package io.github.mianalysis.MIA.Module.Visualisation.Overlays;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class AddAllObjectPointsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new AddAllObjectPoints(null).getDescription());
    }
}