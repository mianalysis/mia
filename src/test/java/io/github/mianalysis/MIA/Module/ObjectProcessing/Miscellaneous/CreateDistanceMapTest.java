package io.github.mianalysis.MIA.Module.ObjectProcessing.Miscellaneous;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class CreateDistanceMapTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CreateDistanceMap(null).getDescription());
    }
}