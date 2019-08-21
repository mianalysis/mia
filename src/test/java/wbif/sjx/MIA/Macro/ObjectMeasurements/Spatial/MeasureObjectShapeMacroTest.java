package wbif.sjx.MIA.Macro.ObjectMeasurements.Spatial;

import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class MeasureObjectShapeMacroTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new MeasureObjectShapeMacro(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MeasureObjectShapeMacro(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MeasureObjectShapeMacro(null).getDescription());
    }
}