package wbif.sjx.MIA.Module.ImageProcessing.Miscellaneous;

import wbif.sjx.MIA.Module.Deprecated.RunImageJMacro;
import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class RunImageJMacroTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RunImageJMacro(null).getHelp());
    }

}