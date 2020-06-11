package wbif.sjx.MIA.Macro.ObjectMeasurements.Spatial;

import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class MIA_MeasureObjectShapeTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new MIA_MeasureObjectShape(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MIA_MeasureObjectShape(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MIA_MeasureObjectShape(null).getDescription());
    }
}