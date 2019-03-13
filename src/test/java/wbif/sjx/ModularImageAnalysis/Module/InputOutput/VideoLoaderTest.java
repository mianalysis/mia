package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

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