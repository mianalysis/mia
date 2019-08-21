package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class ExtractObjectEdgesTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ExtractObjectEdges(null).getDescription());
    }
}