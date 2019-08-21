package wbif.sjx.MIA.Macro.ObjectMeasurements.Intensity;

import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class MeasureObjectIntensityMacroTest extends MacroOperationTest {
    @Override
    public void testGetName() {
        assertNotNull(new MeasureObjectIntensityMacro(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MeasureObjectIntensityMacro(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MeasureObjectIntensityMacro(null).getDescription());
    }
}