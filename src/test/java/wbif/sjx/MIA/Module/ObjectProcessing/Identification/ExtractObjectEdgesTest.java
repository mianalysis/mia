package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class ExtractObjectEdgesTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ExtractObjectEdges(null).getHelp());
    }
}