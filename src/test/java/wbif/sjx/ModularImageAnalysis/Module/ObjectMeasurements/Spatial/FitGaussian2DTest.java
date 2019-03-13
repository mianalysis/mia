package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class FitGaussian2DTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new FitGaussian2D().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new FitGaussian2D().getHelp());
    }
}