package wbif.sjx.ModularImageAnalysis.Macro.General;

import wbif.sjx.ModularImageAnalysis.Macro.MacroOperationTest;

import static org.junit.Assert.*;

public class ShowAllObjectMeasurementsMacroTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new ShowAllObjectMeasurementsMacro(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new ShowAllObjectMeasurementsMacro(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new ShowAllObjectMeasurementsMacro(null).getDescription());
    }
}