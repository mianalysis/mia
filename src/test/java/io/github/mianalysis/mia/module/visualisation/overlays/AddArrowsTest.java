package io.github.mianalysis.mia.module.visualisation.overlays;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.mia.module.ModuleTest;

/**
 * Created by Stephen Cross on 29/03/2019.
 */
public class AddArrowsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new AddArrows(null).getDescription());
    }
}