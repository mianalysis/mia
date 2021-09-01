package io.github.mianalysis.MIA.Macro.InputOutput;

import io.github.mianalysis.MIA.Macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class MIA_LoadImageFromImageJTest extends MacroOperationTest {
    @Override
    public void testGetName() {
        assertNotNull(new MIA_LoadImageFromImageJ(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MIA_LoadImageFromImageJ(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MIA_LoadImageFromImageJ(null).getDescription());
    }
}