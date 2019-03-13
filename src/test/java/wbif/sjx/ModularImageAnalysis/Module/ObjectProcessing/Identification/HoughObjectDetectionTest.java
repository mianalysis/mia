package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class HoughObjectDetectionTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new HoughObjectDetection().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new HoughObjectDetection().getHelp());
    }
}