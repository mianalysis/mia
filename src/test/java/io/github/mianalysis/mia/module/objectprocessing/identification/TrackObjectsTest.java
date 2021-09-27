package io.github.mianalysis.mia.module.objectprocessing.identification;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.objectprocessing.relationships.TrackObjects;

import static org.junit.jupiter.api.Assertions.*;


public class TrackObjectsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new TrackObjects(null).getDescription());
    }
}