package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class ApplyManualClassificationTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ApplyManualClassification().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ApplyManualClassification().getHelp());
    }
}