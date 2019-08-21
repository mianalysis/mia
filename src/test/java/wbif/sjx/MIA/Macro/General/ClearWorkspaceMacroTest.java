package wbif.sjx.MIA.Macro.General;

import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class ClearWorkspaceMacroTest extends MacroOperationTest {
    @Override
    public void testGetName() {
        assertNotNull(new ClearWorkspaceMacro(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new ClearWorkspaceMacro(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new ClearWorkspaceMacro(null).getDescription());
    }
}