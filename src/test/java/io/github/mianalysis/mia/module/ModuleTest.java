package io.github.mianalysis.mia.module;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public abstract class ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Test
    public abstract void testGetHelp();

}