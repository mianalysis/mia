package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class RunTrackMateTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RunTrackMate(null).getDescription());
    }
}