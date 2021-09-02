package io.github.mianalysis.mia.module.imageprocessing.miscellaneous;

import io.github.mianalysis.mia.module.Miscellaneous.Macros.RunSingleCommand;
import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class RunSingleMacroCommandTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RunSingleCommand(null).getDescription());
    }

}