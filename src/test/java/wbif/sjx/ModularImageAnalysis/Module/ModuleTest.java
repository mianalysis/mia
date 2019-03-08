package wbif.sjx.ModularImageAnalysis.Module;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public abstract class ModuleTest {
    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Test
    public abstract void testGetTitle();

    @Test
    public abstract void testGetHelp();

}