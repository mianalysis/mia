package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class RidgeDetectionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RidgeDetection(null).getDescription());
    }
}