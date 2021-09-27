package io.github.mianalysis.mia.module.visualisation.overlays;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Stephen Cross on 29/03/2019.
 */

public class AddObjectCentroidTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new AddObjectCentroid(null).getDescription());
    }
}