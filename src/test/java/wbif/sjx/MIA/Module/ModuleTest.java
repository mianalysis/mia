package wbif.sjx.MIA.Module;

import org.junit.BeforeClass;
import org.junit.Test;

public abstract class ModuleTest {
    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Test
    public abstract void testGetHelp();

}