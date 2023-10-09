package io.github.mianalysis.mia.module.visualisation.overlays;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.visualise.overlays.AddAllObjectPoints;

import static org.junit.jupiter.api.Assertions.*;


public class AddAllObjectPointsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new AddAllObjectPoints(null).getDescription());
    }
}