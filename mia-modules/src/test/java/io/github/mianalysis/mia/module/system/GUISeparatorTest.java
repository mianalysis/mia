package io.github.mianalysis.mia.module.system;

import org.junit.jupiter.api.BeforeAll;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;


public class GUISeparatorTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new GUISeparator(null).getDescription());

    }
}