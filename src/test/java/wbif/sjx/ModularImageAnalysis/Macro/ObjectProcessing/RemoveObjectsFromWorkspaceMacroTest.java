package wbif.sjx.ModularImageAnalysis.Macro.ObjectProcessing;

import wbif.sjx.ModularImageAnalysis.Macro.MacroOperationTest;

import static org.junit.Assert.*;

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