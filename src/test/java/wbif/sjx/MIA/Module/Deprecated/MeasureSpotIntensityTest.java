package wbif.sjx.MIA.Module.Deprecated;

import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Module.ObjectMeasurements.Intensity.MeasureSpotIntensity;

import static org.junit.jupiter.api.Assertions.*;

public class MeasureSpotIntensityTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureSpotIntensity(null).getDescription());
    }
}