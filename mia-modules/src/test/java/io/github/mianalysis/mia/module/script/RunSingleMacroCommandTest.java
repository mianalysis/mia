package io.github.mianalysis.mia.module.script;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.mia.module.ModuleTest;


public class RunSingleMacroCommandTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new RunSingleCommand(null).getDescription());
    }

}