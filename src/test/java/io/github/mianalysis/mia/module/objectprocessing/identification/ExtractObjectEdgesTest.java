package io.github.mianalysis.mia.module.objectprocessing.identification;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;


public class ExtractObjectEdgesTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ExtractObjectEdges(null).getDescription());
    }
}