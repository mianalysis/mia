package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class FitEllipsoidTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new FitEllipsoid(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new FitEllipsoid(null).getHelp());
    }
}