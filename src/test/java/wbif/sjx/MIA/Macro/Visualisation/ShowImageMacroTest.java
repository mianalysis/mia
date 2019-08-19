package wbif.sjx.MIA.Macro.Visualisation;

import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class ShowImageMacroTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new ShowImageMacro(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new ShowImageMacro(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new ShowImageMacro(null).getDescription());
    }
}