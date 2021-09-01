package io.github.mianalysis.MIA.Macro.ImageProcessing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.MIA.Macro.MacroOperationTest;

public class MIA_ListImagesInWorkspaceTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new MIA_ListImagesInWorkspace(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MIA_ListImagesInWorkspace(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MIA_ListImagesInWorkspace(null).getDescription());
    }
}