package wbif.sjx.ModularImageAnalysis.Macro.ObjectProcessing;

import wbif.sjx.ModularImageAnalysis.Macro.MacroOperationTest;

import static org.junit.Assert.*;

public class IdentifyObjectsMacroTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new IdentifyObjectsMacro(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new IdentifyObjectsMacro(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new IdentifyObjectsMacro(null).getDescription());
    }
}