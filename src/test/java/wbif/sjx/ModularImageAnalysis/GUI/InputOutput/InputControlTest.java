package wbif.sjx.ModularImageAnalysis.GUI.InputOutput;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class InputControlTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new InputControl().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new InputControl().getHelp());
    }
}