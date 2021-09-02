package io.github.mianalysis.mia.module.InputOutput;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleTest;

public class MetadataExtractorTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MetadataExtractor(null).getDescription());
    }
}