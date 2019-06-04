package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class CalculateMeasurementPeriodogramTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CalculateMeasurementPeriodogram(null).getHelp());
    }
}