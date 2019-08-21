package wbif.sjx.MIA.Macro.InputOutput;

import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class LoadImageFromImageJMacroTest extends MacroOperationTest {
    @Override
    public void testGetName() {
        assertNotNull(new LoadImageFromImageJMacro(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new LoadImageFromImageJMacro(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new LoadImageFromImageJMacro(null).getDescription());
    }
}