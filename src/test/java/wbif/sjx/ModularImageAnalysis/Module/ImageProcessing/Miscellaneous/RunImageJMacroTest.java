package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Miscellaneous;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class RunImageJMacroTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new RunImageJMacro().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new RunImageJMacro().getHelp());
    }

}