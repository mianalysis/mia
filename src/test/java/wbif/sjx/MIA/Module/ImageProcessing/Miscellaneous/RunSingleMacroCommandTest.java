package wbif.sjx.MIA.Module.ImageProcessing.Miscellaneous;

import wbif.sjx.MIA.Module.Miscellaneous.Macros.RunSingleMacroCommand;
import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class RunSingleMacroCommandTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RunSingleMacroCommand(null).getHelp());
    }

}