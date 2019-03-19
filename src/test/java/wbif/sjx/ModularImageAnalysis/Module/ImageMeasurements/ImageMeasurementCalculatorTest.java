package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 19/03/2019.
 */
public class ImageMeasurementCalculatorTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ImageMeasurementCalculator().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ImageMeasurementCalculator().getHelp());
    }
}