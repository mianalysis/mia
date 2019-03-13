package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class ActiveContourObjectDetectionTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ActiveContourObjectDetection().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ActiveContourObjectDetection().getHelp());
    }
}