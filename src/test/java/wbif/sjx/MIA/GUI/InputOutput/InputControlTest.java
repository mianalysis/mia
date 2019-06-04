package wbif.sjx.MIA.GUI.InputOutput;

import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class InputControlTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new InputControl(null).getHelp());
    }
}