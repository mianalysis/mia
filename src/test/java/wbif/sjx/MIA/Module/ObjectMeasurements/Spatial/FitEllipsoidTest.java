package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class FitEllipsoidTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new FitEllipsoid(null).getDescription());
    }
}