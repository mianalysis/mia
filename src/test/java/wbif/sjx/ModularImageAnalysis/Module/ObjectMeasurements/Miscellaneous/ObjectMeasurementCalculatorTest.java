package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 19/03/2019.
 */
public class ObjectMeasurementCalculatorTest extends ModuleTest {
    @Override
    public void testGetTitle() {
        assertNotNull(new ObjectMeasurementCalculator().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ObjectMeasurementCalculator().getHelp());
    }
}