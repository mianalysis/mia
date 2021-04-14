package wbif.sjx.MIA.Module.ImageProcessing.Miscellaneous;

import wbif.sjx.MIA.Module.Miscellaneous.Macros.RunSingleCommand;
import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class RunSingleMacroCommandTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RunSingleCommand(null).getDescription());
    }

}