package wbif.sjx.MIA.Macro.General;

import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class ListImagesInWorkspaceMacroTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new ListImagesInWorkspaceMacro(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new ListImagesInWorkspaceMacro(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new ListImagesInWorkspaceMacro(null).getDescription());
    }
}