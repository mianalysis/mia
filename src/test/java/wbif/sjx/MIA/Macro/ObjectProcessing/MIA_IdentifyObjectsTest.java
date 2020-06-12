package wbif.sjx.MIA.Macro.ObjectProcessing;

import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class MIA_IdentifyObjectsTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new MIA_IdentifyObjects(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MIA_IdentifyObjects(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MIA_IdentifyObjects(null).getDescription());
    }
}