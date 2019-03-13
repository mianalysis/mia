package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class RunTrackMateTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new RunTrackMate().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new RunTrackMate().getHelp());
    }
}