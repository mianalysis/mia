package io.github.mianalysis.mia.module.images.measure;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Stephen Cross on 19/03/2019.
 */

public class ImageMeasurementCalculatorTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ImageMeasurementCalculator(null).getDescription());
    }
}