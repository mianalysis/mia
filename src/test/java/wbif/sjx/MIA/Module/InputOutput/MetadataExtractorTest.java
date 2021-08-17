package wbif.sjx.MIA.Module.InputOutput;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;

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