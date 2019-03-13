package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class MeasureSpotIntensityTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new MeasureSpotIntensity().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureSpotIntensity().getHelp());
    }
}