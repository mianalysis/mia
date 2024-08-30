package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.mia.module.ModuleTest;

/**
 * Created by Stephen Cross on 19/03/2019.
 */

public class ObjectMeasurementCalculatorTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ObjectMeasurementCalculator(null).getDescription());
    }
}