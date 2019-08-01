package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class ApplyOffsetCorrectionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ApplyOffsetCorrection<>(null).getDescription());
    }
}