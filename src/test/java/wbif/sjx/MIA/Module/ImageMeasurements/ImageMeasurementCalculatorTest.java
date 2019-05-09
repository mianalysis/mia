package wbif.sjx.MIA.Module.ImageMeasurements;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 19/03/2019.
 */
public class ImageMeasurementCalculatorTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ImageMeasurementCalculator(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ImageMeasurementCalculator(null).getHelp());
    }
}