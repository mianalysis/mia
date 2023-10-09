package io.github.mianalysis.mia.macro.visualisation;

import io.github.mianalysis.mia.macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class MIA_ShowImageTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new MIA_ShowImage(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MIA_ShowImage(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MIA_ShowImage(null).getDescription());
    }
}