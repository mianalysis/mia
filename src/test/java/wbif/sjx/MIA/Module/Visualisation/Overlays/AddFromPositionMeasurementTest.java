package wbif.sjx.MIA.Module.Visualisation.Overlays;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 29/03/2019.
 */
public class AddFromPositionMeasurementTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new AddFromPositionMeasurement().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new AddFromPositionMeasurement().getHelp());
    }
}