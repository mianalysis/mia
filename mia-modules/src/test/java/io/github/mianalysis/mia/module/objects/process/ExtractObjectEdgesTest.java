package io.github.mianalysis.mia.module.objects.process;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.mia.module.ModuleTest;


public class ExtractObjectEdgesTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ExtractObjectEdges(null).getDescription());
    }
}