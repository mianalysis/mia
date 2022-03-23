package io.github.mianalysis.mia.module.objects.transform;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.objects.transform.ReassignEnclosedObjects;

import static org.junit.jupiter.api.Assertions.*;


public class ReassignEnclosedObjectsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ReassignEnclosedObjects(null).getDescription());
    }
}