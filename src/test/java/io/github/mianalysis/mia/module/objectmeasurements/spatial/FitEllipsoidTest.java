package io.github.mianalysis.mia.module.objectmeasurements.spatial;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;


public class FitEllipsoidTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new FitEllipsoid(null).getDescription());
    }
}