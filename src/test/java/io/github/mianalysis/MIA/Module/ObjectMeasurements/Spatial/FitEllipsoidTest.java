package io.github.mianalysis.MIA.Module.ObjectMeasurements.Spatial;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class FitEllipsoidTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new FitEllipsoid(null).getDescription());
    }
}