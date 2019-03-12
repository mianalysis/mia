package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class MeasureObjectLimitsTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new MeasureObjectLimits().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureObjectLimits().getHelp());
    }
}