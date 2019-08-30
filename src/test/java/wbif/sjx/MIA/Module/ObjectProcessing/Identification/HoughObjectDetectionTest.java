package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class HoughObjectDetectionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new HoughObjectDetection(null).getDescription());
    }
}