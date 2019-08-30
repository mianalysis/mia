package wbif.sjx.MIA.Module.InputOutput;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class VideoLoaderTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new VideoLoader(null).getDescription());
    }
}