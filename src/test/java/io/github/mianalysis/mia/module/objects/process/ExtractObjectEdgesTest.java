package io.github.mianalysis.mia.module.objects.process;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.objects.process.ExtractObjectEdges;

import static org.junit.jupiter.api.Assertions.*;


public class ExtractObjectEdgesTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ExtractObjectEdges(null).getDescription());
    }
}