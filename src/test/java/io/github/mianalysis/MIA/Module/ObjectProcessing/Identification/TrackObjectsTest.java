package io.github.mianalysis.MIA.Module.ObjectProcessing.Identification;

import io.github.mianalysis.MIA.Module.ModuleTest;
import io.github.mianalysis.MIA.Module.ObjectProcessing.Relationships.TrackObjects;

import static org.junit.jupiter.api.Assertions.*;

public class TrackObjectsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new TrackObjects(null).getDescription());
    }
}