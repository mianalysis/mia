package io.github.mianalysis.mia.module.objectprocessing.miscellaneous;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class CreateDistanceMapTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CreateDistanceMap(null).getDescription());
    }
}