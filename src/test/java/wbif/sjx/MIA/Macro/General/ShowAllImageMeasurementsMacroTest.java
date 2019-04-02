package wbif.sjx.MIA.Macro.General;

import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.Assert.*;

public class ShowAllImageMeasurementsMacroTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new ShowAllImageMeasurementsMacro(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new ShowAllImageMeasurementsMacro(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new ShowAllImageMeasurementsMacro(null).getDescription());
    }
}