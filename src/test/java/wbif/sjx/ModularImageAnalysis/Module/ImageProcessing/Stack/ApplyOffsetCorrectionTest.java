package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class ApplyOffsetCorrectionTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ApplyOffsetCorrection<>().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ApplyOffsetCorrection<>().getHelp());
    }
}