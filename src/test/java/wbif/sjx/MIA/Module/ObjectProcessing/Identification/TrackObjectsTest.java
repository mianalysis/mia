package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class TrackObjectsTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new TrackObjects(null).getDescription());
    }
}