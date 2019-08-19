package wbif.sjx.MIA.Macro.ObjectProcessing;

import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

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