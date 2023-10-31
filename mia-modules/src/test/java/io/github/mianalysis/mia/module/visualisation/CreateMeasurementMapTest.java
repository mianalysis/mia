package io.github.mianalysis.mia.module.visualisation;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.visualise.CreateMeasurementMap;

import static org.junit.jupiter.api.Assertions.*;


public class CreateMeasurementMapTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CreateMeasurementMap(null).getDescription());
    }
}