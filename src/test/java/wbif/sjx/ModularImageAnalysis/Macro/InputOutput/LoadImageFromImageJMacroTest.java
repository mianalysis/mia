package wbif.sjx.ModularImageAnalysis.Macro.InputOutput;

import wbif.sjx.ModularImageAnalysis.Macro.MacroOperationTest;

import static org.junit.Assert.*;

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