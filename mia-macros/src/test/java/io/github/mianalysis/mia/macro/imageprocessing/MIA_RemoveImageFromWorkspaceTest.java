package io.github.mianalysis.mia.macro.imageprocessing;

import org.junit.jupiter.api.Test;
import io.github.mianalysis.mia.macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class MIA_RemoveImageFromWorkspaceTest extends MacroOperationTest {

    @Override
    @Test
    public void testGetName() {
        assertNotNull(new MIA_RemoveImageFromWorkspace(null).getName());
    }

    @Override
    @Test
    public void testGetArgumentsDescription() {
        assertNotNull(new MIA_RemoveImageFromWorkspace(null).getArgumentsDescription());
    }

    @Override
    @Test
    public void testGetDescription() {
        assertNotNull(new MIA_RemoveImageFromWorkspace(null).getDescription());
    }
}