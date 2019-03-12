package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class CalculateStatsForChildrenTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new CalculateStatsForChildren().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new CalculateStatsForChildren().getHelp());
    }
}