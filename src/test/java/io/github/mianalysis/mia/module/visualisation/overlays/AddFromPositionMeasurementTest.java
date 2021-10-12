package io.github.mianalysis.mia.module.visualisation.overlays;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.visualise.overlays.AddFromPositionMeasurement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Stephen Cross on 29/03/2019.
 */

public class AddFromPositionMeasurementTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new AddFromPositionMeasurement(null).getDescription());
    }
}