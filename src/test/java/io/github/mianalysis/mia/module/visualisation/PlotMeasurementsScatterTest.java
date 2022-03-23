package io.github.mianalysis.mia.module.visualisation;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.visualise.PlotMeasurementsScatter;

import static org.junit.jupiter.api.Assertions.*;


public class PlotMeasurementsScatterTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new PlotMeasurementsScatter(null).getDescription());
    }
}