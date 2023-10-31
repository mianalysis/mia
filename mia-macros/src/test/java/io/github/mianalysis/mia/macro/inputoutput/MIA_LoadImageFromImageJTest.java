package io.github.mianalysis.mia.macro.inputoutput;

import io.github.mianalysis.mia.macro.MacroOperationTest;

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