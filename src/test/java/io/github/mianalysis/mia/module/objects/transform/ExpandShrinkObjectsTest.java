package io.github.mianalysis.mia.module.objects.transform;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.objects.transform.ExpandShrinkObjects;

import static org.junit.jupiter.api.Assertions.*;


public class ExpandShrinkObjectsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ExpandShrinkObjects(null).getDescription());
    }
}