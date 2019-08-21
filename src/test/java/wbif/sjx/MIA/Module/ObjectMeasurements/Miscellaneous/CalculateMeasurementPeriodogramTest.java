package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class CalculateMeasurementPeriodogramTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CalculateMeasurementPeriodogram(null).getDescription());
    }
}