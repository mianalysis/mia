package wbif.sjx.ModularImageAnalysis.Macro.Visualisation;

import wbif.sjx.ModularImageAnalysis.Macro.MacroOperationTest;

import static org.junit.Assert.*;

public class ShowImageMacroTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new ShowImageMacro(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new ShowImageMacro(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new ShowImageMacro(null).getDescription());
    }
}