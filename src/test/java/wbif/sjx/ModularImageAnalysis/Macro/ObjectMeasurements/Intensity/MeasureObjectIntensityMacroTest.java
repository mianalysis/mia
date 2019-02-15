package wbif.sjx.ModularImageAnalysis.Macro.ObjectMeasurements.Intensity;

import wbif.sjx.ModularImageAnalysis.Macro.MacroOperationTest;

import static org.junit.Assert.*;

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