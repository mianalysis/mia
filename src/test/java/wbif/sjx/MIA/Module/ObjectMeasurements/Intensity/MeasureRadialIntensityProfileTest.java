package wbif.sjx.MIA.Module.ObjectMeasurements.Intensity;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class MeasureRadialIntensityProfileTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureRadialIntensityProfile(null).getHelp());
    }
}