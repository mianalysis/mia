package wbif.sjx.MIA.Macro.ObjectProcessing;

import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class RemoveObjectsFromWorkspaceMacroTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new RemoveObjectsFromWorkspaceMacro(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new RemoveObjectsFromWorkspaceMacro(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new RemoveObjectsFromWorkspaceMacro(null).getDescription());
    }
}