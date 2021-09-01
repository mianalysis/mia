package io.github.mianalysis.MIA.Module.Miscellaneous;

import org.junit.jupiter.api.BeforeAll;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class GUISeparatorTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new GUISeparator(null).getDescription());

    }
}