package io.github.mianalysis.mia.module.Visualisation;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class PlotMeasurementsScatterTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new PlotMeasurementsScatter(null).getDescription());
    }
}