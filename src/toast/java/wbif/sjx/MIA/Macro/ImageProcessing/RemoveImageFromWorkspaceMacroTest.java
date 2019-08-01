package wbif.sjx.MIA.Macro.ImageProcessing;

import org.junit.Test;
import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.Assert.*;

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