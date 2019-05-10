package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class RunTrackMateTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new RunTrackMate(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new RunTrackMate(null).getHelp());
    }
}