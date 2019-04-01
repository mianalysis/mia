package wbif.sjx.MIA.Module.Visualisation;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class CreateMeasurementMapTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new CreateMeasurementMap().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new CreateMeasurementMap().getHelp());
    }
}