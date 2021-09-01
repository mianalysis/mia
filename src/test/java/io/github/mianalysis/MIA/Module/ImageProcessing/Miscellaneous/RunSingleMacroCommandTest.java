package io.github.mianalysis.MIA.Module.ImageProcessing.Miscellaneous;

import io.github.mianalysis.MIA.Module.Miscellaneous.Macros.RunSingleCommand;
import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class RunSingleMacroCommandTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RunSingleCommand(null).getDescription());
    }

}