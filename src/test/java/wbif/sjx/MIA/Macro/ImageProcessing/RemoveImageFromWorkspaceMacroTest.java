package wbif.sjx.MIA.Macro.ImageProcessing;

import org.junit.jupiter.api.Test;
import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class RemoveImageFromWorkspaceMacroTest extends MacroOperationTest {

    @Override
    @Test
    public void testGetName() {
        assertNotNull(new RemoveImageFromWorkspaceMacro(null).getName());
    }

    @Override
    @Test
    public void testGetArgumentsDescription() {
        assertNotNull(new RemoveImageFromWorkspaceMacro(null).getArgumentsDescription());
    }

    @Override
    @Test
    public void testGetDescription() {
        assertNotNull(new RemoveImageFromWorkspaceMacro(null).getDescription());
    }
}