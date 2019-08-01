package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class FitGaussian2DTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new FitGaussian2D(null).getDescription());
    }
}