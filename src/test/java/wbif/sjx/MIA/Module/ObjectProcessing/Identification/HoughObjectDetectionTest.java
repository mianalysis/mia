package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class HoughObjectDetectionTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new HoughObjectDetection(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new HoughObjectDetection(null).getHelp());
    }
}