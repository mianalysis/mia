package io.github.mianalysis.mia.macro.objectprocessing;

import io.github.mianalysis.mia.macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class MIA_RemoveObjectsFromWorkspaceTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new MIA_RemoveObjectsFromWorkspace(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MIA_RemoveObjectsFromWorkspace(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MIA_RemoveObjectsFromWorkspace(null).getDescription());
    }
}