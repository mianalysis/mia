package wbif.sjx.MIA.Module;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public abstract class ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Test
    public abstract void testGetHelp();

}