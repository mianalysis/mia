package io.github.mianalysis.mia.module.inputoutput;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleTest;


public class MetadataExtractorTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new MetadataExtractor(null).getDescription());
    }
}