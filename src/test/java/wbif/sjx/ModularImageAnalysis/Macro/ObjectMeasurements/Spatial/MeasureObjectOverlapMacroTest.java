package wbif.sjx.ModularImageAnalysis.Macro.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Macro.MacroOperationTest;

import static org.junit.Assert.*;

public class MeasureObjectOverlapMacroTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new MeasureObjectOverlapMacro(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MeasureObjectOverlapMacro(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MeasureObjectOverlapMacro(null).getDescription());
    }
}