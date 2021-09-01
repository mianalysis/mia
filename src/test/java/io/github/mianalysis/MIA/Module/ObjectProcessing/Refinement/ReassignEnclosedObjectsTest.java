package io.github.mianalysis.MIA.Module.ObjectProcessing.Refinement;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class ReassignEnclosedObjectsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ReassignEnclosedObjects(null).getDescription());
    }
}