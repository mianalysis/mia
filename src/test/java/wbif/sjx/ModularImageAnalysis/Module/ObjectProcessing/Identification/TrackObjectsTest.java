package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class TrackObjectsTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new TrackObjects().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new TrackObjects().getHelp());
    }
}