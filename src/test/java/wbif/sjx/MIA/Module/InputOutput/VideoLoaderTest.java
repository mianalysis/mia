package wbif.sjx.MIA.Module.InputOutput;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class VideoLoaderTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new VideoLoader().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new VideoLoader().getHelp());
    }
}